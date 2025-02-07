package org.bc.jebeMarketCore.config;

import org.bc.jebeMarketCore.JebeMarket;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Configuration {

    private final JebeMarket plugin;
    private FileConfiguration config;

    public Configuration(JebeMarket plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.saveDefaultConfig();
        plugin.saveResource("shops.json", false);
        createFolder("shops");
        config = plugin.getConfig();
    }

    /**
     * 在插件的数据文件夹中创建指定名称的空文件夹。
     *
     * @param folderName 要创建的文件夹名称
     */
    public void createFolder(String folderName) {
        // 获取插件的数据文件夹路径
        Path dataFolderPath = plugin.getDataFolder().toPath();
        // 构建文件夹的完整路径
        Path folderPath = dataFolderPath.resolve(folderName);

        try {
            // 创建文件夹（如果不存在）
            Files.createDirectories(folderPath);
        } catch (IOException ignored) {
        }
    }

    public double getDefaultPrice() {
        return config.getDouble("defaults.price", 100.0);
    }

    public String getCurrency() {
        return config.getString("defaults.currency", "JCoin");
    }

    public String getStorageType() {
        return config.getString("storage.type", "memory");
    }

    public String getLanguage() {
        return config.getString("language", "zh_CN");
    }

    //    获取特定key
    public String getString(String key) {
        return config.getString(key);
    }

    public int getInt(String key) {
        return config.getInt(key);
    }
}