package org.bc.jebeMarketCore.command;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bc.jebeMarketCore.JebeMarket;
import org.bc.jebeMarketCore.api.ShopManager;
import org.bc.jebeMarketCore.config.Configuration;
import org.bc.jebeMarketCore.gui.je.GUIType;
import org.bc.jebeMarketCore.gui.je.GuiManager;
import org.bc.jebeMarketCore.model.Shop;
import org.bc.jebeMarketCore.model.ShopItem;
import org.bc.jebeMarketCore.utils.PlayerHeadManager;
import org.bc.jebeMarketCore.utils.PlayerInputHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.bc.jebeMarketCore.utils.MessageUtils.color;


public class ShopCommand implements CommandExecutor {
    @Getter
    private final JebeMarket plugin;
    private final ShopManager shopManager;
    @Getter
    private final Configuration config;
    @Getter
    private final PlayerInputHandler inputHandler;
    @Getter
    private final PlayerHeadManager playerHeadManager;
    private final GuiManager guiManager;

    public ShopCommand(JebeMarket plugin, ShopManager shopManager, Configuration config, PlayerInputHandler inputHandler, PlayerHeadManager playerHeadManager, GuiManager guiManager) {
        this.plugin = plugin;
        this.shopManager = shopManager;
        this.config = config;
        this.inputHandler = inputHandler;
        this.playerHeadManager = playerHeadManager;
        this.guiManager = guiManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "gui":
                handleGui(sender, args);
                break;
            case "create":
                handleCreate(sender, args);
                break;
            case "edit":
                handleEdit(sender, args);
                break;
            case "delete":
                handleDelete(sender, args);
                break;
            case "open":
                handleOpen(sender, args);
                break;
            case "info":
                handleInfo(sender, args);
                break;
            case "item":
                handleItem(sender, args);
                break;
            case "list":
                handleList(sender);
                break;
            case "help":
                sendHelp(sender);
                break;
            default:
                sender.sendMessage(color(plugin.getString("commands.errors.unknown_command")));
        }
        return true;
    }

    private void handleOpen(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 2) {
            sender.sendMessage(color(plugin.getString("commands.open.usage")));
        }
        Shop shop = shopManager.getShop(args[1]);
        guiManager.openGuiWithContext((Player) sender, GUIType.SHOP_DETAILS, shop);
    }

    private void handleGui(@NotNull CommandSender sender, @NotNull String[] args) {
        Player player = (Player) sender;
        guiManager.openShopMainGui(player);
    }

    private void handleCreate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(color(plugin.getString("commands.errors.player_only")));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(color(plugin.getString("commands.create.usage")));
            return;
        }

        String shopName = args[1];
        if (shopName.length() < 2 || shopName.length() > 16) {
            player.sendMessage(color(plugin.getString("commands.create.errors.name_length")));
            return;
        }

        Shop shop = shopManager.createShop(shopName, player.getUniqueId());
        if (shop != null) {
            sender.sendMessage(color(plugin.getString("commands.create.success")
                    .replace("%s", shopName)
                    .replace("%s", shop.getUuid().toString())));
        } else {
            sender.sendMessage(color(plugin.getString("commands.create.errors.duplicate_name")));
        }
    }

    private void handleEdit(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(color(plugin.getString("commands.edit.usage")));
            return;
        }

        String editType = args[1].toLowerCase();
        String shopName = args[2];

        try {
            Shop shop = shopManager.getShop(shopName);
            if (shop == null) {
                sender.sendMessage(color(plugin.getString("commands.errors.shop_not_found")));
                return;
            }

            if (!isOwnerOrAdmin(sender, shop)) {
                sender.sendMessage(color(plugin.getString("commands.errors.no_permission")));
                return;
            }

            switch (editType) {
                case "name":
                    handleEditName(sender, args, shop);
                    break;
                case "lore":
                    handleEditLore(sender, args, shop);
                    break;
                case "owner":
                    handleEditOwner(sender, args, shop);
                    break;
                default:
                    sender.sendMessage(color("&c无效的编辑类型"));
            }
        } catch (IllegalArgumentException e) {
            sender.sendMessage(color(plugin.getString("commands.errors.invalid_shop_name")));
        }
    }

    private void handleEditName(CommandSender sender, String[] args, Shop shop) {
        if (args.length < 3) {
            sender.sendMessage(color(plugin.getString("commands.edit.name.usage")));
            return;
        }
        String newName = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
        if (newName.length() < 2 || newName.length() > 16) {
            sender.sendMessage("§c名称长度需在2-16字符之间");
            return;
        }
        shop.setName(newName);
        if (shopManager.updateShopName(shop)) {
            sender.sendMessage(color(plugin.getString("commands.edit.name.success")));
        } else {
            sender.sendMessage(color(plugin.getString("commands.edit.name.errors.duplicate")));
        }
    }

    private void handleEditLore(CommandSender sender, String[] args, Shop shop) {
        if (args.length < 3) {
            sender.sendMessage(color(plugin.getString("commands.edit.lore.usage")));
            return;
        }
        String lore = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
        if (lore.length() > 256) {
            sender.sendMessage(color(plugin.getString("commands.edit.lore.errors.length")));
            return;
        }
        shop.setLore(lore);
        shopManager.updateShopLore(shop);
    }

    private void handleEditOwner(CommandSender sender, String[] args, Shop shop) {
        if (args.length < 5 || !args[4].equalsIgnoreCase("yes")) {
            sender.sendMessage(color(plugin.getString("commands.edit.owner.usage")));
            return;
        }

        Player newOwner = Bukkit.getPlayer(args[3]);
        if (newOwner == null) {
            sender.sendMessage(color(plugin.getString("commands.edit.owner.errors.player_offline")));
            return;
        } else if (args[3].equals(sender.getName())) {
            sender.sendMessage(color(plugin.getString("commands.edit.owner.errors.self")));
        }

        shop.setOwner(newOwner.getUniqueId());
        shopManager.updateShopOwner(shop);
        sender.sendMessage(color(plugin.getString("commands.edit.owner.success")
                .replace("%s", newOwner.getName())));
    }

    private void handleDelete(CommandSender sender, String[] args) {
        if (args.length < 3 || !args[2].equalsIgnoreCase("yes")) {
            sender.sendMessage(color("&c请在命令末尾添加 yes 确认删除\n例: /shop delete <Name> yes"));
            return;
        }

        try {
            Shop shop = shopManager.getShop(args[1]);
            if (shop == null) {
                sender.sendMessage(color(plugin.getString("commands.errors.shop_not_found")));
                return;
            }

            if (shopManager.getItems(shop.getUuid()).isEmpty() &&
                    shopManager.deleteShop(shop.getUuid(), sender.hasPermission("jebemarket.admin"))) {
                sender.sendMessage(color(plugin.getString("commands.delete.success")));
            } else {
                sender.sendMessage(color(plugin.getString("commands.delete.errors.not_empty")));
            }
        } catch (IllegalArgumentException e) {
            sender.sendMessage(color(plugin.getString("commands.errors.invalid_shop_name")));
        }
    }

    private void handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(color(plugin.getString("commands.info.usage")));
            return;
        }

        try {
            Shop shop = shopManager.getShop(args[1]);
            if (shop == null) {
                sender.sendMessage(color(plugin.getString("commands.errors.shop_not_found")));
                return;
            }
            sender.sendMessage(color(plugin.getString("commands.info.header")));
            sender.sendMessage(color(plugin.getString("commands.info.entries.uid")
                    .replace("%s", shop.getUuid().toString())));
            sender.sendMessage(color(plugin.getString("commands.info.entries.name")
                    .replace("%s", shop.getName())));
            sender.sendMessage(color(plugin.getString("commands.info.entries.owner")
                    .replace("%s", Bukkit.getOfflinePlayer(shop.getOwner()).getName())));
            sender.sendMessage(color(plugin.getString("commands.info.entries.lore")
                    .replace("%s", shop.getLore())));
            sender.sendMessage(color(plugin.getString("commands.info.entries.item_count")
                    .replace("%s", String.valueOf(shopManager.getItemCount(shop.getUuid())))));
        } catch (IllegalArgumentException e) {
            sender.sendMessage(color(plugin.getString("commands.errors.invalid_shop_name")));
        }
    }

    private void handleItem(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(color("&c只有玩家可以管理商品"));
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(color("&c用法: /shop item <up/down/info/edit> ..."));
            return;
        }

        String operation = args[1].toLowerCase();
        switch (operation) {
            case "up":
                handleItemUp((Player) sender, args);
                break;
            case "down":
                handleItemDown((Player) sender, args);
                break;
            case "info":
                handleItemInfo((Player) sender, args);
                break;
            case "edit":
                handleItemEdit((Player) sender, args);
                break;
            default:
                sender.sendMessage(color("&4用法: /shop item <up/down/info/edit>"));
                break;
        }
    }

    private void handleItemEdit(Player player, String[] args) {
        try {
            Shop shop = shopManager.getShop(args[2]);
            ShopItem shopItem = shopManager.getItem(shop.getUuid(), UUID.fromString(args[3]));
            double price = Double.parseDouble(args[4]);
            price = Math.round(price * 100) / 100.0;
            shopItem.setPrice(price);
            if (shopManager.updatePrice(shopItem)) {
                player.sendMessage(color(plugin.getString("commands.item.edit.success")
                        .replace("%.2f", String.format("%.2f", price))));
            } else {
                player.sendMessage(color(plugin.getString("commands.item.edit.error")));
            }
        } catch (Exception e) {
            player.sendMessage(color(plugin.getString("commands.item.edit.usage")));
        }
    }

    private void handleItemInfo(Player player, String[] args) {
        try {
            Shop shop = shopManager.getShop(args[2]);
            List<ShopItem> shopItems = shopManager.getItems(shop.getUuid());

            if (shopItems.isEmpty()) {
                player.sendMessage(color(plugin.getString("commands.item.info.empty")));
                return;
            }
            for (ShopItem shopItem : shopItems) {
                ItemStack itemStack = shopItem.getItemStack();
                if (itemStack != null && itemStack.getItemMeta() != null) {
                    String uuid = shopItem.getUuid().toString();
                    String name = itemStack.getI18NDisplayName() != null ?
                            itemStack.getI18NDisplayName() :
                            plugin.getString("ui.shop_item_info.unknown_item");

                    Component hoverComponent = Component.text(
                            plugin.getString("ui.shop_item_info.hover_text")
                                    .replace("%s", uuid));

                    Component priceComponent = Component.text(
                            String.format(
                                    plugin.getString("ui.shop_item_info.price_format"),
                                    shopItem.getPrice()));

                    // 构建完整消息组件
                    Component message = Component.text()
                            .append(Component.text("[")
                                    .color(NamedTextColor.DARK_GRAY))
                            .append(Component.text(uuid.substring(0, 8))
                                    .color(NamedTextColor.YELLOW)
                                    .hoverEvent(HoverEvent.showText(hoverComponent)))
                            .append(Component.text("] ")
                                    .color(NamedTextColor.DARK_GRAY))
                            .append(Component.text(name)
                                    .color(NamedTextColor.WHITE))
                            .append(Component.text(" (x")
                                    .color(NamedTextColor.DARK_GRAY))
                            .append(Component.text(itemStack.getAmount())
                                    .color(NamedTextColor.YELLOW))
                            .append(Component.text(") ")
                                    .color(NamedTextColor.DARK_GRAY))
                            .append(priceComponent)
                            .build();
                    player.sendMessage(message);
                }
            }
        } catch (Exception e) {
            player.sendMessage(color(plugin.getString("commands.errors.unknown_error")));
        }
    }

    private void handleItemUp(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(color(plugin.getString("commands.item.up.usage")));
            return;
        }

        try {
            Shop shop = shopManager.getShop(args[3]);
            if (shop == null || !isOwnerOrAdmin(player, shop)) {
                player.sendMessage(color("&c无权限操作此商铺"));
                return;
            }

            switch (args[2].toLowerCase()) {
                case "hand":
                    shopManager.addHandItem(shop.getUuid(), player);
                    break;
                case "inventory":
                    shopManager.addInventoryItem(shop.getUuid(), player);

                    break;
                default:
                    player.sendMessage(color(plugin.getString("commands.item.errors.invalid_source")));
            }
        } catch (IllegalArgumentException e) {
            player.sendMessage(color(plugin.getString("commands.errors.invalid_shop_name")));
        }
    }

    private void handleItemDown(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(color(plugin.getString("commands.item.down.usage")));
            return;
        }

        try {
            Shop shop = shopManager.getShop(args[2]);
            if (shop == null || !isOwnerOrAdmin(player, shop)) {
                player.sendMessage(color("&c无权限操作此商铺"));
                return;
            }

            if (args.length < 4) {
                player.sendMessage(color(plugin.getString("commands.item.down.errors.missing_id")));
                return;
            }
            String isall = args[3];
            if (isall.equals("all")) {
                List<ShopItem> shopItemList = shopManager.getItems(shop.getUuid());
                shopItemList.forEach(item -> {
                    ItemStack itemStack = shopManager.removeItem(shop, item.getUuid());
                    givePlayerItemStack(player, itemStack);
                });
            } else {
                UUID itemId = UUID.fromString(args[3]);
                ItemStack itemStack = shopManager.removeItem(shop, itemId);
                givePlayerItemStack(player, itemStack);
            }
        } catch (IllegalArgumentException e) {
            player.sendMessage(color(plugin.getString("commands.item.errors.invalid_id")));
        }
    }

    private void givePlayerItemStack(Player player, ItemStack itemStack) {
        if (itemStack != null) {
            if (player.getInventory().firstEmpty() == -1) {
                player.getWorld().dropItem(player.getLocation(), itemStack);
                player.sendMessage(color(plugin.getString("items.inventory_full")));
            } else {
                player.getInventory().addItem(itemStack);
            }
            player.sendMessage(color(plugin.getString("commands.item.down.success")));
        } else {
            player.sendMessage(color(plugin.getString("commands.item.errors.not_found")));
        }
    }

    private void handleList(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(color(plugin.getString("commands.errors.player_only")));
            return;
        }

        List<Shop> shops = shopManager.getShopsByOwner(player.getUniqueId());
        if (shops.isEmpty()) {
            sender.sendMessage(color(plugin.getString("commands.list.empty")));
            return;
        }

        sender.sendMessage(color(plugin.getString("commands.list.header")));
        shops.forEach(shop -> {
            String entry = plugin.getString("commands.list.entry")
                    .replace("%s", shop.getName())
                    .replace("%s", shop.getUuid().toString());
            sender.sendMessage(color(entry));
        });
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(color(plugin.getString("commands.help.header")));
        plugin.getStringList("commands.help.entries").forEach(entry ->
                sender.sendMessage(color(entry)));
    }

    private boolean isOwnerOrAdmin(CommandSender sender, Shop shop) {
        if (sender instanceof Player) {
            return shop.getOwner().equals(((Player) sender).getUniqueId());
        }
        return false;
    }


}