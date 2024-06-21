package com.pincerdevelopment.command;

import com.pincerdevelopment.CustomPlayerAdapter;
import com.pincerdevelopment.Universal.CustomSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BukkitBanCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        CustomSender customSender = convertToCustomSender(sender);
        BanCommand.handleBanCommand(customSender, args);
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
