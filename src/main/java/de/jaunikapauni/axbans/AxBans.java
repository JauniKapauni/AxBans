package de.jaunikapauni.axbans;

import de.jaunikapauni.axbans.manager.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class AxBans extends JavaPlugin {
    DatabaseManager databaseManager;
    public DatabaseManager getDatabaseManager(){
        return databaseManager;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        databaseManager = new DatabaseManager(this);
        try {
            if (databaseManager.initDatabaseTable1() == false) {
                getLogger().severe("Error creating table!");
                Bukkit.getServer().shutdown();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        getLogger().info("DB connection successfully established!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
