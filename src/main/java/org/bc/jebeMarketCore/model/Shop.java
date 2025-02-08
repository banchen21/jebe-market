package org.bc.jebeMarketCore.model;

import lombok.*;
import org.jdbi.v3.core.mapper.reflect.ColumnName;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * 星际市集商铺实体类
 */
@Setter
@Getter
@NoArgsConstructor  // 添加 Lombok 无参构造
public class Shop {
    @ColumnName("uuid") // 明确映射到数据库的 uuid 列（TEXT 类型）
    private UUID uuid;
    @ColumnName("name") // 明确映射到数据库的 name 列（TEXT 类型）
    private String name;
    @ColumnName("owner") // 明确映射到数据库的 owner 列（TEXT 类型）
    private UUID owner;
    @ColumnName("type")  // 明确映射数据库列名
    private boolean shopType;
    @ColumnName("lore") // 明确映射到数据库的 lore 列（TEXT 类型）
    private String lore;

    public Shop(@NotNull UUID uniqueId, String shopName, UUID ownerUuid, boolean shopType1) {
        uuid = UUID.randomUUID();
        name = shopName;
        owner = ownerUuid;
        shopType = shopType1;
        lore = "hi~";
    }

    public Shop(UUID uuid, String name, UUID owner, boolean shopType, String lore) {
        this.uuid = uuid;
        this.name = name;
        this.owner = owner;
        this.shopType = shopType;
        this.lore = lore;
    }
}