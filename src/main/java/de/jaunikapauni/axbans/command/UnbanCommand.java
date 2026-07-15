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

public class UnbanCommand implements CommandExecutor {
    AxBans reference;

    public UnbanCommand(AxBans reference) {
        this.reference = reference;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can run this command!");
            return true;
        }
        Player sourcePlayer = (Player) sender;
        if(!sourcePlayer.hasPermission("axbans.unban")){
            sourcePlayer.sendMessage("You don't have the permission! [axbans.unban]");
            return true;
        }
        if (args.length == 0) {
            sourcePlayer.sendMessage(ChatColor.RED + "Please enter a playername.");
            return false;
        }
        OfflinePlayer targetPlayer = Bukkit.getServer().getOfflinePlayer(args[0]);
        try {
            if(!reference.isBanned(targetPlayer.getUniqueId())){
                sender.sendMessage("Player isn't banned!");
                return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try (Connection conn = reference.getDatabaseManager().getConnection()) {
            PreparedStatement ps = conn.prepareStatement("UPDATE players SET isBanned = false WHERE uuid = ?");
            ps.setString(1, targetPlayer.getUniqueId().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        sourcePlayer.sendMessage("You unbanned " + targetPlayer.getName());
        return true;
    }
}
