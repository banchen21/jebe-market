package org.bc.jebeMarketCore.gui.je;

import lombok.extern.slf4j.Slf4j;
import org.bc.jebeMarketCore.JebeMarket;
import org.bc.jebeMarketCore.api.ShopManager;
import org.bc.jebeMarketCore.model.Shop;
import org.bc.jebeMarketCore.utils.ItemBuilder;
import org.bc.jebeMarketCore.utils.PlayerHeadManager;
import org.bc.jebeMarketCore.utils.PlayerInputHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.bc.jebeMarketCore.utils.MessageUtils.color;

@Slf4j
public class ShopBrowseGui extends GuiManager.BaseGUI {

    public enum DisplayMode {
        ALL_SHOPS,
        MY_SHOPS
    }

    // 更新布局常量与参考页面一致
    private static final int BACK_SLOT = 0;
    private static final int[] BORDER_SLOTS = {1, 2, 3, 4, 5, 6, 7, 8, 45, 46, 47, 48, 49, 50, 51, 52, 53};
    private static final int PREV_PAGE_SLOT = 48;
    private static final int NEXT_PAGE_SLOT = 50;
    private static final int PAGE_INFO_SLOT = 49;
    private static final int ITEMS_PER_PAGE = 36; // 9-44共36个槽位
    private static final NamespacedKey SHOP_UUID_KEY = new NamespacedKey("jebemarket", "shop_uuid");

    // 依赖服务
    private final ShopManager shopManager;
    private final PlayerHeadManager headManager;
    private final PlayerInputHandler inputHandler;
    private final GuiManager guiManager;
    private final DisplayMode displayMode;

    // 状态管理
    private int currentPage = 0;
    private UUID currentOwner;

    public ShopBrowseGui(JebeMarket plugin,
                         ShopManager shopManager,
                         PlayerHeadManager headManager,
                         PlayerInputHandler inputHandler,
                         GuiManager guiManager,
                         DisplayMode displayMode) {
        super(plugin);
        this.shopManager = shopManager;
        this.headManager = headManager;
        this.inputHandler = inputHandler;
        this.guiManager = guiManager;
        this.displayMode = displayMode;
        this.inventory = Bukkit.createInventory(this, 54, getTitle());
        initializeLayout();
    }

    private String getTitle() {
        String key = displayMode == DisplayMode.ALL_SHOPS ?
                "ui.browse.title.all_shops" :
                "ui.browse.title.my_shops";
        return color(plugin.getI18nString(key));
    }

    private void initializeLayout() {
        // 填充边框
        ItemStack border = ItemBuilder.of(Material.BLUE_STAINED_GLASS_PANE)
                .name(color(plugin.getI18nString("ui.browse.common.border_item")))
                .build();
        Arrays.stream(BORDER_SLOTS).forEach(slot -> inventory.setItem(slot, border));

        // 返回按钮
        inventory.setItem(BACK_SLOT, ItemBuilder.of(Material.BARRIER)
                .name(color(plugin.getI18nString("ui.back_button")))
                .build());
        refreshPage();
    }

    private void refreshPage() {
        clearItems();
        loadShops();
        updateNavigationButtons();
    }

    private void clearItems() {
        for (int i = 9; i < 45; i++) {
            inventory.setItem(i, null);
        }
    }

    private void loadShops() {
        List<Shop> shops = getCurrentShops();
        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, shops.size());

