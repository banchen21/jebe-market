package org.bc.jebeMarketCore;

import org.bc.jebeMarketCore.api.ItemManager;
import org.bc.jebeMarketCore.api.ShopManager;
import org.bc.jebeMarketCore.command.MarketCommand;
import org.bc.jebeMarketCore.command.MarketTabCompleter;
import org.bc.jebeMarketCore.config.Configuration;
import org.bc.jebeMarketCore.listeners.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

public final class JebeMarket extends JavaPlugin {

    //    全局路径
    ShopManager shopManager;
    ItemManager itemManager;
    Configuration config;
    YamlConfiguration i18nConfig;

    @Override
    public void onEnable() {
        // 初始化配置
        config = new Configuration(this);
        i18nConfig = config.getI18nConfig();
        checkFilesystem();

        // 初始化数据存储（示例使用内存存储）
        switch (config.getStorageType()) {
            case file -> {
            }
            case sqlite -> {
            }
            case mysql -> {
            }
        }

        // 注册命令
        PluginCommand shopCommand = getCommand("market");
        if (shopCommand != null) {
            shopCommand.setExecutor(new MarketCommand(shopManager, itemManager, config));
            shopCommand.setTabCompleter(new MarketTabCompleter(shopManager, itemManager, config));
        }

//        注册事件监听
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getLogger().info("JebeMarketCore 已启用");
    }

    private void checkFilesystem() {
        try {
            Path tempFile = Files.createTempFile(Paths.get("plugins/JebeMarket/"), "test", ".tmp");
            Files.delete(tempFile);
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "文件系统检测失败，请确保：", e);
            getLogger().severe("1. 插件目录可写");
            getLogger().severe("2. 有足够的磁盘空间");
            getLogger().severe("3. 没有杀毒软件拦截文件操作");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {

    }
}