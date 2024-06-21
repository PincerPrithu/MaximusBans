package com.pincerdevelopment.command;

import com.pincerdevelopment.DataManager;
import com.pincerdevelopment.Main;
import com.pincerdevelopment.Punishment;
import com.pincerdevelopment.Universal.CustomPlayer;
import com.pincerdevelopment.Universal.CustomSender;
import com.pincerdevelopment.Universal.CustomSenderType;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class KickCommand {

    private static final Pattern IP_PATTERN = Pattern.compile("^(([0-9]{1,3})\\.){3}([0-9]{1,3})$");

    public static void handleKickCommand(CustomSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(Main.getLang().getMessage("messages.usage.kick"));
            return;
        }

        String targetIdentifier = args[0];
        StringBuilder reasonBuilder = new StringBuilder(Main.getLang().getMessage("messages.default_reason.kick"));

        boolean kickByIP = false;
        for (int i = 1; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-ip")) {
                kickByIP = true;
            } else if (!args[i].startsWith("-")) {
                if (reasonBuilder.toString().equals(Main.getLang().getMessage("messages.default_reason.kick"))) {
                    reasonBuilder = new StringBuilder(args[i]);
                } else {
                    reasonBuilder.append(" ").append(args[i]);
                }
            }
        }

        String reason = reasonBuilder.toString();
        String issuerUUID = sender.getType() == CustomSenderType.PLAYER ? ((CustomPlayer) sender).getUniqueId().toString() : null;
        Punishment.IssuerType issuerType = sender.getType() == CustomSenderType.PLAYER ? Punishment.IssuerType.PLAYER : Punishment.IssuerType.CONSOLE;

        if (IP_PATTERN.matcher(targetIdentifier).matches()) {
            handleIPKick(sender, targetIdentifier, reason, issuerUUID, issuerType);
        } else {
            handlePlayerKick(sender, targetIdentifier, reason, kickByIP, issuerUUID, issuerType);
        }
    }

    private static void handlePlayerKick(CustomSender sender, String targetIdentifier, String reason, boolean kickByIP, String issuerUUID, Punishment.IssuerType issuerType) {
        UUID targetUUID = Main.getPlatform().getUUIDFromIdentifier(targetIdentifier);
        if (targetUUID == null) {
            sender.sendMessage(Main.getLang().getMessage("messages.general.player_not_found"));
            return;
        }

        CustomPlayer target = Main.getPlatform().getPlayerByUUID(targetUUID);
        if (target == null || !target.isOnline()) {
            sender.sendMessage(Main.getLang().getMessage("messages.general.player_not_online"));
            return;
        }

        if (kickByIP) {
            String playerIP = target.getAddress().getHostAddress();
            handleIPKick(sender, playerIP, reason, issuerUUID, issuerType);
        } else {
            target.kickPlayer(reason);
            System.out.println(reason);
            logKickToDatabase(targetUUID, null, reason, issuerUUID, issuerType);
            sender.sendMessage(Main.getLang().getMessage("messages.kick.success", "target", target.getName(), "reason", reason));
        }
    }

    private static void handleIPKick(CustomSender sender, String ip, String reason, String issuerUUID, Punishment.IssuerType issuerType) {
        try {
            InetAddress targetIP = InetAddress.getByName(ip);
            for (CustomPlayer player : Main.getPlatform().getOnlinePlayers()) {
                if (player.getAddress().equals(targetIP)) {
                    player.kickPlayer(reason);
                    System.out.println(reason);
                    logKickToDatabase(player.getUniqueId(), targetIP, reason, issuerUUID, issuerType);
                }
            }
            sender.sendMessage(Main.getLang().getMessage("messages.kick.ip_success", "ip", ip, "reason", reason));
        } catch (UnknownHostException e) {
            sender.sendMessage(Main.getLang().getMessage("messages.general.invalid_ip", "ip", ip));
        }
    }

    private static void logKickToDatabase(UUID playerUUID, InetAddress playerIP, String reason, String issuerUUID, Punishment.IssuerType issuerType) {
        CompletableFuture.runAsync(() -> {
            Punishment punishment = new Punishment(playerUUID, issuerUUID != null ? UUID.fromString(issuerUUID) : null, issuerType.name(), Punishment.PunishmentType.KICK, new Date(), null, reason, playerIP, playerIP != null);
            try {
                DataManager.addPunishment(punishment).join();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
