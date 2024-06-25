package com.pincerdevelopment;

import com.pincerdevelopment.utils.ChatUtils;
import com.pincerdevelopment.utils.FileUtils;
import dev.dejvokep.boostedyaml.YamlDocument;

import static com.pincerdevelopment.utils.ChatUtils.encode;

public class LanguageManager {
    private static YamlDocument language;

    public LanguageManager() {
        String lang = FileUtils.getConfig("config", FileUtils.FileType.CONFIG).getString("language");
        language = getLanguage(lang);
        if (language == null) {
            System.err.println("Failed to load language file: " + lang);
        } else {
            System.out.println("Successfully loaded language file: " + lang);
        }
        FileUtils.getConfig("BanMessage", FileUtils.FileType.MESSAGE);
        FileUtils.getConfig("TempBanMessage", FileUtils.FileType.MESSAGE);
        FileUtils.getConfig("BlacklistMessage", FileUtils.FileType.MESSAGE);
        FileUtils.getConfig("KickMessage", FileUtils.FileType.MESSAGE);
    }

    public String getMessage(String key, String... placeholders) {
        String message = language.getString(key);


        if (message == null) {
            return "Message not found!";
        }

        for (int i = 0; i < placeholders.length; i += 2) {
            String placeholder = placeholders[i];
            String value = placeholders[i + 1];
            message = message.replace("{" + placeholder + "}", value);
        }
//        System.out.println("----------------------------------------------------------------");
//        System.out.println("Retrieved message for key: " + key);
//        System.out.println("Message: " + message);
//        System.out.println("----------------------------------------------------------------");
        return message;
    }

    private YamlDocument getLanguage(String language) {
        switch (language) {
            case "en":
                return FileUtils.getConfig("en", FileUtils.FileType.LANGUAGE);
            // Add other cases for different languages if needed
            default:
                return FileUtils.getConfig("en", FileUtils.FileType.LANGUAGE);
        }
    }

    public static String getBanMessage(Punishment punishment) {
        return encode(FileUtils.getConfig("BanMessage", FileUtils.FileType.MESSAGE).getStringList("ban-message"), punishment);
    }

    public static String getTempBanMessage(Punishment punishment) {
        return encode(FileUtils.getConfig("TempBanMessage", FileUtils.FileType.MESSAGE).getStringList("tempban-message"), punishment);
    }

    public static String getBlacklistMessage(Punishment punishment) {
        return encode(FileUtils.getConfig("BlacklistMessage", FileUtils.FileType.MESSAGE).getStringList("blacklist-message"), punishment);
    }
    public static String getKickMessage(Punishment punishment) {
        return encode(FileUtils.getConfig("KickMessage", FileUtils.FileType.MESSAGE).getStringList("kick-message"), punishment);
    }
    public static String getFooter() {
        return encode(language.getStringList("screen.footer"));
    }
}
