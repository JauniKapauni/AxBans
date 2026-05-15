package de.jaunikapauni.axbans.listener;

import de.jaunikapauni.axbans.AxBans;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;

public class PlayerQuitListener implements Listener {
    AxBans reference;
    public PlayerQuitListener(AxBans reference){
        this.reference = reference;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) throws SQLException {
        Player p = e.getPlayer();
        if(reference.isBanned(p.getUniqueId())){
            p.kickPlayer(reference.getBanReason(p.getUniqueId()));
        }
    }
}
