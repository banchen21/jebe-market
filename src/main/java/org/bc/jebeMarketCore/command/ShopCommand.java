package org.bc.jebeMarketCore.command;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bc.jebeMarketCore.JebeMarket;
import org.bc.jebeMarketCore.api.ShopManager;
import org.bc.jebeMarketCore.config.Configuration;
import org.bc.jebeMarketCore.gui.be.ShopMainForm;
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
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.player.FloodgatePlayer;
import org.geysermc.floodgate.util.DeviceOs;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
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
    private final ShopMainForm shopMainForm;

    public ShopCommand(JebeMarket plugin, ShopManager shopManager, Configuration config, PlayerInputHandler inputHandler, PlayerHeadManager playerHeadManager, GuiManager guiManager, ShopMainForm shopMainForm) {
        this.plugin = plugin;
        this.shopManager = shopManager;
        this.config = config;
        this.inputHandler = inputHandler;
        this.playerHeadManager = playerHeadManager;
        this.guiManager = guiManager;
        this.shopMainForm = shopMainForm;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(color(plugin.getI18nString("commands.errors.player_only")));
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "create":
                handleCreate(player, args);
                break;
            case "delete":
                handleDelete(player, args);
                break;
            case "edit":
                handleEdit(player, args);
                break;
            case "info":
                handleInfo(player, args);
                break;
            case "list":
                handleList(player);
                break;
            case "open":
                handleOpen(player, args);
                break;
            case "gui":
                handleGui(player, args);
                break;
            case "item":
                handleItem(player, args);
                break;
            case "help":
                sendHelp(player);
                break;
            default:
                player.sendMessage(color(plugin.getI18nString("commands.errors.unknown_command")));
        }
        return true;
    }

    //    ================== 商铺管理 ==============
    private void handleCreate(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(color(plugin.getI18nString("commands.create.usage")));
            return;
        }

        String shopName = args[1];
        shopManager.createShop(shopName, player.getUniqueId());
    }

    private void handleDelete(Player player, String[] args) {
        if (args.length < 3 || !args[2].equalsIgnoreCase("yes")) {
            player.sendMessage(color(plugin.getI18nString("commands.delete.usage")));
            return;
        }

        Shop shop = shopManager.getShop(args[1]);
        if (shop == null) {
            player.sendMessage(color(plugin.getI18nString("commands.errors.shop_not_found")));
            return;
        }

        if (checkPermission(shop, player)) return;

        if (shopManager.deleteShop(shop.getUuid())) {
            player.sendMessage(color(plugin.getI18nString("commands.delete.success")));
        } else {
            player.sendMessage(color(plugin.getI18nString("commands.delete.errors.not_empty")));
        }

    }

    private void handleEdit(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(color(plugin.getI18nString("commands.edit.usage")));
            return;
        }

        String editType = args[1].toLowerCase();
        String shopName = args[2];

        try {
            Shop shop = shopManager.getShop(shopName);
            if (shop == null) {
                player.sendMessage(color(plugin.getI18nString("commands.errors.invalid_type")));
                return;
            }
            if (checkPermission(shop, player)) return;
            switch (editType) {
                case "name":
                    handleEditName(player, args, shop);
                    break;
                case "lore":
                    handleEditLore(player, args, shop);
                    break;
                case "owner":
                    handleEditOwner(player, args, shop);
                    break;
                default:
                    player.sendMessage(color("&c无效的编辑类型"));
            }
        } catch (IllegalArgumentException e) {
            player.sendMessage(color(plugin.getI18nString("commands.errors.invalid_shop_name")));
        }
    }

    private void handleEditName(Player player, String[] args, Shop shop) {
        if (args.length < 3) {
            player.sendMessage(color(plugin.getI18nString("commands.edit.name.usage")));
            return;
        }

        String newName = args[3];
        if (shopManager.updateShopName(shop, newName, player)) {
            player.sendMessage(color(plugin.getI18nString("transaction.edit.name.success")));
        } else {
            player.sendMessage(color(plugin.getI18nString("commands.edit.name.errors.duplicate")));
        }
    }

    private void handleEditLore(Player player, String[] args, Shop shop) {
        if (args.length < 3) {
            player.sendMessage(color(plugin.getI18nString("commands.edit.lore.usage")));
            return;
        }
        String lore = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
        shop.setLore(lore);
        if (shopManager.updateShopLore(shop)) {
            player.sendMessage(color(plugin.getI18nString("commands.edit.lore.success")));
        } else {
            player.sendMessage(color(plugin.getI18nString("commands.edit.lore.errors.duplicate")));
        }
    }

    private void handleEditOwner(Player player, String[] args, Shop shop) {
        if (args.length < 5 || !args[4].equalsIgnoreCase("yes")) {
            player.sendMessage(color(plugin.getI18nString("commands.edit.owner.usage")));
            return;
        }

        Player newOwner = Bukkit.getPlayer(args[3]);
        if (newOwner == null) {
            player.sendMessage(color(plugin.getI18nString("commands.edit.owner.errors.player_offline")));
            return;
        }
        if (newOwner.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(color(plugin.getI18nString("commands.edit.owner.errors.self")));
        }

        if (shopManager.updateShopOwner(shop, newOwner)) {
            player.sendMessage(color(plugin.getI18nString("commands.edit.owner.success").replace("%s", newOwner.getName())));
        } else {
            player.sendMessage(color(plugin.getI18nString("commands.edit.owner.errors.duplicate")));
        }

    }

    private void handleInfo(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(color(plugin.getI18nString("commands.info.usage")));
            return;
        }

        try {
            Shop shop = shopManager.getShop(args[1]);
            if (checkPermission(shop, player)) return;
            player.sendMessage(color(plugin.getI18nString("commands.info.header")));
            player.sendMessage(color(plugin.getI18nString("commands.info.entries.uid").replace("%s", shop.getUuid().toString())));
            player.sendMessage(color(plugin.getI18nString("commands.info.entries.name").replace("%s", shop.getName())));
            player.sendMessage(color(plugin.getI18nString("commands.info.entries.owner").replace("%s", Bukkit.getOfflinePlayer(shop.getOwner()).getName())));
            player.sendMessage(color(plugin.getI18nString("commands.info.entries.lore").replace("%s", shop.getLore())));
            player.sendMessage(color(plugin.getI18nString("commands.info.entries.item_count").replace("%s", String.valueOf(shopManager.getItemCount(shop.getUuid())))));
        } catch (IllegalArgumentException e) {
            player.sendMessage(color(plugin.getI18nString("commands.errors.invalid_shop_name")));
        }
    }

    private void handleList(Player player) {

        List<Shop> shops = shopManager.getShopsByOwner(player.getUniqueId());
        if (shops.isEmpty()) {
            player.sendMessage(color(plugin.getI18nString("commands.list.empty")));
            return;
        }

        player.sendMessage(color(plugin.getI18nString("commands.list.header")));
        shops.forEach(shop -> {
            String entry = plugin.getI18nString("commands.list.entry").replace("%name", shop.getName()).replace("%uid", shop.getUuid().toString());
            player.sendMessage(color(entry));
        });
    }

    private void handleOpen(@NotNull Player player, @NotNull String[] args) {
        if (args.length < 2) {
            player.sendMessage(color(plugin.getI18nString("commands.open.usage")));
        }
        try {
            Shop shop = shopManager.getShop(args[1]);
            if (shop == null) {
                player.sendMessage(color(plugin.getI18nString("commands.errors.shop_not_found")));
                return;
            }
            guiManager.openGuiWithContext(player, GUIType.SHOP_DETAILS, shop);
        } catch (Exception e) {
            player.sendMessage(color(plugin.getI18nString("commands.errors.shop_not_found")));
        }
    }

    private void handleGui(@NotNull Player player, @NotNull String[] args) {
        if (args.length < 1) {
            player.sendMessage(color(plugin.getI18nString("commands.gui.usage")));
            return;
        }
        FloodgatePlayer floodgatePlayer = FloodgateApi.getInstance().getPlayer(player.getUniqueId());
        if (floodgatePlayer != null) {
//            if (floodgatePlayer.getDeviceOs().equals(DeviceOs.GOOGLE)) {
            shopMainForm.toMainForm(player);
//            }
        } else {

            guiManager.openShopMainGui(player);
        }
    }

    //    ==================== 商品管理 ===============
    private void handleItem(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(color(plugin.getI18nString("commands.item.usage")));
            return;
        }

        String operation = args[1].toLowerCase();
        switch (operation) {
            case "up":
                handleItemUp(player, args);
                break;
            case "down":
                handleItemDown(player, args);
                break;
            case "edit":
                handleItemEdit(player, args);
                break;
            case "info":
                handleItemInfo(player, args);
                break;
            default:
                player.sendMessage(color("&4用法: /shop item <up/down/info/edit>"));
                break;
        }
    }

    private void handleItemUp(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(color(plugin.getI18nString("commands.item.up.usage")));
            return;
        }

        try {
            Shop shop = shopManager.getShop(args[3]);
            if (checkPermission(shop, player)) return;
            switch (args[2].toLowerCase()) {
                case "hand":
                    ShopItem shopIte = new ShopItem(shop.getUuid(), player.getInventory().getItemInMainHand());
                    try {
                        double price = Double.parseDouble(args[4]);
                        double max_price = plugin.getConfig().getDouble("settings.item.max_price");
                        if (price < 0) {
                            player.sendMessage(color(plugin.getI18nString("commands.item.up.hand.errors.invalid_price")));
                            return;
                        }

                        if (price > max_price) {
                            player.sendMessage(color(plugin.getI18nString("commands.item.up.hand.errors.max_price").replace("%max%", String.valueOf(max_price))));
                            return;
                        }
                        shopIte.setPrice(price);
                    } catch (Exception e) {
                        player.sendMessage(color(plugin.getI18nString("commands.item.up.hand.errors.hand_price")));
                    }
                    shopManager.addHandItem(shopIte, player);
                    break;
                case "inventory":
                    shopManager.addInventoryItem(shop.getUuid(), player);
                    break;
                default:
                    player.sendMessage(color(plugin.getI18nString("commands.item.errors.invalid_source")));
            }
        } catch (IllegalArgumentException e) {
            player.sendMessage(color(plugin.getI18nString("commands.errors.invalid_shop_name")));
        }
    }

    private void handleItemDown(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(color(plugin.getI18nString("commands.item.down.usage")));
            return;
        }

        try {
            Shop shop = shopManager.getShop(args[2]);
            if (args.length < 4) {
                player.sendMessage(color(plugin.getI18nString("commands.item.down.errors.missing_id")));
                return;
            }
            String text = args[3];
            if (text.equals("all")) {
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
            player.sendMessage(color(plugin.getI18nString("commands.item.errors.invalid_id")));
        }
    }

    private void handleItemEdit(Player player, String[] args) {
        if (args.length < 5) player.sendMessage(color(plugin.getI18nString("commands.item.edit.usage")));
        try {
            Shop shop = shopManager.getShop(args[2]);

            ShopItem shopItem = shopManager.getItem(shop.getUuid(), UUID.fromString(args[3]));

            double price = Double.parseDouble(args[4]);

            price = Math.round(price * 100) / 100.0;

            shopItem.setPrice(price);

            if (shopManager.updatePrice(shopItem, player)) {
                player.sendMessage(color(plugin.getI18nString("commands.item.edit.success").replace("%.2f", String.valueOf(price))));
            }
        } catch (Exception e) {
            player.sendMessage(color(plugin.getI18nString("commands.item.edit.usage")));
        }
    }

    private void handleItemInfo(Player player, String[] args) {
        try {
            Shop shop = shopManager.getShop(args[2]);
            List<ShopItem> shopItems = shopManager.getItems(shop.getUuid());

            if (shopItems.isEmpty()) {
                player.sendMessage(color(plugin.getI18nString("commands.item.info.empty")));
                return;
            }
            for (ShopItem shopItem : shopItems) {
                ItemStack itemStack = shopItem.getItemStack();
                if (itemStack != null && itemStack.getItemMeta() != null) {
                    String uuid = shopItem.getUuid().toString();
                    String name = itemStack.getI18NDisplayName() != null ? itemStack.getI18NDisplayName() : plugin.getI18nString("commands.item.error.unknown_item");

                    Component hoverComponent = Component.text(plugin.getI18nString("commands.item.info.hover_text").replace("%s", uuid));

                    Component priceComponent = Component.text(color(plugin.getI18nString("commands.item.info.price_format").replace("%.2f", new DecimalFormat("#.00").format(shopItem.getPrice()))));
                    ;
                    // 构建完整消息组件
                    Component message = Component.text()
                            .append(Component.text("[")
                                    .color(NamedTextColor.DARK_GRAY))
                            .append(Component.text(uuid.substring(0, 8))
                                    .color(NamedTextColor.YELLOW)
                                    .hoverEvent(HoverEvent.showText(hoverComponent)))
                            .append(Component.text("] ")
                                    .color(NamedTextColor.DARK_GRAY))
                            .append(Component.text(name).color(NamedTextColor.WHITE))
                            .append(Component.text(" (x")
                                    .color(NamedTextColor.DARK_GRAY))
                            .append(Component.text(itemStack.getAmount())
                                    .color(NamedTextColor.YELLOW))
                            .append(Component.text(") ")
                                    .color(NamedTextColor.DARK_GRAY)).append(priceComponent).build();
                    player.sendMessage(message);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(color(plugin.getI18nString("commands.errors.unknown_error")));
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(color(plugin.getI18nString("commands.help.header")));
        plugin.getStringList("commands.help.entries").forEach(entry -> sender.sendMessage(color(entry)));
    }

    private void givePlayerItemStack(Player player, ItemStack itemStack) {
        if (itemStack != null) {
            if (player.getInventory().firstEmpty() == -1) {
                player.getWorld().dropItem(player.getLocation(), itemStack);
                player.sendMessage(color(plugin.getI18nString("commands.item.down.errors.inventory_full")));
            } else {
                player.getInventory().addItem(itemStack);
            }
            player.sendMessage(color(plugin.getI18nString("commands.item.down.success")));
        } else {
            player.sendMessage(color(plugin.getI18nString("commands.item.errors.not_found")));
        }
    }

    public boolean checkPermission(Shop shop, Player player) {
        if (!shop.getOwner().equals(player.getUniqueId())) {
            player.sendMessage(color(plugin.getI18nString("commands.errors.no_permission")));
        }
        return !shop.getOwner().equals(player.getUniqueId());
    }
}