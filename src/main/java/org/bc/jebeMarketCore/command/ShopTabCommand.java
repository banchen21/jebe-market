package org.bc.jebeMarketCore.command;

import org.bc.jebeMarketCore.api.ItemManager;
import org.bc.jebeMarketCore.api.ShopManager;
import org.bc.jebeMarketCore.config.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ShopTabCommand implements TabCompleter {
    private final ShopManager shopManager;
    private final ItemManager itemManager;
    private final Configuration config;

    public ShopTabCommand(ShopManager shopManager, ItemManager itemManager, Configuration config) {
        this.shopManager = shopManager;
        this.itemManager = itemManager;
        this.config = config;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        boolean isAdmin = sender.hasPermission("jebemarket.admin");

        switch (args.length) {
            case 1:
                handleFirstArgument(sender, completions);
                break;
            case 2:
                handleSecondArgument(sender, args[0], completions, isAdmin);
                break;
            case 3:
                handleThirdArgument(sender, args[0], args[1], completions, isAdmin);
                break;
            case 4:
                handleFourthArgument(sender, args[0], args[1], args[2], completions, isAdmin);
                break;
            case 5:
                handleFifthArgument(args[0], args[1], completions);
                break;
        }

        return filterCompletions(completions, args);
    }

    private void handleFirstArgument(CommandSender sender, List<String> completions) {
        if (hasPermission(sender)) {
            completions.add("create");
            completions.add("list");
            completions.add("edit");
            completions.add("delete");
            completions.add("info");
            completions.add("item");
            completions.add("help");
        }
    }

    private void handleSecondArgument(CommandSender sender, String arg0, List<String> completions, boolean isAdmin) {
        switch (arg0.toLowerCase()) {
            case "edit":
                completions.addAll(List.of("name", "lore", "owner", "type"));
                break;
            case "delete":
            case "info":
                completions.addAll(getOwnedShopsUuid(sender, isAdmin));
                break;
            case "item":
                completions.addAll(List.of("up", "down"));
                break;
            case "create":
                completions.addAll(List.of("shop", "pawnshop"));
                break;
        }
    }

    private void handleThirdArgument(CommandSender sender, String arg0, String arg1, List<String> completions, boolean isAdmin) {
        switch (arg0.toLowerCase()) {
            case "edit":
                handleEditSubCommands(sender, arg1, completions, isAdmin);
                break;
            case "item":
                handleItemSubCommands(sender, arg1, completions, isAdmin);
                break;
        }
    }

    private void handleEditSubCommands(CommandSender sender, String subCmd, List<String> completions, boolean isAdmin) {
        switch (subCmd.toLowerCase()) {
            case "name":
            case "lore":
            case "owner":
                completions.addAll(getOwnedShopsUuid(sender, isAdmin));
                break;
            case "type":
                completions.addAll(List.of("shop", "pawnshop"));
                break;
        }
    }

    private void handleItemSubCommands(CommandSender sender, String subCmd, List<String> completions, boolean isAdmin) {
        if ("up".equalsIgnoreCase(subCmd)) {
            completions.addAll(List.of("hand", "inventory"));
        } else if ("down".equalsIgnoreCase(subCmd)) {
            completions.addAll(getOwnedShopsUuid(sender, isAdmin));
        }
    }

    private void handleFourthArgument(CommandSender sender, String arg0, String arg1, String arg2, List<String> completions, boolean isAdmin) {
        if ("edit".equalsIgnoreCase(arg0)) {
            if ("owner".equalsIgnoreCase(arg1)) {
                Bukkit.getOnlinePlayers().forEach(p -> completions.add(p.getName()));
            }
        } else if ("item".equalsIgnoreCase(arg0)) {
            if ("up".equalsIgnoreCase(arg1)) {
                completions.addAll(getOwnedShopsUuid(sender, isAdmin));
            } else if ("down".equalsIgnoreCase(arg1)) {
                handleItemDownCompletion(arg2, completions);
            }
        }
    }

    private void handleFifthArgument(String arg0, String arg1, List<String> completions) {
        if ("edit".equalsIgnoreCase(arg0) && "owner".equalsIgnoreCase(arg1)) {
            completions.add("yes");
        }
    }

    private void handleItemDownCompletion(String shopUuid, List<String> completions) {
        try {
            List<String> items = shopManager.getShopItems(UUID.fromString(shopUuid))
                    .stream()
                    .toList();
            completions.addAll(items);
        } catch (IllegalArgumentException e) {
            // Invalid UUID format
        }
    }

    private List<String> getOwnedShopsUuid(CommandSender sender, boolean isAdmin) {
        if (sender instanceof Player) {
            return shopManager.getShopsByOwner(((Player) sender).getUniqueId(), isAdmin)
                    .stream()
                    .map(shop -> shop.getUuid().toString())
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private List<String> filterCompletions(List<String> completions, String[] args) {
        String lastArg = args[args.length - 1].toLowerCase();
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(lastArg))
                .sorted()
                .collect(Collectors.toList());
    }

    private boolean hasPermission(CommandSender sender) {
        return sender.hasPermission("jebemarket.user") || sender.hasPermission("jebemarket.admin");
    }
}