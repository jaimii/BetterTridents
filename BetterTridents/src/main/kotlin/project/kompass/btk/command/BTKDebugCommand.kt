package project.kompass.btk.command

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import java.util.UUID
import java.util.HashSet
import java.util.concurrent.ConcurrentHashMap

class BTKDebugCommand : CommandExecutor {

    companion object {
        private val debuggers = ConcurrentHashMap.newKeySet<UUID>()

        fun isDebugEnabled(player: Player): Boolean {
            return debuggers.contains(player.uniqueId)
        }

        fun getDebuggers(): Set<UUID> {
            return debuggers
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("This command can only be executed by players.")
            return true
        }

        val player = sender
        if (!player.isOp && !player.hasPermission("btk.admin")) {
            player.sendMessage("§cYou do not have permission to execute this command.")
            return true
        }

        if (debuggers.contains(player.uniqueId)) {
            debuggers.remove(player.uniqueId)
            player.sendMessage("§a[BetterTridents] Debugging mode disabled.")
        } else {
            debuggers.add(player.uniqueId)
            player.sendMessage("§a[BetterTridents] Debugging mode enabled! You will now receive real-time item merging diagnostics in chat.")
        }

        return true
    }

    fun startDebugTask(plugin: JavaPlugin) {
        val loggedPairs = HashSet<String>()

        object : BukkitRunnable() {
            override fun run() {
                if (debuggers.isEmpty()) {
                    loggedPairs.clear()
                    return
                }

                for (uuid in debuggers) {
                    val player = Bukkit.getPlayer(uuid) ?: continue
                    if (!player.isOnline) continue

                    val world = player.world
                    val items = player.getNearbyEntities(16.0, 16.0, 16.0)
                        .filterIsInstance<Item>()

                    for (i in items.indices) {
                        val item1 = items[i]
                        for (j in i + 1 until items.size) {
                            val item2 = items[j]

                            if (item1.itemStack.type == item2.itemStack.type &&
                                item1.location.distanceSquared(item2.location) <= 4.0) {

                                val pairId = "${item1.uniqueId}_${item2.uniqueId}"
                                val reversePairId = "${item2.uniqueId}_${item1.uniqueId}"

                                if (!loggedPairs.contains(pairId) && !loggedPairs.contains(reversePairId)) {
                                    loggedPairs.add(pairId)
                                    debugItemDifferences(item1, item2, player)
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 40L, 40L)
    }

    private fun debugItemDifferences(item1: Item, item2: Item, player: Player) {
        val stack1 = item1.itemStack
        val stack2 = item2.itemStack

        player.sendMessage("§c[BTK-Debug] Non-merging items found near each other:")
        player.sendMessage("§eItem Type: §f${stack1.type}")
        player.sendMessage("§7- §eEntity 1 ID: §f${item1.uniqueId}")
        player.sendMessage("§7- §eEntity 2 ID: §f${item2.uniqueId}")

        // Check ItemStack similarity
        if (!stack1.isSimilar(stack2)) {
            player.sendMessage("§c -> Mismatch: ItemStack.isSimilar() is FALSE")

            val hasMeta1 = stack1.hasItemMeta()
            val hasMeta2 = stack2.hasItemMeta()

            if (hasMeta1 != hasMeta2) {
                player.sendMessage("§7   * ItemMeta presence mismatch: §f$hasMeta1 §7vs §f$hasMeta2 (One has NBT, the other does not!)")
            }

            if (hasMeta1 && hasMeta2) {
                val meta1 = stack1.itemMeta
                val meta2 = stack2.itemMeta

                if (meta1 != null && meta2 != null) {
                    val name1 = meta1.displayName()
                    val name2 = meta2.displayName()
                    if (name1 != name2) {
                        player.sendMessage("§7   * Display Name discrepancy: §f$name1 §7vs §f$name2")
                    }

                    if (stack1.enchantments != stack2.enchantments) {
                        player.sendMessage("§7   * Enchantments mismatch: §f${stack1.enchantments} §7vs §f${stack2.enchantments}")
                    }

                    val pdc1 = meta1.persistentDataContainer.keys
                    val pdc2 = meta2.persistentDataContainer.keys
                    if (pdc1 != pdc2) {
                        player.sendMessage("§7   * ItemStack PDC Namespace keys differ: §f$pdc1 §7vs §f$pdc2")
                    }
                }
            }
        } else {
            player.sendMessage("§a -> Match: ItemStack.isSimilar() is TRUE (Item stacks are identical)")
        }

        if (item1.pickupDelay != item2.pickupDelay) {
            player.sendMessage("§7 - Pickup Delay disparity: §f${item1.pickupDelay} ticks §7vs §f${item2.pickupDelay} ticks")
        }
        if (item1.fireTicks != item2.fireTicks) {
            player.sendMessage("§7 - Fire Ticks discrepancy: §f${item1.fireTicks} ticks §7vs §f${item2.fireTicks} ticks")
        }

        val epdc1 = item1.persistentDataContainer.keys
        val epdc2 = item2.persistentDataContainer.keys
        if (epdc1 != epdc2) {
            player.sendMessage("§7 - Entity PDC Namespace keys differ: §f$epdc1 §7vs §f$epdc2")
        }
    }
}