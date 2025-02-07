package org.bc.jebeMarketCore.api;

import org.bc.jebeMarketCore.model.ShopItem;

import java.util.Optional;
import java.util.UUID;

public interface ItemManager {
    void registerItem(ShopItem item);
    void updateItemPrice(UUID itemId, double price);
    void updateItemOnSaleStatus(UUID itemId, boolean onSale);
    void updateItemShop(UUID itemId, UUID shopId);
    Optional<ShopItem> getItemById(UUID itemId);
}