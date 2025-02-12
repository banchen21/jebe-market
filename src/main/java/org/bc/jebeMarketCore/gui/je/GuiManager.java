package org.bc.jebeMarketCore.gui.je;

import org.bc.jebeMarketCore.JebeMarket;
import org.bc.jebeMarketCore.api.ShopManager;
import org.bc.jebeMarketCore.utils.PlayerHeadManager;
import org.bc.jebeMarketCore.utils.PlayerInputHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 改进后的GUI管理系统
 */
public class GuiManager implements Listener {

    public interface ShopGUI {
        void open(Player player);

        void openWithContext(Player player, Object context);
    }

    // 使用 Supplier 存储 GUI 工厂方法
        private final Map<GUIType, Supplier<ShopGUI>> guiRegistry = new EnumMap<>(GUIType.class);
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
        guiRegistry.put(GUIType.MAIN, () -> new ShopMainGUI(plugin, this, shopManager,inputHandler));
        guiRegistry.put(GUIType.PLAYER_SHOP, () -> new ShopBrowseGui(
                plugin, shopManager, headManager, inputHandler,
                this,
                ShopBrowseGui.DisplayMode.ALL_SHOPS
        ));

        guiRegistry.put(GUIType.SHOP_DETAILS, () -> new ShopDetailsGui(plugin, shopManager, this, inputHandler));

        guiRegistry.put(GUIType.MY_SHOP, () -> new ShopBrowseGui(
                plugin, shopManager, headManager, inputHandler,
                this,
                ShopBrowseGui.DisplayMode.MY_SHOPS
        ));

        guiRegistry.put(GUIType.SHOP_EDIT, () -> new ShopEditGui(plugin, shopManager, inputHandler, this));

        guiRegistry.put(GUIType.ITEM_EDIT, () -> new ShopDetailsGui(plugin, shopManager, this, inputHandler));
    }

    // 统一开放接口
    public void openGui(Player player, GUIType type) {
        Supplier<ShopGUI> guiSupplier = guiRegistry.get(type);
        if (guiSupplier != null) {
            ShopGUI gui = guiSupplier.get();
            gui.open(player);
        }
    }

    // 带上下文打开（用于需要额外数据的场景）
    public void openGuiWithContext(Player player, GUIType type, Object context) {
        Supplier<ShopGUI> guiSupplier = guiRegistry.get(type);
        if (guiSupplier != null) {
            ShopGUI gui = guiSupplier.get();
            gui.openWithContext(player, context);
        }
    }

    // 保留原始接口（保持兼容性）
    public void openShopMainGui(Player player) {
        openGui(player, GUIType.MAIN);
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