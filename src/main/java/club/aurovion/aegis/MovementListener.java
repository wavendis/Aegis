package club.aurovion.aegis;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Speed and Fly checks, driven off {@link PlayerMoveEvent}. Detection-only:
 * violations raise VL (and eventually punish); movement itself is not cancelled.
 */
public final class MovementListener implements Listener {

    private static final double[] OFFSETS = {-0.3, 0.0, 0.3};

    private final Aegis plugin;

    public MovementListener(Aegis plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;

        Player player = event.getPlayer();
        PlayerData data = plugin.data(player.getUniqueId());
        if (data == null) return;

        // Look-only movement: nothing positional to check.
        if (from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ()) {
            return;
        }

        AegisSettings s = plugin.settings();
        boolean ground = isOnGround(to);
        if (ground) data.airTicks = 0; else data.airTicks++;

        if (isMovementExempt(player, data)) {
            data.lastLocation = to;
            data.speedBufferCount = 0;
            return;
        }

        // ── Speed ────────────────────────────────────────────────────────
        if (s.speedEnabled) {
            double dx = to.getX() - from.getX();
            double dz = to.getZ() - from.getZ();
            double horizontal = Math.sqrt(dx * dx + dz * dz);

            double allowed = s.speedMaxHorizontal;
            PotionEffect speed = player.getPotionEffect(PotionEffectType.SPEED);
            if (speed != null) allowed += 0.06 * (speed.getAmplifier() + 1);
            if (isIce(blockBelow(to))) allowed += 0.10;

            if (horizontal > allowed) {
                data.speedBufferCount++;
                if (data.speedBufferCount > s.speedBuffer) {
                    plugin.violations().flag(player, Check.SPEED,
                            String.format("%.2f>%.2f b/t", horizontal, allowed));
                    data.speedBufferCount = s.speedBuffer / 2;
                }
            } else if (data.speedBufferCount > 0) {
                data.speedBufferCount--;
            }
        }

        // ── Fly ──────────────────────────────────────────────────────────
        if (s.flyEnabled && !ground && !isFlyExempt(player)) {
            double dy = to.getY() - from.getY();
            if (data.airTicks > s.flyAirTicks && dy >= -0.035) {
                plugin.violations().flag(player, Check.FLY,
                        String.format("air=%d dy=%.3f", data.airTicks, dy));
                data.airTicks = s.flyAirTicks / 2;
            }
        }

        data.lastLocation = to;
    }

    private boolean isMovementExempt(Player p, PlayerData d) {
        if (plugin.isGloballyExempt(p)) return true;
        if (p.isFlying() || p.getAllowFlight()) return true;
        if (p.isGliding()) return true;
        if (p.isInsideVehicle()) return true;
        return d.inTeleportGrace();
    }

    private boolean isFlyExempt(Player p) {
        if (p.hasPotionEffect(PotionEffectType.LEVITATION)) return true;
        if (p.hasPotionEffect(PotionEffectType.SLOW_FALLING)) return true;
        Block feet = p.getLocation().getBlock();
        if (feet.isLiquid()) return true;
        Material m = feet.getType();
        return m == Material.LADDER || m == Material.VINE || m == Material.SCAFFOLDING
                || m == Material.WEEPING_VINES || m == Material.TWISTING_VINES
                || m == Material.WEEPING_VINES_PLANT || m == Material.TWISTING_VINES_PLANT
                || m == Material.POWDER_SNOW;
    }

    private boolean isOnGround(Location loc) {
        World w = loc.getWorld();
        if (w == null) return true;
        int y = (int) Math.floor(loc.getY() - 0.02);
        for (double dx : OFFSETS) {
            for (double dz : OFFSETS) {
                Block b = w.getBlockAt((int) Math.floor(loc.getX() + dx), y, (int) Math.floor(loc.getZ() + dz));
                if (b.getType().isSolid()) return true;
            }
        }
        return false;
    }

    private Material blockBelow(Location loc) {
        World w = loc.getWorld();
        if (w == null) return Material.AIR;
        return w.getBlockAt((int) Math.floor(loc.getX()), (int) Math.floor(loc.getY() - 0.3),
                (int) Math.floor(loc.getZ())).getType();
    }

    private boolean isIce(Material m) {
        return m == Material.ICE || m == Material.PACKED_ICE || m == Material.BLUE_ICE || m == Material.FROSTED_ICE;
    }
}
