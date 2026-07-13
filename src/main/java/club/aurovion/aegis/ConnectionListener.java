package club.aurovion.aegis;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/** Lifecycle + grace handling so teleports/respawns don't trip movement checks. */
public final class ConnectionListener implements Listener {

    private final Aegis plugin;

    public ConnectionListener(Aegis plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        PlayerData data = plugin.createData(p.getUniqueId());
        data.lastLocation = p.getLocation();
        data.teleportGraceUntil = System.currentTimeMillis() + plugin.settings().teleportGraceMs;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.removeData(event.getPlayer().getUniqueId());
        plugin.violations().clear(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        PlayerData data = plugin.data(event.getPlayer().getUniqueId());
        if (data == null || event.getTo() == null) return;
        data.teleportGraceUntil = System.currentTimeMillis() + plugin.settings().teleportGraceMs;
        data.lastLocation = event.getTo();
        data.airTicks = 0;
        data.speedBufferCount = 0;
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        PlayerData data = plugin.data(event.getPlayer().getUniqueId());
        if (data == null) return;
        data.teleportGraceUntil = System.currentTimeMillis() + plugin.settings().teleportGraceMs;
        data.lastLocation = event.getRespawnLocation();
        data.airTicks = 0;
        data.speedBufferCount = 0;
    }
}
