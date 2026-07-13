package club.aurovion.aegis;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

/** Immutable snapshot of the Aegis configuration. */
public final class AegisSettings {

    public final boolean exemptOps;
    public final boolean alertConsole;
    public final long teleportGraceMs;
    public final double decayPerSecond;

    public final boolean speedEnabled;
    public final double speedMaxHorizontal;
    public final int speedBuffer;
    public final double speedVl;
    public final double speedPunishAt;

    public final boolean flyEnabled;
    public final int flyAirTicks;
    public final double flyVl;
    public final double flyPunishAt;

    public final boolean reachEnabled;
    public final double reachMax;
    public final double reachVl;
    public final double reachPunishAt;

    public final List<String> punishCommands = new ArrayList<>();

    public AegisSettings(FileConfiguration c) {
        this.exemptOps = c.getBoolean("exempt-ops", true);
        this.alertConsole = c.getBoolean("alert-console", true);
        this.teleportGraceMs = Math.max(0, c.getLong("teleport-grace-ms", 1500));
        this.decayPerSecond = Math.max(0, c.getDouble("decay-per-second", 0.25));

        this.speedEnabled = c.getBoolean("checks.speed.enabled", true);
        this.speedMaxHorizontal = c.getDouble("checks.speed.max-horizontal", 0.45);
        this.speedBuffer = Math.max(1, c.getInt("checks.speed.buffer", 6));
        this.speedVl = c.getDouble("checks.speed.vl-per-flag", 1.0);
        this.speedPunishAt = c.getDouble("checks.speed.punish-at", 15);

        this.flyEnabled = c.getBoolean("checks.fly.enabled", true);
        this.flyAirTicks = Math.max(6, c.getInt("checks.fly.air-ticks", 14));
        this.flyVl = c.getDouble("checks.fly.vl-per-flag", 1.0);
        this.flyPunishAt = c.getDouble("checks.fly.punish-at", 12);

        this.reachEnabled = c.getBoolean("checks.reach.enabled", true);
        this.reachMax = c.getDouble("checks.reach.max-distance", 3.15);
        this.reachVl = c.getDouble("checks.reach.vl-per-flag", 1.0);
        this.reachPunishAt = c.getDouble("checks.reach.punish-at", 8);

        this.punishCommands.addAll(c.getStringList("punishments.commands"));
    }

    public boolean isEnabled(Check check) {
        return switch (check) {
            case SPEED -> speedEnabled;
            case FLY -> flyEnabled;
            case REACH -> reachEnabled;
        };
    }

    public double vlPerFlag(Check check) {
        return switch (check) {
            case SPEED -> speedVl;
            case FLY -> flyVl;
            case REACH -> reachVl;
        };
    }

    public double punishAt(Check check) {
        return switch (check) {
            case SPEED -> speedPunishAt;
            case FLY -> flyPunishAt;
            case REACH -> reachPunishAt;
        };
    }
}
