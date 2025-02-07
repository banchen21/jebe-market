package org.bc.jebeMarketCore.command;

import ca.tweetzy.skulls.flight.utils.QuickItem;
import lombok.extern.slf4j.Slf4j;
import org.bc.jebeMarketCore.MarketPulseUtil.GetPlayerHandItemStack;
import org.bc.jebeMarketCore.api.ItemManager;
import org.bc.jebeMarketCore.api.ShopManager;
import org.bc.jebeMarketCore.config.Configuration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
public class MarketCommand implements CommandExecutor {

    private final ShopManager shopManager;
    private final ItemManager itemManager;
    private final Configuration config;

    public MarketCommand(ShopManager shopManager, ItemManager itemManager, Configuration config) {
        this.shopManager = shopManager;
        this.itemManager = itemManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
//         检查是否为玩家
        if (!(sender instanceof Player)) {
            return false;
        }
        Player player = (Player) sender;
        if (args.length < 1) {
            return false;
        }

        switch (args[0]) {
            case "shop":
                break;
            case "item":
                break;
            case "test":
                ItemStack itemStack = GetPlayerHandItemStack.getPlayerHandItemStack(player);
//                给玩家
                player.getInventory().addItem(itemStack);
                break;
        }
        return false;
    }

}