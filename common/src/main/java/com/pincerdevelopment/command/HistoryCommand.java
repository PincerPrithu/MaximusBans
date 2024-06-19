package com.pincerdevelopment.command;

import com.pincerdevelopment.DataManager;
import com.pincerdevelopment.Main;
import com.pincerdevelopment.Punishment;
import com.pincerdevelopment.Universal.CustomSender;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class HistoryCommand {

    public static void handleHistoryCommand(CustomSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Main.getLang().getMessage("messages.usage.history"));
            return;
        }

        String type = args[0];
        String identifier = args[1];
        int page = args.length > 2 ? parsePage(args[2]) : 1;

        CompletableFuture.runAsync(() -> {
            UUID uuid = Main.getPlatform().getUUIDFromIdentifier(identifier);
            if (uuid == null) {
                sender.sendMessage(Main.getLang().getMessage("messages.general.invalid_uuid_or_name", "identifier", identifier));
                return;
            }

            List<Punishment> punishments;
            try {
                if (type.equalsIgnoreCase("player")) {
                    punishments = DataManager.getPunishmentsByPlayer(uuid).join();
                } else if (type.equalsIgnoreCase("issuer")) {
                    punishments = DataManager.getPunishmentsByIssuer(uuid).join();
                } else {
                    sender.sendMessage(Main.getLang().getMessage("messages.general.invalid_type"));
                    return;
                }

                if (punishments.isEmpty()) {
                    sender.sendMessage(Main.getLang().getMessage("messages.general.no_punishments_found", "type", type));
                } else {
                    paginateResults(sender, punishments, page);
                }
            } catch (Exception e) {
                sender.sendMessage(Main.getLang().getMessage("messages.general.fetch_error"));
                e.printStackTrace(); // To help debug any SQL issues
            }
        });
    }

    private static int parsePage(String pageString) {
        try {
            return Math.max(Integer.parseInt(pageString), 1);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private static void paginateResults(CustomSender sender, List<Punishment> punishments, int page) {
        int pageSize = 5;
        int totalPunishments = punishments.size();
        int totalPages = (int) Math.ceil((double) totalPunishments / pageSize);

        if (page > totalPages) {
            page = totalPages;
        }

        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalPunishments);

        sender.sendMessage(Main.getLang().getMessage("messages.history.header", "page", String.valueOf(page), "totalPages", String.valueOf(totalPages)));

        for (int i = startIndex; i < endIndex; i++) {
            Punishment punishment = punishments.get(i);
            sender.sendMessage(Main.getLang().getMessage("messages.history.separator"));
            sender.sendMessage(formatPunishment(punishment));
            if (punishment.getPardonDate() != null) {
                sender.sendMessage(formatPardon(punishment));
            }
        }

        sender.sendMessage(Main.getLang().getMessage("messages.history.separator"));
        sender.sendMessage(Main.getLang().getMessage("messages.history.footer"));
    }

    private static String formatPunishment(Punishment punishment) {
        return Main.getLang().getMessage("messages.history.punishment",
                "type", punishment.getPunishmentType().toString(),
                "issuedBy", (punishment.getIssuerUUID() != null ? Main.getPlatform().getPlayerByUUID(punishment.getIssuerUUID()).getName() : "CONSOLE"),
                "issuerType", punishment.getIssuerType(),
                "occurrence", punishment.getTimeOfOccurrence().toString(),
                "expiry", (punishment.getTimeOfExpiry() != null ? punishment.getTimeOfExpiry().toString() : "N/A"),
                "reason", punishment.getReason());
    }

    private static String formatPardon(Punishment punishment) {
        return Main.getLang().getMessage("messages.history.pardon",
                "pardonDate", punishment.getPardonDate().toString(),
                "pardonedBy", (punishment.getIssuerUUID() != null ? Main.getPlatform().getPlayerByUUID(punishment.getIssuerUUID()).getName() : "CONSOLE"),
                "pardonIssuerType", punishment.getPardonIssuerType(),
                "pardonReason", punishment.getPardonReason());
    }
}
