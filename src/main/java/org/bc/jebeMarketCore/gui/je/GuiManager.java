package org.bc.jebeMarketCore.gui.je;

import org.bc.jebeMarketCore.JebeMarket;
import org.bc.jebeMarketCore.api.ShopManager;
import org.bc.jebeMarketCore.model.Shop;
import org.bc.jebeMarketCore.utils.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;

/**
 * 改进后的GUI管理系统
 */
public class GuiManager implements Listener {

    // 接口定义
    public interface ShopGUI {
        void open(Player player);

        void openWithContext(Player player, Object context);
    }

    private final Map<GUIType, ShopGUI> guiRegistry = new EnumMap<>(GUIType.class);
    private final JebeMarket plugin;

    public GuiManager(JebeMarket plugin,
                      ShopManager shopManager,
                      PlayerHeadManager headManager,
                      PlayerInputHandler inputHandler) {
        this.plugin = plugin;
        initializeGUIs(shopManager, headManager, inputHandler);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void initializeGUIs(ShopManager shopManager,
                                PlayerHeadManager headManager,
                                PlayerInputHandler inputHandler) {
        // 使用工厂方法创建实例
        guiRegistry.put(GUIType.MAIN, new ShopMainGUI(plugin, this, shopManager));
        guiRegistry.put(GUIType.PLAYER_SHOP, new ShopBrowseGui(
                plugin, shopManager, headManager, inputHandler,
                this,
                ShopBrowseGui.DisplayMode.ALL_SHOPS
        ));

        guiRegistry.put(GUIType.SHOP_DETAILS, new ShopDetailsGui(plugin, shopManager, this));

        guiRegistry.put(GUIType.MY_SHOP, new ShopBrowseGui(
                plugin, shopManager, headManager, inputHandler,
                this,
                ShopBrowseGui.DisplayMode.MY_SHOPS
        ));

        guiRegistry.put(GUIType.SHOP_EDIT, new ShopEditGui(plugin, shopManager, inputHandler, this));
    }

    // 统一开放接口
    public void openGui(Player player, GUIType type) {
        ShopGUI gui = guiRegistry.get(type);
        if (gui != null) {
            gui.open(player);
        }
    }

    // 带上下文打开（用于需要额外数据的场景）
    public void openGuiWithContext(Player player, GUIType type, Object context) {
        ShopGUI gui = guiRegistry.get(type);
        if (gui != null) {
            gui.openWithContext(player, context);
        }
    }

    // 保留原始接口（保持兼容性）
    public void openShopMainGui(Player player) {
        openGui(player, GUIType.MAIN);
    }

    public void openShopPlayerGui(Shop shop, Player player) {
        openGuiWithContext(player, GUIType.PLAYER_SHOP, shop);
    }

    public void openMyShopGui(Player player) {
        openGui(player, GUIType.MY_SHOP);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof BaseGUI) {
            ((BaseGUI) holder).handleClick(event);
        }
    }

    // 基础GUI抽象类
    public static abstract class BaseGUI implements InventoryHolder, ShopGUI {
        protected final JebeMarket plugin;
        protected @NotNull Inventory inventory;

        protected BaseGUI(JebeMarket plugin) {
            this.plugin = plugin;
        }

        protected void handleClick(InventoryClickEvent event) {
            event.setCancelled(true);
        }

        @Override
        public void openWithContext(Player player, Object context) {
            // 默认实现（可被覆盖）
            open(player);
        }
    }
}