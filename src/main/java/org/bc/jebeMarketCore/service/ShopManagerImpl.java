package org.bc.jebeMarketCore.service;

import org.bc.jebeMarketCore.api.ShopManager;
import org.bc.jebeMarketCore.model.Shop;
import org.bc.jebeMarketCore.model.ShopItem;
import org.bc.jebeMarketCore.repository.ShopServiceImpl;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class ShopManagerImpl implements ShopManager {

    ShopServiceImpl shopService;

    public ShopManagerImpl(ShopServiceImpl shopService) {
        this.shopService = shopService;
    }

    @Override
    public List<Shop> getShopsByOwner(UUID playerId) {
        return shopService.getShopsByOwner(playerId);
    }

    @Override
    public Shop createShop(String shopName, UUID owner) {
        Shop shop = new Shop(shopName, owner);
        if (shopService.createShop(shop)) {
            return shop;
        }
        return null;
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
    public boolean setShop(Shop shop) {
        return shopService.updateShopName(shop) && shopService.updateShopOwner(shop) && shopService.updateShopLore(shop);
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
        ShopItem shopItem = new ShopItem(shopUuid, clone);
        shopItem.setPrice(9999999);
        if (shopService.addItem(shopItem)) {
            return shopItem;
        } else {
            return null;
        }
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
    public boolean updateItem(ShopItem shopItem) {
        return shopService.updatePrice(shopItem);
    }
}
