package org.bc.jebeMarketCore.dao;

import org.bc.jebeMarketCore.model.Shop;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.UUID;

public interface ShopDao {

    @SqlUpdate("INSERT INTO shops (uuid, name, owner, type, lore) VALUES (:uuid, :name, :owner, :type, :lore)")
    void upsert(
            @Bind("uuid") UUID uuid,
            @Bind("name") String name,
            @Bind("owner") UUID owner,
            @Bind("type") boolean shopType,  // 确保数据库的 type 字段是布尔类型（非 BLOB）
            @Bind("lore") String lore
    );

    @SqlQuery("SELECT * FROM shops WHERE name = :name")
    Shop findByUuid(@Bind("name") String uuid);

    @SqlQuery("SELECT * FROM shops WHERE owner = :owner")
    List<Shop> findByOwner(@Bind("owner") UUID owner);
}
