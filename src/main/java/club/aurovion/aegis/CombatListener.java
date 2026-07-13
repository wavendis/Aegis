package club.aurovion.aegis;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.BoundingBox;

/**
 * Reach check: measures the distance from the attacker's eye to the closest
 * point of the victim's hitbox on melee hits, and cancels hits beyond the
 * configured limit.
 */
public final class CombatListener implements Listener {

    private final Aegis plugin;

    public CombatListener(Aegis plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        AegisSettings s = plugin.settings();
        if (!s.reachEnabled) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (plugin.isGloballyExempt(player)) return;

        Entity victim = event.getEntity();
        Location eye = player.getEyeLocation();
        BoundingBox box = victim.getBoundingBox();

        double dist = distanceToBox(eye.getX(), eye.getY(), eye.getZ(), box);
        if (dist > s.reachMax) {
            plugin.violations().flag(player, Check.REACH,
                    String.format("%.2f>%.2f b", dist, s.reachMax));
            event.setCancelled(true);
        }
    }

    /** Distance from a point to the closest point on an axis-aligned box. */
    private static double distanceToBox(double x, double y, double z, BoundingBox box) {
        double cx = clamp(x, box.getMinX(), box.getMaxX());
        double cy = clamp(y, box.getMinY(), box.getMaxY());
        double cz = clamp(z, box.getMinZ(), box.getMaxZ());
        double dx = x - cx, dy = y - cy, dz = z - cz;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private static double clamp(double v, double min, double max) {
        return v < min ? min : Math.min(v, max);
    }
}
