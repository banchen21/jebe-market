package org.bc.jebeMarketCore.repository;

import org.bc.jebeMarketCore.model.ShopItem;
import org.bc.jebeMarketCore.model.Shop;
import org.bukkit.inventory.ItemStack;

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
    public boolean updateShopLore(Shop shop) {
        return shopRepository.updateShopLore(shop);
    }

    @Override
    public Shop findByUuid(UUID uuid) {
        return shopRepository.findByUuid(uuid);
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

    @Override
    public boolean addItem(ShopItem shopItem) {
        return shopRepository.addItem(shopItem);
    }

    @Override
    public List<ShopItem> getItemsByShop(UUID shopUuid) {
        return shopRepository.getItemsByShop(shopUuid);
    }

    @Override
    public ItemStack removeItem(Shop shop, UUID itemId) {
        return shopRepository.removeItem(shop, itemId);
    }

    @Override
    public ShopItem getItemById(UUID shopuuid, UUID itemId) {
        return shopRepository.getItemById(shopuuid, itemId);
    }

    @Override
    public boolean updatePrice(ShopItem shopItem) {
        return shopRepository.updatePrice(shopItem);
    }

    @Override
    public List<Shop> getAllShops() {
        return shopRepository.getAllShops();
    }
}


