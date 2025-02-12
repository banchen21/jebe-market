package org.bc.jebeMarketCore.repository;

import org.bc.jebeMarketCore.model.ShopItem;
import org.bc.jebeMarketCore.model.Shop;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public interface ShopRepository {
    boolean createShop(Shop shop);

    //    修改商店name
    boolean updateShopName(Shop shop);

    //    修改商店所有者uuid
    boolean updateShopOwner(Shop shop);

    //    修改商店lore
    boolean updateShopLore(Shop shop);

    //    查询商店
    Shop findByUuid(UUID uuid);

    Shop findByName(String name);

    //    查询玩家拥有的商店
    List<Shop> getShopsByOwner(UUID playerId);

    //    删除商店
    boolean deleteShop(UUID uuid);

    boolean addItem(ShopItem shopItem);

    List<ShopItem> getItemsByShop(UUID shopUuid);

    ItemStack removeItem(Shop shop, UUID itemId);

    ShopItem getItemById(UUID shopUuid, UUID itemId);

    boolean updatePrice(ShopItem shopItem);

    List<Shop> getAllShops();

    boolean updateItemStack(ShopItem shopItem);
}
