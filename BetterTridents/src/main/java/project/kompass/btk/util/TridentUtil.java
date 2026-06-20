package project.kompass.btk.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;
import java.util.Set;

public final class TridentUtil {

    public static final NamespacedKey RANGE_KEY = new NamespacedKey("better_tridents", "reach_distance");
    public static final NamespacedKey BLOCK_RANGE_KEY = new NamespacedKey("better_tridents", "block_reach");
    public static final NamespacedKey SPEED_KEY = new NamespacedKey("better_tridents", "attack_speed");

    public static final NamespacedKey CHANNELING_PROTECTED_KEY = new NamespacedKey("better_tridents", "channeling_protected");
    public static final NamespacedKey CHANNELING_LIGHTNING_KEY = new NamespacedKey("better_tridents", "channeling_lightning");

    private static final Set<Material> DAMAGE_TOOLS = EnumSet.noneOf(Material.class);
    private static final Set<Material> SPEARS = EnumSet.noneOf(Material.class);

    static {
        for (Material material : Material.values()) {
            String name = material.name();
            if (name.startsWith("LEGACY_")) continue;

            if (name.contains("SWORD") || name.contains("AXE") || name.contains("SPEAR") || name.contains("TRIDENT") || name.contains("MACE")) {
                DAMAGE_TOOLS.add(material);
            }
            if (name.contains("SPEAR")) {
                SPEARS.add(material);
            }
        }
    }

    private TridentUtil() {}

    public static boolean isTrident(ItemStack item) {
        return item != null && DAMAGE_TOOLS.contains(item.getType()) && item.getType() == Material.TRIDENT;
    }

    // MICRO-OPTIMIZATION: Evaluates block lookups exactly once to prevent CraftBlock heap allocation churn
    public static boolean isVictimWet(LivingEntity victim) {
        if (victim.isInWater()) return true;

        Block block = victim.getLocation().getBlock();
        if (block.getType() == Material.BUBBLE_COLUMN) return true;

        if (victim.getWorld().hasStorm()) {
            return block.getLightFromSky() > 0;
        }

        return false;
    }

    // MICRO-OPTIMIZATION: Pulls the block instance once and passes it to secondary checks
    public static boolean isPlayerWet(Player player) {
        if (player.isInWater()) return true;

        Block block = player.getLocation().getBlock();
        boolean isStorming = player.getWorld().hasStorm();
        boolean hasSkyLight = block.getLightFromSky() > 0;

        return isStorming && hasSkyLight && !isSnowing(player, block);
    }

    // MICRO-OPTIMIZATION: Uses pre-evaluated Block parameter to avoid secondary block retrieval
    public static boolean isSnowing(Player player, Block block) {
        if (!player.getWorld().hasStorm()) return false;
        double temp = block.getTemperature();

        String biomeName = block.getBiome().toString().toLowerCase();

        boolean hasPrecipitation = !biomeName.contains("desert")
                && !biomeName.contains("badlands")
                && !biomeName.contains("savanna");

        return hasPrecipitation && temp < 0.15;
    }

    public static boolean isDamageDealingTool(ItemStack item) {
        return item != null && DAMAGE_TOOLS.contains(item.getType());
    }

    public static boolean isSpear(ItemStack item) {
        return item != null && SPEARS.contains(item.getType());
    }
}