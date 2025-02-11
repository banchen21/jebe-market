package org.bc.jebeMarketCore.gui.je;

import org.bc.jebeMarketCore.JebeMarket;
import org.bc.jebeMarketCore.api.ShopManager;
import org.bc.jebeMarketCore.model.Shop;
import org.bc.jebeMarketCore.model.ShopItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ShopPlayerGui implements InventoryHolder, Listener {

    private final JebeMarket plugin;
    private final ShopManager shopManager;
    private List<ShopItem> shopItemList; // 确保初始化
    private Inventory inventory;

    public ShopPlayerGui(JebeMarket plugin, ShopManager shopManager1) {
        this.plugin = plugin;
        this.shopManager = shopManager1;
        this.shopItemList = new ArrayList<>(); // 初始化为空列表
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // 处理主界面点击
        if (event.getInventory().getHolder() instanceof ShopPlayerGui) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            int i = event.getRawSlot();

            // 检查 shopItemList 是否为空
            if (shopItemList == null || i >= shopItemList.size()) {
                return; // 如果为空或索引超出范围，直接返回
            }

            try {
                ShopItem shopItem = shopItemList.get(i);
                // 处理点击事件
                // 示例：购买商品
                if (shopItem != null) {
                    player.sendMessage("§a你点击了: " + shopItem.getUuid());
                }
            } catch (Exception e) {
                player.closeInventory();
            }

        }
    }

    public void open(Shop shop, Player player) {
        // 创建库存
        this.inventory = Bukkit.createInventory(this, 54, "§b" + shop.getName());

        // 获取商店商品列表
        List<ShopItem> shopItems = shopManager.getItems(shop.getUuid());
        this.shopItemList = shopItems; // 初始化 shopItemList

        // 遍历商品并填充库存
        for (int i = 0; i < shopItems.size(); i++) {
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
}