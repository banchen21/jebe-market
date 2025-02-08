package org.bc.jebeMarketCore.service;

import org.bc.jebeMarketCore.api.ItemManager;
import org.bc.jebeMarketCore.repository.ItemRepository;
import org.bc.jebeMarketCore.repository.ItemServiceImpl;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ItemManagerImpl implements ItemManager {

    ItemServiceImpl itemService;

    public ItemManagerImpl(ItemServiceImpl service) {
        this.itemService = service;
    }

    @Override
    public int getItemCount(UUID shopUuid) {
        return 0;
    }

    @Override
    public void addItem(UUID shopUuid, @NotNull ItemStack clone) {

    }

    @Override
    public boolean removeItem(UUID shopUuid, UUID itemId) {
        return false;
    }
}
