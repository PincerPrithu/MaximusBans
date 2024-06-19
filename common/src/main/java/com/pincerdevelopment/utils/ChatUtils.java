package com.pincerdevelopment.utils;

import com.pincerdevelopment.Main;
import com.pincerdevelopment.Punishment;
import com.pincerdevelopment.Universal.CustomPlatform;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ChatUtils {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String encode(List<String> list, Punishment punishment) {
        String duration = "Permanent";
        String issuer = "CONSOLE";
        if (punishment.getPunishmentType() == Punishment.PunishmentType.TEMPBAN || punishment.getPunishmentType() == Punishment.PunishmentType.TEMPMUTE) {
            duration = formatDuration(punishment.getTimeOfExpiry().getTime() - System.currentTimeMillis());
        }

        switch (Punishment.IssuerType.valueOf(punishment.getIssuerType())) {
            case PLAYER:
                issuer = Main.getPlatform().getPlayerByUUID(punishment.getIssuerUUID()).getName();
                break;
            case ANTICHEAT:
                issuer = "AntiCheat";
                break;
            case AUTOMOD:
                issuer = "AutoMod";
                break;
        }

        String x = "";
        for (String str : list) {
            if (Objects.equals(x, ""))
                x = str;
            else
                x = x + "\n" + str;
        }

        x = x.replace("{reason}", punishment.getReason());
        x = x.replace("{duration}", duration);
        x = x.replace("{issuer}", issuer);
        x = x.replace("{dateIssued}", formatDate(punishment.getTimeOfOccurrence()));
        x = x.replace("{dateExpiry}", formatDate(punishment.getTimeOfExpiry()));
        x = x.replace("{id}", punishment.getPunishmentID());

        return (x);
    }

    public static List<String> decode(String str) {
        return List.of(str.split("\n"));
    }

    public static long parseDuration(String durationStr) {
        char unit = durationStr.charAt(durationStr.length() - 1);
        long duration = Long.parseLong(durationStr.substring(0, durationStr.length() - 1));
        switch (unit) {
            case 's': return TimeUnit.SECONDS.toMillis(duration);
            case 'm': return TimeUnit.MINUTES.toMillis(duration);
            case 'h': return TimeUnit.HOURS.toMillis(duration);
            case 'd': return TimeUnit.DAYS.toMillis(duration);
            case 'M': return TimeUnit.DAYS.toMillis(duration * 30); // Approximate month
            case 'y': return TimeUnit.DAYS.toMillis(duration * 365); // Approximate year
            default: throw new IllegalArgumentException("Invalid duration unit.");
        }
    }

    public static String formatDuration(long durationInMillis) {
        long seconds = TimeUnit.MILLISECONDS.toSeconds(durationInMillis) % 60;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(durationInMillis) % 60;
        long hours = TimeUnit.MILLISECONDS.toHours(durationInMillis) % 24;
        long days = TimeUnit.MILLISECONDS.toDays(durationInMillis) % 30;
        long months = (TimeUnit.MILLISECONDS.toDays(durationInMillis) % 365) / 30;
        long years = TimeUnit.MILLISECONDS.toDays(durationInMillis) / 365;

        StringBuilder formattedDuration = new StringBuilder();

        if (years > 0) {
            formattedDuration.append(years).append("y ");
        }
        if (months > 0) {
            formattedDuration.append(months).append("M ");
        }
        if (days > 0) {
            formattedDuration.append(days).append("d ");
        }
        if (hours > 0) {
            formattedDuration.append(hours).append("h ");
        }
        if (minutes > 0) {
            formattedDuration.append(minutes).append("m ");
        }
        if (seconds > 0) {
            formattedDuration.append(seconds).append("s");
        }

        return formattedDuration.toString().trim();
    }

    public static String formatDate(Date date) {
        if (date == null) {
            return "N/A";
        }
        return DATE_FORMAT.format(date);
    }

}
