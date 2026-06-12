package project.kompass.btk;

import project.kompass.btk.listener.*;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class BTK extends JavaPlugin {

    @Override
    public void onEnable() {
        PluginManager pm = getServer().getPluginManager();

        // Register all separated listener classes
        pm.registerEvents(new TridentAttributeListener(), this);
        pm.registerEvents(new TridentRiptideListener(), this);
        pm.registerEvents(new TridentChannelingListener(), this);
        pm.registerEvents(new TridentDamageListener(), this);
        pm.registerEvents(new TridentLootingListener(), this);
        pm.registerEvents(new TridentAnvilListener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}