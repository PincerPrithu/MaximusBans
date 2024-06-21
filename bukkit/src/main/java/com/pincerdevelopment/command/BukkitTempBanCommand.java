package com.pincerdevelopment.command;

import com.pincerdevelopment.CustomPlayerAdapter;
import com.pincerdevelopment.Universal.CustomSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BukkitTempBanCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        BanCommand.handleTempBanCommand(convertToCustomSender(sender), args);
        return true;
    }

    private CustomSender convertToCustomSender(CommandSender sender) {
        if (sender instanceof Player) {
            return new CustomPlayerAdapter((Player) sender);
        } else {
            return new CustomConsoleSender(sender);
        }
    }
}
