package org.bc.jebeMarketCore.repository;

import org.bc.jebeMarketCore.model.Shop;

import java.util.List;
import java.util.UUID;

public interface ShopRepository {
    boolean createShop(Shop shop);

    //    修改商店name
    boolean updateShopName(Shop shop);

    //    修改商店所有者uuid
    boolean updateShopOwner(Shop shop);

    //    修改商店类型
    boolean updateShopType(Shop shop);

    //    修改商店lore
    boolean updateShopLore(Shop shop);

    //    查询商店
    Shop findByUuid(String name);

    Shop findByName(String name);

    //    查询玩家拥有的商店
    List<Shop> getShopsByOwner(UUID playerId);

    //    删除商店
    boolean deleteShop(UUID uuid);
}
