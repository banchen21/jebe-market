package org.bc.jebeMarketCore.listeners;

import org.bc.jebeMarketCore.JebeMarket;
import org.bukkit.event.Listener;

public class PlayerListener implements Listener {


    private final JebeMarket jebeMarket;

    public PlayerListener(JebeMarket jebeMarket) {
        this.jebeMarket = jebeMarket;
    }
}
