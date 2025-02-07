package org.bc.jebeMarketCore.command;

import org.bc.jebeMarketCore.api.ItemManager;
import org.bc.jebeMarketCore.api.ShopManager;
import org.bc.jebeMarketCore.config.Configuration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MarketTabCompleter implements TabCompleter {
    private final ShopManager shopManager;
    private final ItemManager itemManager;
    private final Configuration config;

    public MarketTabCompleter(ShopManager shopManager, ItemManager itemManager, Configuration config) {
        this.shopManager = shopManager;
        this.itemManager = itemManager;
        this.config = config;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        Player player = (Player) sender;

        switch (args.length) {
            case 1:
                completions.add("shop");
                completions.add("item");
                completions.add("test");
                break;
            case 2:
                break;
            case 3:

                break;
            case 4:
                break;
            case 5:

                break;
        }

        return filterCompletions(completions, args);
    }

    private List<String> filterCompletions(List<String> completions, String[] args) {
        String lastArg = args[args.length - 1].toLowerCase();
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(lastArg))
                .sorted()
                .collect(Collectors.toList());
    }

}
