package de.jaunikapauni.axbans;

import de.jaunikapauni.axbans.command.BanCommand;
import de.jaunikapauni.axbans.command.UnbanCommand;
import de.jaunikapauni.axbans.listener.PlayerJoinListener;
import de.jaunikapauni.axbans.listener.PlayerQuitListener;
import de.jaunikapauni.axbans.manager.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.units.qual.A;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
        getCommand("ban").setExecutor(new BanCommand(this));
        getCommand("unban").setExecutor(new UnbanCommand(this));
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getLogger().info("");
        getLogger().info("----------------------------------------");
        getLogger().info("Name: " + getName());
        getLogger().info("Version: " + getDescription().getVersion());
        getLogger().info(String.join("Authors: " + ", ", getDescription().getAuthors()));
        getLogger().info("----------------------------------------");
        getLogger().info("");
        Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                try(Connection conn = databaseManager.getConnection()){
                    try(PreparedStatement ps = conn.prepareStatement("SELECT * FROM players WHERE isBanned = TRUE")){
                        ResultSet rs = ps.executeQuery();
                        while (rs.next()){
                            UUID uuidString = UUID.fromString(rs.getString("uuid"));
                            Player p = Bukkit.getPlayer(uuidString);
                            p.kick();
                        }
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }, 0L, 20L);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        databaseManager.close();
    }

    public boolean isBanned(UUID uuid) throws SQLException {
        try(Connection conn = databaseManager.getConnection()){
            PreparedStatement ps = conn.prepareStatement("SELECT isBanned FROM players WHERE uuid = ?");
                ps.setString(1, uuid.toString());
                try(ResultSet rs = ps.executeQuery()){
                    if(!rs.next()){
                        return false;
                    }
                    return rs.getBoolean("isBanned");
                }
        }
    }

    public String getBanReason(UUID uuid){
        try(Connection conn = databaseManager.getConnection()){
            PreparedStatement ps = conn.prepareStatement("SELECT reason FROM players WHERE uuid = ?");
            ps.setString(1, uuid.toString());
            try(ResultSet rs = ps.executeQuery()){
                if(!rs.next()){
                    return "false";
                }
                return rs.getString("reason");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
