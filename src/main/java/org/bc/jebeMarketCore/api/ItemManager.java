package org.bc.jebeMarketCore.api;

import org.bc.jebeMarketCore.model.Item;
import org.bc.jebeMarketCore.model.Shop;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public interface ItemManager {

    int getItemCount(UUID shopUuid);

    Item addItem(UUID shopUuid, @NotNull ItemStack clone);

    ItemStack removeItem(Shop shop, UUID itemId);

    List<Item> getItems(UUID shopUuid);

    Item getItem(UUID shopUuid, UUID itemId);

    boolean updateItem(Item item);
}