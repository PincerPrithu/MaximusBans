package com.pincerdevelopment;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import org.apache.commons.lang.RandomStringUtils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Date;
import java.util.UUID;

@Getter
@DatabaseTable(tableName = "punishments")
public class Punishment {

    public enum PunishmentType {
        BAN, TEMPBAN, MUTE, TEMPMUTE, KICK, BLACKLIST, WARN
    }

    public enum IssuerType {
        PLAYER, CONSOLE, ANTICHEAT, AUTOMOD
    }

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField
    private String punishmentID;

    @DatabaseField
    private UUID playerUUID;

    @DatabaseField
    private UUID issuerUUID;

    @DatabaseField
    private String issuerType;

    @DatabaseField
    private PunishmentType punishmentType;

    @DatabaseField
    private Date timeOfOccurrence;

    @DatabaseField
    private Date timeOfExpiry;

    @DatabaseField
    private String reason;

    @DatabaseField
    private String playerIP;

    @DatabaseField
    private boolean IPPunishment;

    @DatabaseField
    private Date pardonDate;

    @DatabaseField
    private UUID pardonIssuerUUID;

    @DatabaseField
    private String pardonIssuerType;

    @DatabaseField
    private String pardonReason;

    public Punishment() {}

    public Punishment(UUID playerUUID, UUID issuerUUID, String issuerType, PunishmentType punishmentType, Date timeOfOccurrence,
                      Date timeOfExpiry, String banReason, InetAddress IP, boolean IPPunishment) {
        this.playerUUID = playerUUID;
        this.issuerUUID = issuerUUID;
        this.issuerType = issuerType;
        this.punishmentType = punishmentType;
        this.timeOfOccurrence = timeOfOccurrence;
        this.timeOfExpiry = timeOfExpiry;
        this.reason = banReason;
        this.punishmentID = generatePunishmentID(this);
        this.playerIP = IP != null ? IP.getHostAddress() : null;
        this.IPPunishment = IPPunishment;
    }

    private String generatePunishmentID(Punishment punishment) {
        String prefix = "C";
        String issuerType = punishment.getIssuerType();

        if (IssuerType.PLAYER.toString().equals(issuerType)) {
            prefix = "P";
        } else if (IssuerType.ANTICHEAT.toString().equals(issuerType)) {
            prefix = "A";
        } else if (IssuerType.AUTOMOD.toString().equals(issuerType)) {
            prefix = "M";
        }

        prefix = prefix + punishment.getPunishmentType().toString().charAt(0) + "-";
        prefix = prefix + RandomStringUtils.randomAlphabetic(3) + "-";
        prefix = prefix + RandomStringUtils.randomAlphanumeric(8);

        return prefix;
    }

    public InetAddress getPlayerIPAddress() {
        try {
            return playerIP != null ? Inet4Address.getByName(playerIP) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public void setPlayerIP(InetAddress playerIP) {
        this.playerIP = playerIP != null ? playerIP.getHostAddress() : null;
        updatePlayerIPInDatabase();
    }
    public boolean isPermanent() {
        return (timeOfExpiry == null);
    }
    public void setPardonFields(Date pardonDate, String pardonIssuerUUID, String pardonIssuerType, String pardonReason) {
        this.pardonDate = pardonDate;
        this.pardonIssuerUUID = "CONSOLE".equals(pardonIssuerType) ? null : UUID.fromString(pardonIssuerUUID);
        this.pardonIssuerType = pardonIssuerType;
        this.pardonReason = pardonReason;
    }
    private void updatePlayerIPInDatabase() {
        DataManager.updatePunishmentIP(this);
    }
}
