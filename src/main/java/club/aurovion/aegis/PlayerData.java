package club.aurovion.aegis;

import org.bukkit.Location;

import java.util.EnumMap;
import java.util.Map;

/** Per-player anti-cheat state (violation levels + movement bookkeeping). */
public final class PlayerData {

    private final Map<Check, Double> violations = new EnumMap<>(Check.class);

    // Movement tracking
    public Location lastLocation;
    public int airTicks;
    public int speedBufferCount;
    public long teleportGraceUntil;

    public double addVl(Check check, double amount) {
        double v = violations.getOrDefault(check, 0.0) + amount;
        violations.put(check, v);
        return v;
    }

    public double getVl(Check check) {
        return violations.getOrDefault(check, 0.0);
    }

    public void setVl(Check check, double value) {
        violations.put(check, Math.max(0.0, value));
    }

    public double totalVl() {
        double sum = 0;
        for (double v : violations.values()) sum += v;
        return sum;
    }

    public void decay(double amount) {
        for (Map.Entry<Check, Double> e : violations.entrySet()) {
            e.setValue(Math.max(0.0, e.getValue() - amount));
        }
    }

    public boolean inTeleportGrace() {
        return System.currentTimeMillis() < teleportGraceUntil;
    }
}
