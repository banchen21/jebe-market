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
public class Item {
    @ColumnName("uuid")
    private UUID uuid;
    @ColumnName("shopuuid")
    private UUID shopuuid;
    @ColumnName("material")
    private String material;
    @ColumnName("price")
    private double price;
    @ColumnName("amount")
    private int amount;
    private ItemStack itemStack;

    public Item(UUID shopUuid, ItemStack itemStack) {
        this.uuid = UUID.randomUUID();
        this.shopuuid = shopUuid;
        this.material = itemStack.getType().name();
        this.price = 0.0;
        this.amount = itemStack.getAmount();
        this.itemStack = itemStack;
    }

    public Item(UUID uuid, UUID shopUuid, String material, double price, int amount, ItemStack itemStack) {
        this.uuid = uuid;
        this.shopuuid = shopUuid;
        this.material = material;
        this.price = price;
        this.amount = amount;
        this.itemStack = itemStack;
    }


}
