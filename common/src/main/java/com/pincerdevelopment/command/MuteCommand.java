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

public class MuteCommand {

    private static final Pattern IP_PATTERN = Pattern.compile("^(([0-9]{1,3})\\.){3}([0-9]{1,3})$");

    public static void handleMuteCommand(CustomSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(Main.getLang().getMessage("messages.usage.mute"));
            return;
        }

        boolean isIPMute = IP_PATTERN.matcher(args[0]).matches();
        String targetIdentifier = args[0];
        String reason = parseReason(args, 1, Main.getLang().getMessage("messages.mute.default_reason"));
        String issuerUUID = getIssuerUUID(sender);
        Punishment.IssuerType issuerType = getIssuerType(sender, args);
        Date timeOfOccurrence = new Date();

        if (isIPMute) {
            handleIPMute(sender, targetIdentifier, reason, issuerUUID, issuerType, timeOfOccurrence);
        } else {
            handlePlayerMute(sender, targetIdentifier, reason, issuerUUID, issuerType, timeOfOccurrence, false);
        }
    }

    public static void handleTempMuteCommand(CustomSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Main.getLang().getMessage("messages.usage.tempmute"));
            return;
        }

        boolean isIPMute = IP_PATTERN.matcher(args[0]).matches();
        String targetIdentifier = args[0];
        long durationInMillis;

        try {
            durationInMillis = ChatUtils.parseDuration(args[1]);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(Main.getLang().getMessage("messages.general.invalid_duration"));
            return;
        }

        String reason = parseReason(args, 2, Main.getLang().getMessage("messages.tempmute.default_reason"));
        String issuerUUID = getIssuerUUID(sender);
        Punishment.IssuerType issuerType = getIssuerType(sender, args);
        Date timeOfOccurrence = new Date();
        Date timeOfExpiry = new Date(System.currentTimeMillis() + durationInMillis);

        if (isIPMute) {
            handleIPMute(sender, targetIdentifier, reason, issuerUUID, issuerType, timeOfOccurrence, timeOfExpiry);
        } else {
            handlePlayerMute(sender, targetIdentifier, reason, issuerUUID, issuerType, timeOfOccurrence, true, timeOfExpiry);
        }
    }

    private static void handlePlayerMute(CustomSender sender, String targetIdentifier, String reason, String issuerUUID, Punishment.IssuerType issuerType, Date timeOfOccurrence, boolean isTempMute, Date... timeOfExpiry) {
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
                    if (punishment.getPunishmentType() == Punishment.PunishmentType.MUTE || punishment.getPunishmentType() == Punishment.PunishmentType.TEMPMUTE) {
                        if (punishment.isPermanent() || punishment.getTimeOfExpiry().after(timeOfExpiry.length > 0 ? timeOfExpiry[0] : timeOfOccurrence)) {
                            sender.sendMessage(Main.getLang().getMessage("messages.mute.already_muted_longer"));
                            return;
                        } else {
                            punishment.setPardonFields(new Date(), issuerUUID, issuerType.name(), Main.getLang().getMessage("messages.mute.extended"));
                            DataManager.updatePunishment(punishment).join();
                        }
                    }
                }

                Punishment newPunishment = new Punishment(targetUUID, UUID.fromString(issuerUUID), issuerType.name(), isTempMute ? Punishment.PunishmentType.TEMPMUTE : Punishment.PunishmentType.MUTE, timeOfOccurrence, timeOfExpiry.length > 0 ? timeOfExpiry[0] : null, reason, target.getAddress(), false);
                DataManager.addPunishment(newPunishment);
                sender.sendMessage(Main.getLang().getMessage("messages.mute.success", "target", target.getName(), "reason", reason, "duration", isTempMute ? Main.getLang().getMessage("messages.mute.temporary") : ""));
            } catch (Exception e) {
                sender.sendMessage(Main.getLang().getMessage("messages.mute.failed", "error", e.getMessage()));
            }
        });
    }

    private static void handleIPMute(CustomSender sender, String ip, String reason, String issuerUUID, Punishment.IssuerType issuerType, Date timeOfOccurrence, Date... timeOfExpiry) {
        CompletableFuture.runAsync(() -> {
            try {
                InetAddress playerIP = InetAddress.getByName(ip);
                List<Punishment> activePunishments = DataManager.getActivePunishmentsByIP(playerIP.getHostAddress()).join();

                for (Punishment punishment : activePunishments) {
                    if (punishment.getPunishmentType() == Punishment.PunishmentType.MUTE || punishment.getPunishmentType() == Punishment.PunishmentType.TEMPMUTE) {
                        if (punishment.isPermanent() || punishment.getTimeOfExpiry().after(timeOfExpiry.length > 0 ? timeOfExpiry[0] : timeOfOccurrence)) {
                            sender.sendMessage(Main.getLang().getMessage("messages.mute.ip_already_muted_longer"));
                            return;
                        } else {
                            punishment.setPardonFields(new Date(), issuerUUID, issuerType.name(), Main.getLang().getMessage("messages.mute.extended"));
                            DataManager.updatePunishment(punishment).join();
                        }
                    }
                }

                Punishment newPunishment = new Punishment(null, UUID.fromString(issuerUUID), issuerType.name(), timeOfExpiry.length > 0 ? Punishment.PunishmentType.TEMPMUTE : Punishment.PunishmentType.MUTE, timeOfOccurrence, timeOfExpiry.length > 0 ? timeOfExpiry[0] : null, reason, playerIP, true);
                DataManager.addPunishment(newPunishment);
                sender.sendMessage(Main.getLang().getMessage("messages.mute.ip_success", "ip", ip, "reason", reason, "duration", timeOfExpiry.length > 0 ? Main.getLang().getMessage("messages.mute.temporary") : ""));
            } catch (UnknownHostException e) {
                sender.sendMessage(Main.getLang().getMessage("messages.general.invalid_ip", "ip", ip));
            } catch (Exception e) {
                sender.sendMessage(Main.getLang().getMessage("messages.mute.failed_ip", "error", e.getMessage()));
            }
        });
    }

    private static String parseReason(String[] args, int startIndex, String defaultReason) {
        StringBuilder reasonBuilder = new StringBuilder(defaultReason);
        for (int i = startIndex; i < args.length; i++) {
            if (!args[i].startsWith("-")) {
                if (reasonBuilder.toString().equals(defaultReason)) {
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

    private static boolean isPlayer(String targetIdentifier) {
        return Main.getPlatform().getPlayerByName(targetIdentifier) != null;
    }
}
