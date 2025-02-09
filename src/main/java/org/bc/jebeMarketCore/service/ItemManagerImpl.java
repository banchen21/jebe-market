package org.bc.jebeMarketCore.service;

import org.bc.jebeMarketCore.api.ItemManager;
import org.bc.jebeMarketCore.model.Item;
import org.bc.jebeMarketCore.model.Shop;
import org.bc.jebeMarketCore.repository.ItemServiceImpl;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class ItemManagerImpl implements ItemManager {

    ItemServiceImpl itemService;

    public ItemManagerImpl(ItemServiceImpl service) {
        this.itemService = service;
    }

    @Override
    public int getItemCount(UUID shopUuid) {
        return itemService.getItemsByShop(shopUuid).size();
    }

    @Override
    public Item addItem(UUID shopUuid, @NotNull ItemStack clone) {
        Item item = new Item(shopUuid, clone);
        if (itemService.addItem(item)) {
            return item;
        } else {
            return null;
        }
    }

    @Override
    public ItemStack removeItem(Shop shop, UUID itemId) {
        return itemService.removeItem(shop, itemId);
    }

    @Override
    public List<Item> getItems(UUID shopUuid) {
        return itemService.getItemsByShop(shopUuid);
    }

    @Override
    public Item getItem(UUID shopUuid, UUID itemId) {
        return itemService.getItemById(shopUuid, itemId);
    }

    @Override
    public boolean updateItem(Item item) {
        return itemService.updatePrice(item) && itemService.updateAmount(item);
    }
}
