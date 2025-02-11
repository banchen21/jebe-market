package org.bc.jebeMarketCore.gui.je;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public interface BaseGUI extends InventoryHolder {
    void open(Player player);

    void handleClick(InventoryClickEvent event);

    void prepareContext(Object... context);
}
