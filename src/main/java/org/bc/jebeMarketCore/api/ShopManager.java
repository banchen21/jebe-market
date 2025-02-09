package org.bc.jebeMarketCore.api;

import org.bc.jebeMarketCore.model.Shop;
import org.jetbrains.annotations.NotNull;

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
     * @param uniqueId 商店所有者的唯一ID
     * @param shopName 商店名称
     * @param shopType 商店类型
     * @return 返回新创建的商店对象
     */
    Shop createShop(@NotNull UUID uniqueId, String shopName, UUID owner, boolean shopType);

    /**
     * 根据商店的UUID获取商店信息
     *
     * @param name 商店的唯一name
     * @return 返回对应的商店对象
     */
    Shop getShop(String name);

    boolean setShop(Shop shop);

    /**
     * 删除指定的商店
     *
     * @param shopUuid 商店的唯一ID
     * @param b        可能用于指定是否进行某种额外操作的标志位
     * @return 如果删除成功返回true，否则返回false
     */
    boolean deleteShop(UUID shopUuid, boolean b);
}
