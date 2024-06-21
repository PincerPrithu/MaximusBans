package com.pincerdevelopment.listener;

import listener.BanListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.net.InetAddress;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayerPreLoginListener implements Listener {

    private static final Logger LOGGER = Logger.getLogger(PlayerPreLoginListener.class.getName());

    public Object[] handlePreLogin(UUID playerUUID, InetAddress loginAddress) {
        try {
            BanListener.BanCheckResult banCheckResult = BanListener.checkBan(playerUUID, loginAddress.getHostAddress()).join();

            if (banCheckResult.ban()) {
                return new Object[]{true, banCheckResult.banMessage()};
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error checking player ban status", e);
        }
        return new Object[]{false, null};
    }

    @EventHandler
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        UUID playerUUID = event.getUniqueId();
        InetAddress loginAddress = event.getAddress();
        if ((Boolean) handlePreLogin(playerUUID, loginAddress)[0])
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, (String) handlePreLogin(playerUUID, loginAddress)[1]);
        System.out.println("Player " + playerUUID + " logged in from " + loginAddress.getHostAddress());
    }
}
