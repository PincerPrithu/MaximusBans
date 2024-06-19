package com.pincerdevelopment;

import com.pincerdevelopment.Universal.CustomPlatform;
import com.pincerdevelopment.utils.FileUtils;
import dev.dejvokep.boostedyaml.YamlDocument;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.sql.SQLException;

public class Main {
    @Getter
    private static DataManager dataManager;
    @Getter
    private static File pluginFolder;
    @Getter
    static CustomPlatform platform;
    @Getter
    public static LanguageManager lang;
    private static boolean initialized = false;

    public static void main(String[] args) {
    }

    public static void init(File pluginFolder, CustomPlatform customPlatform) {
        if (!initialized) {
            Main.pluginFolder = pluginFolder;
            platform = customPlatform;
            YamlDocument mainConfig = FileUtils.getConfig("config", FileUtils.FileType.CONFIG);
            lang = new LanguageManager();
            try {
                // Create database URL based on database type
                String databaseUrl = getDatabaseUrl(
                        mainConfig.getString("DB-user"),
                        mainConfig.getString("DB-pass"),
                        mainConfig.getString("DB-host"),
                        mainConfig.getString("DB-port"),
                        mainConfig.getString("DB-name"),
                        mainConfig.getString("DB-type"));

                // Initialize DataManager with appropriate database URL
                dataManager = new DataManager(databaseUrl);
                initialized = true;
            } catch (SQLException e) {
                System.err.println("Failed to initialize DataManager: " + e.getMessage());
            }


        }

    }

    public static void shutdown() {
        if (initialized) {
            try {
                if (dataManager != null) {
                    dataManager.close();
                }
                initialized = false;
                System.out.println("Common module shutdown successfully.");
            } catch (IOException e) {
                System.err.println("Failed to shutdown properly: " + e.getMessage());
            }
        }
    }

    private static String getDatabaseUrl(String username, String password, String host, String port, String dbName, String dbType) {
        try {
            return switch (dbType.toLowerCase()) {
                case "mysql" ->
                        String.format("jdbc:mysql://%s:%s/%s?user=%s&password=%s&useSSL=false&autoReconnect=true",
                                host, port, dbName,
                                URLEncoder.encode(username, StandardCharsets.UTF_8),
                                URLEncoder.encode(password, StandardCharsets.UTF_8));
                case "postgres" -> String.format("jdbc:postgresql://%s:%s/%s?user=%s&password=%s",
                        host, port, dbName,
                        URLEncoder.encode(username, StandardCharsets.UTF_8),
                        URLEncoder.encode(password, StandardCharsets.UTF_8));
                case "mssql" -> String.format("jdbc:sqlserver://%s:%s;databaseName=%s;user=%s;password=%s",
                        host, port, dbName,
                        URLEncoder.encode(username, StandardCharsets.UTF_8),
                        URLEncoder.encode(password, StandardCharsets.UTF_8));
                case "h2" -> "jdbc:h2:" + Paths.get(String.valueOf(getPluginFolder()), "punishments").toAbsolutePath().toString();
                case "derby" -> "jdbc:derby:" + Paths.get(String.valueOf(getPluginFolder()), "punishments").toAbsolutePath().toString() + ";create=true";
                case "hsqldb" -> "jdbc:hsqldb:file:" + Paths.get(String.valueOf(getPluginFolder()), "punishments").toAbsolutePath().toString();
                default -> "jdbc:sqlite:" + Paths.get(String.valueOf(getPluginFolder()), "punishments.db").toAbsolutePath().toString();
            };
        } catch (Exception e) {
            System.err.println("Error constructing database URL: " + e.getMessage());
            return null;
        }
    }
}
