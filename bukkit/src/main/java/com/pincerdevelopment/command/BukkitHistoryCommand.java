package com.pincerdevelopment.command;

import com.pincerdevelopment.CustomPlayerAdapter;
import com.pincerdevelopment.Universal.CustomSender;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BukkitHistoryCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        CustomSender customSender = convertToCustomSender(sender);
        HistoryCommand.handleHistoryCommand(customSender, args);
        return true;
    }
    private CustomSender convertToCustomSender(CommandSender sender) {
        if (sender instanceof Player) {
            return new CustomPlayerAdapter((Player) sender);
        } else {
            return new CustomConsoleSender(sender);
        }
    }
    public static UUID getUUIDFromIdentifier(String identifier) {
        try {
            return UUID.fromString(identifier);
        } catch (IllegalArgumentException e) {
            Player player = Bukkit.getPlayer(identifier);
            if (player != null && player.hasPlayedBefore()) {
                return player.getUniqueId();
            }
        }
        return null;
    }
}
