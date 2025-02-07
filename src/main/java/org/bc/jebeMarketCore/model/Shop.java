package org.bc.jebeMarketCore.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 星际市集商铺实体类
 */
@Setter
@Getter
public class Shop {
    private UUID uuid;          // 商铺唯一标识
    private String name;              // 商铺名称
    private UUID owner;               // 所有者UUID
    private ShopType type;            // 商铺类型
    private String lore;              // 商铺描述（使用|换行）
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime lastModified; // 最后修改时间


    public enum ShopType {
        SHOP,       // 普通商店
        PAWNSHOP    // 当铺
    }

}