package project.kompass.btk

import project.kompass.btk.listener.*
import org.bukkit.plugin.java.JavaPlugin

class BTK : JavaPlugin() {

    override fun onEnable() {
        val pm = server.pluginManager

        // Register all separated listener classes
        pm.registerEvents(TridentAttributeListener(), this)
        pm.registerEvents(TridentRiptideListener(), this)
        pm.registerEvents(TridentChannelingListener(this), this)
        pm.registerEvents(TridentDamageListener(), this)
        pm.registerEvents(TridentLootingListener(), this)
        pm.registerEvents(TridentAnvilListener(), this)
        pm.registerEvents(SpearListener(), this)
        pm.registerEvents(PotionSoupStackListener(), this)

        // Initiate Copper Armor hazard loops & particle task
        val copperArmorListener = CopperArmorListener()
        pm.registerEvents(copperArmorListener, this)
        copperArmorListener.startArmorCheckTask(this)
        copperArmorListener.startParticleTask(this)
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}