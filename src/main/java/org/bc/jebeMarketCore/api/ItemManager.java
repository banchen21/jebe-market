package org.bc.jebeMarketCore.api;

import org.bc.jebeMarketCore.model.Item;
import org.bc.jebeMarketCore.model.Shop;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public interface ItemManager {

    //    TODO 通过商品ID获取数量
    int getItemCount(UUID shopUuid);

    //    TODO 添加商品
    Item addItem(UUID shopUuid, @NotNull ItemStack clone);

    //    TODO 删除商品
    ItemStack removeItem(Shop shop, UUID itemId);

    //    获取商品列表
    List<Item> getItems(UUID shopUuid);
}