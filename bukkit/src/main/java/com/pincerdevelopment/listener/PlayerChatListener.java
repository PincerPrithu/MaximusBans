package com.pincerdevelopment.listener;

import com.pincerdevelopment.CustomPlayerAdapter;
import listener.MuteListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerChatListener {

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {

        // Cancel the event immediately
        event.setCancelled(MuteListener.checkMuteStatus(new CustomPlayerAdapter(event.getPlayer())));

    }

}
