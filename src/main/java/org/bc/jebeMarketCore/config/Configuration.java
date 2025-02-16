package org.bc.jebeMarketCore.config;

import lombok.Getter;
import org.bc.jebeMarketCore.JebeMarket;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Configuration {

    private final JebeMarket plugin;
    @Getter
    FileConfiguration config;

    public Configuration(JebeMarket plugin) {
        this.plugin = plugin;
        init();
    }

    public void init() {
        plugin.saveDefaultConfig();
        plugin.saveResource("lang/cn.yml", false);
        config = plugin.getConfig();
    }

    //    使用的存储方式
    public StorageType getStorageType() {
        String storageType = config.getString("storage.type");
        return StorageType.valueOf(storageType);
    }

    public YamlConfiguration getI18nConfig() {
        String language = config.getString("language");
        Path langPath = Paths.get(
                plugin.getDataFolder().getAbsolutePath(),
                "lang",
                language + ".yml"
        );
        boolean debug = config.getBoolean("debug");
        if (debug) {
            try {
                Files.deleteIfExists(langPath);
                plugin.saveResource("lang/" + language + ".yml", false);
                return YamlConfiguration.loadConfiguration(langPath.toFile());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return YamlConfiguration.loadConfiguration(langPath.toFile());
        }
    }


    /**
     * 在插件的数据文件夹中创建指定名称的空文件夹。
     * @param folderName 要创建的文件夹名称
     */
    private void createFolder(String folderName) {
        Path dataFolderPath = plugin.getDataFolder().toPath();
        Path folderPath = dataFolderPath.resolve(folderName);
        try {
            Files.createDirectories(folderPath);
        } catch (IOException ignored) {
        }
    }
}