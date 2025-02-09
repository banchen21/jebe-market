package org.bc.jebeMarketCore.dao;

import org.bc.jebeMarketCore.model.Shop;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.UUID;

public interface ShopDao {

    @SqlUpdate("INSERT INTO shops (uuid, name, owner, type, lore) VALUES (:uuid, :name, :owner, :type, :lore)")
    void insert(
            @Bind("uuid") UUID uuid,
            @Bind("name") String name,
            @Bind("owner") UUID owner,
            @Bind("type") boolean shopType,  // 确保数据库的 type 字段是布尔类型（非 BLOB）
            @Bind("lore") String lore
    );

    @SqlQuery("SELECT * FROM shops WHERE name = :name")
    Shop findByUuid(@Bind("name") String uuid);

    @SqlQuery("SELECT * FROM shops WHERE name = :uuid")
    Shop findByName(@Bind("uuid") String uuid);

    @SqlQuery("SELECT * FROM shops WHERE owner = :owner")
    List<Shop> findByOwner(@Bind("owner") UUID owner);

    @SqlUpdate("UPDATE shops SET name = :name WHERE uuid = :uuid")
    void updateName(@Bind("uuid") UUID uuid, @Bind("name") String name);

    @SqlUpdate("UPDATE shops SET owner = :owner WHERE uuid = :uuid")
    void updateOwner(@Bind("uuid") UUID uuid, @Bind("owner") UUID owner);

    @SqlUpdate("UPDATE shops SET type = :type WHERE uuid = :uuid")
    void updateType(@Bind("uuid") UUID uuid, @Bind("type") boolean shopType);

    @SqlUpdate("UPDATE shops SET lore = :lore WHERE uuid = :uuid")
    void updateLore(@Bind("uuid") UUID uuid, @Bind("lore") String lore);

    @SqlUpdate("DELETE FROM shops WHERE uuid = :uuid")
    void delete(@Bind("uuid") UUID uuid);
}
