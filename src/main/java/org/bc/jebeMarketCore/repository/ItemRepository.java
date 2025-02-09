package org.bc.jebeMarketCore.repository;

import org.bc.jebeMarketCore.model.Item;
import org.bc.jebeMarketCore.model.Shop;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public interface ItemRepository {
    boolean addItem(Item item);

    List<Item> getItemsByShop(UUID shopUuid);

    ItemStack removeItem(Shop shop, UUID itemId);

    Item getItemById(UUID shopUuid, UUID itemId);

    boolean updatePrice(Item item);

    boolean updateAmount(Item item);
}
