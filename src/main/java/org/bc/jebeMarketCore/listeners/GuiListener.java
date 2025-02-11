package org.bc.jebeMarketCore.listeners;

import org.bc.jebeMarketCore.JebeMarket;
import org.bc.jebeMarketCore.api.ShopManager;
import org.bc.jebeMarketCore.gui.je.ShopBrowseGui;
import org.bc.jebeMarketCore.gui.je.ShopMainGui;
import org.bc.jebeMarketCore.gui.je.ShopPlayerGui;
import org.bc.jebeMarketCore.utils.PlayerHeadManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import static org.bc.jebeMarketCore.gui.je.ShopMainGui.BROWSE_SHOP_SLOT;


public class GuiListener implements Listener {
    private final JebeMarket plugin;
    private final ShopManager shopManager;
    private final PlayerHeadManager headManager;
    private final ShopBrowseGui shopBrowseGui;

    public GuiListener(JebeMarket plugin, ShopManager shopManager, PlayerHeadManager headManager) {
        this.plugin = plugin;
        this.shopManager = shopManager;
        this.headManager = headManager;
        this.shopBrowseGui = new ShopBrowseGui(plugin, shopManager, headManager);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // 处理主界面点击
        if (event.getInventory().getHolder() instanceof ShopMainGui) {
            handleMainGuiClick(event);
        }
    }

    /**
     * 处理主界面点击
     *
     * @param event InventoryClickEvent
     */
    private void handleMainGuiClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();

        if (item == null || !item.hasItemMeta()) return;

        switch (event.getRawSlot()) {
            case BROWSE_SHOP_SLOT: // 浏览商店
                shopBrowseGui.open(player);
                break;
        }
    }
}
