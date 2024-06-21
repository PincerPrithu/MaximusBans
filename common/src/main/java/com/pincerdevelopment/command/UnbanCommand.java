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

public class UnbanCommand {

    public static void handleUnbanCommand(CustomSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(Main.getLang().getMessage("messages.usage.unban"));
            return;
        }

        String targetIdentifier = args[0];
        String pardonReason = args.length > 1 ? String.join(" ", args).substring(args[0].length() + 1) : Main.getLang().getMessage("messages.unban.default_reason");
        String issuerUUID = sender.getType() == CustomSenderType.PLAYER ? ((CustomPlayer) sender).getUniqueId().toString() : null;
        String issuerType = sender.getType() == CustomSenderType.PLAYER ? "PLAYER" : "CONSOLE";
        Date pardonDate = new Date();

        boolean isIPUnban = targetIdentifier.matches("^(([0-9]{1,3})\\.){3}([0-9]{1,3})$");

        if (isIPUnban) {
            handleIPUnban(sender, targetIdentifier, pardonReason, issuerUUID, issuerType, pardonDate);
        } else {
            handlePlayerUnban(sender, targetIdentifier, pardonReason, issuerUUID, issuerType, pardonDate);
        }
    }

    private static void handleIPUnban(CustomSender sender, String targetIdentifier, String pardonReason, String issuerUUID, String issuerType, Date pardonDate) {
        try {
            InetAddress ipAddress = InetAddress.getByName(targetIdentifier);
            CompletableFuture.supplyAsync(() -> {
                try {
                    return DataManager.getActivePunishmentsByIP(ipAddress.getHostAddress()).join();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).thenAccept(activePunishments -> {
                boolean unbanned = false;
                for (Punishment punishment : activePunishments) {
                    if (punishment.getPunishmentType() == Punishment.PunishmentType.BAN ||
                            punishment.getPunishmentType() == Punishment.PunishmentType.TEMPBAN) {
                        punishment.setPardonFields(pardonDate, issuerUUID, issuerType, pardonReason);
                        DataManager.updatePardonInfo(punishment);
                        unbanned = true;
                    }
                }
                if (unbanned) {
                    sender.sendMessage(Main.getLang().getMessage("messages.unban.ip_success", "ip", targetIdentifier));
                } else {
                    sender.sendMessage(Main.getLang().getMessage("messages.unban.ip_not_banned", "ip", targetIdentifier));
                }
            }).exceptionally(e -> {
                sender.sendMessage(Main.getLang().getMessage("messages.unban.failed_ip", "error", e.getMessage()));
                return null;
            });
        } catch (UnknownHostException e) {
            sender.sendMessage(Main.getLang().getMessage("messages.general.invalid_ip", "ip", targetIdentifier));
        }
    }

    private static void handlePlayerUnban(CustomSender sender, String targetIdentifier, String pardonReason, String issuerUUID, String issuerType, Date pardonDate) {
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
            boolean unbanned = false;
            for (Punishment punishment : activePunishments) {
                if (punishment.getPunishmentType() == Punishment.PunishmentType.BAN ||
                        punishment.getPunishmentType() == Punishment.PunishmentType.TEMPBAN) {
                    punishment.setPardonFields(pardonDate, issuerUUID, issuerType, pardonReason);
                    DataManager.updatePardonInfo(punishment);
                    unbanned = true;
                }
            }
            if (unbanned) {
                sender.sendMessage(Main.getLang().getMessage("messages.unban.success", "target", targetIdentifier));
            } else {
                sender.sendMessage(Main.getLang().getMessage("messages.unban.not_banned", "target", targetIdentifier));
            }
        }).exceptionally(e -> {
            sender.sendMessage(Main.getLang().getMessage("messages.unban.failed", "error", e.getMessage()));
            return null;
        });
    }
}
