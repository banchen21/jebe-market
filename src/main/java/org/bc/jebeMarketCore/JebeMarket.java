package org.bc.jebeMarketCore;

import org.bc.jebeMarketCore.api.ItemManager;
import org.bc.jebeMarketCore.api.ShopManager;
import org.bc.jebeMarketCore.command.MarketCommand;
import org.bc.jebeMarketCore.command.MarketTabCompleter;
import org.bc.jebeMarketCore.config.Configuration;
import org.bc.jebeMarketCore.i18n.JebeMarketTranslations;
import org.bc.jebeMarketCore.listeners.PlayerListener;
import org.bc.jebeMarketCore.repository.FileShopRepository;
import org.bc.jebeMarketCore.repository.ShopRepository;
import org.bc.jebeMarketCore.service.ItemServiceImpl;
import org.bc.jebeMarketCore.service.ShopServiceImpl;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class JebeMarket extends JavaPlugin {

    //    全局路径
    public static final String JEBEMARKETPATH = "plugins/JebeMarket/";
    private ShopManager shopManager;
    private ItemManager itemManager;

    @Override
    public void onEnable() {
        // 初始化配置
        Configuration config = new Configuration(this);
        // 初始化数据存储（示例使用内存存储）
        ShopRepository shopRepo = null;
        switch (config.getStorageType()) {
            case "sqlite":
                break;
            case "mysql":
                break;
            case "file":
            default:
                shopRepo = new FileShopRepository(this);
                break;
        }
//        初始化翻译器
        new JebeMarketTranslations(config.getLanguage());

        // 构建服务层
        this.shopManager = new ShopServiceImpl(shopRepo);
        this.itemManager = new ItemServiceImpl(config);

        // 注册命令
        PluginCommand shopCommand = getCommand("market");
        if (shopCommand != null) {
            shopCommand.setExecutor(new MarketCommand(shopManager, itemManager,config));
            shopCommand.setTabCompleter(new MarketTabCompleter(shopManager,itemManager,config));
        }

//        注册事件监听
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getLogger().info("JebeMarketCore 已启用");
    }

    @Override
    public void onDisable() {

    }
}