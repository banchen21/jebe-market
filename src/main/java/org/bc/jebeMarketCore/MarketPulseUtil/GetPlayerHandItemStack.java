package org.bc.jebeMarketCore.MarketPulseUtil;

import ca.tweetzy.skulls.flight.utils.QuickItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GetPlayerHandItemStack {
    /**
     * 获取玩家头颅
     *
     * @param player Player
     * @return ItemStack
     */
    public static ItemStack getPlayerHandItemStack(Player player) {
        return QuickItem.of(player).name(player.getName()).amount(1).make();
    }
}
