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
        return shopRepository.updateShopName(shop);
    }

    @Override
    public boolean updateShopOwner(Shop shop) {
        return shopRepository.updateShopOwner(shop);
    }

    @Override
    public boolean updateShopType(Shop shop) {
        return shopRepository.updateShopType(shop);
    }

    @Override
    public boolean updateShopLore(Shop shop) {
        return shopRepository.updateShopLore(shop);
    }

    @Override
    public Shop findByUuid(String name) {
        return shopRepository.findByUuid(name);
    }

    @Override
    public Shop findByName(String name) {
        return shopRepository.findByName(name);
    }

    @Override
    public List<Shop> getShopsByOwner(UUID playerId) {
        return shopRepository.getShopsByOwner(playerId);
    }

    @Override
    public boolean deleteShop(UUID uuid) {
        return shopRepository.deleteShop(uuid);
    }
}


