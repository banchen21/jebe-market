package org.bc.jebeMarketCore.gui.je;

import org.bc.jebeMarketCore.JebeMarket;
import org.bc.jebeMarketCore.api.ShopManager;
import org.bc.jebeMarketCore.model.Shop;
import org.bc.jebeMarketCore.utils.ItemBuilder;
import org.bc.jebeMarketCore.utils.MessageUtils;
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
import java.util.stream.Collectors;

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
        this.inventory = Bukkit.createInventory(this, 54, color(plugin.getString("ui.main.title")));
        initializeItems();
    }

    private void initializeItems() {
        // 创建边框物品
        ItemStack border = ItemBuilder.of(Material.BLUE_STAINED_GLASS_PANE)
                .name(" ")
                .build();

        // 填充边框
        Arrays.stream(BORDER_SLOTS).forEach(slot -> inventory.setItem(slot, border));

        // 功能按钮（从配置文件获取文本）
        inventory.setItem(BROWSE_SHOP_SLOT, createMainButton(
                Material.DIAMOND,
                "ui.main.buttons.browse_shop",
                "查看所有上架商品", // 默认值，实际从配置读取
                "按分类查找",
                "支持价格排序"
        ));

        inventory.setItem(MY_SHOP_SLOT, createMainButton(
                Material.CHEST,
                "ui.main.buttons.my_shop",
                "管理您的商铺",
                "修改价格/下架"
        ));

        inventory.setItem(PAWN_SHOP_SLOT, createMainButton(
                Material.GOLD_NUGGET,
                "ui.main.buttons.pawn_shop",
                "物品抵押快速变现",
                "§c利率: §f5%/天",
                "支持赎回期限设置"
        ));

        inventory.setItem(MY_PAWN_SLOT, createMainButton(
                Material.GOLD_INGOT,
                "ui.main.buttons.my_pawn",
                "管理您的典当行",
                "设置利率/赎回期限"
        ));

        inventory.setItem(RECYCLE_SHOP_SLOT, createMainButton(
                Material.FURNACE,
                "ui.main.buttons.recycle_shop",
                "物品兑换金币",
                "批量回收支持",
                "实时价格查询"
        ));

        inventory.setItem(MY_RECYCLE_SLOT, createMainButton(
                Material.COAL,
                "ui.main.buttons.my_recycle",
                "管理您的回收铺",
                "设置回收价格/规则"
        ));
    }

    private ItemStack createMainButton(Material material, String configPath, String... defaultLore) {
        // 从配置获取按钮信息
        String name = color(plugin.getString(configPath + ".name"));
        List<String> lore = plugin.getStringList(configPath + ".lore")
                .stream()
                .map(MessageUtils::color)
                .collect(Collectors.toList());

        // 如果配置为空则使用默认值
        if (lore.isEmpty() && defaultLore.length > 0) {
            lore = Arrays.asList(defaultLore);
        }

        return ItemBuilder.of(material)
                .name(name)
                .lore(lore.toArray(new String[0]))
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
            case BROWSE_SHOP_SLOT:
                guiManager.openGuiWithContext(player, GUIType.PLAYER_SHOP, ShopBrowseGui.DisplayMode.ALL_SHOPS);
                break;

            case MY_SHOP_SLOT:
                List<Shop> shopList = shopManager.getShopsByOwner(player.getUniqueId());
                if (shopList.isEmpty()) {
                    player.closeInventory();
                    // 从配置获取输入提示
                    String inputPrompt = color(plugin.getString("commands.create.input_prompt"));
                    inputHandler.requestInput(player, inputPrompt,
                            input -> {
                                // 验证名称长度
                                if (input.length() < plugin.getInt("settings.shop.min_name_length") ||
                                        input.length() > plugin.getInt("settings.shop.max_name_length")) {
                                    player.sendMessage(color(plugin.getString("commands.create.errors.name_length")));
                                    return;
                                }
                                Shop shop = shopManager.createShop(input, player.getUniqueId());
                                if (shop != null) {
                                    // 成功消息格式化
                                    String successMsg = color(plugin.getString("commands.create.success"));
                                    player.sendMessage(color(String.format(successMsg, input, shop.getUuid())));
                                } else {
                                    player.sendMessage(color(plugin.getString("commands.create.errors.duplicate_name")));
                                }
                            },
                            30
                    );
                } else {
                    guiManager.openGuiWithContext(player, GUIType.MY_SHOP, ShopBrowseGui.DisplayMode.MY_SHOPS);
                }
                break;

            // 其他按钮处理...
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}