package org.bc.jebeMarketCore;

import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bc.jebeMarketCore.api.ShopManager;
import org.bc.jebeMarketCore.command.ShopCommand;
import org.bc.jebeMarketCore.command.ShopTabCommand;
import org.bc.jebeMarketCore.config.Configuration;
import org.bc.jebeMarketCore.database.MysqlUtil;
import org.bc.jebeMarketCore.database.ShopSqlite3Util;
import org.bc.jebeMarketCore.gui.be.ShopMainForm;
import org.bc.jebeMarketCore.gui.je.GuiManager;
import org.bc.jebeMarketCore.listeners.PlayerListener;
import org.bc.jebeMarketCore.repository.ShopServiceImpl;
import org.bc.jebeMarketCore.service.ShopManagerImpl;
import org.bc.jebeMarketCore.utils.PlayerHeadManager;
import org.bc.jebeMarketCore.utils.PlayerInputHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

public final class JebeMarket extends JavaPlugin {

    //    全局路径
    @Getter
    ShopManager shopManager;
    Configuration config;
    @Getter
    YamlConfiguration i18nConfig;
    PlayerInputHandler inputHandler;
    PlayerHeadManager playerHeadManager;
    GuiManager guiManager;
    @Getter
    Economy labor_econ;
    ShopMainForm shopMainForm;

    @Override
    public void onEnable() {
        // 初始化配置
        config = new Configuration(this);
        i18nConfig = config.getI18nConfig();

        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                labor_econ = rsp.getProvider();
                getLogger().info(i18nConfig.getString("init.message"));
            } else {
                getLogger().warning(i18nConfig.getString("vault.no_economy_service"));
            }
        } else {
            getServer().getPluginManager().disablePlugin(this);
        }

        checkFilesystem();

        // 初始化数据存储（示例使用内存存储）
        ShopServiceImpl shopService = null;
        switch (config.getStorageType()) {
            case sqlite -> {
                try {
                    shopService = new ShopServiceImpl(new ShopSqlite3Util(this), this);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            case mysql -> {
                shopService = new ShopServiceImpl(new MysqlUtil(this), this);
            }
        }

        shopManager = new ShopManagerImpl(shopService, this);
        inputHandler = new PlayerInputHandler(this);

        playerHeadManager = new PlayerHeadManager(this);
        guiManager = new GuiManager(this, shopManager, playerHeadManager, inputHandler);
        shopMainForm = new ShopMainForm(this);
        // 注册命令
        PluginCommand shopCommand = getCommand("shop");
        if (shopCommand != null) {
            shopCommand.setExecutor(new ShopCommand(this, shopManager, config, inputHandler, playerHeadManager, guiManager,shopMainForm));
            shopCommand.setTabCompleter(new ShopTabCommand(shopManager, config, inputHandler));
        }

        getServer().getPluginManager().registerEvents(new PlayerListener(this, playerHeadManager), this);

        getLogger().info(i18nConfig.getString("plugin.enabled"));

        playerHeadManager.scheduleCacheCleanup(); // 启动缓存清理
    }

    private void checkFilesystem() {
        try {
            Path tempFile = Files.createTempFile(Paths.get("plugins/JebeMarket/"), "test", ".tmp");
            Files.delete(tempFile);
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, i18nConfig.getString("filesystem.check.failed"), e);
            getLogger().severe(i18nConfig.getString("filesystem.check.requirements.writable"));
            getLogger().severe(i18nConfig.getString("filesystem.check.requirements.disk_space"));
            getLogger().severe(i18nConfig.getString("filesystem.check.requirements.antivirus"));
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        getLogger().info(i18nConfig.getString("plugin.disabled"));
    }

    public String getI18nString(String key) {
        return i18nConfig.getString(key);
    }

    public List<String> getStringList(String s) {
        return i18nConfig.getStringList(s);
    }

    public int getInt(String s) {
        return i18nConfig.getInt(s);
    }
}