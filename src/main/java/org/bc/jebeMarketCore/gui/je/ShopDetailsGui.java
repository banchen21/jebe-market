package org.bc.jebeMarketCore.gui.je;

import lombok.extern.slf4j.Slf4j;
import org.bc.jebeMarketCore.JebeMarket;
import org.bc.jebeMarketCore.api.ShopManager;
import org.bc.jebeMarketCore.model.Shop;
import org.bc.jebeMarketCore.model.ShopItem;
import org.bc.jebeMarketCore.utils.ItemBuilder;
import org.bc.jebeMarketCore.utils.PlayerInputHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.bc.jebeMarketCore.utils.MessageUtils.color;

/**
 * 商品详情界面（支持购买/编辑模式）
 */
@Slf4j
public class ShopDetailsGui extends GuiManager.BaseGUI {

    public enum Mode {
        BUY,  // 购买模式
        EDIT  // 编辑模式
    }

    // 布局常量
    private static final int BACK_SLOT = 0;  // 返回按钮
    private static final int[] BORDER_SLOTS = {1, 2, 3, 4, 5, 6, 7, 8, 45, 46, 47, 48, 49, 50, 51, 52, 53}; // 边框位置
    private static final int PREV_PAGE_SLOT = 48; // 上一页按钮
    private static final int NEXT_PAGE_SLOT = 50; // 下一页按钮
    private static final int PAGE_INFO_SLOT = 49; // 页面信息
    private static final int ITEMS_PER_PAGE = 36; // 每页显示商品数

    // 依赖服务
    private final ShopManager shopManager;
    private final GuiManager guiManager;
    private final PlayerInputHandler inputHandler;
    private final JebeMarket plugin;

    // 状态管理
    private Mode currentMode = Mode.BUY;
    private Shop currentShop;
    private List<ShopItem> shopItemList = new ArrayList<>();
    private int currentPage = 0;
    private int totalPages = 0;

    public ShopDetailsGui(JebeMarket plugin, ShopManager shopManager, GuiManager guiManager, PlayerInputHandler inputHandler) {
        super(plugin);
        this.plugin = plugin;
        this.shopManager = shopManager;
        this.guiManager = guiManager;
        this.inputHandler = inputHandler;
    }

    @Override
    public void open(Player player) {
        player.openInventory(inventory);
    }

    @Override
    public void openWithContext(Player player, Object context) {
        if (context instanceof Shop) {
            this.currentShop = (Shop) context;
            initializeUI();
            player.openInventory(inventory);
        } else if (context instanceof Object[]) {
            Object[] params = (Object[]) context;
            this.currentShop = (Shop) params[0];
            this.currentMode = (Mode) params[1];
            initializeUI();
            player.openInventory(inventory);
        }
    }

    private void initializeUI() {
        this.inventory = Bukkit.createInventory(this, 54, getTitle());

        ItemStack border = ItemBuilder.of(Material.BLUE_STAINED_GLASS_PANE).name(color(plugin.getI18nString("ui.border_item")))
                .build();
        Arrays.stream(BORDER_SLOTS).forEach(slot -> inventory.setItem(slot, border));

//        返回按钮
        inventory.setItem(BACK_SLOT, ItemBuilder.of(Material.BARRIER).name(color(plugin.getI18nString("ui.back_button")))
                .build());

        // 加载商品和翻页按钮
        refreshItems();
        updateNavigationButtons();
    }

    /**
     * 获取标题
     *
     * @return String
     */
    private String getTitle() {
        String key = currentMode == Mode.BUY ? "ui.details.title.buy" : "ui.details.title.edit";
        return color(plugin.getI18nString(key).replace("%shop%", currentShop.getName()));
    }

    private void refreshItems() {
        // 清空商品区域
        for (int i = 9; i < 45; i++) {
            inventory.setItem(i, null);
        }

        // 重新获取商品列表
        shopItemList = shopManager.getItems(currentShop.getUuid());
        totalPages = (int) Math.ceil(shopItemList.size() / (double) ITEMS_PER_PAGE);

        // 计算当前页的起始和结束索引
        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, shopItemList.size());

