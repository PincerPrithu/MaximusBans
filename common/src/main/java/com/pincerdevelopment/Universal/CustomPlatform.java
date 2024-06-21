package com.pincerdevelopment.Universal;

import java.util.List;
import java.util.UUID;

public interface CustomPlatform {
    CustomPlayer getPlayerByUUID(UUID uuid);
    CustomPlayer getPlayerByName(String name);
    List<CustomPlayer> getOnlinePlayers();
    UUID getUUIDFromIdentifier(String identifier);
    public String getPlayerUsernameByUUID(UUID uuid);
    String colorize(String message);
}
