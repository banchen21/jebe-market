package org.bc.jebeMarketCore.gui.je;

import org.bc.jebeMarketCore.JebeMarket;
import org.bc.jebeMarketCore.api.ShopManager;
import org.bc.jebeMarketCore.model.Shop;
import org.bc.jebeMarketCore.model.ShopItem;
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
        this.inventory = Bukkit.createInventory(this, 54, color(plugin.getI18nString("ui.edit.title")));
    }

    private void initializeUI() {
        // 填充边框
        ItemStack border = ItemBuilder.of(Material.BLUE_STAINED_GLASS_PANE).name(color(plugin.getI18nString("ui.border_item"))).build();
        Arrays.stream(BORDER_SLOTS).forEach(slot -> inventory.setItem(slot, border));

        // 功能按钮
        updateInfoDisplay();

        // 返回按钮
        inventory.setItem(BACK_SLOT, ItemBuilder.of(Material.BARRIER).name(color(plugin.getI18nString("ui.back_button"))).build());
    }

    private void updateInfoDisplay() {
        // 名称按钮
        inventory.setItem(NAME_SLOT, ItemBuilder.of(Material.BOOK).name(color(plugin.getI18nString("ui.edit.name_button"))).lore(color(plugin.getI18nString("ui.edit.current_name").replace("%name%", currentShop.getName()))).glow(true).build());

        // 介绍按钮
        inventory.setItem(DESC_SLOT, ItemBuilder.of(Material.WRITABLE_BOOK).name(color(plugin.getI18nString("ui.edit.desc_button"))).lore(color(plugin.getI18nString("ui.edit.current_desc").replace("%lore%", currentShop.getLore()))).glow(true).build());

        List<String> loreList = plugin.getStringList("ui.edit.items_lore").stream().map(MessageUtils::color).collect(Collectors.toList());

        // 商品管理按钮
        inventory.setItem(ITEMS_SLOT, ItemBuilder.of(Material.GOLD_INGOT).name(color(plugin.getI18nString("ui.edit.items_button"))).lore(loreList.toArray(new String[0])) // 转换为数组
                .build());

        List<String> loreListlore = plugin.getStringList("ui.edit.sell_hand_lore").stream().map(MessageUtils::color).collect(Collectors.toList());

        // 上架手持物品按钮
        inventory.setItem(SELL_HAND_SLOT, ItemBuilder.of(Material.DIAMOND).name(color(plugin.getI18nString("ui.edit.sell_hand_button"))).lore(loreListlore.toArray(new String[0])) // 转换为数组
                .build());

        List<String> loreListbulk_sell_button = plugin.getStringList("ui.edit.bulk_sell_lore").stream().map(MessageUtils::color).collect(Collectors.toList());

        // 上架背包物品按钮
        inventory.setItem(SELL_INVENTORY_SLOT, ItemBuilder.of(Material.CHEST).name(color(plugin.getI18nString("ui.edit.bulk_sell_button"))).lore(loreListbulk_sell_button.toArray(new String[0])) // 转换为数组
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

        if (player.getInventory().getItemInMainHand().getType() == Material.AIR) {
            player.sendMessage(color(plugin.getI18nString("ui.edit.price.hand")));
            return;
        }

        player.closeInventory();

        inputHandler.requestInput(player, color(plugin.getI18nString("ui.edit.price.input")), input -> {
        try{

            double price = Double.parseDouble(input);
            ShopItem shopIte = new ShopItem(currentShop.getUuid(), player.getInventory().getItemInMainHand());
            shopIte.setPrice(price);
            shopManager.addHandItem(shopIte, player);
        } catch (Exception e) {
             player.sendMessage(color(plugin.getI18nString("ui.edit.price.invalid")));
        }

        }, 30);

    }

    private void openBulkSellInterface(Player player) {
        shopManager.addInventoryItem(currentShop.getUuid(), player);
    }

    private void handleNameEdit(Player player) {

        player.closeInventory();

        int minNameLength = plugin.getConfig().getInt("settings.shop.create.min_name_length");
        int maxNameLength = plugin.getConfig().getInt("settings.shop.create.max_name_length");

        String inputPrompt = color(plugin.getI18nString("ui.edit.name.input").replace("%a", String.valueOf(minNameLength)).replace("%b", String.valueOf(maxNameLength)));

        inputHandler.requestInput(player, inputPrompt, input -> {
            if (shopManager.updateShopName(currentShop, input, player)) {
                player.sendMessage(color(plugin.getI18nString("ui.edit.name.success")));
            } else {
                player.sendMessage(color(plugin.getI18nString("ui.edit.name.invalid")));
            }
        }, 30);

    }

    private void handleDescEdit(Player player) {
        player.closeInventory();
        int maxLoreLength = plugin.getConfig().getInt("settings.shop.edit.lore.max_length");
        String string = color(plugin.getI18nString("ui.edit.lore.input").replace("%b", String.valueOf(maxLoreLength)));
        inputHandler.requestInput(player, string, input -> {
            if (input.length() > maxLoreLength) {
                player.sendMessage(string);
                return;
            }
            currentShop.setLore(input);
            if (shopManager.updateShopLore(currentShop)) {
                player.sendMessage(color(plugin.getI18nString("ui.edit.lore.success")));
            } else {
                player.sendMessage(color(plugin.getI18nString("ui.edit.lore.invalid")));
            }
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