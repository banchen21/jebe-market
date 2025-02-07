package org.bc.jebeMarketCore.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bc.jebeMarketCore.api.ItemManager;
import org.bc.jebeMarketCore.api.ShopManager;
import org.bc.jebeMarketCore.config.Configuration;
import org.bc.jebeMarketCore.model.Shop;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ShopCommand implements CommandExecutor {
    private final ShopManager shopManager;
    private final ItemManager itemManager;
    private final Configuration config;

    public ShopCommand(ShopManager shopManager, ItemManager itemManager, Configuration config) {
        this.shopManager = shopManager;
        this.itemManager = itemManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "create":
                handleCreate(sender, args);
                break;
            case "edit":
                handleEdit(sender, args);
                break;
            case "delete":
                handleDelete(sender, args);
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
                sender.sendMessage(color("&c未知命令，使用/shop help 查看帮助"));
        }
        return true;
    }

    private void handleCreate(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "jebemarket.create")) return;
        if (!(sender instanceof Player)) {
            sender.sendMessage(color("&c只有玩家可以创建商铺"));
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(color("&c用法: /shop create <名称> <shop/pawnshop>"));
            return;
        }

        Player player = (Player) sender;
        String shopName = args[1];
        String shopType = args[2].toLowerCase();

        if (!shopType.equals("shop") && !shopType.equals("pawnshop")) {
            sender.sendMessage(color("&c无效的商铺类型，可选：shop 或 pawnshop"));
            return;
        }

        Shop shop = shopManager.createShop(player.getUniqueId(), shopName, shopType);
        if (shop != null) {
            sender.sendMessage(color(String.format("&a成功创建 %s 商铺！UID: %s", shopType, shop.getUuid())));
        } else {
            sender.sendMessage(color("&c创建商铺失败，请检查名称是否重复"));
        }
    }

    private void handleEdit(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "jebemarket.edit")) return;
        if (args.length < 3) {
            sender.sendMessage(color("&c用法: /shop edit <name/lore/owner/type> <UID> [参数]"));
            return;
        }

        String editType = args[1].toLowerCase();
        String shopUuid = args[2];

        try {
            Shop shop = shopManager.getShop(UUID.fromString(shopUuid));
            if (shop == null) {
                sender.sendMessage(color("&c商铺不存在"));
                return;
            }

            if (!isOwnerOrAdmin(sender, shop)) {
                sender.sendMessage(color("&c你没有权限修改此商铺"));
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
                case "type":
                    handleEditType(sender, args, shop);
                    break;
                default:
                    sender.sendMessage(color("&c无效的编辑类型"));
            }
        } catch (IllegalArgumentException e) {
            sender.sendMessage(color("&c无效的商铺UID格式"));
        }
    }

    private void handleEditName(CommandSender sender, String[] args, Shop shop) {
        if (args.length < 4) {
            sender.sendMessage(color("&c用法: /shop edit name <UID> <新名称>"));
            return;
        }
        String newName = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
        if (shopManager.updateShopName(shop.getUuid(), newName)) {
            sender.sendMessage(color("&a商铺名称已更新"));
        } else {
            sender.sendMessage(color("&c名称更新失败，可能重复"));
        }
    }

    private void handleEditLore(CommandSender sender, String[] args, Shop shop) {
        if (args.length < 4) {
            sender.sendMessage(color("&c用法: /shop edit lore <UID> <描述内容>"));
            return;
        }
        String lore = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
        shop.setLore(lore.replace("|", "\n"));
        shopManager.updateShop(shop);
        sender.sendMessage(color("&a商铺描述已更新"));
    }

    private void handleEditOwner(CommandSender sender, String[] args, Shop shop) {
        if (args.length < 5 || !args[4].equalsIgnoreCase("yes")) {
            sender.sendMessage(color("&c请在命令末尾添加 yes 确认转让\n例: /shop edit owner <UID> <玩家> yes"));
            return;
        }

        Player newOwner = Bukkit.getPlayer(args[3]);
        if (newOwner == null) {
            sender.sendMessage(color("&c目标玩家不在线"));
            return;
        }

        shop.setOwner(newOwner.getUniqueId());
        shopManager.updateShop(shop);
        sender.sendMessage(color(String.format("&a已将商铺转让给 %s", newOwner.getName())));
    }

    private void handleEditType(CommandSender sender, String[] args, Shop shop) {
        if (args.length < 4) {
            sender.sendMessage(color("&c用法: /shop edit type <UID> <shop/pawnshop>"));
            return;
        }
        String newType = args[3].toLowerCase();
        if (!newType.equals("shop") && !newType.equals("pawnshop")) {
            sender.sendMessage(color("&c无效类型，可选：shop 或 pawnshop"));
            return;
        }
        shop.setType(Shop.ShopType.valueOf(newType));
        shopManager.updateShop(shop);
        sender.sendMessage(color("&a商铺类型已更新"));
    }

    private void handleDelete(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "jebemarket.delete")) return;
        if (args.length < 3 || !args[2].equalsIgnoreCase("yes")) {
            sender.sendMessage(color("&c请在命令末尾添加 yes 确认删除\n例: /shop delete <UID> yes"));
            return;
        }

        try {
            UUID shopUuid = UUID.fromString(args[1]);
            if (shopManager.deleteShop(shopUuid, sender.hasPermission("jebemarket.admin"))) {
                sender.sendMessage(color("&a商铺已删除"));
            } else {
                sender.sendMessage(color("&c删除失败，商铺不存在或仍有商品"));
            }
        } catch (IllegalArgumentException e) {
            sender.sendMessage(color("&c无效的商铺UID格式"));
        }
    }

    private void handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(color("&c用法: /shop info <UID>"));
            return;
        }

        try {
            Shop shop = shopManager.getShop(UUID.fromString(args[1]));
            if (shop == null) {
                sender.sendMessage(color("&c商铺不存在"));
                return;
            }

            sender.sendMessage(color("&6=== 商铺信息 ==="));
            sender.sendMessage(color("&eUID: &f" + shop.getUuid()));
            sender.sendMessage(color("&e名称: &f" + shop.getName()));
            sender.sendMessage(color("&e所有者: &f" + Bukkit.getOfflinePlayer(shop.getOwner()).getName()));
            sender.sendMessage(color("&e类型: &f" + shop.getType().name().toUpperCase()));
            sender.sendMessage(color("&e商品数量: &f" + itemManager.getItemCount(shop.getUuid())));
        } catch (IllegalArgumentException e) {
            sender.sendMessage(color("&c无效的商铺UID格式"));
        }
    }

    private void handleItem(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(color("&c只有玩家可以管理商品"));
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(color("&c用法: /shop item <up/down> ..."));
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
            default:
                sender.sendMessage(color("&c无效操作，可用 up/down"));
        }
    }

    private void handleItemUp(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(color("&c用法: /shop item up <hand/inventory> <UID>"));
            return;
        }

        try {
            UUID shopUuid = UUID.fromString(args[3]);
            Shop shop = shopManager.getShop(shopUuid);
            if (shop == null || !isOwnerOrAdmin(player, shop)) {
                player.sendMessage(color("&c无权限操作此商铺"));
                return;
            }

            switch (args[2].toLowerCase()) {
                case "hand":
                    ItemStack handItem = player.getInventory().getItemInMainHand();
                    if (handItem == null || handItem.getAmount() == 0) {
                        player.sendMessage(color("&c请手持要上架的商品"));
                        return;
                    }
                    itemManager.addItem(shopUuid, handItem.clone());
                    player.sendMessage(color("&a成功上架手持物品"));
                    break;
                case "inventory":
                    int count = 0;
                    for (ItemStack item : player.getInventory().getContents()) {
                        if (item != null && !item.getType().isAir()) {
                            itemManager.addItem(shopUuid, item.clone());
                            count++;
                        }
                    }
                    player.sendMessage(color(String.format("&a成功上架 %d 种物品", count)));
                    break;
                default:
                    player.sendMessage(color("&c无效来源，可用 hand/inventory"));
            }
        } catch (IllegalArgumentException e) {
            player.sendMessage(color("&c无效的商铺UID格式"));
        }
    }

    private void handleItemDown(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(color("&c用法: /shop item down <UID> <商品ID>"));
            return;
        }

        try {
            UUID shopUuid = UUID.fromString(args[2]);
            Shop shop = shopManager.getShop(shopUuid);
            if (shop == null || !isOwnerOrAdmin(player, shop)) {
                player.sendMessage(color("&c无权限操作此商铺"));
                return;
            }

            if (args.length < 4) {
                player.sendMessage(color("&c请输入要下架的商品ID"));
                return;
            }

            UUID itemId = UUID.fromString(args[3]);
            if (itemManager.removeItem(shopUuid, itemId)) {
                player.sendMessage(color("&a商品已下架"));
            } else {
                player.sendMessage(color("&c商品不存在"));
            }
        } catch (IllegalArgumentException e) {
            player.sendMessage(color("&c无效的ID格式"));
        }
    }

    private void handleList(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(color("&c只有玩家可以查看商铺列表"));
            return;
        }

        Player player = (Player) sender;
        List<Shop> shops = shopManager.getShopsByOwner(player.getUniqueId(), false);

        if (shops.isEmpty()) {
            sender.sendMessage(color("&e你还没有创建任何商铺"));
            return;
        }

        sender.sendMessage(color("&6=== 你的商铺列表 ==="));
        shops.forEach(shop -> sender.sendMessage(color(
                String.format("&e%s &7(UID: &f%s&7) &8- &f%s",
                        shop.getName(),
                        shop.getUuid(),
                        shop.getType().name().toUpperCase())
        )));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(color("&6=== 星际市集帮助 ==="));
        sender.sendMessage(color("&e/shop create <类型> <名称> &7- 创建新商铺"));
        sender.sendMessage(color("&e/shop edit <属性> <UID> [参数] &7- 编辑商铺"));
        sender.sendMessage(color("&e/shop delete <UID> yes &7- 删除商铺"));
        sender.sendMessage(color("&e/shop info <UID> &7- 查看商铺信息"));
        sender.sendMessage(color("&e/shop item up <方式> <UID> &7- 上架商品"));
        sender.sendMessage(color("&e/shop item down <UID> <ID> &7- 下架商品"));
        sender.sendMessage(color("&e/shop list &7- 列出所有商铺"));
    }

    private boolean checkPermission(CommandSender sender, String permission) {
        if (!sender.hasPermission(permission) && !sender.hasPermission("jebemarket.admin")) {
            sender.sendMessage(color("&c你没有执行此操作的权限"));
            return false;
        }
        return true;
    }

    private boolean isOwnerOrAdmin(CommandSender sender, Shop shop) {
        if (sender instanceof Player) {
            return shop.getOwner().equals(((Player) sender).getUniqueId()) ||
                    sender.hasPermission("jebemarket.admin");
        }
        return false;
    }

    private String color(String text) {

        return ChatColor.translateAlternateColorCodes('&', text);
    }
}