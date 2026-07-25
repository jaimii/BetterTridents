package project.kompass.btk

import project.kompass.btk.listener.*
import org.bukkit.plugin.java.JavaPlugin

class BTK : JavaPlugin() {

    override fun onEnable() {
        saveDefaultConfig()

        val pm = server.pluginManager

        pm.registerEvents(TridentAttributeListener(), this)
        pm.registerEvents(TridentRiptideListener(), this)
        pm.registerEvents(TridentChannelingListener(this), this)
        pm.registerEvents(TridentDamageListener(), this)
        pm.registerEvents(RangedLootingListener(), this)
        pm.registerEvents(TridentAnvilListener(), this)
        pm.registerEvents(SpearListener(), this)
        pm.registerEvents(ArmorDurabilityListener(this), this)
        pm.registerEvents(PotionSoupStackListener(), this)
        pm.registerEvents(BundleCapacityListener(this), this)
        pm.registerEvents(AlwaysEatListener(this), this)
        pm.registerEvents(EnchantablePetArmorListener(), this)
        pm.registerEvents(WindChargeListener(this), this) // Registered Wind Charge Listener

        // Initiate Copper Armor hazard loops & particle task
        val copperArmorListener = CopperArmorListener()
        pm.registerEvents(copperArmorListener, this)
        copperArmorListener.startArmorCheckTask(this)
        copperArmorListener.startParticleTask(this)

        // Initiate Magical Campfire task
        val campfireListener = MagicalCampfireListener(this)
        pm.registerEvents(campfireListener, this)
        campfireListener.startCampfireTask()
    }

    override fun onDisable() {
    }
}