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
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 商品详情界面（支持购买/编辑模式）
 */
@Slf4j
public class ShopDetailsGui extends GuiManager.BaseGUI {

    @Override
    public @NotNull Inventory getInventory() {
        return null;
    }

    public enum Mode {
        BUY,  // 购买模式
        EDIT   // 编辑模式
    }

    // 布局常量
    private static final int[] BORDER_SLOTS = {0, 1, 2, 3, 4, 5, 6, 7, 8, 45, 46, 47, 48, 49, 50, 51, 52, 53};
    private static final int BACK_SLOT = 49;
    private static final int EDIT_SLOT = 53;

    // 依赖服务
    private final ShopManager shopManager;
    private final GuiManager guiManager;
    private final PlayerInputHandler inputHandler;

    // 状态管理
    private Mode currentMode = Mode.BUY;
    private Shop currentShop;
    private List<ShopItem> shopItemList = new ArrayList<>();

    public ShopDetailsGui(JebeMarket plugin,
                          ShopManager shopManager,
                          GuiManager guiManager, PlayerInputHandler inputHandler) {
        super(plugin);
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
            log.info("ShopDetailsGui.openWithContext: " + currentShop.getName() + " " + currentMode);
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

        // 加载商品
        loadShopItems();

        // 功能按钮
        inventory.setItem(BACK_SLOT, ItemBuilder.of(Material.ARROW)
                .name("§a返回")
                .build());

        if (currentMode == Mode.EDIT) {
            inventory.setItem(EDIT_SLOT, ItemBuilder.of(Material.ANVIL)
                    .name("§e批量管理")
                    .build());
        }
    }

    private String getTitle() {
        return currentMode == Mode.BUY ?
                "§b" + currentShop.getName() :
                "§6管理 - " + currentShop.getName();
    }

    private void loadShopItems() {
        shopItemList = shopManager.getItems(currentShop.getUuid());

        for (int i = 0; i < shopItemList.size() && i < 45; i++) {
            ShopItem item = shopItemList.get(i);
            inventory.setItem(i + 9, buildItemDisplay(item)); // 从第9格开始放置
        }
    }

    private ItemStack buildItemDisplay(ShopItem shopItem) {
        ItemStack item = shopItem.getItemStack().clone();
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore() != null ? meta.getLore() : new ArrayList<>();

        // 添加价格信息
        lore.add("§7单价: §a" + shopItem.getPrice());
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

        // 处理功能按钮
        if (slot == BACK_SLOT) {
            returnToPrevious(player);
            return;
        }
        if (slot == EDIT_SLOT && currentMode == Mode.EDIT) {
            handleBulkEdit(player);
            return;
        }

        // 处理商品点击（9-44为商品区域）
        if (slot >= 9 && slot <= 44) {
            int index = slot - 9;
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
                purchaseItem(item, 1, player);
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

    private void purchaseItem(ShopItem item, int amount, Player player) {
        // TODO: 实现购买逻辑
        player.sendMessage("§a购买成功：" + amount + "个");
        refresh();
    }

    private void openBulkPurchase(ShopItem item, Player player) {
        // TODO: 打开批量购买界面
        player.sendMessage("§e批量购买功能开发中...");
    }

    private void modifyPrice(ShopItem item, Player player) {
        player.closeInventory();
        inputHandler.requestInput(player, "请输入新价格（数字）",
                input -> {
                    try {
                        double newPrice = Double.parseDouble(input);
                        item.setPrice(newPrice);
                        shopManager.updateItem(item);
                        player.sendMessage("§a价格已更新！");
                        refresh();
                    } catch (NumberFormatException e) {
                        player.sendMessage("§c请输入有效的数字");
                    }
                },
                30
        );
    }

    private void deleteItem(ShopItem item, Player player) {
        shopManager.removeItem(currentShop, item.getUuid());
        player.sendMessage("§c商品已移除");
        refresh();
    }

    private void handleBulkEdit(Player player) {
        // TODO: 批量管理实现
        player.sendMessage("§e批量管理功能开发中...");
    }

    private void returnToPrevious(Player player) {
        if (currentMode == Mode.EDIT) {
            guiManager.openGuiWithContext(player,
                    GUIType.MY_SHOP,
                    player.getUniqueId()
            );
        } else {
            guiManager.openGui(player, GUIType.MY_SHOP);
        }
    }

    private void refresh() {
        loadShopItems();
        initializeUI();
    }
}