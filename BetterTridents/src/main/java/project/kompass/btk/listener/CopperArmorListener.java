package project.kompass.btk.listener;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class CopperArmorListener implements Listener {

    private final Random random = new Random();

    public void startArmorCheckTask(JavaPlugin plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                // 1. Sweep online players first
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getWorld().hasStorm()) {
                        checkAndStrike(player, player.getWorld());
                    }
                }

                // 2. Sweep mobs in loaded storming worlds using optimized getEntitiesByClass()
                for (World world : Bukkit.getWorlds()) {
                    if (!world.hasStorm()) continue;

                    for (Mob mob : world.getEntitiesByClass(Mob.class)) {
                        checkAndStrike(mob, world);
                    }
                }
            }
        }.runTaskTimer(plugin, 600L, 600L); // Execute every 30 seconds
    }

    private void checkAndStrike(LivingEntity entity, World world) {
        // Must be exposed to open sky
        int highestY = world.getHighestBlockYAt(entity.getLocation());
        if (entity.getLocation().getBlockY() < highestY) return;

        EntityEquipment equipment = entity.getEquipment();
        if (equipment == null) return;

        ItemStack[] armorContents = equipment.getArmorContents();
        int copperCount = 0;
        for (ItemStack item : armorContents) {
            if (item != null && isCopperArmor(item.getType())) {
                copperCount++;
            }
        }
        if (copperCount == 0) return;

        // Cumulative 25% chance per copper piece
        double chance = copperCount * 0.25;
        if (random.nextDouble() < chance) {
            world.strikeLightning(entity.getLocation());

            // 3. Strike any nearby hostile mobs wearing any piece of copper armor.
            double radius = 16.0; // 16 block search bounds
            for (Entity nearby : entity.getNearbyEntities(radius, radius, radius)) {
                if (nearby instanceof Monster hostile) {
                    EntityEquipment hostileEquipment = hostile.getEquipment();
                    if (hostileEquipment != null) {
                        for (ItemStack armor : hostileEquipment.getArmorContents()) {
                            if (armor != null && isCopperArmor(armor.getType())) {
                                world.strikeLightning(hostile.getLocation());
                                break; // Strike once and continue to the next mob
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isCopperArmor(Material type) {
        return type == Material.COPPER_HELMET ||
                type == Material.COPPER_CHESTPLATE ||
                type == Material.COPPER_LEGGINGS ||
                type == Material.COPPER_BOOTS;
    }
}