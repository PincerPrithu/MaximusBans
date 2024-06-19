package com.pincerdevelopment;

import com.pincerdevelopment.command.*;
import lombok.Getter;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public final class MaximusBansBukkit extends JavaPlugin {
    public static DataManager dataManager;
    @Getter
    private static MaximusBansBukkit instance;

    @Override
    public void onEnable() {
        instance = this;
        Main.init(getDataFolder(), new BukkitPlatform(this));

//        getServer().getPluginManager().registerEvents(new PlayerPreLoginListener(), this);
//        getServer().getPluginManager().registerEvents(new PlayerChatListener(), this);
//
        getCommand("mute").setExecutor(new BukkitMuteCommand());
        getCommand("tempmute").setExecutor(new BukkitTempBanCommand());
        getCommand("ban").setExecutor(new BukkitBanCommand());
        getCommand("tempban").setExecutor(new BukkitTempBanCommand());
        getCommand("history").setExecutor(new BukkitHistoryCommand());
        getCommand("unban").setExecutor(new BukkitUnbanCommand());
        getCommand("unmute").setExecutor(new BukkitUnmuteCommand());
        getCommand("kick").setExecutor(new BukkitKickCommand());
        getLogger().info("MaximusBans has been enabled!");
    }


        @Override
        public void onDisable () {
            try {
                if (dataManager != null) {
                    dataManager.close();
                }
            } catch (IOException e) {
                getLogger().severe("Failed to close database: " + e.getMessage());
            }

    }
}
