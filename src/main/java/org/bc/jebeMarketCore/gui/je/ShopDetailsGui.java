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
    private static final int[] BORDER_SLOTS = {1, 2, 3, 4, 5, 6, 7, 8, 45, 46, 47, 48, 49, 50, 51, 52}; // 边框位置
    private static final int PREV_PAGE_SLOT = 48; // 上一页按钮
    private static final int NEXT_PAGE_SLOT = 50; // 下一页按钮
    private static final int PAGE_INFO_SLOT = 49; // 页面信息
    private static final int EDIT_SLOT = 53;      // 批量管理按钮
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

    public ShopDetailsGui(JebeMarket plugin,
                          ShopManager shopManager,
                          GuiManager guiManager,
                          PlayerInputHandler inputHandler) {
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

        // 填充边框
        ItemStack border = ItemBuilder.of(Material.BLUE_STAINED_GLASS_PANE)
                .name(" ")
                .build();
        Arrays.stream(BORDER_SLOTS).forEach(slot -> inventory.setItem(slot, border));

        // 返回按钮
        inventory.setItem(BACK_SLOT, ItemBuilder.of(Material.BARRIER)
                .name("§c返回")
                .build());

        // 编辑模式的批量管理按钮
        if (currentMode == Mode.EDIT) {
            inventory.setItem(EDIT_SLOT, ItemBuilder.of(Material.ANVIL)
                    .name("§e批量管理")
                    .build());
        } else {
            inventory.setItem(EDIT_SLOT, ItemBuilder.of(Material.BLUE_STAINED_GLASS_PANE)
                    .name(" ")
                    .build());
        }

        // 加载商品和翻页按钮
        refreshItems();
        updateNavigationButtons();
    }

    private String getTitle() {
        return currentMode == Mode.BUY ?
                "§b" + currentShop.getName() :
                "§6管理 - " + currentShop.getName();
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
        // 更新页面信息
        inventory.setItem(PAGE_INFO_SLOT, ItemBuilder.of(Material.PAPER)
                .name("§f第 " + (currentPage + 1) + "/" + (totalPages == 0 ? 1 : totalPages) + " 页")
                .build());

        // 上一页按钮
        if (currentPage > 0) {
            inventory.setItem(PREV_PAGE_SLOT, ItemBuilder.of(Material.ARROW)
                    .name("§a上一页")
                    .build());
        } else {
            inventory.setItem(PREV_PAGE_SLOT, ItemBuilder.of(Material.RED_STAINED_GLASS_PANE)
                    .name("§c已是第一页")
                    .build());
        }

        // 下一页按钮
        if (currentPage < totalPages - 1) {
            inventory.setItem(NEXT_PAGE_SLOT, ItemBuilder.of(Material.ARROW)
                    .name("§a下一页")
                    .build());
        } else {
            inventory.setItem(NEXT_PAGE_SLOT, ItemBuilder.of(Material.RED_STAINED_GLASS_PANE)
                    .name("§c已是最后一页")
                    .build());
        }
    }

    private ItemStack buildItemDisplay(ShopItem shopItem) {
        ItemStack item = shopItem.getItemStack().clone();
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore() != null ? meta.getLore() : new ArrayList<>();

        // 添加价格信息
        lore.add("§7单价: §a" + new DecimalFormat("#.00").format(shopItem.getPrice()));
        lore.add("§7库存: §a" + item.getAmount());

        // 模式相关操作提示
        if (currentMode == Mode.BUY) {
            lore.add("§a左键购买1个");
            lore.add("§6右键查看批量购买");
        } else {
            lore.add("§e左键修改价格");
            lore.add("§c右键移除商品");
        }

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

        // 处理批量管理按钮
        if (slot == EDIT_SLOT && currentMode == Mode.EDIT) {
            handleBulkEdit(player);
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
            player.sendMessage("§c你没有权限修改此商品");
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

    private void purchaseItem(ShopItem item, Player player) {
        ItemStack itemStack = item.getItemStack();
        if (itemStack.getAmount() >= 1 && plugin.getLabor_econ().has(player, item.getPrice())) {
            itemStack.setAmount(itemStack.getAmount() - 1);
            if (itemStack.getAmount() == 0) {
                Shop shop = shopManager.getShop(currentShop.getUuid());
                shopManager.removeItem(shop, item.getUuid());
            } else {
                item.setItemStack(itemStack);
                shopManager.updateItemStack(item);
            }
            plugin.getLabor_econ().withdrawPlayer(player, item.getPrice());
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(currentShop.getOwner());
            plugin.getLabor_econ().depositPlayer(offlinePlayer.getPlayer(), item.getPrice());

            ItemStack givePlayerItem = itemStack.clone();
            givePlayerItem.setAmount(1);
            if (player.getInventory().firstEmpty() == -1) {
                player.getWorld().dropItem(player.getLocation(), givePlayerItem);
                player.sendMessage(color("&c你的背包已满，商品已生成为掉落物"));
            } else {
                player.getInventory().addItem(givePlayerItem);
            }
            player.sendMessage("§a购买成功：1个");
            refresh();
        } else {
            player.sendMessage("§c你没有足够的钱购买此商品");
        }
    }

    private void openBulkPurchase(ShopItem item, Player player) {
        player.sendMessage("§e批量购买功能开发中...");
    }

    private void modifyPrice(ShopItem item, Player player) {
        player.closeInventory();
        inputHandler.requestInput(player, "请输入新价格（数字）",
                input -> {
                    try {
                        double newPrice = Double.parseDouble(input);
                        if (newPrice <= 0 || newPrice > plugin.getConfig().getDouble("max_price")) {
                            player.sendMessage("§c价格必须大于0且小于" + plugin.getConfig().getDouble("max_price"));
                            return;
                        }
                        item.setPrice(newPrice);
                        if (shopManager.updatePrice(item)) {
                            player.sendMessage("§a价格已更新！");
                        } else {
                            player.sendMessage("§c更新失败，请重试");
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage("§c请输入有效的数字");
                    }
                },
                30
        );
    }

    private void deleteItem(ShopItem item, Player player) {
        ItemStack itemStack = shopManager.removeItem(currentShop, item.getUuid());
        if (player.getInventory().firstEmpty() == -1) {
            player.getWorld().dropItem(player.getLocation(), itemStack);
            player.sendMessage(color("&c你的背包已满，商品已生成为掉落物"));
        } else {
            player.getInventory().addItem(itemStack);
        }
        player.sendMessage("§c商品已移除");
        refresh();
    }

    private void handleBulkEdit(Player player) {
        player.sendMessage("§e批量管理功能开发中...");
    }

    private void returnToPrevious(Player player) {
        if (currentMode == Mode.EDIT) {
            guiManager.openGuiWithContext(player,
                    GUIType.SHOP_EDIT,
                    currentShop);
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