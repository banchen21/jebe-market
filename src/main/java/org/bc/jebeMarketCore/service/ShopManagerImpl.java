package org.bc.jebeMarketCore.service;

import org.bc.jebeMarketCore.api.ShopManager;
import org.bc.jebeMarketCore.model.Shop;
import org.bc.jebeMarketCore.repository.ShopServiceImpl;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class ShopManagerImpl implements ShopManager {

    ShopServiceImpl shopService;

    public ShopManagerImpl(ShopServiceImpl shopService) {
        this.shopService = shopService;
    }

    @Override
    public List<Shop> getShopsByOwner(UUID playerId, boolean isAdmin) {
        return shopService.getShopsByOwner(playerId, isAdmin);
    }

    @Override
    public List<String> getShopItems(UUID playerId) {
        return List.of();
    }

    @Override
    public Shop createShop(@NotNull UUID uniqueId, String shopName, UUID owner, boolean shopType) {
        Shop shop = new Shop(uniqueId, shopName, owner, shopType);
        if (shopService.createShop(shop)) {
            return shop;
        }
        return null;
    }

    @Override
    public Shop getShop(String name) {
        return shopService.getShop(name);
    }

    @Override
    public boolean updateShopName(UUID uuid, String newName) {
        return false;
    }

    @Override
    public void updateShop(Shop shop) {

    }

    @Override
    public boolean deleteShop(UUID shopUuid, boolean b) {
        return false;
    }
}
