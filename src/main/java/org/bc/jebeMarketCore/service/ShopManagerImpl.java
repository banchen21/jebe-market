package org.bc.jebeMarketCore.service;

import org.bc.jebeMarketCore.JebeMarket;
import org.bc.jebeMarketCore.api.ShopManager;
import org.bc.jebeMarketCore.model.Shop;
import org.bc.jebeMarketCore.model.ShopItem;
import org.bc.jebeMarketCore.repository.ShopServiceImpl;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

import static org.bc.jebeMarketCore.utils.MessageUtils.color;


public class ShopManagerImpl implements ShopManager {

    ShopServiceImpl shopService;
    private final JebeMarket plugin;

    public ShopManagerImpl(ShopServiceImpl shopService, JebeMarket plugin) {
        this.shopService = shopService;
        this.plugin = plugin;
    }

    @Override
    public List<Shop> getShopsByOwner(UUID playerId) {
        return shopService.getShopsByOwner(playerId);
    }

    @Override
    public Shop createShop(String shopName, UUID owner) {
        Shop shop = new Shop(shopName, owner);

        Player player = plugin.getServer().getPlayer(owner);
        assert player != null;
        if (isShopNameBanned(plugin.getConfig().getStringList("item_ban_words"), shopName)) {
            player.sendMessage("§c商店创建失败！");
            player.sendMessage("§c商店名称包含违规字符！");
            return null;
        }
//        获取玩家商铺数量
        if (shopService.getShopsByOwner(owner).size() >= plugin.getConfig().getInt("shop_create_limit")) {
            player.sendMessage("§c商店创建失败！");
            player.sendMessage("§c商店创建数量已达上限！");
            return null;
        }
        if (plugin.getLabor_econ().has(player, plugin.getConfig().getInt("shop_create_cost")) && shopService.createShop(shop)) {
            plugin.getLabor_econ().withdrawPlayer(player, plugin.getConfig().getInt("shop_create_cost"));
            player.sendMessage("§a商店创建成功！");
            player.sendMessage("§a商店名称：" + shopName);
            player.sendMessage("§a商店创建费用：" + plugin.getConfig().getInt("shop_create_cost") + plugin.getLabor_econ().getName());
            return shop;
        }
        return null;
    }

