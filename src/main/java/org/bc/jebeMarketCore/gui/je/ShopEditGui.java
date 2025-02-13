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

    public ShopEditGui(JebeMarket plugin, ShopManager shopManager, PlayerInputHandler inputHandler, GuiManager guiManager) {
        super(plugin);
        this.shopManager = shopManager;
        this.inputHandler = inputHandler;
        this.guiManager = guiManager;
        this.inventory = Bukkit.createInventory(this, 54, color(plugin.getString("ui.edit.title")));
    }

    private void initializeUI() {
        // 填充边框
        ItemStack border = ItemBuilder.of(Material.BLUE_STAINED_GLASS_PANE).name(color(plugin.getString("ui.common.border_item"))).build();
        Arrays.stream(BORDER_SLOTS).forEach(slot -> inventory.setItem(slot, border));

        // 功能按钮
        updateInfoDisplay();

        // 返回按钮
        inventory.setItem(BACK_SLOT, ItemBuilder.of(Material.BARRIER).name(color(plugin.getString("commands.gui.back_button"))).build());
    }

    private void updateInfoDisplay() {
        // 名称按钮
        inventory.setItem(NAME_SLOT, ItemBuilder.of(Material.BOOK).name(color(plugin.getString("ui.edit.name_button"))).lore(color(plugin.getString("ui.edit.current_name").replace("%name%", currentShop.getName()))).glow(true).build());

        // 介绍按钮
        inventory.setItem(DESC_SLOT, ItemBuilder.of(Material.WRITABLE_BOOK).name(color(plugin.getString("ui.edit.desc_button"))).lore(color(plugin.getString("ui.edit.current_desc").replace("%lore%", currentShop.getLore()))).glow(true).build());

        List<String> loreList = plugin.getStringList("ui.edit.items_lore").stream().map(MessageUtils::color).collect(Collectors.toList());

        // 商品管理按钮
        inventory.setItem(ITEMS_SLOT, ItemBuilder.of(Material.GOLD_INGOT).name(color(plugin.getString("ui.edit.items_button"))).lore(loreList.toArray(new String[0])) // 转换为数组
                .build());

        List<String> loreListlore = plugin.getStringList("ui.edit.sell_hand_lore").stream().map(MessageUtils::color).collect(Collectors.toList());

        // 上架手持物品按钮
        inventory.setItem(SELL_HAND_SLOT, ItemBuilder.of(Material.DIAMOND).name(color(plugin.getString("ui.edit.sell_hand_button"))).lore(loreListlore.toArray(new String[0])) // 转换为数组
                .build());

        List<String> loreListbulk_sell_button = plugin.getStringList("ui.edit.bulk_sell_lore").stream().map(MessageUtils::color).collect(Collectors.toList());

        // 上架背包物品按钮
        inventory.setItem(SELL_INVENTORY_SLOT, ItemBuilder.of(Material.CHEST).name(color(plugin.getString("ui.edit.bulk_sell_button"))).lore(loreListbulk_sell_button.toArray(new String[0])) // 转换为数组
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
        initializeUI();
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
        int minNameLength = plugin.getConfig().getInt("settings.shop.min_name_length");
        int maxNameLength = plugin.getConfig().getInt("settings.shop.max_name_length");
        String inputPrompt = color(plugin.getString("commands.edit.name.input").replace("%min_name_length%", String.valueOf(minNameLength)).replace("%max_name_length%", String.valueOf(maxNameLength)));

        inputHandler.requestInput(player, inputPrompt, input -> {
            if (input.length() < minNameLength || input.length() > maxNameLength) {
                String errorMessage = color(plugin.getString("commands.edit.name.errors.length").replace("%min_name_length%", String.valueOf(minNameLength)).replace("%max_name_length%", String.valueOf(maxNameLength)));
                player.sendMessage(errorMessage);
                return;
            }

            currentShop.setName(input);
            if (shopManager.updateShopName(currentShop)) {
                player.sendMessage(color(plugin.getString("commands.edit.name.success")));
            } else {
                player.sendMessage(color(plugin.getString("commands.edit.name.errors.duplicate")));
            }
        }, 30);
    }

    private void handleDescEdit(Player player) {
        player.closeInventory();
        inputHandler.requestInput(player, color(plugin.getString("commands.edit.lore.input")), input -> {
            if (input.length() > plugin.getConfig().getInt("settings.shop.max_lore_length")) {
                player.sendMessage(color(plugin.getString("commands.edit.lore.errors.length").replace("%max%", String.valueOf(plugin.getConfig().getInt("settings.shop.max_lore_length")))));
                return;
            }
            currentShop.setLore(input);
            shopManager.updateShopLore(currentShop);
            player.sendMessage(color(plugin.getString("commands.edit.lore.success")));
        }, 60);
    }


    private void openItemManagement(Player player) {
        Object[] context = {currentShop, ShopDetailsGui.Mode.EDIT};
        guiManager.openGuiWithContext(player, GUIType.ITEM_EDIT, context);
    }

    private void returnToPrevious(Player player) {
        // 返回上一级
        guiManager.openGuiWithContext(player, GUIType.MY_SHOP, player.getUniqueId());
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}