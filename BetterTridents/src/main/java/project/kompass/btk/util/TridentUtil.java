package project.kompass.btk.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Biome;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;
import java.util.Set;

public final class TridentUtil {

    public static final NamespacedKey RANGE_KEY = new NamespacedKey("better_tridents", "reach_distance");
    public static final NamespacedKey BLOCK_RANGE_KEY = new NamespacedKey("better_tridents", "block_reach");
    public static final NamespacedKey SPEED_KEY = new NamespacedKey("better_tridents", "attack_speed");

    // Modern persistent keys replacing legacy deprecated metadata
    public static final NamespacedKey CHANNELING_PROTECTED_KEY = new NamespacedKey("better_tridents", "channeling_protected");
    public static final NamespacedKey CHANNELING_LIGHTNING_KEY = new NamespacedKey("better_tridents", "channeling_lightning");

    private static final Set<Material> DAMAGE_TOOLS = EnumSet.noneOf(Material.class);
    private static final Set<Material> SPEARS = EnumSet.noneOf(Material.class);

    static {
        for (Material material : Material.values()) {
            String name = material.name();
            if (name.startsWith("LEGACY_")) continue;

            if (name.contains("SWORD") || name.contains("PICKAXE") || name.contains("AXE") || name.contains("SHOVEL") || name.contains("HOE") || name.contains("SPEAR") || name.contains("TRIDENT") || name.contains("MACE")) {
                DAMAGE_TOOLS.add(material);
            }
            if (name.contains("SPEAR")) {
                SPEARS.add(material);
            }
        }
    }

    private TridentUtil() {}

    public static boolean isTrident(ItemStack item) {
        return item != null && item.getType() == Material.TRIDENT;
    }

    public static boolean isVictimWet(LivingEntity victim) {
        return victim.isInWater() ||
                (victim.getWorld().hasStorm() && victim.getLocation().getBlock().getLightFromSky() > 0) ||
                victim.getLocation().getBlock().getType() == Material.BUBBLE_COLUMN;
    }

    public static boolean isPlayerWet(Player player) {
        if (player.isInWater()) return true;

        boolean isStorming = player.getWorld().hasStorm();
        boolean hasSkyLight = player.getLocation().getBlock().getLightFromSky() > 0;

        return isStorming && hasSkyLight && !isSnowing(player);
    }

    public static boolean isSnowing(Player player) {
        if (!player.getWorld().hasStorm()) return false;
        Location loc = player.getLocation();
        double temp = loc.getBlock().getTemperature();

        Biome biome = loc.getBlock().getBiome();
        String biomeName = biome.toString().toLowerCase();

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