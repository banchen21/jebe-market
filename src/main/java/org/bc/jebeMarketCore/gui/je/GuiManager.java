package org.bc.jebeMarketCore.gui.je;

import org.bc.jebeMarketCore.JebeMarket;
import org.bc.jebeMarketCore.api.ShopManager;
import org.bc.jebeMarketCore.model.Shop;
import org.bc.jebeMarketCore.utils.PlayerHeadManager;
import org.bc.jebeMarketCore.utils.PlayerInputHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GuiManager implements Listener {


    private final JebeMarket plugin;
    private final PlayerInputHandler inputHandler;
    private final ShopManager shopManager;
    private final PlayerHeadManager playerHeadManager;
    private final ShopMainGui shopMainGui;
    private final ShopBrowseGui shopBrowseGui;
    private final ShopPlayerGui shopPlayerGui;
    private final MyShopGui myShopGui;
    private final MyShopEditGui myShopEditGui;


    public GuiManager(JebeMarket plugin, ShopManager shopManager, PlayerHeadManager playerHeadManager, PlayerInputHandler inputHandler) {
        this.plugin = plugin;
        this.inputHandler = inputHandler;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.shopManager = shopManager;
        this.playerHeadManager = playerHeadManager;
        this.myShopEditGui = new MyShopEditGui(plugin, shopManager, this, inputHandler);
        this.shopPlayerGui = new ShopPlayerGui(plugin, shopManager);
        this.myShopGui = new MyShopGui(plugin, shopManager, playerHeadManager, shopPlayerGui, myShopEditGui);
        this.shopBrowseGui = new ShopBrowseGui(plugin, shopManager, playerHeadManager, shopPlayerGui);
        this.shopMainGui = new ShopMainGui(plugin, shopBrowseGui, myShopGui);
    }

    public void openShopMainGui(Player player) {
        shopMainGui.open(player);
    }

    public void openShopPlayerGui(Shop shop, Player player) {
        shopPlayerGui.open(shop, player);
    }

    public void openMyShopGui(Player player) {
        myShopGui.open(player);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // 处理 ShopMainGui 的点击事件
        if (event.getInventory().getHolder() instanceof ShopMainGui) {
            shopMainGui.handleShopMainGuiClick(event);
        }
        // 处理 ShopPlayerGui 的点击事件
        else if (event.getInventory().getHolder() instanceof ShopPlayerGui) {
            shopPlayerGui.handleShopPlayerGuiClick(event);
        }
    }

}
