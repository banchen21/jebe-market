package org.bc.jebeMarketCore.gui.je;

import org.bc.jebeMarketCore.JebeMarket;
import org.bc.jebeMarketCore.api.ShopManager;
import org.bc.jebeMarketCore.model.Shop;
import org.bc.jebeMarketCore.model.ShopItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品购买界面
 */
public class ShopDetailsGui extends GuiManager.BaseGUI {

    private final JebeMarket plugin;
    private final ShopManager shopManager;
    private final GuiManager guiManager;
    private List<ShopItem> shopItemList; // 确保初始化

    public ShopDetailsGui(JebeMarket plugin, ShopManager shopManager, GuiManager guiManager) {
        super(plugin);
        this.plugin = plugin;
        this.shopManager = shopManager;
        this.guiManager = guiManager;
        this.shopItemList = new ArrayList<>(); // 初始化为空列表
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    @Override
    protected void handleClick(InventoryClickEvent event) {
        super.handleClick(event);
        Player player = (Player) event.getWhoClicked();
        int i = event.getRawSlot();

        // 检查 shopItemList 是否为空
        if (shopItemList == null || i >= shopItemList.size()) {
            return; // 如果为空或索引超出范围，直接返回
        }

        try {
            ShopItem shopItem = shopItemList.get(i);
            // TODO：购买商品
            if (shopItem != null) {
                player.sendMessage("§a你点击了: " + shopItem.getUuid());
            }
        } catch (Exception e) {
            player.closeInventory();
        }

    }

    @Override
    public void open(Player player) {
        player.openInventory(inventory);
    }

    @Override
    public void openWithContext(Player player, Object context) {
        Shop shop = (Shop) context;
        // 创建库存
        this.inventory = Bukkit.createInventory(this, 54, "§b" + shop.getName());

        // 获取商店商品列表
        List<ShopItem> shopItems = shopManager.getItems(shop.getUuid());
        this.shopItemList = shopItems;

        // 遍历商品并填充库存
        for (int i = 0; i < shopItems.size(); i++) {
            ItemStack itemStack = getItemStack(shopItems, i);
            // 将商品放入库存
            if (i < inventory.getSize()) {
                inventory.setItem(i, itemStack);
            } else {
                break; // 如果超出库存大小，停止填充
            }
        }
        // 打开库存
        player.openInventory(inventory);
    }

    private static @NotNull ItemStack getItemStack(List<ShopItem> shopItems, int i) {
        ShopItem shopItem = shopItems.get(i);
        ItemStack itemStack = shopItem.getItemStack();
        ItemMeta itemMeta = itemStack.getItemMeta();

        // 获取或创建lore
        List<String> lore = itemMeta.getLore() != null ? itemMeta.getLore() : new ArrayList<>();

        // 添加价格和数量信息
        lore.add("§7单价: §a" + shopItem.getPrice());
        lore.add("§7剩余数量: §a" + itemStack.getAmount());

        // 设置lore并更新ItemMeta
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}