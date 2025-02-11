package org.bc.jebeMarketCore.gui.je;

import org.bc.jebeMarketCore.JebeMarket;
import org.bc.jebeMarketCore.api.ShopManager;
import org.bc.jebeMarketCore.model.Shop;
import org.bc.jebeMarketCore.model.ShopItem;
import org.bc.jebeMarketCore.utils.PlayerInputHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MyShopItemEditGui implements InventoryHolder, Listener {

    private final JebeMarket plugin;
    private final ShopManager shopManager;
    private final PlayerInputHandler inputHandler;
    Inventory inventory;
    private Shop shop;

    public MyShopItemEditGui(JebeMarket plugin, ShopManager shopManager, PlayerInputHandler inputHandler) {
        this.plugin = plugin;
        this.shopManager = shopManager;
        this.inputHandler = inputHandler;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() != this) return;
        event.setCancelled(true);
    }

    public void open(Shop shop, Player player) {
        this.shop = shop;
        List<ShopItem> items = shopManager.getItems(shop.getUuid());
        if (items.isEmpty()) {
            player.sendMessage("该商店没有商品");
            return;
        }
        inventory = plugin.getServer().createInventory(this, 54, "商店商品编辑");
        int slot = 0;
        for (ShopItem item : items) {
            inventory.setItem(slot, item.getItemStack());
            slot++;
        }
        player.openInventory(inventory);
    }
}
