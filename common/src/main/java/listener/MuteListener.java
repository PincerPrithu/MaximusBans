package listener;

import com.pincerdevelopment.DataManager;
import com.pincerdevelopment.Punishment;
import com.pincerdevelopment.Universal.CustomPlayer;
import com.pincerdevelopment.utils.ChatUtils;

import java.util.Date;

public class MuteListener {

    public static boolean checkMuteStatus(CustomPlayer player) {
        boolean isMuted = false;
        String muteMessage = null;

        for (Punishment punishment : DataManager.getActivePunishments(player.getUniqueId()).join()) {
            if (punishment.getPunishmentType().equals(Punishment.PunishmentType.MUTE) ||
                    (punishment.getPunishmentType().equals(Punishment.PunishmentType.TEMPMUTE) &&
                            (punishment.getTimeOfExpiry() == null || punishment.getTimeOfExpiry().after(new Date())))) {

                String muteType = punishment.getPunishmentType().equals(Punishment.PunishmentType.MUTE) ? "permanently" : "temporarily";
                String expiryInfo = punishment.getTimeOfExpiry() != null ? " for " + ChatUtils.formatDuration(punishment.getTimeOfExpiry().getTime() - System.currentTimeMillis()) : "";
                player.sendMessage("&cYou have been " + muteType + " muted" + expiryInfo + ". Reason: " + punishment.getReason());
                isMuted = true;
                break;
            }
        }

        return isMuted;
    }
}
