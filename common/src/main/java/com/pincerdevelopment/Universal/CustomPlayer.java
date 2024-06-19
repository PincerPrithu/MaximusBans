package com.pincerdevelopment.Universal;

import java.net.InetAddress;
import java.util.UUID;

public interface CustomPlayer extends CustomSender {
    UUID getUniqueId();
    String getName();
    void kickPlayer(String reason);
    InetAddress getAddress();
    boolean isOnline();
}
