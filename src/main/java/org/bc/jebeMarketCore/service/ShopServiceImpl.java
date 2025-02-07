package org.bc.jebeMarketCore.service;

import org.bc.jebeMarketCore.api.ShopManager;
import org.bc.jebeMarketCore.model.Shop;
import org.bc.jebeMarketCore.repository.ShopRepository;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class ShopServiceImpl implements ShopManager {
    private final ShopRepository shopRepository;

    public ShopServiceImpl(ShopRepository shopRepository) {
        this.shopRepository = shopRepository;
    }

    @Override
    public Shop createShop(UUID owner, String name){

        Shop shop = new Shop(owner, name);
        shopRepository.AddShop(shop);
        return shop;
    }

    @Override
    public boolean deleteShop(UUID shopId) {
        return shopRepository.delete(shopId);
    }

    @Override
    public boolean updateShopName(UUID shopId, String name) {
        if (this.getShopById(shopId) == null) {
            return false;
        } else {
            try {
                shopRepository.findById(shopId).ifPresent(shop -> {
                    shop.setName(name);
                    shopRepository.AddShop(shop);
                });
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            return true;
        }
    }

    @Override
    public boolean updateShopDescription(UUID shopId, List<String> description) {
        if (this.getShopById(shopId) == null) {
            return false;
        } else {
            try {
                shopRepository.findById(shopId).ifPresent(shop -> {
                    shop.setLore(description);
                    shopRepository.AddShop(shop);
                });
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            return true;
        }
    }

    @Override
    public boolean updateShopType(UUID shopId, boolean type) {
        if (this.getShopById(shopId) == null) {
            return false;
        } else {
            try {
                shopRepository.findById(shopId).ifPresent(shop -> {
                    shop.setType(type);
                    shopRepository.AddShop(shop);
                });
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            return true;
        }
    }

    @Override
    public Shop getShopById(UUID shopId) {
        try {
            return shopRepository.findById(shopId).orElse(null);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // 必须实现接口所有方法
    @Override
    public List<Shop> getPlayerShops(UUID playerId) {
        try {
            return shopRepository.findByOwner(playerId);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}