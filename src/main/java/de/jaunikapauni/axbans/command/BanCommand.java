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
import java.sql.SQLException;

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
        String reason = args[1];
        try(Connection conn = reference.getDatabaseManager().getConnection()){
            PreparedStatement ps = conn.prepareStatement("UPDATE players SET isBanned = true, reason = ? WHERE uuid = ?");
                ps.setString(1, reason);
                ps.setString(2, targetPlayer.getUniqueId().toString());
                ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        reference.kickPlayerProxy(targetPlayer.getName(), "You are banned: " + reason);
        sourcePlayer.sendMessage("You banned " + targetPlayer.getName());
        return true;
    }
}
