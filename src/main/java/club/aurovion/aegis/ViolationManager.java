package club.aurovion.aegis;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central place a check reports to. Adds violation levels, notifies staff, and
 * runs the punishment ladder when a check crosses its threshold.
 */
public final class ViolationManager {

    private final Aegis plugin;
    // Staff who have muted alerts with /aegis alerts.
    private final Set<UUID> alertsMuted = ConcurrentHashMap.newKeySet();

    public ViolationManager(Aegis plugin) {
        this.plugin = plugin;
    }

    /** Called by checks (always on the main thread) when a player fails. */
    public void flag(Player player, Check check, String detail) {
        AegisSettings s = plugin.settings();
        PlayerData data = plugin.data(player.getUniqueId());
        if (data == null) return;

        double vl = data.addVl(check, s.vlPerFlag(check));
        alertStaff(player, check, vl, detail);

        if (s.alertConsole) {
            plugin.getLogger().info(player.getName() + " failed " + check.display()
                    + " (vl " + fmt(vl) + ") " + detail);
        }

        if (vl >= s.punishAt(check)) {
            punish(player, check, vl);
            data.setVl(check, 0);
        }
    }

    private void alertStaff(Player player, Check check, double vl, String detail) {
        String msg = ChatColor.DARK_GRAY + "[" + ChatColor.RED + "Aegis" + ChatColor.DARK_GRAY + "] "
                + ChatColor.WHITE + player.getName() + ChatColor.GRAY + " failed "
                + ChatColor.RED + check.display() + ChatColor.GRAY + " (vl " + ChatColor.WHITE + fmt(vl)
                + ChatColor.GRAY + ") " + ChatColor.DARK_GRAY + detail;

        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (staff.hasPermission("aegis.alerts") && !alertsMuted.contains(staff.getUniqueId())) {
                staff.sendMessage(msg);
            }
        }
    }

    private void punish(Player player, Check check, double vl) {
        AegisSettings s = plugin.settings();
        String announce = ChatColor.DARK_GRAY + "[" + ChatColor.RED + "Aegis" + ChatColor.DARK_GRAY + "] "
                + ChatColor.WHITE + player.getName() + ChatColor.GRAY + " reached the "
                + ChatColor.RED + check.display() + ChatColor.GRAY + " punishment threshold.";
        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (staff.hasPermission("aegis.alerts") && !alertsMuted.contains(staff.getUniqueId())) {
                staff.sendMessage(announce);
            }
        }
        for (String raw : s.punishCommands) {
            String command = ChatColor.translateAlternateColorCodes('&',
                    raw.replace("%player%", player.getName()));
            try {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            } catch (Throwable t) {
                plugin.getLogger().warning("Punishment command failed: " + t.getMessage());
            }
        }
    }

    /** Toggle alerts for a staff member. Returns true if now receiving alerts. */
    public boolean toggleAlerts(UUID uuid) {
        if (alertsMuted.remove(uuid)) return true;
        alertsMuted.add(uuid);
        return false;
    }

    public void clear(UUID uuid) {
        alertsMuted.remove(uuid);
    }

    private static String fmt(double v) {
        return String.format("%.1f", v);
    }
}