    //    禁止使用的商品名称关键词
    public boolean isShopNameBanned(List<String> stringList, String shopName) {
        for (String s : stringList) {
            if (s.equals(shopName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Shop getShop(String name) {
        return shopService.findByName(name);
    }

    @Override
    public Shop getShop(UUID uuid) {
        return shopService.findByUuid(uuid);
    }

    @Override
    public boolean updateShopName(Shop shop) {
        Player player = plugin.getServer().getPlayer(shop.getOwner());
        assert player != null;
        if (plugin.getLabor_econ().has(player, plugin.getConfig().getInt("shop_rename_cost"))) {
            if (shopService.updateShopName(shop)) {
                plugin.getLabor_econ().withdrawPlayer(player, plugin.getConfig().getInt("shop_rename_cost"));
                return true;
            }
        }
        return shopService.updateShopName(shop);
    }

    @Override
    public boolean updateShopOwner(Shop shop) {
        Player player = plugin.getServer().getPlayer(shop.getOwner());
        assert player != null;
        List<ShopItem> shopItems = shopService.getItemsByShop(shop.getUuid());
        if (shopItems.size() >= plugin.getConfig().getInt("shop_create_limit")) {
            player.sendMessage("§c商店添加抵达上限");
            return false;
        }
        if (plugin.getLabor_econ().has(player, plugin.getConfig().getInt("shop_transfer_cost"))) {
            if (shopService.updateShopOwner(shop)) {
                plugin.getLabor_econ().withdrawPlayer(player, plugin.getConfig().getInt("shop_transfer_cost"));
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean updateShopLore(Shop shop) {
        Player player = plugin.getServer().getPlayer(shop.getOwner());
        assert player != null;
        if (plugin.getLabor_econ().has(player, plugin.getConfig().getInt("shop_desc_cost"))) {
            if (shopService.updateShopLore(shop)) {
                plugin.getLabor_econ().withdrawPlayer(player, plugin.getConfig().getInt("shop_desc_cost"));
                return true;
            }
        }
        return false;
    }


    @Override
    public boolean deleteShop(UUID shopUuid, boolean b) {
        return shopService.deleteShop(shopUuid);
    }

    @Override
    public List<Shop> getShops() {
        return shopService.getAllShops();
    }

    @Override
    public int getItemCount(UUID shopUuid) {
        return shopService.getItemsByShop(shopUuid).size();
    }

    @Override
    public ShopItem addItem(UUID shopUuid, @NotNull ItemStack clone) {

        Shop shop = shopService.findByUuid(shopUuid);
        Player player = plugin.getServer().getPlayer(shop.getOwner());
        assert player != null;
        List<ShopItem> shopItems = shopService.getItemsByShop(shopUuid);
        if (shopItems.size() >= plugin.getConfig().getInt("shop_item_limit")) {
            player.sendMessage("§c商店添加抵达上限");
            return null;
        }
        if (plugin.getLabor_econ().has(player, plugin.getConfig().getInt("item_list_cost"))) {
            ShopItem shopItem = new ShopItem(shopUuid, clone);
            shopItem.setPrice(9999999);
            if (shopService.addItem(shopItem)) {
                plugin.getLabor_econ().withdrawPlayer(player, plugin.getConfig().getInt("item_list_cost"));
                return shopItem;
            }
        }
        return null;
    }

    @Override
    public void addHandItem(UUID shopUuid, Player player) {
        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem.getAmount() == 0) {
            player.sendMessage(color("&c请手持要上架的商品"));
            return;
        }
        ShopItem shopItem = new ShopItem(shopUuid, handItem.clone());
        if (shopService.addItem(shopItem)) {
            handItem.setAmount(0);
            player.sendMessage(color(String.format("&a成功上架商品 %s", shopItem.getUuid().toString())));
        } else {
            player.sendMessage(color("&c添加商品失败"));
        }
    }

    @Override
    public void addInventoryItem(UUID shopUuid, Player player) {
        // 如果玩家背包为空就返回
        if (player.getInventory().isEmpty()) {
            player.sendMessage(color("&c背包为空"));
            return;
        }
        int count = 0;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack != null && !stack.getType().isAir()) {
                ShopItem shopItem = new ShopItem(shopUuid, stack.clone());
                if (shopService.addItem(shopItem)) {
                    stack.setAmount(0);
                    player.sendMessage(color(String.format("&a成功上架 %s", shopItem.getUuid().toString())));
                    count++;
                } else {
                    player.sendMessage(color("&c添加商品失败"));
                }
            }
        }
        player.sendMessage(color(String.format("&a成功上架 %d 种物品", count)));
    }

    @Override
    public ItemStack removeItem(Shop shop, UUID itemId) {
        return shopService.removeItem(shop, itemId);
    }

    @Override
    public List<ShopItem> getItems(UUID shopUuid) {
        return shopService.getItemsByShop(shopUuid);
    }

    @Override
    public ShopItem getItem(UUID shopUuid, UUID itemId) {
        return shopService.getItemById(shopUuid, itemId);
    }

    @Override
    public boolean updatePrice(ShopItem shopItem) {
        Shop shop = shopService.findByUuid(shopItem.getShopuuid());
        Player player = plugin.getServer().getPlayer(shop.getOwner());
        assert player != null;
        if (shopItem.getPrice() <= plugin.getConfig().getInt("max_price")) {
            return shopService.updatePrice(shopItem);
        } else {
            player.sendMessage("§c价格超出最大价格");
            return false;
        }
    }

    @Override
    public boolean updateItemStack(ShopItem shopItem) {
        return shopService.updateItemStack(shopItem);
    }
}
