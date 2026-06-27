package project.kompass.btk

import project.kompass.btk.listener.*
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.java.JavaPlugin

class BTK : JavaPlugin() {

    override fun onEnable() {
        saveDefaultConfig()

        val pm = server.pluginManager

        pm.registerEvents(TridentAttributeListener(), this)
        pm.registerEvents(TridentRiptideListener(), this)
        pm.registerEvents(TridentChannelingListener(this), this)
        pm.registerEvents(TridentDamageListener(), this)
        pm.registerEvents(TridentLootingListener(), this)
        pm.registerEvents(TridentAnvilListener(), this)
        pm.registerEvents(SpearListener(), this)
        pm.registerEvents(PotionSoupStackListener(), this)
        pm.registerEvents(ArmorDurabilityListener(this), this) // Register durability listener

        val copperArmorListener = CopperArmorListener()
        pm.registerEvents(copperArmorListener, this)
        copperArmorListener.startArmorCheckTask(this)
        copperArmorListener.startParticleTask(this)
    }

    override fun onDisable() {
    }
}