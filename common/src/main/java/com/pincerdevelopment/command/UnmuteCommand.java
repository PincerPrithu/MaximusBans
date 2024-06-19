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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class UnmuteCommand {

    private static final Pattern IP_PATTERN = Pattern.compile("^(([0-9]{1,3})\\.){3}([0-9]{1,3})$");

    public static void handleUnmuteCommand(CustomSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(Main.getLang().getMessage("messages.usage.unmute"));
            return;
        }

        boolean isIPUnmute = IP_PATTERN.matcher(args[0]).matches();
        String targetIdentifier = args[0];
        String pardonReason = args.length > 1 ? String.join(" ", args).substring(args[0].length() + 1) : Main.getLang().getMessage("messages.unmute.default_reason");
        String issuerUUID = sender.getType() == CustomSenderType.PLAYER ? ((CustomPlayer) sender).getUniqueId().toString() : null;
        String issuerType = sender.getType() == CustomSenderType.PLAYER ? "PLAYER" : "CONSOLE";
        Date pardonDate = new Date();

        if (isIPUnmute) {
            handleIPUnmute(sender, targetIdentifier, pardonReason, issuerUUID, issuerType, pardonDate);
        } else {
            handlePlayerUnmute(sender, targetIdentifier, pardonReason, issuerUUID, issuerType, pardonDate);
        }
    }

    private static void handleIPUnmute(CustomSender sender, String targetIdentifier, String pardonReason, String issuerUUID, String issuerType, Date pardonDate) {
        try {
            InetAddress ipAddress = InetAddress.getByName(targetIdentifier);
            CompletableFuture.supplyAsync(() -> {
                try {
                    return DataManager.getActivePunishmentsByIP(ipAddress.getHostAddress()).join();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).thenAccept(activePunishments -> {
                boolean unmuted = false;
                for (Punishment punishment : activePunishments) {
                    if (punishment.getPunishmentType() == Punishment.PunishmentType.MUTE ||
                            punishment.getPunishmentType() == Punishment.PunishmentType.TEMPMUTE) {
                        punishment.setPardonFields(pardonDate, issuerUUID, issuerType, pardonReason);
                        DataManager.updatePardonInfo(punishment);
                        unmuted = true;
                    }
                }
                if (unmuted) {
                    sender.sendMessage(Main.getLang().getMessage("messages.unmute.ip_success", "ip", targetIdentifier));
                } else {
                    sender.sendMessage(Main.getLang().getMessage("messages.unmute.ip_not_muted", "ip", targetIdentifier));
                }
            }).exceptionally(e -> {
                sender.sendMessage(Main.getLang().getMessage("messages.unmute.failed_ip", "error", e.getMessage()));
                return null;
            });
        } catch (UnknownHostException e) {
            sender.sendMessage(Main.getLang().getMessage("messages.general.invalid_ip", "ip", targetIdentifier));
        }
    }

    private static void handlePlayerUnmute(CustomSender sender, String targetIdentifier, String pardonReason, String issuerUUID, String issuerType, Date pardonDate) {
        UUID targetUUID = Main.getPlatform().getUUIDFromIdentifier(targetIdentifier);
        if (targetUUID == null) {
            sender.sendMessage(Main.getLang().getMessage("messages.general.invalid_uuid_or_name", "identifier", targetIdentifier));
            return;
        }

        CompletableFuture.supplyAsync(() -> {
            try {
                return DataManager.getActivePunishments(targetUUID).join();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenAccept(activePunishments -> {
            boolean unmuted = false;
            for (Punishment punishment : activePunishments) {
                if (punishment.getPunishmentType() == Punishment.PunishmentType.MUTE ||
                        punishment.getPunishmentType() == Punishment.PunishmentType.TEMPMUTE) {
                    punishment.setPardonFields(pardonDate, issuerUUID, issuerType, pardonReason);
                    DataManager.updatePardonInfo(punishment);
                    unmuted = true;
                }
            }
            if (unmuted) {
                sender.sendMessage(Main.getLang().getMessage("messages.unmute.success", "target", targetIdentifier));
            } else {
                sender.sendMessage(Main.getLang().getMessage("messages.unmute.not_muted", "target", targetIdentifier));
            }
        }).exceptionally(e -> {
            sender.sendMessage(Main.getLang().getMessage("messages.unmute.failed", "error", e.getMessage()));
            return null;
        });
    }
}
