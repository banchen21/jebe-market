package org.bc.jebeMarketCore.gui.je;

import org.bc.jebeMarketCore.JebeMarket;
import org.bc.jebeMarketCore.api.ShopManager;
import org.bc.jebeMarketCore.model.Shop;
import org.bc.jebeMarketCore.model.ShopItem;
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

import static org.bc.jebeMarketCore.utils.MessageUtils.color;


/**
 * 商铺编辑界面
 */
public class ShopEditGui extends GuiManager.BaseGUI {
    private final ShopManager shopManager;
    private final PlayerInputHandler inputHandler;
    private final GuiManager guiManager;
    private Shop currentShop;

    // 界面布局
    private static final int[] BORDER_SLOTS = {1, 2, 3, 4, 5, 6, 7, 8, 45, 46, 47, 48, 49, 50, 51, 52, 53};
    private static final int NAME_SLOT = 20;
    private static final int DESC_SLOT = 22;
    private static final int ITEMS_SLOT = 24;
    private static final int SELL_HAND_SLOT = 29;    // 上架手持物品
    private static final int SELL_INVENTORY_SLOT = 33; // 上架背包物品
    private static final int BACK_SLOT = 0;

    public ShopEditGui(JebeMarket plugin,
                       ShopManager shopManager,
                       PlayerInputHandler inputHandler, GuiManager guiManager) {
        super(plugin);
        this.shopManager = shopManager;
        this.inputHandler = inputHandler;
        this.guiManager = guiManager;
        this.inventory = Bukkit.createInventory(this, 54, "§6商铺管理");
        initializeUI();
    }

    private void initializeUI() {
        // 填充边框
        ItemStack border = ItemBuilder.of(Material.BLUE_STAINED_GLASS_PANE)
                .name(" ")
                .build();
        Arrays.stream(BORDER_SLOTS).forEach(slot -> inventory.setItem(slot, border));

        // 功能按钮
        updateInfoDisplay();

        // 返回按钮
        inventory.setItem(BACK_SLOT, ItemBuilder.of(Material.BARRIER)
                .name("§c返回上级")
                .build());
    }

    private void updateInfoDisplay() {
        // 名称按钮
        inventory.setItem(NAME_SLOT, ItemBuilder.of(Material.BOOK)
                .name("§e修改商铺名称")
                .lore("§7当前名称: " + (currentShop != null ? currentShop.getName() : "未知"))
                .glow(true)
                .build());

        // 介绍按钮
        inventory.setItem(DESC_SLOT, ItemBuilder.of(Material.WRITABLE_BOOK)
                .name("§e修改商铺介绍")
                .lore("§7当前详细描述: " + (currentShop != null ? currentShop.getLore() : "未知"))
                .glow(true)
                .build());

        // 商品管理按钮
        inventory.setItem(ITEMS_SLOT, ItemBuilder.of(Material.GOLD_INGOT)
                .name("§e管理商品价格")
                .lore("§7点击管理商品库存和定价")
                .build());

        // 上架手持物品按钮
        inventory.setItem(SELL_HAND_SLOT, ItemBuilder.of(Material.DIAMOND)
                .name("§6上架手持物品")
                .lore("§7将手中物品快速上架",
                        "§eShift+点击设置价格")
                .build());

        // 上架背包物品按钮
        inventory.setItem(SELL_INVENTORY_SLOT, ItemBuilder.of(Material.CHEST)
                .name("§6批量上架物品")
                .lore("§7从背包选择多个物品上架",
                        "§e点击打开选择界面")
                .build());

    }

    @Override
    public void open(Player player) {
        player.openInventory(inventory);
    }

    @Override
    public void openWithContext(Player player, Object context) {
        if (context instanceof Shop) {
            this.currentShop = (Shop) context;
            updateInfoDisplay();
        }
        open(player);
    }

    @Override
    protected void handleClick(InventoryClickEvent event) {
        super.handleClick(event);
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();

        switch (slot) {
            case NAME_SLOT:
                handleNameEdit(player);
                break;

            case DESC_SLOT:
                handleDescEdit(player);
                break;

            case ITEMS_SLOT:
                openItemManagement(player);
                break;

            case BACK_SLOT:
                returnToPrevious(player);
                break;

            case SELL_HAND_SLOT:
                handleSellHandItem(player);
                break;

            case SELL_INVENTORY_SLOT:
                openBulkSellInterface(player);
                break;
        }
    }

    /**
     * 上架手持物品
     *
     * @param player Player
     */
    private void handleSellHandItem(Player player) {
        shopManager.addHandItem(currentShop.getUuid(), player);
    }

    private void openBulkSellInterface(Player player) {
        shopManager.addInventoryItem(currentShop.getUuid(), player);
    }

    private void handleNameEdit(Player player) {
        player.closeInventory();
        inputHandler.requestInput(player, "请输入新名称（2-16字符之间）",
                input -> {
                    if (input.length() < 2 || input.length() > 16) {
                        player.sendMessage("§c名称长度需在2-16字符之间");
                        return;
                    }
                    currentShop.setName(input);
                    shopManager.updateShopName(currentShop);
                    player.sendMessage("§a名称已更新！");
                },
                30
        );
    }

    private void handleDescEdit(Player player) {
        player.closeInventory();
        inputHandler.requestInput(player, "请输入新介绍（支持多行，最多256字符以 | 符号换行）",
                input -> {
                    if (input.length() > 256) {
                        player.sendMessage("§c介绍过长，最多256字符");
                        return;
                    }
                    currentShop.setLore(input);
                    shopManager.updateShopLore(currentShop);
                    player.sendMessage("§a介绍已更新！");
                },
                60
        );
    }

    private void openItemManagement(Player player) {
        Object[] context = {currentShop, ShopDetailsGui.Mode.EDIT};
        guiManager.openGuiWithContext(player,
                GUIType.ITEM_EDIT,
                context
        );
    }

    private void returnToPrevious(Player player) {
        // 返回上一级
        guiManager.openGuiWithContext(player,
                GUIType.MY_SHOP,
                player.getUniqueId()
        );
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}