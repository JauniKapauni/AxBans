package de.jaunikapauni.axbans;

import de.jaunikapauni.axbans.command.BanCommand;
import de.jaunikapauni.axbans.command.UnbanCommand;
import de.jaunikapauni.axbans.listener.PlayerJoinListener;
import de.jaunikapauni.axbans.manager.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public final class AxBans extends JavaPlugin {
    DatabaseManager databaseManager;
    public DatabaseManager getDatabaseManager(){
        return databaseManager;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
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
        getLogger().info("");
        getLogger().info("----------------------------------------");
        getLogger().info("Name: " + getName());
        getLogger().info("Version: " + getDescription().getVersion());
        getLogger().info(String.join("Authors: " + ", ", getDescription().getAuthors()));
        getLogger().info("----------------------------------------");
        getLogger().info("");
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
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

    public void kickPlayerProxy(String player, String reason){
        Player messenger = Bukkit.getOnlinePlayers().stream().findFirst().orElse(null);
        if(messenger == null){
            return;
        }
        try{
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeUTF("KickPlayer");
            dos.writeUTF(player);
            dos.writeUTF(reason);
            messenger.sendPluginMessage(this, "BungeeCord", baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
