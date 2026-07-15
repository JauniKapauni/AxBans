package de.jaunikapauni.axbans.command;

import de.jaunikapauni.axbans.AxBans;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class BanCommand implements CommandExecutor {
    AxBans reference;
    public BanCommand(AxBans reference){
        this.reference = reference;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if(!(sender instanceof Player)){
            sender.sendMessage("Only players can run this command!");
            return true;
        }
        Player sourcePlayer = (Player) sender;
        if(!sourcePlayer.hasPermission("axbans.ban")){
            sourcePlayer.sendMessage("You don't have the permission! [axbans.ban]");
            return true;
        }
        if(args.length == 0){
            sourcePlayer.sendMessage(ChatColor.RED + "Please enter a playername.");
            return false;
        }
        if(args.length < 2){
            sourcePlayer.sendMessage(ChatColor.RED + "Please enter a reason.");
            return false;
        }
        OfflinePlayer targetPlayer = Bukkit.getServer().getOfflinePlayer(args[0]);
        if(!targetPlayer.hasPlayedBefore() && !targetPlayer.isOnline()){
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }
        String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        Bukkit.getScheduler().runTaskAsynchronously(reference, () -> {
            try(Connection conn = reference.getDatabaseManager().getConnection()){
                try(PreparedStatement ps = conn.prepareStatement("SELECT isBanned FROM players WHERE uuid = ?")){
                    ps.setString(1, targetPlayer.getUniqueId().toString());
                    ResultSet rs = ps.executeQuery();
                    boolean exists = rs.next();
                    if(exists && rs.getBoolean("isBanned")){
                        Bukkit.getScheduler().runTask(reference, () -> {
                            sender.sendMessage("Player is already banned!");
                        });
                        return;
                    }
                    if(!exists){
                        try(PreparedStatement ps1 = conn.prepareStatement("INSERT INTO players (uuid, isBanned, reason) VALUES (?, ?, ?)")){
                            ps1.setString(1, targetPlayer.getUniqueId().toString());
                            ps1.setBoolean(2, true);
                            ps1.setString(3, reason);
                            ps1.executeUpdate();
                        }
                    } else {
                        try(PreparedStatement ps2 = conn.prepareStatement("UPDATE players SET isBanned = true, reason = ? WHERE uuid = ?")){
                            ps2.setString(1, reason);
                            ps2.setString(2, targetPlayer.getUniqueId().toString());
                            ps2.executeUpdate();
                        }
                    }
                }
                Bukkit.getScheduler().runTask(reference, () -> {
                    reference.kickPlayerProxy(targetPlayer.getName(), "You are banned: " + reason);
                    sourcePlayer.sendMessage("You banned " + targetPlayer.getName());
                });
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        return true;
    }
}
