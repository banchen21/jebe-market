package org.bc.jebeMarketCore.dao;

import org.bc.jebeMarketCore.model.Shop;
import org.bc.jebeMarketCore.model.ShopItem;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.UUID;

public interface ShopDao {

    @SqlUpdate("INSERT INTO shops (uuid, name, owner, lore) VALUES (:uuid, :name, :owner,:lore)")
    void insert(
            @Bind("uuid") UUID uuid,
            @Bind("name") String name,
            @Bind("owner") UUID owner,
            @Bind("lore") String lore
    );

    @SqlQuery("SELECT * FROM shops WHERE uuid = :uuid")
    Shop findByUuid(@Bind("uuid") UUID uuid);

    @SqlQuery("SELECT * FROM shops WHERE name = :name")
    Shop findByName(@Bind("name") String name);

    @SqlQuery("SELECT * FROM shops WHERE owner = :owner")
    List<Shop> findByOwner(@Bind("owner") UUID owner);

    @SqlUpdate("UPDATE shops SET name = :name WHERE uuid = :uuid")
    void updateName(@Bind("uuid") UUID uuid, @Bind("name") String name);

    @SqlUpdate("UPDATE shops SET owner = :owner WHERE uuid = :uuid")
    void updateOwner(@Bind("uuid") UUID uuid, @Bind("owner") UUID owner);

    @SqlUpdate("UPDATE shops SET lore = :lore WHERE uuid = :uuid")
    void updateLore(@Bind("uuid") UUID uuid, @Bind("lore") String lore);

    @SqlUpdate("DELETE FROM shops WHERE uuid = :uuid")
    void delete(@Bind("uuid") UUID uuid);

    //    获取所有商店
    @SqlQuery("SELECT * FROM shops")
    List<Shop> getAllShops();


    @SqlUpdate("INSERT INTO shopitems (uuid, shopUuid, price) VALUES (:uuid, :shopUuid, :price)")
    void insert(
            @Bind("uuid") UUID uuid,
            @Bind("shopUuid") UUID shopUuid,
            @Bind("price") double price // 确保数据库的 type 字段是布尔类型（非 BLOB）
    );

    @SqlQuery("SELECT * FROM shopitems WHERE shopuuid = :shopuuid")
    List<ShopItem> getItemsByShop(@Bind("shopuuid") UUID shopuuid);

    @SqlUpdate("DELETE FROM shopitems WHERE shopuuid = :shopuuid AND uuid = :uuid")
    void delete(@Bind("shopuuid") UUID shopuuid, @Bind("uuid") UUID uuid);

    @SqlQuery("SELECT * FROM shopitems WHERE shopuuid = :shopuuid AND uuid = :uuid")
    ShopItem findByUuidAndShopUuid(@Bind("shopuuid") UUID shopuuid, @Bind("uuid") UUID uuid);

    @SqlQuery("SELECT * FROM shopitems WHERE shopuuid= :shopuuid AND uuid = :uuid")
    ShopItem findByUuid(@Bind("shopuuid") UUID shopuuid, @Bind("uuid") UUID uuid);

    //    修改商品价格
    @SqlUpdate("UPDATE shopitems SET price = :price WHERE uuid = :uuid")
    boolean updatePrice(@Bind("uuid") UUID uuid, @Bind("price") double price);
}
