// src/main/java/org/bc/jebeMarketCore/api/ShopManager.java
package org.bc.jebeMarketCore.api;

import org.bc.jebeMarketCore.model.Shop;
import java.util.UUID;
import java.util.List;

public interface ShopManager {
    Shop createShop(UUID owner, String name);
    boolean deleteShop(UUID shopId);
    boolean updateShopName(UUID shopId, String name);
    boolean updateShopDescription(UUID shopId, List<String> description);
    boolean updateShopType(UUID shopId, boolean type);
    Shop getShopById(UUID shopId);
    List<Shop> getPlayerShops(UUID playerId);

}