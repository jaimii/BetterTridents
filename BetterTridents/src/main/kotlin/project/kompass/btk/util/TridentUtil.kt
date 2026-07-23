package project.kompass.btk.util

import project.kompass.btk.hook.ConsumeFood2Hook
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.EnumSet

fun ItemStack?.isTrident(): Boolean {
    return this != null && this.type == Material.TRIDENT
}

fun ItemStack?.isSpear(): Boolean {
    return this != null && TridentUtil.SPEARS.contains(this.type)
}

fun ItemStack?.isDamageDealingTool(): Boolean {
    return this != null && TridentUtil.DAMAGE_TOOLS.contains(this.type)
}

fun ItemStack?.isPotionOrSoup(): Boolean {
    if (this == null || this.type == Material.AIR) return false
    val type = this.type
    return type == Material.POTION || type == Material.SPLASH_POTION || type == Material.LINGERING_POTION ||
            type == Material.MUSHROOM_STEW || type == Material.RABBIT_STEW ||
            type == Material.BEETROOT_SOUP || type == Material.SUSPICIOUS_STEW
}

fun ItemStack?.isBundle(): Boolean {
    return this != null && TridentUtil.BUNDLE_MATERIALS.contains(this.type)
}

// Multi-Layer Food Check: Validates vanilla food, ConsumeFood2 items, and Paper FoodComponents
fun ItemStack?.isFood(): Boolean {
    if (this == null || this.type == Material.AIR) return false

    // 1. Check native vanilla edible flag
    if (this.type.isEdible) return true

    // 2. Check if registered inside ConsumeFood2 (handles non-vanilla custom items)
    if (ConsumeFood2Hook.isConsumeFoodMaterial(this.type)) return true

    // 3. Check if carrying a Paper FoodComponent
    if (this.hasItemMeta()) {
        val meta = this.itemMeta
        if (meta != null && meta.hasFood()) return true
    }

    return false
}

object TridentUtil {

    val RANGE_KEY = NamespacedKey("better_tridents", "reach_distance")
    val BLOCK_RANGE_KEY = NamespacedKey("better_tridents", "block_reach")
    val SPEED_KEY = NamespacedKey("better_tridents", "attack_speed")

    val CHANNELING_LIGHTNING_KEY = NamespacedKey("better_tridents", "channeling_lightning")
    val LOOTING_KEY = NamespacedKey("better_tridents", "looting_level")

    internal val DAMAGE_TOOLS: Set<Material> = EnumSet.noneOf(Material::class.java)
    internal val SPEARS: Set<Material> = EnumSet.noneOf(Material::class.java)
    internal val BUNDLE_MATERIALS: Set<Material> = EnumSet.noneOf(Material::class.java)

    init {
        for (material in Material.entries) {
            val name = material.name
            if (name.startsWith("LEGACY_")) continue

            if (name.contains("SWORD") || name.contains("AXE") || name.contains("SPEAR") || name.contains("TRIDENT") || name.contains("MACE")) {
                (DAMAGE_TOOLS as EnumSet).add(material)
            }
            if (name.contains("SPEAR")) {
                (SPEARS as EnumSet).add(material)
            }
            if (name == "BUNDLE" || name.endsWith("_BUNDLE")) {
                (BUNDLE_MATERIALS as EnumSet).add(material)
            }
        }
    }

    fun isVictimWet(victim: LivingEntity): Boolean {
        if (victim.isInWater) return true

        val block = victim.location.block
        if (block.type == Material.BUBBLE_COLUMN) return true

        return if (victim.world.hasStorm()) {
            block.lightFromSky > 0
        } else {
            false
        }
    }

    fun isPlayerWet(player: Player): Boolean {
        if (player.isInWater) return true

        val block = player.location.block
        val isStorming = player.world.hasStorm()
        val hasSkyLight = block.lightFromSky > 0

        return isStorming && hasSkyLight && !isSnowing(player, block)
    }

    fun isSnowing(player: Player, block: Block): Boolean {
        if (!player.world.hasStorm()) return false
        val temp = block.temperature
        val biomeName = block.biome.toString().lowercase()

        val hasPrecipitation = !biomeName.contains("desert") &&
                !biomeName.contains("badlands") &&
                !biomeName.contains("savanna")

        return hasPrecipitation && temp < 0.15
    }
}