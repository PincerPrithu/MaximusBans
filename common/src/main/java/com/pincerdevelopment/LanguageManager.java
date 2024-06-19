package com.pincerdevelopment;

import com.mysql.cj.conf.ConnectionUrlParser;
import com.pincerdevelopment.utils.FileUtils;
import dev.dejvokep.boostedyaml.YamlDocument;

import java.security.KeyPair;

public class LanguageManager {
    private static YamlDocument language;
    public LanguageManager() {
        String lang = FileUtils.getConfig("config", FileUtils.FileType.CONFIG).getString("language");
        language = getLanguage(lang);
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

        return message;
    }
        private YamlDocument getLanguage(String language) {
            switch (language) {
                case "en":
                    return FileUtils.getConfig("en", FileUtils.FileType.LANGUAGE);
//                case "es":
//                    return "Spanish";
//                case "fr":
//                    return "French";
//                case "de":
//                    return "German";
//                case "it":
//                    return "Italian";
//                case "nl":
//                    return "Dutch";
//                case "pl":
//                    return "Polish";
//                case "pt":
//                    return "Portuguese";
//                case "ru":
//                    return "Russian";
//                case "tr":
//                    return "Turkish";
//                case "zh":
//                    return "Chinese";
//                case "ja":
//                    return "Japanese";
//                case "ko":
//                    return "Korean";
//                case "ar":
//                    return "Arabic";
//                case "sv":
//                    return "Swedish";
//                case "cs":
//                    return "Czech";
//                case "da":
//                    return "Danish";
//                case "fi":
//                    return "Finnish";
//                case "el":
//                    return "Greek";
//                case "hu":
//                    return "Hungarian";
//                case "no":
//                    return "Norwegian";
//                case "sk":
//                    return "Slovak";
//                case "th":
//                    return "Thai";
//                case "uk":
//                    return "Ukrainian";
//                case "vi":
//                    return "Vietnamese";
                default:
                    return FileUtils.getConfig("en", FileUtils.FileType.LANGUAGE);
            }
        }
}
