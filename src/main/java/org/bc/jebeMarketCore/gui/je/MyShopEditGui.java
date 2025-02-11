package org.bc.jebeMarketCore.gui.je;

import org.bc.jebeMarketCore.JebeMarket;
import org.bc.jebeMarketCore.api.ShopManager;
import org.bc.jebeMarketCore.model.Shop;
import org.bc.jebeMarketCore.utils.PlayerInputHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class MyShopEditGui implements InventoryHolder, Listener {
    private final JebeMarket plugin;
    private final ShopManager shopManager;
    private final GuiManager guiManager;
    private final PlayerInputHandler inputHandler;
    private Inventory inventory;

    // 按钮布局定义
    private static final int NAME_SLOT = 20;
    private static final int DESC_SLOT = 22;
    private static final int PRICE_SLOT = 24;
    private static final int BACK_SLOT = 49;
    Shop shop;

    public MyShopEditGui(JebeMarket plugin, ShopManager shopManager, GuiManager guiManager, PlayerInputHandler inputHandler) {
        this.plugin = plugin;
        this.shopManager = shopManager;
        this.guiManager = guiManager;
        this.inputHandler = inputHandler;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Shop shop, Player player) {
        this.shop = shop;
        inventory = Bukkit.createInventory(this, 54, shop.getName() + "的§6商铺管理");
        setupUI();
        player.openInventory(inventory);
    }

    private void setupUI() {
        // 填充边框（蓝色玻璃板）
        ItemStack border = createGuiItem(Material.BLUE_STAINED_GLASS_PANE, " ");
        for (int i : new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 45, 46, 47, 48, 49, 50, 51, 52, 53}) {
            inventory.setItem(i, border);
        }

        // 功能按钮（通过物品类型+名称识别）
        inventory.setItem(NAME_SLOT, createButton(
                Material.BOOK,
                "§e修改商铺名称"
        ));

        inventory.setItem(DESC_SLOT, createButton(
                Material.WRITABLE_BOOK,
                "§e修改商铺介绍"
        ));

        inventory.setItem(PRICE_SLOT, createButton(
                Material.GOLD_INGOT,
                "§e商品价格管理"
        ));

        // 返回按钮（特殊材质）
        inventory.setItem(BACK_SLOT, createButton(
                Material.BARRIER,
                "§c返回上级"
        ));
    }

    private ItemStack createButton(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() != this) return;
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();

        player.closeInventory();
        switch (slot) {
            case NAME_SLOT:
                inputHandler.requestInput(player, "请输入新的名称", s -> {
                    shop.setName(s);
                    shopManager.setShop(shop);
                    player.sendMessage(shop.getName() + "的新的名称：" + s);
                }, 30);
                break;

            case DESC_SLOT:
                inputHandler.requestInput(player, "请输入新的介绍", s -> {
                    shop.setLore(s);
                    shopManager.setShop(shop);
                    player.sendMessage(shop.getName() + "的新的介绍：" + s);
                }, 30);
                break;

            case PRICE_SLOT:
                // TODO: 打开商品价格管理界面
                break;
            case BACK_SLOT:
                player.closeInventory();
                guiManager.openMyShopGui(player);
                break;
        }
    }

    private ItemStack createGuiItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        return item;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}