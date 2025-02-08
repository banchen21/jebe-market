package org.bc.jebeMarketCore.repository;

import org.bc.jebeMarketCore.model.Shop;

import java.util.List;
import java.util.UUID;

public class ShopServiceImpl implements ShopRepository {

    private final ShopRepository shopRepository;

    public ShopServiceImpl(ShopRepository shopRepository) {
        this.shopRepository = shopRepository;
    }

    @Override
    public boolean createShop(Shop shop) {
        return shopRepository.createShop(shop);
    }

    @Override
    public boolean updateShopName(Shop shop) {
        return false;
    }

    @Override
    public boolean updateShopOwner(Shop shop) {
        return false;
    }

    @Override
    public boolean updateShopType(Shop shop) {
        return false;
    }

    @Override
    public boolean updateShopLore(Shop shop) {
        return false;
    }

    @Override
    public Shop getShop(String name) {
        return shopRepository.getShop(name);
    }

    @Override
    public List<Shop> getShopsByOwner(UUID playerId, boolean isAdmin) {
        return shopRepository.getShopsByOwner(playerId, isAdmin);
    }

    @Override
    public boolean deleteShop(UUID uuid) {
        return false;
    }
}


