package org.bc.jebeMarketCore.model;

import lombok.*;
import org.jdbi.v3.core.mapper.reflect.ColumnName;

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
    @ColumnName("lore") // 明确映射到数据库的 lore 列（TEXT 类型）
    private String lore;

    public Shop(String shopName, UUID ownerUuid) {
        uuid = UUID.randomUUID();
        name = shopName;
        owner = ownerUuid;
        lore = "hi~";
    }

    public Shop(UUID uuid, String name, UUID owner, String lore) {
        this.uuid = uuid;
        this.name = name;
        this.owner = owner;
        this.lore = lore;
    }
}