package club.aurovion.aegis;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class AegisCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUB = Arrays.asList("alerts", "vl", "checks", "reload");
    private final Aegis plugin;

    public AegisCommand(Aegis plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String tag = ChatColor.DARK_GRAY + "[" + ChatColor.RED + "Aegis" + ChatColor.DARK_GRAY + "] " + ChatColor.GRAY;

        if (args.length == 0) {
            sender.sendMessage(tag + "v" + plugin.getDescription().getVersion()
                    + " — " + ChatColor.WHITE + "/aegis <alerts|vl|checks|reload>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.reload();
                sender.sendMessage(tag + ChatColor.GREEN + "Configuration reloaded.");
            }
            case "alerts" -> {
                if (!(sender instanceof Player p)) {
                    sender.sendMessage(tag + ChatColor.RED + "Only players can toggle alerts.");
                    return true;
                }
                boolean on = plugin.violations().toggleAlerts(p.getUniqueId());
                sender.sendMessage(tag + "Alerts " + (on ? ChatColor.GREEN + "enabled" : ChatColor.RED + "muted") + ".");
            }
            case "checks" -> {
                AegisSettings s = plugin.settings();
                sender.sendMessage(tag + ChatColor.WHITE + "Checks");
                sender.sendMessage(line("Speed", s.speedEnabled, "max " + s.speedMaxHorizontal + " b/t, punish " + s.speedPunishAt));
                sender.sendMessage(line("Fly", s.flyEnabled, "air " + s.flyAirTicks + "t, punish " + s.flyPunishAt));
                sender.sendMessage(line("Reach", s.reachEnabled, "max " + s.reachMax + " b, punish " + s.reachPunishAt));
            }
            case "vl" -> {
                if (args.length < 2) {
                    sender.sendMessage(tag + ChatColor.RED + "Usage: /aegis vl <player>");
                    return true;
                }
                Player target = plugin.getServer().getPlayerExact(args[1]);
                if (target == null) {
                    sender.sendMessage(tag + ChatColor.RED + "Player not online.");
                    return true;
                }
                PlayerData d = plugin.data(target.getUniqueId());
                if (d == null) {
                    sender.sendMessage(tag + ChatColor.RED + "No data for that player.");
                    return true;
                }
                sender.sendMessage(tag + ChatColor.WHITE + "Violations for " + target.getName());
                for (Check c : Check.values()) {
                    sender.sendMessage(ChatColor.GRAY + " • " + c.display() + ": "
                            + ChatColor.WHITE + String.format("%.1f", d.getVl(c)));
                }
                sender.sendMessage(ChatColor.GRAY + " • Total: " + ChatColor.WHITE + String.format("%.1f", d.totalVl()));
            }
            default -> sender.sendMessage(tag + ChatColor.RED + "Unknown subcommand.");
        }
        return true;
    }

    private static String line(String name, boolean enabled, String detail) {
        return ChatColor.GRAY + " • " + name + ": "
                + (enabled ? ChatColor.GREEN + "on" : ChatColor.RED + "off")
                + ChatColor.DARK_GRAY + "  (" + detail + ")";
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return SUB.stream().filter(o -> o.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("vl")) {
            List<String> names = new ArrayList<>();
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) names.add(p.getName());
            }
            return names;
        }
        return Collections.emptyList();
    }
}
