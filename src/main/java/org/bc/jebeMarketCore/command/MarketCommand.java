package org.bc.jebeMarketCore.command;

import lombok.extern.slf4j.Slf4j;
import org.bc.jebeMarketCore.api.ItemManager;
import org.bc.jebeMarketCore.api.ShopManager;
import org.bc.jebeMarketCore.config.Configuration;
import org.bc.jebeMarketCore.i18n.JebeMarketTranslations;
import org.bc.jebeMarketCore.model.Shop;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
public class MarketCommand implements CommandExecutor {

    // 统一消息前缀
    final String prefix = JebeMarketTranslations.getLocalizedMessage("message.prefix");
    final String errorPrefix = JebeMarketTranslations.getLocalizedMessage("successPrefix");
    final String successPrefix = JebeMarketTranslations.getLocalizedMessage("successPrefix");
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
        if (!ValidatePlayer(sender)) return false;
        Player player = (Player) sender;
        if (args.length < 1) {
            return false;
        }

        switch (args[0]) {
            case "shop":
                HandleShopCommands(player, args);
                break;
            case "item":
                HandleItemCommands(player, args);
                break;
        }
        return false;
    }

    private void HandleShopCommands(Player player, String[] args) {

        if (args.length < 2) {
            sendUsage(player, JebeMarketTranslations.getLocalizedMessage("command.shop.help.title"), JebeMarketTranslations.getLocalizedMessage("command.shop.help.description.create"), JebeMarketTranslations.getLocalizedMessage("command.shop.help.description.edit"), JebeMarketTranslations.getLocalizedMessage("command.shop.help.description.delete"), JebeMarketTranslations.getLocalizedMessage("command.shop.help.description.transfer"), JebeMarketTranslations.getLocalizedMessage("command.shop.help.description.list"));
            return;
        }

        switch (args[1].toLowerCase()) {
            case "create": {
//                if (!checkPermission(player, "jebe.shop.create")) return;

                if (args.length < 3) {
                    sendFormattedError(player, JebeMarketTranslations.getLocalizedMessage("command.shop.help.description.create"));
                    return;
                }
                Shop shop = shopManager.createShop(player.getUniqueId(), args[2]);
                player.sendMessage(prefix + successPrefix + "商店创建成功!", " §8▪ §7商店ID: §f" + shop.getId(), " §8▪ §7商店名称: §e" + shop.getName());
                break;
            }
            case "list": {
                if (!checkPermission(player, "jebe.shop.list")) return;

                try {
                    List<Shop> shops = shopManager.getPlayerShops(player.getUniqueId());
                    if (shops.isEmpty()) {
                        player.sendMessage(prefix + "§7您还没有创建任何商店");
                        return;
                    }

                    int pageSize = config.getInt("shop.list.page"); // 每页显示 5 个商店
                    int currentPage = 1; // 默认显示第一页

                    // 检查用户是否提供了页码参数
                    if (args.length > 2) {
                        if (isNumeric(args[2])) {
                            currentPage = Integer.parseInt(args[2]);
                        } else {
                            // 如果页码参数无效，提示用户
                            sendFormattedError(player, "无效的页码参数！请使用数字指定页码");
                            return;
                        }
                    }

                    // 计算总页数
                    int totalPages = (int) Math.ceil((double) shops.size() / pageSize);

                    // 验证页码是否有效
                    if (currentPage < 1 || currentPage > totalPages) {
                        sendFormattedError(player, "页码超出范围！请使用 1 ~ " + totalPages);
                        return;
                    }

                    // 计算当前页的索引起始和结束位置
                    int startIndex = (currentPage - 1) * pageSize;
                    int endIndex = Math.min(startIndex + pageSize, shops.size());

                    // 获取当前页的数据
                    List<Shop> pageShops = shops.subList(startIndex, endIndex);

                    // 发送分页信息
                    player.sendMessage(JebeMarketTranslations.getLocalizedMessage("command.shop.list.title"));
//                    您的商店列表 (§e%1$s§a个) - 页面 §e%2$s§a/§e%3$s
                    String message = JebeMarketTranslations.getLocalizedMessage("command.shop.list.total.shops");
                    player.sendMessage(message.replace("%1$s", String.valueOf(shops.size())).replace("%2$s", String.valueOf(currentPage)).replace("%3$s", String.valueOf(totalPages)));
                    player.sendMessage("");
                    pageShops.forEach(shop -> {
                        String type = shop.isType() ? JebeMarketTranslations.getLocalizedMessage("command.shop.type.public") : JebeMarketTranslations.getLocalizedMessage("command.shop.type.private");
                        player.sendMessage(JebeMarketTranslations.getLocalizedMessage("command.shop.list.uuid") + shop.getId());
                        player.sendMessage(JebeMarketTranslations.getLocalizedMessage("command.shop.list.name") + shop.getName());
                        player.sendMessage(JebeMarketTranslations.getLocalizedMessage("command.shop.list.type") + type);
                        player.sendMessage("");
                    });

                    // 发送分页提示
                    if (currentPage == totalPages) {
                        player.sendMessage(successPrefix + JebeMarketTranslations.getLocalizedMessage("command.shop.list.last.page"));
                    } else {
                        player.sendMessage(successPrefix + JebeMarketTranslations.getLocalizedMessage("command.shop.list.description") + (currentPage + 1) + JebeMarketTranslations.getLocalizedMessage("command.shop.list.next.page"));
                    }

                    player.sendMessage(JebeMarketTranslations.getLocalizedMessage("command.shop.divider"));

                } catch (Exception e) {
                    sendFormattedError(player, "获取商店列表时发生错误");
                }
                break;
            }
            case "edit": {
                if (!checkPermission(player, "jebe.shop.edit")) return;

                if (args.length < 4) {
                    sendUsage(player, JebeMarketTranslations.getLocalizedMessage("command.shop.edit.help.title"), JebeMarketTranslations.getLocalizedMessage("command.shop.edit.help.description.name"), JebeMarketTranslations.getLocalizedMessage("command.shop.edit.help.description.desc"), JebeMarketTranslations.getLocalizedMessage("command.shop.edit.help.description.type"));

                    return;
                }
                break;
            }

            case "delete": {
//                if (!checkPermission(player, "jebe.shop.delete")) return;
//
                if (args.length < 3) {
                    sendFormattedError(player, JebeMarketTranslations.getLocalizedMessage("command.shop.help.description.delete"));
                    return;
                }

                UUID shopId = UUID.fromString(args[2]);

                try {
                    if (shopManager.deleteShop(shopId)) {
                        player.sendMessage(prefix + successPrefix + "商店 §e" + shopId + " §a已删除");
                    } else {
                        sendFormattedError(player, "删除失败: 商店不存在或没有权限");
                    }
                } catch (IllegalArgumentException e) {
                    sendFormattedError(player, "无效的商店ID格式");
                }
                break;
            }

            case "transfer": {
//                if (!checkPermission(player, "jebe.shop.transfer")) return;
//
//                if (args.length < 4) {
//                    sendFormattedError(player, "用法: /market shop transfer <§e商店ID§c> <§b新所有者§c>");
//                    return;
//                }
//
//                // 实现转移逻辑...
//                player.sendMessage(prefix + successPrefix + "所有权已转移至 §b" + args[3]);
                break;
            }

            default: {
                sendFormattedError(player, "未知指令，输入/market shop 查看可用命令");
                break;
            }
        }
    }

    private boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // 辅助方法：发送带格式的错误信息
    private void sendFormattedError(Player player, String message) {
        player.sendMessage(errorPrefix + message);
    }

    // 辅助方法：发送多行使用说明
    private void sendUsage(Player player, String... lines) {
        String divider = JebeMarketTranslations.getLocalizedMessage("command.shop.divider");
        player.sendMessage(divider);
        Arrays.stream(lines).forEach(player::sendMessage);
        player.sendMessage(divider);
    }

    // 辅助方法：权限检查
    private boolean checkPermission(Player player, String permission) {
//        TODO 权限
        if (!player.hasPermission(permission)) {
            player.sendMessage(JebeMarketTranslations.getLocalizedMessage("command.no.permission"));
            return false;
        }
        return true;
    }

    private void HandleItemCommands(Player player, String[] args) {
        if (args.length < 2) {
            ShowUsage(player, "/market item <hand|edit|delete>");
        }
    }

    private boolean ValidatePlayer(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(JebeMarketTranslations.getLocalizedMessage("command.only.player"));
            return false;
        }
        return true;
    }

    private void ShowUsage(CommandSender sender, String message) {
        sender.sendMessage(message);
    }

}