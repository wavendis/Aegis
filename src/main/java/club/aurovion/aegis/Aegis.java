package club.aurovion.aegis;

import org.bukkit.GameMode;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Aegis — a lightweight, dependency-free server-side anti-cheat.
 *
 * <p>Ships three heuristic checks (Speed, Fly, Reach) that report to a shared
 * {@link ViolationManager} with per-check violation levels, staff alerts, VL
 * decay and a configurable punishment ladder. Conservative by design to keep
 * false positives low; not a packet-level system.</p>
 */
public final class Aegis extends JavaPlugin {

    private volatile AegisSettings settings;
    private ViolationManager violations;

    private final ConcurrentHashMap<UUID, PlayerData> players = new ConcurrentHashMap<>();
    private BukkitTask decayTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.settings = new AegisSettings(getConfig());
        this.violations = new ViolationManager(this);

        getServer().getPluginManager().registerEvents(new MovementListener(this), this);
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new ConnectionListener(this), this);

        PluginCommand cmd = getCommand("aegis");
        if (cmd != null) {
            AegisCommand handler = new AegisCommand(this);
            cmd.setExecutor(handler);
            cmd.setTabCompleter(handler);
        }

        // Seed data for anyone already online (e.g. after a /reload).
        for (Player p : getServer().getOnlinePlayers()) {
            PlayerData d = createData(p.getUniqueId());
            d.lastLocation = p.getLocation();
            d.teleportGraceUntil = System.currentTimeMillis() + settings.teleportGraceMs;
        }

        startDecayTask();

        getLogger().info("Aegis enabled — checks: "
                + (settings.speedEnabled ? "Speed " : "")
                + (settings.flyEnabled ? "Fly " : "")
                + (settings.reachEnabled ? "Reach" : "") + ".");
    }

    @Override
    public void onDisable() {
        if (decayTask != null) decayTask.cancel();
        players.clear();
    }

    public void reload() {
        reloadConfig();
        this.settings = new AegisSettings(getConfig());
        startDecayTask();
    }

    private void startDecayTask() {
        if (decayTask != null) decayTask.cancel();
        decayTask = getServer().getScheduler().runTaskTimer(this, () -> {
            double amount = settings.decayPerSecond;
            if (amount <= 0) return;
            for (PlayerData d : players.values()) d.decay(amount);
        }, 20L, 20L);
    }

    // ── Exemptions ───────────────────────────────────────────────────────

    public boolean isGloballyExempt(Player p) {
        GameMode gm = p.getGameMode();
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return true;
        if (settings.exemptOps && p.isOp()) return true;
        if (p.hasPermission("aegis.bypass")) return true;
        return p.isDead();
    }

    // ── Data ─────────────────────────────────────────────────────────────

    public PlayerData createData(UUID uuid) {
        return players.computeIfAbsent(uuid, k -> new PlayerData());
    }

    public PlayerData data(UUID uuid) {
        return players.get(uuid);
    }

    public void removeData(UUID uuid) {
        players.remove(uuid);
    }

    // ── Accessors ────────────────────────────────────────────────────────

    public AegisSettings settings() {
        return settings;
    }

    public ViolationManager violations() {
        return violations;
    }
}