        // 显示当前页的商品
        for (int i = startIndex; i < endIndex; i++) {
            ShopItem item = shopItemList.get(i);
            inventory.setItem(9 + (i - startIndex), buildItemDisplay(item));
        }
    }

    private void updateNavigationButtons() {

        // 修复：页面信息处理
        String pageInfo = color(plugin.getI18nString("ui.details.navigation.page_info").replace("%current%", String.valueOf(currentPage + 1)).replace("%total%", String.valueOf(totalPages == 0 ? 1 : totalPages)));

        // 修复：按钮文本处理
        ItemStack prevPageItem = (currentPage > 0 ? ItemBuilder.of(Material.ARROW).name(color(plugin.getI18nString("ui.details.navigation.previous_page"))) : ItemBuilder.of(Material.RED_STAINED_GLASS_PANE).name(color(plugin.getI18nString("ui.details.navigation.no_previous_page")))).build();

        ItemStack nextPageItem = (currentPage < totalPages - 1 ? ItemBuilder.of(Material.ARROW).name(color(plugin.getI18nString("ui.details.navigation.next_page"))) : ItemBuilder.of(Material.RED_STAINED_GLASS_PANE).name(color(plugin.getI18nString("ui.details.navigation.no_next_page")))).build();

        inventory.setItem(PREV_PAGE_SLOT, prevPageItem);
        inventory.setItem(PAGE_INFO_SLOT, ItemBuilder.of(Material.PAPER).name(pageInfo).build());
        inventory.setItem(NEXT_PAGE_SLOT, nextPageItem);
    }

    private ItemStack buildItemDisplay(ShopItem shopItem) {
        ItemStack item = shopItem.getItemStack().clone();
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore() != null ? meta.getLore() : new ArrayList<>();

        // 添加基础信息
        lore.add(color(plugin.getI18nString("ui.shop_item.price_line").replace("%price%", new DecimalFormat("#.00").format(shopItem.getPrice()))));
        lore.add(color(plugin.getI18nString("ui.shop_item.stock_line")).replace("%amount%", String.valueOf(item.getAmount())));

        // 添加操作提示
        List<String> actions = currentMode == Mode.BUY ? plugin.getStringList("ui.shop_item.actions.buy") : plugin.getStringList("ui.shop_item.actions.edit");

        actions.forEach(action -> lore.add(color(action)));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @Override
    protected void handleClick(InventoryClickEvent event) {
        super.handleClick(event);
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();

        // 处理返回按钮
        if (slot == BACK_SLOT) {
            returnToPrevious(player);
            return;
        }

        // 处理翻页
        if (slot == PREV_PAGE_SLOT && currentPage > 0) {
            currentPage--;
            refreshItems();
            updateNavigationButtons();
            return;
        }
        if (slot == NEXT_PAGE_SLOT && currentPage < totalPages - 1) {
            currentPage++;
            refreshItems();
            updateNavigationButtons();
            return;
        }

        // 处理商品点击
        if (slot >= 9 && slot <= 44) {
            int index = currentPage * ITEMS_PER_PAGE + (slot - 9);
            if (index < shopItemList.size()) {
                ShopItem item = shopItemList.get(index);
                if (currentMode == Mode.BUY) {
                    handleBuyClick(event, item, player);
                } else {
                    handleEditClick(event, item, player);
                }
            }
        }
    }

    private void handleBuyClick(InventoryClickEvent event, ShopItem item, Player player) {
        switch (event.getClick()) {
            case LEFT: // 单次购买
                purchaseItem(item, player);
                break;
            case RIGHT: // 批量购买
                openBulkPurchase(item, player);
                break;
        }
    }

    private void handleEditClick(InventoryClickEvent event, ShopItem item, Player player) {
        if (!player.getUniqueId().equals(currentShop.getOwner())) {
            player.sendMessage(color(plugin.getI18nString("commands.errors.no_permission")));
            return;
        }

        switch (event.getClick()) {
            case LEFT: // 修改价格
                modifyPrice(item, player);
                break;
            case RIGHT: // 删除商品
                deleteItem(item, player);
                break;
        }
    }

    /**
     * 购买商品
     *
     * @param shopItem ShopItem
     * @param player   Player
     */
    private void purchaseItem(ShopItem shopItem, Player player) {
        ItemStack itemStack = shopItem.getItemStack();
        if (itemStack.getAmount() >= 1 && plugin.getLabor_econ().has(player, shopItem.getPrice())) {
            plugin.getLabor_econ().withdrawPlayer(player, shopItem.getPrice());

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(currentShop.getOwner());

            plugin.getLabor_econ().depositPlayer(offlinePlayer.getPlayer(), shopItem.getPrice());

            ItemStack givePlayerItem = itemStack.clone();
            givePlayerItem.setAmount(1);
            if (player.getInventory().firstEmpty() == -1) {
                player.getWorld().dropItem(player.getLocation(), givePlayerItem);
                player.sendMessage(color(plugin.getI18nString("ui.details.items.inventory_full")));
            } else {
                player.getInventory().addItem(givePlayerItem);
            }
            player.sendMessage(color(plugin.getI18nString("ui.details.transaction.success.single")
                    .replace("%amount%", "1")
                    .replace("%cost%", String.valueOf(shopItem.getPrice()))));

            itemStack.setAmount(itemStack.getAmount() - 1);
            if (itemStack.getAmount() == 0) {
                Shop shop = shopManager.getShop(currentShop.getUuid());
                shopManager.removeItem(shop, shopItem.getUuid());
            } else {
                shopItem.setItemStack(itemStack);
                shopManager.updateItemStack(shopItem);
            }
            refresh();
        } else {
            player.sendMessage(color(plugin.getI18nString("ui.details.transaction.errors.insufficient_funds_2").replace("%cost%", String.valueOf(shopItem.getPrice()))));
        }
    }

    private void openBulkPurchase(ShopItem item, Player player) {
        player.sendMessage("§e批量购买功能开发中...");
    }

    private void modifyPrice(ShopItem shopItem, Player player) {
        player.closeInventory();
        inputHandler.requestInput(player, color(plugin.getI18nString("ui.details.edit.price.input")), input -> {
            try {
                double newPrice = Double.parseDouble(input);

                newPrice = Math.round(newPrice * 100) / 100.0;

                shopItem.setPrice(newPrice);

                if (shopManager.updatePrice(shopItem, player)) {

                    player.sendMessage(color(plugin.getI18nString("ui.details.edit.price.success").replace("%.2f", String.valueOf(newPrice))));
                }
            } catch (NumberFormatException e) {
                player.sendMessage(color(plugin.getI18nString("ui.details.edit.price.invalid")));
            }
        });
    }

    private void deleteItem(ShopItem item, Player player) {
        ItemStack itemStack = shopManager.removeItem(currentShop, item.getUuid());
        if (player.getInventory().firstEmpty() == -1) {
            player.getWorld().dropItem(player.getLocation(), itemStack);
            player.sendMessage(color(plugin.getI18nString("ui.details.items.inventory_full")));
        } else {
            player.getInventory().addItem(itemStack);
        }
        player.sendMessage(color(plugin.getI18nString("ui.details.items.success")));
        refresh();
    }

    private void returnToPrevious(Player player) {
        if (currentMode == Mode.EDIT) {
            guiManager.openGuiWithContext(player, GUIType.SHOP_EDIT, currentShop);
        } else {
            guiManager.openGui(player, GUIType.PLAYER_SHOP);
        }
    }

    private void refresh() {
        // 保存当前页码
        int previousPage = currentPage;

        // 重新加载商品
        refreshItems();

        // 如果当前页码超出范围，调整到最后一页
        if (previousPage >= totalPages) {
            currentPage = Math.max(0, totalPages - 1);
            refreshItems();
        }

        // 更新导航按钮
        updateNavigationButtons();
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}