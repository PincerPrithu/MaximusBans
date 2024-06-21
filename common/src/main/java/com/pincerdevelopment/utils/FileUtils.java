package com.pincerdevelopment.utils;

import com.pincerdevelopment.Main;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {

    public enum FileType {
        CONFIG, LANGUAGE, MESSAGE
    }

    public static YamlDocument getConfig(@NotNull String name, @NotNull String path) {
        @Nullable
        YamlDocument config = null;
        try {
            InputStream stream = Main.class.getModule().getResourceAsStream(name + ".yml");
            config = YamlDocument.create(new File(Main.getPluginFolder() + "/" + path + name + ".yml"), stream
                    , GeneralSettings.DEFAULT, LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT, UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version")).build());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return config;
    }

    public static YamlDocument getConfig(@NotNull String name, FileType type) {
        String path = Main.getPluginFolder() + "/" + name + ".yml";
        String defaultPath = "config/" + name + ".yml";
        if (type == FileType.MESSAGE) {
            path = Main.getPluginFolder() + "/messages/" + name + ".yml";
            defaultPath = "messages/" + name + ".yml";
        } else if (type == FileType.LANGUAGE) {
            path = Main.getPluginFolder() + "/language/" + name + ".yml";
            defaultPath = "language/" + name + ".yml";
        }

        InputStream stream = null;
        try {
            stream = Main.class.getModule().getResourceAsStream(defaultPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        YamlDocument config = null;
        try {

            if (type == FileType.MESSAGE) {
                config = YamlDocument.create(new File(path), stream);
                return config;
            }

            config = YamlDocument.create(new File(path), stream
                    , GeneralSettings.DEFAULT, LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT, UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version")).build());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return config;
    }
}
