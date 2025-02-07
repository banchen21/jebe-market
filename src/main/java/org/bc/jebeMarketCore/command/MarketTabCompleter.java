package org.bc.jebeMarketCore.command;

import org.bc.jebeMarketCore.api.ItemManager;
import org.bc.jebeMarketCore.api.ShopManager;
import org.bc.jebeMarketCore.config.Configuration;
import org.bc.jebeMarketCore.model.Shop;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

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
                break;

            case 2:
                if ("shop".equalsIgnoreCase(args[0])) {
                    completions.addAll(Arrays.asList("create", "edit", "delete", "gui", "list"));
                }
                break;

            case 3:
                if ("shop".equalsIgnoreCase(args[0])) {
                    List<Shop> shops = shopManager.getPlayerShops(player.getUniqueId());
                    switch (args[1].toLowerCase()) {
                        case "edit":
                            // 编辑属性补全
                            completions.addAll(Arrays.asList("name", "desc", "type"));
                            break;
                        case "list":
                            // 列表分页补全（示例：1-5页）
                            int pageSize = config.getInt("shop.list.page"); // 每页显示 5 个商店

                            // 计算总页数
                            int totalPages = (int) Math.ceil((double) shops.size() / pageSize);
                            for (int i = 1; i <= totalPages; i++) {
                                completions.add(String.valueOf(i));
                            }
                            break;
                        case "delete":
                            if (!shops.isEmpty()) {
                                for (Shop shop : shops) {
                                    completions.add(String.valueOf(shop.getId()));
                                }
                            }
                            break;
                    }
                }
                break;
            case 4:
                if ("shop".equalsIgnoreCase(args[0])
                        && "edit".equalsIgnoreCase(args[1])
                        && Arrays.asList("name", "desc", "type").contains(args[2].toLowerCase())) {
                    List<Shop> shops = shopManager.getPlayerShops(player.getUniqueId());
                    if (!shops.isEmpty()) {
                        for (Shop shop : shops) {
                            completions.add(String.valueOf(shop.getId()));
                        }
                    }
                }
                break;

            case 5:
                if ("shop".equalsIgnoreCase(args[0])
                        && "edit".equalsIgnoreCase(args[1])
                        && "type".equalsIgnoreCase(args[2])) {
                    completions.addAll(Arrays.asList("public", "private"));
                }
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
