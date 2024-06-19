package com.pincerdevelopment;

import com.pincerdevelopment.Universal.CustomPlatform;
import com.pincerdevelopment.Universal.CustomPlayer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class BukkitPlatform implements CustomPlatform {
    private final JavaPlugin plugin;

    public BukkitPlatform(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public CustomPlayer getCustomPlayer(String name) {
        Player player = Bukkit.getPlayer(name);
        if (player != null) {
            return new CustomPlayerAdapter(player);
        }
        return null;
    }


    @Override
    public CustomPlayer getPlayerByUUID(UUID uuid) {
        return Bukkit.getPlayer(uuid) == null ? null : new CustomPlayerAdapter(Bukkit.getPlayer(uuid));
    }

    @Override
    public CustomPlayer getPlayerByName(String name) {
        return Bukkit.getPlayer(name) == null ? null : new CustomPlayerAdapter(Bukkit.getPlayer(name));
    }

    public List<CustomPlayer> getOnlinePlayers() {
        return Bukkit.getOnlinePlayers().stream()
                .map(CustomPlayerAdapter::new)
                .collect(Collectors.toList());
    }


    public void runAsync(Runnable task) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }


    public void runTask(Runnable task) {
        Bukkit.getScheduler().runTask(plugin, task);
    }
    @Override
    public UUID getUUIDFromIdentifier(String identifier) {
        try {
            return UUID.fromString(identifier);
        } catch (IllegalArgumentException e) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(identifier);
            if (player != null) {
                return player.getUniqueId();
            }
        }
        return null;
    }
}
