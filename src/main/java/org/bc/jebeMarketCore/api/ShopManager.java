package org.bc.jebeMarketCore.api;

import org.bc.jebeMarketCore.model.ShopItem;
import org.bc.jebeMarketCore.model.Shop;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

/**
 * ShopManager接口定义了管理商店的相关方法
 * 它提供了对商店信息的查询、创建、更新和删除操作
 */
public interface ShopManager {
    /**
     * 根据店主的ID获取商店列表
     *
     * @param playerId 玩家的唯一ID
     * @return 返回属于指定玩家的商店列表
     */
    List<Shop> getShopsByOwner(UUID playerId);


    /**
     * 创建一个新的商店
     *
     * @param shopName 商店名称
     * @return 返回新创建的商店对象
     */
    Shop createShop(String shopName, UUID owner);

    /**
     * 根据商店的UUID获取商店信息
     *
     * @param name 商店的唯一name
     * @return 返回对应的商店对象
     */
    Shop getShop(String name);

    Shop getShop(UUID uuid);

    boolean updateShopName(Shop shop,String newName,Player player);

    boolean updateShopOwner(Shop shop,Player newOwner);

    boolean updateShopLore(Shop shop);

    /**
     * 删除指定的商店
     *
     * @param shopUuid 商店的唯一ID
     * @return 如果删除成功返回true，否则返回false
     */
    boolean deleteShop(UUID shopUuid);


    List<Shop> getShops();

    int getItemCount(UUID shopUuid);

    boolean addHandItem(ShopItem shopIte, Player player);

    void addInventoryItem(UUID shopUuid, Player player);

    ItemStack removeItem(Shop shop, UUID itemId);

    List<ShopItem> getItems(UUID shopUuid);

    ShopItem getItem(UUID shopUuid, UUID itemId);

    boolean updatePrice(ShopItem shopItem,Player player);

    //    修改物品数量
    boolean updateItemStack(ShopItem shopItem);
}
