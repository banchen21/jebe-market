package org.bc.jebeMarketCore.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bc.jebeMarketCore.JebeMarket;
import org.bc.jebeMarketCore.api.ShopManager;
import org.bc.jebeMarketCore.config.Configuration;
import org.bc.jebeMarketCore.gui.je.GUIType;
import org.bc.jebeMarketCore.gui.je.GuiManager;
import org.bc.jebeMarketCore.model.ShopItem;
import org.bc.jebeMarketCore.model.Shop;
import org.bc.jebeMarketCore.utils.MessageUtils;
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
    private final JebeMarket plugin;
    private final ShopManager shopManager;
    private final Configuration config;
    private final PlayerInputHandler inputHandler;
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
                sender.sendMessage(color("&c未知命令，使用/shop help 查看帮助"));
        }
        return true;
    }

    private void handleOpen(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 2) {
            sender.sendMessage(color("&c用法: /shop open <shop>"));
        }
        Shop shop = shopManager.getShop(args[1]);
        if (shop == null) {
            sender.sendMessage(color("&c商铺不存在"));
            return;
        }
        guiManager.openGuiWithContext((Player) sender, GUIType.PLAYER_SHOP, shop);
    }

    private void handleGui(@NotNull CommandSender sender, @NotNull String[] args) {
        Player player = (Player) sender;
        guiManager.openShopMainGui(player);
    }

    private void handleCreate(CommandSender sender, String[] args) {

        // 检查是否是玩家
        if (!(sender instanceof Player player)) {
            sender.sendMessage(color("&c只有玩家可以创建商铺"));
            return;
        }

        // 检查参数数量
        if (args.length < 2) {
            sender.sendMessage(color("&c用法: /shop create  <shop> <新名称>"));
            return;
        }

        String shopName = args[1];
        if (shopName.length() < 2 || shopName.length() > 16) {
            player.sendMessage("§c新名称长度需在2-16字符之间");
            return;
        }
        Shop shop = shopManager.createShop(shopName, player.getUniqueId());

        // 处理商铺创建结果
        if (shop != null) {
            sender.sendMessage(color(String.format("&a成功创建商店 %s！UID: %s", shopName, shop.getUuid())));
        } else {
            sender.sendMessage(color("&c创建商铺失败，检查名称发现重复"));
        }
    }

    private void handleEdit(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(color("&c用法: /shop edit <name/lore/owner/type> <Name> [参数]"));
            return;
        }

        String editType = args[1].toLowerCase();
        String shopName = args[2];

        try {
            Shop shop = shopManager.getShop(shopName);
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
                default:
                    sender.sendMessage(color("&c无效的编辑类型"));
            }
        } catch (IllegalArgumentException e) {
            sender.sendMessage(color("&c无效的商铺名"));
        }
    }

    private void handleEditName(CommandSender sender, String[] args, Shop shop) {
        if (args.length < 3) {
            sender.sendMessage(color("&c用法: /shop edit name <Name> <新名称>"));
            return;
        }
        String newName = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
        if (newName.length() < 2 || newName.length() > 16) {
            sender.sendMessage("§c名称长度需在2-16字符之间");
            return;
        }
        shop.setName(newName);
        if (shopManager.updateShopName(shop)) {
            sender.sendMessage(color("&a商铺名称已更新"));
        } else {
            sender.sendMessage(color("&c名称更新失败，可能重复"));
        }
    }

    private void handleEditLore(CommandSender sender, String[] args, Shop shop) {
        if (args.length < 3) {
            sender.sendMessage(color("&c用法: /shop edit lore <Name> <介绍内容>"));
            return;
        }
        String lore = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
        if (lore.length() > 256) {
            sender.sendMessage("§c介绍过长，最多256字符");
            return;
        }
        shop.setLore(lore);
        shopManager.updateShopLore(shop);
        sender.sendMessage(color("&a商铺描述已更新"));
    }

    private void handleEditOwner(CommandSender sender, String[] args, Shop shop) {
        if (args.length < 5 || !args[4].equalsIgnoreCase("yes")) {
            sender.sendMessage(color("&c请在命令末尾添加 yes 确认转让\n例: /shop edit owner <商品名> <玩家> yes"));
            return;
        }

        Player newOwner = Bukkit.getPlayer(args[3]);
        if (newOwner == null) {
            sender.sendMessage(color("&c目标玩家不在线"));
            return;
        }

        shop.setOwner(newOwner.getUniqueId());
        shopManager.updateShopOwner(shop);
        sender.sendMessage(color(String.format("&a已将商铺转让给 %s", newOwner.getName())));
    }

    private void handleDelete(CommandSender sender, String[] args) {
        if (args.length < 3 || !args[2].equalsIgnoreCase("yes")) {
            sender.sendMessage(color("&c请在命令末尾添加 yes 确认删除\n例: /shop delete <Name> yes"));
            return;
        }

        try {
            Shop shop = shopManager.getShop(args[1]);
            if (shopManager.getItems(shop.getUuid()).isEmpty() && shopManager.deleteShop(shop.getUuid(), sender.hasPermission("jebemarket.admin"))) {
                sender.sendMessage(color("&a商店已删除"));
            } else {
                sender.sendMessage(color("&c删除失败，商铺内有商品，请先清空商品"));
            }

        } catch (IllegalArgumentException e) {
            sender.sendMessage(color("&c无效的商铺名"));
        }
    }

    private void handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(color("&c用法: /shop info <Name>"));
            return;
        }

        try {
            Shop shop = shopManager.getShop(args[1]);
            if (shop == null) {
                sender.sendMessage(color("&c商铺不存在"));
                return;
            }

            sender.sendMessage(color("&6=== 商铺信息 ==="));
            sender.sendMessage(color("&eUID: &f" + shop.getUuid()));
            sender.sendMessage(color("&e名称: &f" + shop.getName()));
            sender.sendMessage(color("&e所有者: &f" + Bukkit.getOfflinePlayer(shop.getOwner()).getName()));
            sender.sendMessage(color("&e描述: &f" + shop.getLore()));
            sender.sendMessage(color("&e商品数量: &f" + shopManager.getItemCount(shop.getUuid())));
        } catch (IllegalArgumentException e) {
            sender.sendMessage(color("&c无效的商铺名"));
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
            player.sendMessage(color("&a单价已更新: " + price));
            if (shopManager.updatePrice(shopItem)) {
                player.sendMessage(color("&a更新成功"));
            } else {
                player.sendMessage(color("&c更新失败，请重试"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(color("&c/shop item edit ..."));
        }
    }

    private void handleItemInfo(Player player, String[] args) {
        try {
            Shop shop = shopManager.getShop(args[2]);
            List<ShopItem> shopItems = shopManager.getItems(shop.getUuid());

            if (shopItems.isEmpty()) {
                player.sendMessage(color("&c该商店没有商品"));
                return;
            }
            for (ShopItem shopItem : shopItems) {
                ItemStack itemStack = shopItem.getItemStack();
                if (itemStack != null && itemStack.getItemMeta() != null) {
                    // 构建基本信息
                    String uuid = shopItem.getUuid().toString();
                    String shortUuid = uuid.substring(0, 8);
                    String name = itemStack.getI18NDisplayName() != null ? itemStack.getI18NDisplayName() : "未知物品";
                    List<String> lore = itemStack.getItemMeta().getLore();

                    // 创建带悬停效果的消息
                    Component message = Component.text().append(Component.text("[").color(NamedTextColor.DARK_GRAY)).append(Component.text(shortUuid).color(NamedTextColor.YELLOW).hoverEvent(HoverEvent.showText(Component.text("UUID: " + uuid)))).append(Component.text("] ").color(NamedTextColor.DARK_GRAY)).append(Component.text(name).color(NamedTextColor.WHITE)).append(Component.text(" (x").color(NamedTextColor.DARK_GRAY)).append(Component.text(shopItem.getItemStack().getAmount()).color(NamedTextColor.YELLOW)).append(Component.text(") ").color(NamedTextColor.DARK_GRAY)).append(Component.text(String.format("$%.2f", shopItem.getPrice())).color(NamedTextColor.GOLD)).build();

                    player.sendMessage(message);

                    if (lore != null && !lore.isEmpty()) {
                        player.sendMessage(Component.text(String.join(" | ", lore)).color(NamedTextColor.GRAY));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(color("&c未知错误"));
        }
    }

    private void handleItemUp(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(color("&c用法: /shop item up <hand/inventory> <Name>"));
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
                    player.sendMessage(color("&c无效来源，可用 主手持/背包"));
            }
        } catch (IllegalArgumentException e) {
            player.sendMessage(color("&c无效的商铺名"));
        }
    }

    private void handleItemDown(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(color("&c用法: /shop item down <Name> <商品ID>"));
            return;
        }

        try {
            Shop shop = shopManager.getShop(args[2]);
            if (shop == null || !isOwnerOrAdmin(player, shop)) {
                player.sendMessage(color("&c无权限操作此商铺"));
                return;
            }

            if (args.length < 4) {
                player.sendMessage(color("&c请输入要下架的商品ID"));
                return;
            }
            String isall = args[3];
            if (isall.equals("all")) {
//                 获取所有itemId
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
            player.sendMessage(color("&c无效的ID格式"));
        }
    }

    private void givePlayerItemStack(Player player, ItemStack itemStack) {
        if (itemStack != null) {
            if (player.getInventory().firstEmpty() == -1) {
                player.getWorld().dropItem(player.getLocation(), itemStack);
                player.sendMessage(color("&c你的背包已满，商品已生成为掉落物"));
            } else {
                player.getInventory().addItem(itemStack);
            }
            player.sendMessage(color("&a商品已下架"));
        } else {
            player.sendMessage(color("&c商品不存在"));
        }
    }

    private void handleList(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(color("&c只有玩家可以查看商铺列表"));
            return;
        }

        List<Shop> shops = shopManager.getShopsByOwner(player.getUniqueId());
        if (shops.isEmpty()) {
            sender.sendMessage(color("&e你还没有创建任何商铺"));
            return;
        }

        sender.sendMessage(color("&6=== 你的商铺列表 ==="));
        shops.forEach(shop -> {
            sender.sendMessage(color(String.format("&e%s &7(UID: &f%s&7) &8", shop.getName(), shop.getUuid())));
        });
    }

    private void sendHelp(CommandSender sender) {

        sender.sendMessage(color("&6=== 星际市集帮助 ==="));
        sender.sendMessage(color("&e/shop create <名称> &7- 创建新商铺"));
        sender.sendMessage(color("&e/shop edit <属性> <Name> [参数] &7- 编辑商铺"));
        sender.sendMessage(color("&e/shop delete <Name> yes &7- 删除商铺"));
        sender.sendMessage(color("&e/shop info <Name> &7- 查看商铺信息"));
        sender.sendMessage(color("&e/shop item up <方式> <Name> &7- 上架商品"));
        sender.sendMessage(color("&e/shop item down <Name> <ID> &7- 下架商品"));
        sender.sendMessage(color("&e/shop list &7- 列出所有商铺"));
    }

    //    TODO 权限验证
    private boolean isOwnerOrAdmin(CommandSender sender, Shop shop) {
        if (sender instanceof Player) {
            return shop.getOwner().equals(((Player) sender).getUniqueId()) || sender.hasPermission("jebemarket.admin");
        }
        return false;
    }


}