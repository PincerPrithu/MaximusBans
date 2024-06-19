package com.pincerdevelopment.command;

import com.pincerdevelopment.Universal.CustomSender;
import com.pincerdevelopment.Universal.CustomSenderType;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class CustomConsoleSender implements CustomSender {
    private final CommandSender sender;

    public CustomConsoleSender(CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public CustomSenderType getType() {
        return CustomSenderType.CONSOLE;
    }

    @Override
    public void sendMessage(String message) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
}