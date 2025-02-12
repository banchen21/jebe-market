package org.bc.jebeMarketCore.gui.je;

import org.bc.jebeMarketCore.JebeMarket;
import org.bc.jebeMarketCore.api.ShopManager;
import org.bc.jebeMarketCore.model.Shop;
import org.bc.jebeMarketCore.utils.ItemBuilder;
import org.bc.jebeMarketCore.utils.PlayerInputHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import static org.bc.jebeMarketCore.utils.MessageUtils.color;


/**
 * 优化后的商店主界面GUI
 */
public class ShopMainGUI extends GuiManager.BaseGUI {

    // 界面布局常量
    private static final int[] BORDER_SLOTS = {0, 1, 2, 3, 4, 5, 6, 7, 8, 45, 46, 47, 48, 49, 50, 51, 52, 53};
    private static final int BROWSE_SHOP_SLOT = 20; // 市集商铺
    private static final int MY_SHOP_SLOT = 29;  // 我的商铺
    private static final int PAWN_SHOP_SLOT = 24;  // 典当行
    private static final int MY_PAWN_SLOT = 33;   // 我的典当行
    private static final int RECYCLE_SHOP_SLOT = 22;  // 回收铺
    private static final int MY_RECYCLE_SLOT = 31; // 我的回收铺

    private final GuiManager guiManager;
    private final ShopManager shopManager;
    private final PlayerInputHandler inputHandler;
    private final @NotNull Inventory inventory;

    public ShopMainGUI(JebeMarket plugin, GuiManager guiManager, ShopManager shopManager, PlayerInputHandler inputHandler) {
        super(plugin);
        this.guiManager = guiManager;
        this.shopManager = shopManager;
        this.inputHandler = inputHandler;
        this.inventory = Bukkit.createInventory(this, 54, "§8§l市集中心");
        initializeItems();
    }

    private void initializeItems() {
        // 创建边框物品
        ItemStack border = ItemBuilder.of(Material.BLUE_STAINED_GLASS_PANE)
                .name(" ")
                .build();

        // 填充边框
        Arrays.stream(BORDER_SLOTS).forEach(slot -> inventory.setItem(slot, border));

        // 功能按钮
        inventory.setItem(BROWSE_SHOP_SLOT, createMainButton(
                Material.DIAMOND,
                "§b§l市集商铺",
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
                Material.GOLD_NUGGET,
                "§e§l典当行",
                "物品抵押快速变现",
                "§c利率: §f5%/天",
                "支持赎回期限设置"
        ));

        inventory.setItem(MY_PAWN_SLOT, createMainButton(
                Material.GOLD_INGOT,
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
                .glow(true)
                .build();
    }

    @Override
    public void open(Player player) {
        player.openInventory(inventory);
    }

    @Override
    protected void handleClick(InventoryClickEvent event) {
        super.handleClick(event);
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();

        switch (slot) {
            case BROWSE_SHOP_SLOT: // 市集商铺
                guiManager.openGuiWithContext(player, GUIType.PLAYER_SHOP, ShopBrowseGui.DisplayMode.ALL_SHOPS);
                break;

            case MY_SHOP_SLOT: // 我的商铺
                List<Shop> shopList = shopManager.getShopsByOwner(player.getUniqueId());
                if (shopList.isEmpty()) {
                    inputHandler.requestInput(player, "你还没有一个商铺，请输入新名称（2-16字符）以创建你的第一个商铺",
                            input -> {
                                if (input.length() < 2 || input.length() > 16) {
                                    player.sendMessage("§c名称长度需在2-16字符之间");
                                    return;
                                }
                                Shop shop = shopManager.createShop(input, player.getUniqueId());
                                if (shop != null) {
                                    player.sendMessage(color(String.format("&a成功创建商店 %s！UID: %s", input, shop.getUuid())));
                                } else {
                                    player.sendMessage(color("&c创建商铺失败，检查名称发现重复"));
                                }
                                guiManager.openGuiWithContext(player, GUIType.MY_SHOP, ShopBrowseGui.DisplayMode.MY_SHOPS);
                            },
                            30
                    );
                } else {
                    guiManager.openGuiWithContext(player, GUIType.MY_SHOP, ShopBrowseGui.DisplayMode.MY_SHOPS);
                }
                break;

            case PAWN_SHOP_SLOT: // 典当行
//                guiManager.openGui(player, GuiManager.GUIType.PAWN_SHOP);
                break;

            case MY_PAWN_SLOT: // 我的典当行
//                guiManager.openGui(player, GuiManager.GUIType.MY_PAWN);
                break;

            case RECYCLE_SHOP_SLOT: // 回收铺
//                guiManager.openGui(player, GuiManager.GUIType.RECYCLE_SHOP);
                break;

            case MY_RECYCLE_SLOT: // 我的回收铺
//                guiManager.openGui(player, GuiManager.GUIType.MY_RECYCLE);
                break;
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}