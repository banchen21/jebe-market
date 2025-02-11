package org.bc.jebeMarketCore.gui.je;

import org.bc.jebeMarketCore.JebeMarket;
import org.bc.jebeMarketCore.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * 商店主页Gui
 */
public class ShopMainGui implements InventoryHolder {
    private final Inventory inventory;
    private final ShopBrowseGui shopBrowseGui1;
    private final MyShopGui myShopGui;

    // 按钮位置定义
    private static final int[] BORDER_SLOTS = {0, 1, 2, 3, 4, 5, 6, 7, 8, 45, 46, 47, 48, 49, 50, 51, 52, 53};
    private static final int[] VERTICAL_BORDER = {9, 17, 18, 26, 27, 35, 36, 44};

    // 功能按钮槽位（第三行居中布局）
    public static final int BROWSE_SHOP_SLOT = 20;
    public static final int MY_SHOP_SLOT = 29;
    public static final int PAWN_SHOP_SLOT = 24;
    public static final int MY_PAWN_SLOT = 33;
    public static final int RECYCLE_SHOP_SLOT = 22;
    public static final int MY_RECYCLE_SLOT = 31;

    public ShopMainGui(JebeMarket plugin, ShopBrowseGui shopBrowseGui, MyShopGui myShopGui) {
        shopBrowseGui1 = shopBrowseGui;
        this.myShopGui = myShopGui;
        inventory = Bukkit.createInventory(this, 54, "§6§l市场中心");
    }

    private void initializeItems() {
        // 使用统一方法创建边框
        ItemStack border = createGuiItem(Material.BLUE_STAINED_GLASS_PANE, " ");

        // 填充边框
        Arrays.stream(BORDER_SLOTS).forEach(slot -> inventory.setItem(slot, border));
        Arrays.stream(VERTICAL_BORDER).forEach(slot -> {
            inventory.setItem(slot, border);
        });

        // 功能按钮（居中布局）
        inventory.setItem(BROWSE_SHOP_SLOT, createMainButton(
                Material.DIAMOND,
                "§b§l浏览商铺",
                "查看所有上架商品",
                "按分类查找",
                "支持价格排序"
        ));

        inventory.setItem(MY_SHOP_SLOT, createMainButton(
                Material.CHEST,
                "§6§l我的商铺",
                "管理您的商铺",
                "修改价格/下架"
        ));

        inventory.setItem(PAWN_SHOP_SLOT, createMainButton(
                Material.GOLD_INGOT,
                "§e§l典当行",
                "物品抵押快速变现",
                "§c利率: §f5%/天",
                "支持赎回期限设置"
        ));

        inventory.setItem(MY_PAWN_SLOT, createMainButton(
                Material.GOLD_NUGGET,
                "§e§l我的典当行",
                "管理您的典当行",
                "设置利率/赎回期限"
        ));

        inventory.setItem(RECYCLE_SHOP_SLOT, createMainButton(
                Material.FURNACE,
                "§a§l回收铺",
                "物品兑换金币",
                "批量回收支持",
                "实时价格查询"
        ));

        inventory.setItem(MY_RECYCLE_SLOT, createMainButton(
                Material.COAL,
                "§a§l我的回收铺",
                "管理您的回收铺",
                "设置回收价格/规则"
        ));
    }

    private ItemStack createMainButton(Material material, String name, String... lore) {
        return ItemBuilder.of(material)
                .name(name)
                .lore(lore)
                .glow(true) // 添加发光效果
                .build();
    }

    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    public void open(Player player) {
        initializeItems();
        player.openInventory(inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void handleShopMainGuiClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();

        switch (slot) {
            case BROWSE_SHOP_SLOT:
                shopBrowseGui1.open(player);
                break;
            case MY_SHOP_SLOT:
                myShopGui.open(player);
                break;
            case PAWN_SHOP_SLOT:
                // 打开典当行界面
                // TODO: 实现典当行界面
                player.sendMessage("§a即将打开典当行界面");
                break;
            case MY_PAWN_SLOT:
                // 打开我的典当行管理界面
                // TODO: 实现我的典当行管理界面
                player.sendMessage("§a即将打开我的典当行管理界面");
                break;
            case RECYCLE_SHOP_SLOT:
                // 打开回收铺界面
                // TODO: 实现回收铺界面
                player.sendMessage("§a即将打开回收铺界面");
                break;
            case MY_RECYCLE_SLOT:
                // 打开我的回收铺管理界面
                // TODO: 实现我的回收铺管理界面
                player.sendMessage("§a即将打开我的回收铺管理界面");
                break;
        }
    }
}