        for (int i = startIndex; i < endIndex; i++) {
            int slot = 9 + (i - startIndex);
            inventory.setItem(slot, createShopItem(shops.get(i)));
        }
    }

    private List<Shop> getCurrentShops() {
        return displayMode == DisplayMode.ALL_SHOPS ?
                shopManager.getShops() :
                shopManager.getShopsByOwner(currentOwner);
    }

    private void updateNavigationButtons() {
        int totalShops = displayMode == DisplayMode.ALL_SHOPS ?
                shopManager.getShops().size() :
                shopManager.getShopsByOwner(currentOwner).size();
        int totalPages = (int) Math.ceil((double) totalShops / ITEMS_PER_PAGE);

        // 修复：添加color处理
        String pageInfo = color(plugin.getI18nString("ui.browse.navigation.page_info")
                .replace("%current%", String.valueOf(currentPage + 1))
                .replace("%total%", String.valueOf(totalPages == 0 ? 1 : totalPages)));
        inventory.setItem(PAGE_INFO_SLOT, ItemBuilder.of(Material.PAPER)
                .name(pageInfo)
                .build());


        // 修复：添加color处理
        if (currentPage > 0) {
            inventory.setItem(PREV_PAGE_SLOT, ItemBuilder.of(Material.ARROW)
                    .name(color(plugin.getI18nString("ui.browse.navigation.previous_page")))
                    .build());
        } else {
            inventory.setItem(PREV_PAGE_SLOT, ItemBuilder.of(Material.RED_STAINED_GLASS_PANE)
                    .name(color(plugin.getI18nString("ui.browse.navigation.no_previous_page")))
                    .build());
        }

        // 修复：添加color处理
        if (currentPage < totalPages - 1) {
            inventory.setItem(NEXT_PAGE_SLOT, ItemBuilder.of(Material.ARROW)
                    .name(color(plugin.getI18nString("ui.browse.navigation.next_page")))
                    .build());
        } else {
            inventory.setItem(NEXT_PAGE_SLOT, ItemBuilder.of(Material.RED_STAINED_GLASS_PANE)
                    .name(color(plugin.getI18nString("ui.browse.navigation.no_next_page")))
                    .build());
        }
    }

    private ItemStack createShopItem(Shop shop) {
        ItemStack head = headManager.getPlayerHead(shop.getOwner());
        ItemMeta meta = head.getItemMeta();

        // 名称和描述
        // 修复：添加color处理
        meta.setDisplayName(color(plugin.getI18nString("ui.browse.shop_item.name")
                .replace("%name%", shop.getName())));

        List<String> lore = plugin.getStringList("ui.browse.shop_item.lore").stream()
                .map(line -> {
                    String replaced = line.replace("%count%",
                            String.valueOf(shopManager.getItemCount(shop.getUuid())));
                    return color(displayMode == DisplayMode.ALL_SHOPS ?  // 修复：添加color处理
                            replaced.replace("%action%", plugin.getI18nString("ui.browse.shop_item.view_action")) :
                            replaced.replace("%action%", plugin.getI18nString("ui.browse.shop_item.manage_action")));
                })
                .collect(Collectors.toList());
        meta.setLore(lore);

        // 存储商店UUID
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(SHOP_UUID_KEY, PersistentDataType.STRING, shop.getUuid().toString());

        head.setItemMeta(meta);
        return head;
    }

    @Override
    protected void handleClick(InventoryClickEvent event) {
        super.handleClick(event);
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();

        if (slot == BACK_SLOT) {
            returnToPrevious(player);
            return;
        }

        if (slot == PREV_PAGE_SLOT && currentPage > 0) {
            currentPage--;
            refreshPage();
            return;
        }

        if (slot == NEXT_PAGE_SLOT) {
            int totalShops = getCurrentShops().size();
            int totalPages = (int) Math.ceil((double) totalShops / ITEMS_PER_PAGE);
            if (currentPage < totalPages - 1) {
                currentPage++;
                refreshPage();
            }
            return;
        }

        if (slot >= 9 && slot <= 44) {
            handleShopClick(event.getCurrentItem(), player);
        }
    }

    private void handleShopClick(ItemStack item, Player player) {
        if (item == null || !item.hasItemMeta()) return;

        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        if (pdc.has(SHOP_UUID_KEY, PersistentDataType.STRING)) {
            UUID shopId = UUID.fromString(pdc.get(SHOP_UUID_KEY, PersistentDataType.STRING));

            Shop shop = shopManager.getShop(shopId);
            if (displayMode == DisplayMode.MY_SHOPS) {
                guiManager.openGuiWithContext(player, GUIType.SHOP_EDIT, shop);
            } else {
                guiManager.openGuiWithContext(player, GUIType.SHOP_DETAILS, shop);
            }
        }
    }

    @Override
    public void open(Player player) {
        player.openInventory(inventory);
    }

    @Override
    public void openWithContext(Player player, Object context) {
        if (displayMode == DisplayMode.MY_SHOPS) {
            this.currentOwner = player.getUniqueId();
            refreshPage();
            player.openInventory(inventory);
        } else {
            super.openWithContext(player, context);
            refreshPage();
            player.openInventory(inventory);
        }
    }

    private void returnToPrevious(Player player) {
        guiManager.openGui(player, GUIType.MAIN);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}