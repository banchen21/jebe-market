package org.bc.jebeMarketCore.dao;

import org.bc.jebeMarketCore.model.Item;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.UUID;

public interface ItemDao {

    @SqlUpdate("INSERT INTO items (uuid, shopUuid, material, price, amount) VALUES (:uuid, :shopUuid, :material, :price, :amount)")
    void insert(
            @Bind("uuid") UUID uuid,
            @Bind("shopUuid") UUID shopUuid,
            @Bind("material") String material,
            @Bind("price") double price,  // 确保数据库的 type 字段是布尔类型（非 BLOB）
            @Bind("amount") int amount
    );

    @SqlQuery("SELECT * FROM items WHERE shopuuid = :shopuuid")
    List<Item> getItemsByShop(@Bind("shopuuid") UUID shopuuid);

    @SqlUpdate("DELETE FROM items WHERE shopuuid = :shopuuid AND uuid = :uuid")
    void delete(@Bind("shopuuid") UUID shopuuid, @Bind("uuid") UUID uuid);

    @SqlQuery("SELECT * FROM items WHERE shopuuid = :shopuuid AND uuid = :uuid")
    Item findByUuidAndShopUuid(@Bind("shopuuid") UUID shopuuid, @Bind("uuid") UUID uuid);

    @SqlQuery("SELECT * FROM items WHERE shopuuid= :shopuuid AND uuid = :uuid")
    Item findByUuid(@Bind("shopuuid") UUID shopuuid, @Bind("uuid") UUID uuid);

    //    修改商品价格
    @SqlUpdate("UPDATE items SET price = :price WHERE uuid = :uuid")
    boolean updatePrice(@Bind("uuid") UUID uuid, @Bind("price") double price);

//    修改商品数量
    @SqlUpdate("UPDATE items SET amount = :amount WHERE uuid = :uuid")
    boolean updateAmount(@Bind("uuid") UUID uuid, @Bind("amount") int amount);
}
