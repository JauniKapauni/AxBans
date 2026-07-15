package de.jaunikapauni.axbans.listener;

import de.jaunikapauni.axbans.AxBans;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerJoinListener implements Listener {
    AxBans reference;

    public PlayerJoinListener(AxBans reference) {
        this.reference = reference;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        Bukkit.getScheduler().runTaskAsynchronously(reference, () -> {
            try(Connection conn = reference.getDatabaseManager().getConnection()){
                try(PreparedStatement ps = conn.prepareStatement("SELECT isBanned, reason FROM players WHERE uuid = ?")){
                    ps.setString(1, p.getUniqueId().toString());
                    ResultSet rs = ps.executeQuery();
                    if(!rs.next()){
                        try(PreparedStatement ps1 = conn.prepareStatement("INSERT INTO players (uuid, isBanned, reason) VALUES (?, ?, ?)")){
                            ps1.setString(1, p.getUniqueId().toString());
                            ps1.setBoolean(2, false);
                            ps1.setString(3, "");
                            ps1.executeUpdate();
                        }
                    } else {
                        boolean banned = rs.getBoolean("isBanned");
                        String reason = rs.getString("reason");
                        if(banned){
                            Bukkit.getScheduler().runTask(reference, () -> {
                                reference.kickPlayerProxy(p.getName(), "You are banned: " + reason);
                                Bukkit.getLogger().info(p.getName() + " tried to join, but is banned!");
                            });
                        }
                    }
                }
            } catch (SQLException err){
                err.printStackTrace();
            }
        });
    }
}
