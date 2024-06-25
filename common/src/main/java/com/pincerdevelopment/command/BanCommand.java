package com.pincerdevelopment.command;

import com.pincerdevelopment.DataManager;
import com.pincerdevelopment.Main;
import com.pincerdevelopment.Punishment;
import com.pincerdevelopment.Universal.CustomPlayer;
import com.pincerdevelopment.Universal.CustomSender;
import com.pincerdevelopment.Universal.CustomSenderType;
import com.pincerdevelopment.utils.ChatUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class BanCommand {

    private static final Pattern IP_PATTERN = Pattern.compile("^(([0-9]{1,3})\\.){3}([0-9]{1,3})$");

    public static void handleBanCommand(CustomSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(Main.getLang().getMessage("messages.usage.ban"));
            return;
        }

        boolean isIPBan = IP_PATTERN.matcher(args[0]).matches();
        String targetIdentifier = args[0];
        String reason = parseReason(args, 1);
        String issuerUUID = getIssuerUUID(sender);
        Punishment.IssuerType issuerType = getIssuerType(sender, args);
        Date timeOfOccurrence = new Date();

        if (isIPBan) {
            handleIPBan(sender, targetIdentifier, reason, issuerUUID, issuerType, timeOfOccurrence, null);
        } else {
            handlePlayerBan(sender, targetIdentifier, reason, issuerUUID, issuerType, timeOfOccurrence, false, null);
        }
    }

    public static void handleTempBanCommand(CustomSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Main.getLang().getMessage("messages.usage.tempban"));
            return;
        }

        boolean isIPBan = IP_PATTERN.matcher(args[0]).matches();
        String targetIdentifier = args[0];
        long durationInMillis;

        try {
            durationInMillis = ChatUtils.parseDuration(args[1]);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(Main.getLang().getMessage("messages.general.invalid_duration"));
            return;
        }

        String reason = parseReason(args, 2);
        String issuerUUID = getIssuerUUID(sender);
        Punishment.IssuerType issuerType = getIssuerType(sender, args);
        Date timeOfOccurrence = new Date();
        Date timeOfExpiry = new Date(System.currentTimeMillis() + durationInMillis);

        if (isIPBan) {
            handleIPBan(sender, targetIdentifier, reason, issuerUUID, issuerType, timeOfOccurrence, timeOfExpiry);
        } else {
            handlePlayerBan(sender, targetIdentifier, reason, issuerUUID, issuerType, timeOfOccurrence, true, timeOfExpiry);
        }
    }

    private static void handlePlayerBan(CustomSender sender, String targetIdentifier, String reason, String issuerUUID, Punishment.IssuerType issuerType, Date timeOfOccurrence, boolean isTempBan, Date timeOfExpiry) {
        CustomPlayer target = Main.getPlatform().getPlayerByName(targetIdentifier);
        if (target == null) {
            sender.sendMessage(Main.getLang().getMessage("messages.general.player_not_found"));
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                UUID targetUUID = target.getUniqueId();
                List<Punishment> activePunishments = DataManager.getActivePunishments(targetUUID).join();

                for (Punishment punishment : activePunishments) {
                    if (punishment.getPunishmentType() == Punishment.PunishmentType.BAN || punishment.getPunishmentType() == Punishment.PunishmentType.TEMPBAN) {
                        if (punishment.isPermanent() || punishment.getTimeOfExpiry().after(timeOfExpiry != null ? timeOfExpiry : timeOfOccurrence)) {
                            sender.sendMessage(Main.getLang().getMessage("messages.general.already_banned"));
                            return;
                        } else {
                            punishment.setPardonFields(new Date(), issuerUUID, issuerType.name(), Main.getLang().getMessage("messages.general.ban_extended_reason"));
                            DataManager.updatePunishment(punishment).join();
                        }
                    }
                }
                UUID uuid = null;
                if (issuerUUID != null)
                    uuid = UUID.fromString(issuerUUID);
                Punishment newPunishment = new Punishment(targetUUID, uuid, issuerType.name(), isTempBan ? Punishment.PunishmentType.TEMPBAN : Punishment.PunishmentType.BAN, timeOfOccurrence, timeOfExpiry, reason, target.getAddress(), false);
                DataManager.addPunishment(newPunishment);
                if (target.isOnline()) {
                    target.kickPlayer(reason);
                }
                sender.sendMessage(Main.getLang().getMessage(isTempBan ? "messages.general.player_temp_banned" : "messages.general.player_banned", "target", target.getName()));
            } catch (Exception e) {
                sender.sendMessage(Main.getLang().getMessage("messages.general.ban_failed", "error", e.getMessage()));
                e.printStackTrace();
            }
        });
    }

    private static void handleIPBan(CustomSender sender, String ip, String reason, String issuerUUID, Punishment.IssuerType issuerType, Date timeOfOccurrence, Date timeOfExpiry) {
        CompletableFuture.runAsync(() -> {
            try {
                InetAddress playerIP = InetAddress.getByName(ip);
                List<Punishment> activePunishments = DataManager.getActivePunishmentsByIP(playerIP.getHostAddress()).join();

                for (Punishment punishment : activePunishments) {
                    if (punishment.getPunishmentType() == Punishment.PunishmentType.BAN || punishment.getPunishmentType() == Punishment.PunishmentType.TEMPBAN) {
                        if (punishment.isPermanent() || punishment.getTimeOfExpiry().after(timeOfExpiry != null ? timeOfExpiry : timeOfOccurrence)) {
                            sender.sendMessage(Main.getLang().getMessage("messages.general.ip_already_banned"));
                            return;
                        } else {
                            punishment.setPardonFields(new Date(), issuerUUID, issuerType.name(), Main.getLang().getMessage("messages.general.ban_extended_reason"));
                            DataManager.updatePunishment(punishment).join();
                        }
                    }
                }
                UUID uuid = null;
                if (issuerUUID != null)
                    uuid = UUID.fromString(issuerUUID);
                Punishment newPunishment = new Punishment(null, uuid, issuerType.name(), timeOfExpiry != null ? Punishment.PunishmentType.TEMPBAN : Punishment.PunishmentType.BAN, timeOfOccurrence, timeOfExpiry, reason, playerIP, true);
                DataManager.addPunishment(newPunishment);
                banOnlinePlayersWithIP(playerIP, reason);
                sender.sendMessage(Main.getLang().getMessage(timeOfExpiry != null ? "messages.general.ip_temp_banned" : "messages.general.ip_banned", "ip", ip));
            } catch (UnknownHostException e) {
                sender.sendMessage(Main.getLang().getMessage("messages.general.invalid_ip", "ip", ip));
            } catch (Exception e) {
                sender.sendMessage(Main.getLang().getMessage("messages.general.ban_failed", "error", e.getMessage()));
                e.printStackTrace();
            }
        });
    }

    private static String parseReason(String[] args, int startIndex) {
        StringBuilder reasonBuilder = new StringBuilder(Main.getLang().getMessage("messages.default_reason.ban"));
        for (int i = startIndex; i < args.length; i++) {
            if (!args[i].startsWith("-")) {
                if (reasonBuilder.toString().equals(Main.getLang().getMessage("messages.default_reason.ban"))) {
                    reasonBuilder = new StringBuilder(args[i]);
                } else {
                    reasonBuilder.append(" ").append(args[i]);
                }
            }
        }
        return reasonBuilder.toString();
    }

    private static String getIssuerUUID(CustomSender sender) {
        return sender.getType() == CustomSenderType.PLAYER ? ((CustomPlayer) sender).getUniqueId().toString() : null;
    }

    private static Punishment.IssuerType getIssuerType(CustomSender sender, String[] args) {
        for (String arg : args) {
            if (arg.equalsIgnoreCase("-anticheat")) {
                return Punishment.IssuerType.ANTICHEAT;
            } else if (arg.equalsIgnoreCase("-automod")) {
                return Punishment.IssuerType.AUTOMOD;
            }
        }
        return sender.getType() == CustomSenderType.PLAYER ? Punishment.IssuerType.PLAYER : Punishment.IssuerType.CONSOLE;
    }

    private static void banOnlinePlayersWithIP(InetAddress ip, String reason) {
        for (CustomPlayer player : Main.getPlatform().getOnlinePlayers()) {
            if (player.getAddress().equals(ip)) {
                player.kickPlayer(Main.getLang().getMessage("messages.general.ip_banned_kick", "reason", reason));
            }
        }
    }
}
