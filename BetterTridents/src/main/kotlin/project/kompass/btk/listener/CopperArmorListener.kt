package project.kompass.btk.listener

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Mob
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import java.util.HashSet
import java.util.Random

class CopperArmorListener : Listener {

    private val random = Random()

    fun startArmorCheckTask(plugin: JavaPlugin) {
        object : BukkitRunnable() {
            override fun run() {
                val processed = HashSet<LivingEntity>()

                for (player in Bukkit.getOnlinePlayers()) {
                    val world = player.world
                    if (!world.hasStorm()) continue

                    val playerHighestY = world.getHighestBlockYAt(player.location)
                    if (player.location.blockY < playerHighestY) continue

                    // 1. Process the player
                    if (!processed.contains(player)) {
                        processed.add(player)
                        checkAndStrike(player, world)
                    }

                    // 2. Scan a local 32-block box around the player for nearby mobs
                    val radius = 32.0
                    for (nearby in player.getNearbyEntities(radius, radius, radius)) {
                        if (nearby is Mob) {
                            if (processed.contains(nearby)) continue
                            processed.add(nearby)

                            val mobHighestY = world.getHighestBlockYAt(nearby.location)
                            if (nearby.location.blockY < mobHighestY) continue

                            checkAndStrike(nearby, world)
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 600L, 600L)
    }

    fun startParticleTask(plugin: JavaPlugin) {
        object : BukkitRunnable() {
            override fun run() {
                val processed = HashSet<LivingEntity>()

                for (player in Bukkit.getOnlinePlayers()) {
                    val world = player.world
                    if (!world.hasStorm()) continue

                    // Process player particles
                    if (!processed.contains(player)) {
                        processed.add(player)
                        val playerHighestY = world.getHighestBlockYAt(player.location)
                        if (player.location.blockY >= playerHighestY) {
                            spawnCopperParticlesIfEligible(player, world)
                        }
                    }

                    // Process nearby mob particles within a 32-block radius
                    val radius = 32.0
                    for (nearby in player.getNearbyEntities(radius, radius, radius)) {
                        if (nearby is Mob) {
                            if (processed.contains(nearby)) continue
                            processed.add(nearby)

                            val mobHighestY = world.getHighestBlockYAt(nearby.location)
                            if (nearby.location.blockY >= mobHighestY) {
                                spawnCopperParticlesIfEligible(nearby, world)
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 10L, 10L)
    }

    private fun checkAndStrike(entity: LivingEntity, world: World) {
        val equipment = entity.equipment ?: return

        val armorContents = equipment.armorContents
        var copperCount = 0
        for (item in armorContents) {
            if (item != null && isCopperArmor(item.type)) {
                copperCount++
            }
        }
        if (copperCount == 0) return

        val chance = copperCount * 0.25
        if (random.nextDouble() < chance) {
            world.strikeLightning(entity.location)
        }
    }

    private fun spawnCopperParticlesIfEligible(entity: LivingEntity, world: World) {
        val equipment = entity.equipment ?: return

        val armorContents = equipment.armorContents
        var copperCount = 0
        for (item in armorContents) {
            if (item != null && isCopperArmor(item.type)) {
                copperCount++
            }
        }
        if (copperCount == 0) return

        var particleCount = copperCount * 2
        if (copperCount == 4) {
            particleCount = 10
        }

        val offsetX = 0.15 * copperCount
        val offsetY = entity.height / 2.0
        val offsetZ = 0.15 * copperCount

        world.spawnParticle(
            org.bukkit.Particle.ELECTRIC_SPARK,
            entity.location.add(0.0, offsetY, 0.0),
            particleCount,
            offsetX,
            offsetY,
            offsetZ,
            0.01
        )
    }

    private fun isCopperArmor(type: Material): Boolean {
        return type == Material.COPPER_HELMET ||
                type == Material.COPPER_CHESTPLATE ||
                type == Material.COPPER_LEGGINGS ||
                type == Material.COPPER_BOOTS
    }
}