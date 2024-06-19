package com.pincerdevelopment;

import com.pincerdevelopment.Universal.CustomPlayer;
import com.pincerdevelopment.Universal.CustomSenderType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CustomPlayerAdapter implements CustomPlayer {
    private final OfflinePlayer player;

    public CustomPlayerAdapter(Player player) {
        this.player = player;
    }
    public CustomPlayerAdapter(OfflinePlayer player) {
        this.player = player;
    }
    @Override
    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public boolean isOnline() {
        return player.isOnline();
    }

    @Override
    public InetAddress getAddress() {
        if (!player.isOnline())
            return null;
        try {
            return InetAddress.getByName(((Player)player).getAddress().getAddress().getHostAddress());
        } catch (UnknownHostException e) {
            return null;
        }
    }

    @Override
    public void kickPlayer(String reason) {
        if (player.isOnline()) {
            Bukkit.getScheduler().runTask(MaximusBansBukkit.getInstance(), () -> {
                ((Player) player).kickPlayer(reason);
            });
        }
    }

    public CustomPlayer getCustomPlayer(UUID uuid) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        if (player != null) {
            return new CustomPlayerAdapter(player);
        }
        return null;
    }

    @Override
    public void sendMessage(String message) {
        if (player.isOnline())
            ((Player) player).sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    @Override
    public CustomSenderType getType() {
        return CustomSenderType.PLAYER;
    }
}