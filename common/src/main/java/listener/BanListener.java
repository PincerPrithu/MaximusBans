package listener;

import com.pincerdevelopment.DataManager;
import com.pincerdevelopment.LanguageManager;
import com.pincerdevelopment.Punishment;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BanListener {

    public static record BanCheckResult(boolean ban, String banMessage) {}

    public static CompletableFuture<BanCheckResult> checkBan(UUID playerUUID, String ipAddress) {
        System.out.println("Starting checkBan method...");
        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("Async task started...");
                List<Punishment> activePunishments = DataManager.getActivePunishments(playerUUID).join();
                BanCheckResult result = getBanCheckResult(activePunishments);
                if (result.ban()) {
                    System.out.println("Returning result from playerUUID check: " + result);
                    return result;
                }

                List<Punishment> activeIPPunishments = DataManager.getActivePunishmentsByIP(ipAddress).join();
                result = getBanCheckResult(activeIPPunishments);
                System.out.println("Returning result from IP address check: " + result);
                return result;
            } catch (Exception e) {
                e.printStackTrace();
                return new BanCheckResult(false, null);
            }
        });
    }

    private static BanCheckResult getBanCheckResult(List<Punishment> punishments) {
        System.out.println("Executing getBanCheckResult method...");
        for (Punishment punishment : punishments) {
            if (punishment.getPunishmentType().equals(Punishment.PunishmentType.BAN)) {
                System.out.println("Found BAN punishment: " + punishment);
                return new BanCheckResult(true, LanguageManager.getBanMessage(punishment));
            }
            if (punishment.getPunishmentType().equals(Punishment.PunishmentType.TEMPBAN) && (punishment.getTimeOfExpiry() == null || punishment.getTimeOfExpiry().after(new Date()))) {
                System.out.println("Found TEMPBAN punishment: " + punishment);
                return new BanCheckResult(true, LanguageManager.getTempBanMessage(punishment));
            }
        }
        System.out.println("No active punishments found.");
        return new BanCheckResult(false, null);
    }
}
