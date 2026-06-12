package project.kompass.btk.util;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

public final class TridentUtil {

    public static final NamespacedKey RANGE_KEY = new NamespacedKey("better_tridents", "reach_distance");
    public static final NamespacedKey BLOCK_RANGE_KEY = new NamespacedKey("better_tridents", "block_reach");

    private TridentUtil() {
        // Private constructor to prevent instantiation
    }

    public static boolean isTrident(ItemStack item) {
        return item != null && item.getType() == Material.TRIDENT;
    }

    public static boolean isVictimWet(LivingEntity victim) {
        return victim.isInWater() ||
                (victim.getWorld().hasStorm() && victim.getLocation().getBlock().getLightFromSky() > 0) ||
                victim.getLocation().getBlock().getType() == Material.BUBBLE_COLUMN;
    }
}