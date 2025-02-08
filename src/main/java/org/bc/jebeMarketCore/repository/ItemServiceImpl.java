package org.bc.jebeMarketCore.repository;

public class ItemServiceImpl implements ItemRepository {

    private final ItemRepository itemRepository;

    public ItemServiceImpl(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }
}
