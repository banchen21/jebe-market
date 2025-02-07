package org.bc.jebeMarketCore.api;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;

public interface ItemManager {

    //    TODO 通过商品ID获取数量
    int getItemCount(UUID shopUuid);

    //    TODO 添加商品
    void addItem(UUID shopUuid, @NotNull ItemStack clone);

    //    TODO 删除商品
    boolean removeItem(UUID shopUuid, UUID itemId);

}