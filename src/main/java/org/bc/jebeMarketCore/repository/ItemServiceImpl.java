package org.bc.jebeMarketCore.repository;

import org.bc.jebeMarketCore.model.Item;
import org.bc.jebeMarketCore.model.Shop;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class ItemServiceImpl implements ItemRepository {

    private final ItemRepository itemRepository;

    public ItemServiceImpl(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public boolean addItem(Item item) {
        return itemRepository.addItem(item);
    }

    @Override
    public List<Item> getItemsByShop(UUID shopUuid) {
        return itemRepository.getItemsByShop(shopUuid);
    }

    @Override
    public ItemStack removeItem(Shop shop, UUID itemId) {
        return itemRepository.removeItem(shop, itemId);
    }
}
