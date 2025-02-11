package org.bc.jebeMarketCore.gui.je;

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

/**
 * 通用商铺浏览界面（支持全服/个人模式）
 */
public class ShopBrowseGui extends GuiManager.BaseGUI {

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public enum DisplayMode {
        ALL_SHOPS,   // 全服商铺模式
        MY_SHOPS     // 个人商铺模式
    }

    // 布局常量
    private static final int[] BORDER_SLOTS = {0, 1, 2, 3, 4, 5, 6, 7, 8, 45, 46, 47, 48, 49, 50, 51, 52, 53};
    private static final int ITEMS_PER_PAGE = 45;
    private static final int PREV_PAGE_SLOT = 45;
    private static final int NEXT_PAGE_SLOT = 53;
    private static final int PAGE_INFO_SLOT = 49;
    private static final NamespacedKey SHOP_UUID_KEY = new NamespacedKey("jebemarket", "shop_uuid");

    // 依赖服务
    private final ShopManager shopManager;
    private final PlayerHeadManager headManager;
    private final PlayerInputHandler inputHandler;
    private final GuiManager guiManager;
    private final DisplayMode displayMode;

    // 状态管理
    private int currentPage = 1;
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
        return displayMode == DisplayMode.ALL_SHOPS ?
                "§b§l全服商铺" : "§6§l我的商铺";
    }

    private void initializeLayout() {
        ItemStack border = ItemBuilder.of(Material.BLUE_STAINED_GLASS_PANE)
                .name(" ")
                .build();
        Arrays.stream(BORDER_SLOTS).forEach(slot -> inventory.setItem(slot, border));
        refreshPage();
    }

    private void refreshPage() {
        clearItems();
        updatePagination();
        loadShops();
    }

    private void clearItems() {
        Arrays.stream(new int[]{9, 44}).forEach(slot -> inventory.setItem(slot, null));
    }

    private void loadShops() {
        List<Shop> shops = getCurrentShops();
        int slot = 9;
        for (Shop shop : shops) {
            if (slot > 44) break;
            inventory.setItem(slot++, createShopItem(shop));
        }
    }

    private List<Shop> getCurrentShops() {
        List<Shop> shops = displayMode == DisplayMode.ALL_SHOPS ?
                shopManager.getShops() :
                shopManager.getShopsByOwner(currentOwner);

        int totalPages = getTotalPages(shops.size());
        currentPage = Math.max(1, Math.min(currentPage, totalPages));

        int start = (currentPage - 1) * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, shops.size());
        return shops.subList(start, end);
    }

    private void updatePagination() {
        int totalShops = displayMode == DisplayMode.ALL_SHOPS ?
                shopManager.getShops().size() :
                shopManager.getShopsByOwner(currentOwner).size();

        int totalPages = getTotalPages(totalShops);

        // 更新分页按钮
        inventory.setItem(PREV_PAGE_SLOT, currentPage > 1 ?
                createNavigationButton("§a上一页") : null);
        inventory.setItem(NEXT_PAGE_SLOT, currentPage < totalPages ?
                createNavigationButton("§a下一页") : null);

        // 页面信息
        inventory.setItem(PAGE_INFO_SLOT, createPageInfoItem(totalShops, totalPages));
    }

    private ItemStack createShopItem(Shop shop) {
        ItemStack head = headManager.getPlayerHead(shop.getOwner());
        ItemMeta meta = head.getItemMeta();

        meta.setDisplayName("§e" + shop.getName());
        meta.setLore(List.of(
                "§7商品数量: " + shopManager.getItemCount(shop.getUuid()),
                displayMode == DisplayMode.ALL_SHOPS ?
                        "§a点击查看商品" : "§e点击管理商铺"
        ));

        // 存储商店UUID
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(SHOP_UUID_KEY, PersistentDataType.STRING, shop.getUuid().toString());

        head.setItemMeta(meta);
        return head;
    }


    private void handleShopClick(ItemStack item, Player player) {
        if (item == null || !item.hasItemMeta()) return;

        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        if (pdc.has(SHOP_UUID_KEY, PersistentDataType.STRING)) {
            UUID shopId = UUID.fromString(pdc.get(SHOP_UUID_KEY, PersistentDataType.STRING));
            Shop shop = shopManager.getShop(shopId);

            if (displayMode == DisplayMode.MY_SHOPS) {
                handleMyShopClick(shop, player);
            } else {
                handlePublicShopClick(shop, player);
            }
        }
    }

    private void handleMyShopClick(Shop shop, Player player) {
        guiManager.openGuiWithContext(player,
                GUIType.SHOP_EDIT,
                shop
        );
    }

    private void handlePublicShopClick(Shop shop, Player player) {
        guiManager.openGuiWithContext(player,
                GUIType.SHOP_DETAILS,
                shop
        );
    }

    private void handleMyShopOpening(Player player) {
        List<Shop> shops = shopManager.getShopsByOwner(player.getUniqueId());
        if (shops.isEmpty()) {
            player.closeInventory();
            inputHandler.requestInput(player, "§a请输入新商铺名称（输入cancel取消）",
                    input -> {
                        if (input.equalsIgnoreCase("cancel")) {
                            guiManager.openGui(player, GUIType.MAIN);
                            return;
                        }

                        try {
                            Shop newShop = shopManager.createShop(input, player.getUniqueId());
                            player.sendMessage("§a商铺创建成功！");
                            // 创建成功后重新打开界面
                            currentOwner = player.getUniqueId();
                            currentPage = 1;
                            refreshPage();
                            player.openInventory(inventory);
                        } catch (Exception e) {
                            player.sendMessage("§c创建失败: " + e.getMessage());
                            guiManager.openGui(player, GUIType.MAIN);
                        }
                    },
                    30
            );
        } else {
            // 重要修复：已有商铺时执行打开操作
            currentOwner = player.getUniqueId();
            currentPage = 1;
            refreshPage();
            player.openInventory(inventory);
        }
    }

    // 辅助方法
    private ItemStack createPageInfoItem(int totalShops, int totalPages) {
        return ItemBuilder.of(Material.BOOK)
                .name("§6页面信息")
                .lore(
                        "§7当前页数: " + currentPage + "/" + totalPages,
                        "§7总商店数: " + totalShops,
                        "§7每页显示: " + ITEMS_PER_PAGE + " 个商店"
                )
                .build();
    }

    private ItemStack createNavigationButton(String name) {
        return ItemBuilder.of(Material.ARROW)
                .name(name)
                .build();
    }

    private int getTotalPages(int totalItems) {
        return (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);
    }

    @Override
    public void openWithContext(Player player, Object context) {
        if (displayMode == DisplayMode.MY_SHOPS) {
            this.currentOwner = player.getUniqueId();
        }
        // 调用父类方法确保基础逻辑执行
        super.openWithContext(player, context);
        // 强制刷新数据
        currentPage = 1;
        refreshPage();
    }


    @Override
    public void open(Player player) {
        if (displayMode == DisplayMode.MY_SHOPS) {
            handleMyShopOpening(player);
        } else {
            currentPage = 1;
            currentOwner = null;
            refreshPage();
            player.openInventory(inventory);
        }
    }


    @Override
    protected void handleClick(InventoryClickEvent event) {
        super.handleClick(event);
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();

        switch (slot) {
            case PREV_PAGE_SLOT:
                currentPage--;
                refreshPage();
                break;

            case NEXT_PAGE_SLOT:
                currentPage++;
                refreshPage();
                break;
            default:
                if (slot >= 9 && slot <= 44) {
                    handleShopClick(event.getCurrentItem(), player);
                }
        }
    }


}