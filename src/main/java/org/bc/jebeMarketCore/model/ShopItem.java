package org.bc.jebeMarketCore.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;
import org.jdbi.v3.core.mapper.reflect.ColumnName;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor  // 添加 Lombok 无参构造
public class ShopItem {
    @ColumnName("uuid")
    private UUID uuid;
    @ColumnName("shopuuid")
    private UUID shopuuid;
    @ColumnName("price")
    private double price;
    private ItemStack itemStack;

    public ShopItem(UUID shopUuid, ItemStack itemStack) {
        this.uuid = UUID.randomUUID();
        this.shopuuid = shopUuid;
        this.price = 999999999.99;
        this.itemStack = itemStack;
    }

    public ShopItem(UUID uuid, UUID shopUuid, double price, ItemStack itemStack) {
        this.uuid = uuid;
        this.shopuuid = shopUuid;
        this.price = price;
        this.itemStack = itemStack;
    }


}
