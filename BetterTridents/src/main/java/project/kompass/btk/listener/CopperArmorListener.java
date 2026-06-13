package project.kompass.btk.listener;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class CopperArmorListener implements Listener {

    private final Random random = new Random();

    // 1. Core strike check task (Executes every 30 seconds)
    public void startArmorCheckTask(JavaPlugin plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Set<LivingEntity> processed = new HashSet<>();

                for (Player player : Bukkit.getOnlinePlayers()) {
                    World world = player.getWorld();
                    if (!world.hasStorm()) continue;

                    int playerHighestY = world.getHighestBlockYAt(player.getLocation());
                    if (player.getLocation().getBlockY() < playerHighestY) continue;

                    // Process player
                    if (!processed.contains(player)) {
                        processed.add(player);
                        checkAndStrike(player, world);
                    }

                    // Process nearby mobs within a 32-block radius
                    double radius = 32.0;
                    for (Entity nearby : player.getNearbyEntities(radius, radius, radius)) {
                        if (nearby instanceof Mob mob) {
                            if (processed.contains(mob)) continue;
                            processed.add(mob);

                            int mobHighestY = world.getHighestBlockYAt(mob.getLocation());
                            if (mob.getLocation().getBlockY() < mobHighestY) continue;

                            checkAndStrike(mob, world);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 600L, 600L);
    }

    // 2. High-performance ambient particle task (Executes every 10 ticks / 0.5 seconds)
    public void startParticleTask(JavaPlugin plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Set<LivingEntity> processed = new HashSet<>();

                for (Player player : Bukkit.getOnlinePlayers()) {
                    World world = player.getWorld();
                    if (!world.hasStorm()) continue;

                    // Process player particles
                    if (!processed.contains(player)) {
                        processed.add(player);
                        int playerHighestY = world.getHighestBlockYAt(player.getLocation());
                        if (player.getLocation().getBlockY() >= playerHighestY) {
                            spawnCopperParticlesIfEligible(player, world);
                        }
                    }

                    // Process nearby mob particles within a 32-block radius
                    double radius = 32.0;
                    for (Entity nearby : player.getNearbyEntities(radius, radius, radius)) {
                        if (nearby instanceof Mob mob) {
                            if (processed.contains(mob)) continue;
                            processed.add(mob);

                            int mobHighestY = world.getHighestBlockYAt(mob.getLocation());
                            if (mob.getLocation().getBlockY() >= mobHighestY) {
                                spawnCopperParticlesIfEligible(mob, world);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 10L, 10L);
    }

    private void checkAndStrike(LivingEntity entity, World world) {
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

        double chance = copperCount * 0.25;
        if (random.nextDouble() < chance) {
            world.strikeLightning(entity.getLocation());
        }
    }

    private void spawnCopperParticlesIfEligible(LivingEntity entity, World world) {
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

        // Scale the particle quantity based on the piece count
        int particleCount = copperCount * 2;
        if (copperCount == 4) {
            particleCount = 10; // Extra intense for full sets
        }

        // Adjust boundaries dynamically to scale the "leap" width of the sparks
        double offsetX = 0.15 * copperCount;
        double offsetY = entity.getHeight() / 2.0;
        double offsetZ = 0.15 * copperCount;

        // Spawn ELECTRIC_SPARK particles relative to the entity's center point
        world.spawnParticle(
                org.bukkit.Particle.ELECTRIC_SPARK,
                entity.getLocation().add(0, offsetY, 0),
                particleCount,
                offsetX,
                offsetY,
                offsetZ,
                0.01 // Minimal particle speed/noise parameter
        );
    }

    private boolean isCopperArmor(Material type) {
        return type == Material.COPPER_HELMET ||
                type == Material.COPPER_CHESTPLATE ||
                type == Material.COPPER_LEGGINGS ||
                type == Material.COPPER_BOOTS;
    }
}