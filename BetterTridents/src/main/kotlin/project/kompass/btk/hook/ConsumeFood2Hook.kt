package project.kompass.btk.hook

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.plugin.java.JavaPlugin

object ConsumeFood2Hook {

    fun hookAlwaysEat(plugin: JavaPlugin) {
        val pluginManager = Bukkit.getPluginManager()
        val consumeFoodPlugin = pluginManager.getPlugin("ConsumeFood2") ?: return
        if (!pluginManager.isPluginEnabled(consumeFoodPlugin)) return

        try {
            // Reflectively query the VanillaFoodManager instance
            val getVanillaFoodManager = consumeFoodPlugin.javaClass.getMethod("getVanillaFoodManager")
            val foodManager = getVanillaFoodManager.invoke(consumeFoodPlugin) ?: return

            // Query registered food materials
            val getVanillaFoodMaterials = foodManager.javaClass.getMethod("getVanillaFoodMaterials")
            @Suppress("UNCHECKED_CAST")
            val materials = getVanillaFoodMaterials.invoke(foodManager) as? List<Material> ?: return

            val getVanillaFood = foodManager.javaClass.getMethod("getVanillaFood", Material::class.java)

            // Locate the Options enum class
            val optionsClass = Class.forName("me.msicraft.API.Food.Food\$Options")
            val alwaysEatOption = optionsClass.getField("ALWAYS_EAT").get(null)

            for (material in materials) {
                val vanillaFood = getVanillaFood.invoke(foodManager, material) ?: continue

                // setOption(Food.Options, Object)
                val setOption = vanillaFood.javaClass.getMethod("setOption", optionsClass, Any::class.java)
                setOption.invoke(vanillaFood, alwaysEatOption, true)
            }
            plugin.logger.info("[BTK] Successfully linked with ConsumeFood2 and forced ALWAYS_EAT across ${materials.size} items.")
        } catch (e: Throwable) {
            plugin.logger.warning("[BTK] Failed to reflectively map ConsumeFood2 hooks: ${e.message}")
        }
    }
}