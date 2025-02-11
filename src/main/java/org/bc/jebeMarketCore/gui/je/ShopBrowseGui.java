package org.bc.jebeMarketCore.gui.je;

import org.bc.jebeMarketCore.JebeMarket;
import org.bc.jebeMarketCore.api.ShopManager;
import org.bc.jebeMarketCore.model.Shop;
import org.bc.jebeMarketCore.utils.PlayerHeadManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.UUID;

/**
 * 所有商铺GUI
 */
public class ShopBrowseGui implements InventoryHolder, Listener {
    private Inventory inventory;
    private final JebeMarket plugin;
    private final ShopManager shopManager;
    private final PlayerHeadManager headManager;
    private final ShopPlayerGui shopPlayerGui;
    private int currentPage = 1;

    // 分页相关常量
    private static final int ITEMS_PER_PAGE = 45;
    private static final int PREV_PAGE_SLOT = 45;
    private static final int NEXT_PAGE_SLOT = 53;
    private static final int PAGE_INFO_SLOT = 49;

    public ShopBrowseGui(JebeMarket plugin, ShopManager shopManager, PlayerHeadManager headManager, ShopPlayerGui shopPlayerGui) {
        this.plugin = plugin;
        this.shopManager = shopManager;
        this.headManager = headManager;
        this.shopPlayerGui = shopPlayerGui;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void initializeLayout() {
        // 填充边框
        ItemStack border = createBorderItem();
        for (int i : new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 45, 46, 47, 48, 49, 50, 51, 52, 53}) {
            inventory.setItem(i, border);
        }

        // 分页按钮
        inventory.setItem(PREV_PAGE_SLOT, createNavigationItem(Material.ARROW, "§a上一页"));
        inventory.setItem(NEXT_PAGE_SLOT, createNavigationItem(Material.ARROW, "§a下一页"));
        updatePageInfo(0);

        // 根据当前页码设置分页按钮的可见性
        setPaginationVisibility();
    }

    private void loadPage() {
        // 清空旧物品（保留边框）
        for (int i = 9; i <= 44; i++) {
            inventory.setItem(i, null);
        }

        List<Shop> allShops = shopManager.getShops();
        int totalPages = (int) Math.ceil((double) allShops.size() / ITEMS_PER_PAGE);

        // 确保 currentPage 在有效范围内
        currentPage = Math.max(1, Math.min(currentPage, totalPages));

        int start = (currentPage - 1) * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, allShops.size());
        List<Shop> pageShops = allShops.subList(Math.max(0, start), end);

        // 填充物品
        int slot = 9;
        for (Shop shop : pageShops) {
            if (slot > 44) break;
            inventory.setItem(slot, createShopItem(shop));
            slot++;
        }

        updatePageInfo(allShops.size());

        // 根据当前页码设置分页按钮的可见性
        setPaginationVisibility();
    }

    private void setPaginationVisibility() {
        List<Shop> allShops = shopManager.getShops();
        int totalPages = (int) Math.ceil((double) allShops.size() / ITEMS_PER_PAGE);

        // 如果是第一页，隐藏“上一页”按钮
        if (currentPage == 1) {
            inventory.setItem(PREV_PAGE_SLOT, null);
        } else {
            inventory.setItem(PREV_PAGE_SLOT, createNavigationItem(Material.ARROW, "§a上一页"));
        }

        // 如果是最后一页，隐藏“下一页”按钮
        if (currentPage == totalPages) {
            inventory.setItem(NEXT_PAGE_SLOT, null);
        } else {
            inventory.setItem(NEXT_PAGE_SLOT, createNavigationItem(Material.ARROW, "§a下一页"));
        }
    }

    private ItemStack createShopItem(Shop shop) {
        ItemStack item = headManager.getPlayerHead(shop.getOwner());
        ItemMeta meta = item.getItemMeta();

        // 设置显示信息
        meta.setDisplayName("§e" + shop.getName());
        meta.setLore(List.of(
                "§7店主: " + Bukkit.getOfflinePlayer(shop.getOwner()).getName(),
                "§7商品数量: " + shopManager.getItemCount(shop.getUuid()),
                "§a点击查看商品"
        ));

        // 存储商店UUID到NBT
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(new NamespacedKey(plugin, "shop_uuid"),
                PersistentDataType.STRING,
                shop.getUuid().toString());

        item.setItemMeta(meta);
        return item;
    }

    private void updatePageInfo(int totalShops) {
        int totalPages = (int) Math.ceil((double) totalShops / ITEMS_PER_PAGE);

        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta meta = infoItem.getItemMeta();
        meta.setDisplayName("§6页面信息");
        meta.setLore(List.of(
                "§7当前页数: " + currentPage + "/" + totalPages,
                "§7总商店数: " + totalShops,
                "§7每页显示: " + ITEMS_PER_PAGE + " 个商店"
        ));
        infoItem.setItemMeta(meta);
        inventory.setItem(PAGE_INFO_SLOT, infoItem);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() != this) return;
        event.setCancelled(true);

        int slot = event.getRawSlot();
        Player player = (Player) event.getWhoClicked();

        List<Shop> allShops = shopManager.getShops();
        int totalPages = (int) Math.ceil((double) allShops.size() / ITEMS_PER_PAGE);

        switch (slot) {
            case PREV_PAGE_SLOT:
                if (currentPage > 1) {
                    currentPage--;
                    loadPage();
                }
                break;

            case NEXT_PAGE_SLOT:
                if (currentPage < totalPages) {
                    currentPage++;
                    loadPage();
                }
                break;

            default:
                if (slot >= 9 && slot <= 44) {
                    handleShopClick(event.getCurrentItem(), player);
                } else {
                    player.closeInventory();
                }
        }
    }

    private void handleShopClick(ItemStack item, Player player) {
        if (item == null || !item.hasItemMeta()) return;

        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, "shop_uuid");

        if (pdc.has(key, PersistentDataType.STRING)) {
            UUID shopUuid = UUID.fromString(pdc.get(key, PersistentDataType.STRING));
            Shop shop = shopManager.getShop(shopUuid);
            openShopDetail(shop, player);
        }
    }

    private void openShopDetail(Shop shop, Player player) {
        // 需要实现商店详情界面
        player.closeInventory();
        shopPlayerGui.open(shop, player);
        player.sendMessage("§a正在打开商店: " + shop.getName());
    }

    // 辅助方法
    private ItemStack createBorderItem() {
        ItemStack item = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createNavigationItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void open(Player player) {
        inventory = Bukkit.createInventory(this, 54, "§b§l全服商店");
        initializeLayout();
        loadPage();
        player.openInventory(inventory);
    }
}