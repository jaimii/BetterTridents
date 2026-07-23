package project.kompass.btk.listener

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.meta.EnchantmentStorageMeta

class TridentAnvilListener : Listener {

    companion object {
        private const val ANVIL_REPAIR_COST = 5
    }

    @EventHandler
    fun onAnvilUse(event: PrepareAnvilEvent) {
        val first = event.inventory.getItem(0)
        val second = event.inventory.getItem(1)
        if (first == null || second == null || first.type == Material.AIR) return

        val result = first.clone()
        val incoming = if (second.type == Material.ENCHANTED_BOOK) {
            (second.itemMeta as EnchantmentStorageMeta).storedEnchants
        } else {
            second.enchantments
        }

        var added = 0

        for ((enchant, incomingLvl) in incoming) {
            val cur = result.getEnchantmentLevel(enchant)
            var next = if (cur == incomingLvl) cur + 1 else Math.max(cur, incomingLvl)

            val maxLevel = enchant.maxLevel
            next = Math.min(next, maxLevel)

            if (next > cur) {
                result.addUnsafeEnchantment(enchant, next)
                added++
            }
        }

        if (added > 0) {
            val meta = result.itemMeta ?: return
            val renameText = event.view.renameText

            if (!renameText.isNullOrEmpty()) {
                meta.displayName(Component.text(renameText))
            }

            result.itemMeta = meta
            event.result = result
            event.view.repairCost = Math.min(added, ANVIL_REPAIR_COST)
        }
    }
}