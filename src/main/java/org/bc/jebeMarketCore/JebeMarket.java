package org.bc.jebeMarketCore;

import org.bc.jebeMarketCore.api.ShopManager;
import org.bc.jebeMarketCore.command.ShopCommand;
import org.bc.jebeMarketCore.command.ShopTabCommand;
import org.bc.jebeMarketCore.config.Configuration;
import org.bc.jebeMarketCore.listeners.GuiListener;
import org.bc.jebeMarketCore.listeners.PlayerListener;
import org.bc.jebeMarketCore.repository.ShopServiceImpl;
import org.bc.jebeMarketCore.service.ShopManagerImpl;
import org.bc.jebeMarketCore.database.MysqlUtil;
import org.bc.jebeMarketCore.database.ShopSqlite3Util;
import org.bc.jebeMarketCore.utils.PlayerHeadManager;
import org.bc.jebeMarketCore.utils.PlayerInputHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.logging.Level;

public final class JebeMarket extends JavaPlugin {

    //    全局路径
    ShopManager shopManager;
    Configuration config;
    YamlConfiguration i18nConfig;
    PlayerInputHandler inputHandler;
    PlayerHeadManager playerHeadManager;

    @Override
    public void onEnable() {
        // 初始化配置
        config = new Configuration(this);
        i18nConfig = config.getI18nConfig();
        checkFilesystem();

        // 初始化数据存储（示例使用内存存储）
        ShopServiceImpl shopService = null;
        switch (config.getStorageType()) {
            case sqlite -> {
                try {
                    shopService = new ShopServiceImpl(new ShopSqlite3Util(this));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            case mysql -> {
                shopService = new ShopServiceImpl(new MysqlUtil(this));
            }
        }

        shopManager = new ShopManagerImpl(shopService);
        inputHandler = new PlayerInputHandler(this);

        playerHeadManager = new PlayerHeadManager(this);
        // 注册命令
        PluginCommand shopCommand = getCommand("shop");
        if (shopCommand != null) {
            shopCommand.setExecutor(new ShopCommand(this, shopManager, config, inputHandler, playerHeadManager));
            shopCommand.setTabCompleter(new ShopTabCommand(shopManager, config, inputHandler));
        }

        //        注册事件监听
//        getServer().getPluginManager().registerEvents(new ShopPlayerGuiListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this, playerHeadManager), this);
        getServer().getPluginManager().registerEvents(new GuiListener(this, shopManager, playerHeadManager), this);

        getLogger().info("JebeMarketCore 已启用");

        playerHeadManager.scheduleCacheCleanup(); // 启动缓存清理
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