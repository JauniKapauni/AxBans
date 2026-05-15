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
    public PlayerJoinListener(AxBans reference){
        this.reference = reference;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) throws SQLException {
        Player p = e.getPlayer();
        try(Connection conn = reference.getDatabaseManager().getConnection()){
            PreparedStatement ps = conn.prepareStatement("SELECT uuid FROM players WHERE uuid = ?"){
                ps.setString(1, p.getUniqueId().toString());
                ResultSet rs = ps.executeQuery();
                if(!rs.next()){
                    PreparedStatement ps1 = conn.prepareStatement("INSERT INTO players (uuid, isBanned, reason) VALUES (?, ?, ?)"){
                        ps1.setString(1, p.getUniqueId().toString());
                        ps1.setBoolean(2, false);
                        ps1.setString(3, "");
                        ps1.executeUpdate();
                    }
                }
            }
        }
        if(reference.isBanned(p.getUniqueId())){
            p.kickPlayer(reference.getBanReason(p.getUniqueId()));
            Bukkit.getLogger().info(p.getName() + " tried to join, but is banned!");
        }
    }
}
