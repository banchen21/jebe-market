package org.bc.jebeMarketCore.service;

import org.bc.jebeMarketCore.JebeMarket;
import org.bc.jebeMarketCore.api.ShopManager;
import org.bc.jebeMarketCore.model.Shop;
import org.bc.jebeMarketCore.model.ShopItem;
import org.bc.jebeMarketCore.repository.ShopServiceImpl;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

import static org.bc.jebeMarketCore.utils.MessageUtils.color;


public class ShopManagerImpl implements ShopManager {
    // 配置路径常量
    private static final String CONFIG_PREFIX = "settings.shop.";
    private static final String MSG_PREFIX = "commands.";
    private static final String TXN_PREFIX = "transaction.";


    ShopServiceImpl shopService;
    private final JebeMarket plugin;

    public ShopManagerImpl(ShopServiceImpl shopService, JebeMarket plugin) {
        this.shopService = shopService;
        this.plugin = plugin;
    }

    @Override
    public List<Shop> getShopsByOwner(UUID playerId) {
        return shopService.getShopsByOwner(playerId);
    }

    @Override
    public Shop createShop(String shopName, UUID owner) {
        int maxShops = plugin.getConfig().getInt(CONFIG_PREFIX + "create.create_limit", 5);
        double createCost = plugin.getConfig().getDouble(CONFIG_PREFIX + "create.cost", 1000.0);
        List<String> bannedWords = plugin.getConfig().getStringList(CONFIG_PREFIX + "create.filter.banned_words");

        Player player = plugin.getServer().getPlayer(owner);
//         检查是否玩家
        if (player == null) {
            plugin.getLogger().warning(color(plugin.getI18nString("commands.errors.player_only")));
            return null;
        }

//        检查名称是否满足字数条件
        int minShopNameLength = plugin.getInt("settings.shop.create.min_name_length");
        int maxShopNameLength = plugin.getInt("settings.shop.create.max_name_length");
        if (shopName.length() < minShopNameLength || shopName.length() > maxShopNameLength) {
            player.sendMessage(color(plugin.getI18nString("commands.create.errors.name_length").replace("%a", String.valueOf(minShopNameLength)).replace("%b", String.valueOf(maxShopNameLength))));
        }

//         检查名称是否在禁止列表中
        if (isShopNameBanned(bannedWords, shopName)) {
            player.sendMessage(color(plugin.getI18nString(MSG_PREFIX + "create.errors.invalid_name")));
            return null;
        }

//         检查是否达到最大商店数量
        if (shopService.getShopsByOwner(owner).size() >= maxShops) {
            player.sendMessage(color(plugin.getI18nString(MSG_PREFIX + "create.errors.max_limit")));
            return null;
        }

//         检查玩家是否有足够的资金创建商店
        if (!plugin.getLabor_econ().has(player, createCost)) {
            player.sendMessage(color(plugin.getI18nString(TXN_PREFIX + "errors.insufficient_funds").replace("%cost%", String.valueOf(createCost))));
            return null;
        }

        Shop newshop = new Shop(shopName, owner);
        if (shopService.createShop(newshop)) {
            plugin.getLabor_econ().withdrawPlayer(player, createCost);
            player.sendMessage(color(plugin.getI18nString(MSG_PREFIX + "create.cost_message").replace("%cost%", String.valueOf(createCost))));

            player.sendMessage(color(plugin.getI18nString("commands.create.success").replace("%name", shopName).replace("%uid", newshop.getUuid().toString())));
            return newshop;
        } else {
            String successMsg = plugin.getI18nString(MSG_PREFIX + "create.errors.duplicate_name");
            player.sendMessage(color(successMsg));
        }
        return null;
    }

    //    禁止使用的商品名称关键词
    public boolean isShopNameBanned(List<String> stringList, String shopName) {
        for (String s : stringList) {
            if (shopName.contains(s)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Shop getShop(String name) {
        return shopService.findByName(name);
    }

    @Override
    public Shop getShop(UUID uuid) {
        return shopService.findByUuid(uuid);
    }

    @Override
    public boolean updateShopName(Shop shop, String newName, Player player) {
        int minNameLength = plugin.getConfig().getInt("settings.shop.create.min_name_length");
        int maxNameLength = plugin.getConfig().getInt("settings.shop.create.max_name_length");

        if (newName.length() < minNameLength || newName.length() > maxNameLength) {
            player.sendMessage(color(plugin.getI18nString("commands.edit.name.input").replace("%a", String.valueOf(minNameLength)).replace("%b", String.valueOf(maxNameLength))));
            return false;
        }

        double renameCost = plugin.getConfig().getDouble(CONFIG_PREFIX + "edit.name.cost", 500.0);

        if (plugin.getLabor_econ().has(player, renameCost)) {
            shop.setName(newName);
            if (shopService.updateShopName(shop)) {
                plugin.getLabor_econ().withdrawPlayer(player, renameCost);

                String successMsg = plugin.getI18nString("commands.edit.name.success_message").replace("%cost%", String.valueOf(renameCost));
                player.sendMessage(color(successMsg));
                return true;
            }
        } else {
            player.sendMessage(color(plugin.getI18nString(TXN_PREFIX + "errors.insufficient_funds").replace("%cost%", String.valueOf(renameCost))));
        }
        return false;
    }

    @Override
    public boolean updateShopLore(Shop shop) {
        // 从配置获取参数（带默认值）
        double descCost = plugin.getConfig().getDouble("settings.shop.desc_cost", 300.0);
        int maxLoreLength = plugin.getConfig().getInt("settings.shop.edit.lore.max_length");


        Player player = plugin.getServer().getPlayer(shop.getOwner());

        // 验证描述长度
        if (shop.getLore().length() > maxLoreLength) {
            player.sendMessage(color(plugin.getI18nString("commands.edit.lore.errors.length")).replace("%max%", String.valueOf(maxLoreLength)));
            return false;
        }

        // 扣费逻辑
        if (plugin.getLabor_econ().has(player, descCost)) {
            if (shopService.updateShopLore(shop)) {
                plugin.getLabor_econ().withdrawPlayer(player, descCost);
                String successMsg = plugin.getI18nString("commands.edit.lore.success_message").replace("%cost%", String.valueOf(descCost));
                player.sendMessage(color(successMsg));
                return true;
            } else {
                String fundsError = plugin.getI18nString("transaction.errors.insufficient_funds").replace("%cost%", String.valueOf(descCost));
                player.sendMessage(color(fundsError));
            }
        }
        return false;
    }

    @Override
    public boolean updateShopOwner(Shop shop, Player newOwner) {
        Player player = plugin.getServer().getPlayer(shop.getOwner());
        List<Shop> shopList = shopService.getShopsByOwner(newOwner.getUniqueId());
        if (shopList.size() >= plugin.getConfig().getInt("settings.shop.create.create_limit")) {
            player.sendMessage(color(plugin.getI18nString("commands.edit.owner.errors.max_limit")));
            return false;
        }

        double transferCost = plugin.getConfig().getDouble("settings.shop.edit.owner");

        if (plugin.getLabor_econ().has(player, transferCost)) {
            shop.setOwner(newOwner.getUniqueId());
            if (shopService.updateShopOwner(shop)) {
                plugin.getLabor_econ().withdrawPlayer(player, transferCost);
                String successMsg = plugin.getI18nString("commands.edit.owner.success_message").replace("%cost%", String.valueOf(transferCost));
                player.sendMessage(color(successMsg));
                return true;
            }
        } else {
            player.sendMessage(color(plugin.getI18nString(TXN_PREFIX + "errors.insufficient_funds").replace("%cost%", String.valueOf(transferCost))));
        }
        return false;
    }


    @Override
    public boolean deleteShop(UUID shopUuid) {
        Shop shop = shopService.findByUuid(shopUuid);
        if (shopService.getItemsByShop(shopUuid).isEmpty()) {
            shopService.deleteShop(shop.getUuid());
            return true;
        }
        return false;

    }

    @Override
    public List<Shop> getShops() {
        return shopService.getAllShops();
    }

    @Override
    public int getItemCount(UUID shopUuid) {
        return shopService.getItemsByShop(shopUuid).size();
    }

    @Override
    public boolean addHandItem(ShopItem shopItem, Player player) {
        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem.getAmount() == 0) {
            player.sendMessage(color(plugin.getI18nString("commands.item.up.errors.hand")));
            return false;
        }
        double transferCost = plugin.getConfig().getDouble("settings.item.up.cost");

        if (plugin.getLabor_econ().has(player, transferCost)) {
            if (shopService.addItem(shopItem)) {
                handItem.setAmount(0);
                plugin.getLabor_econ().withdrawPlayer(player, transferCost);
                String successMsg = plugin.getI18nString("commands.item.up.success_message").replace("%cost%", String.valueOf(transferCost));
                player.sendMessage(color(successMsg));
                return true;
            }
        } else {
            player.sendMessage(color(plugin.getI18nString(TXN_PREFIX + "errors.insufficient_funds").replace("%cost%", String.valueOf(transferCost))));
        }
        return false;
    }

    @Override
    public void addInventoryItem(UUID shopUuid, Player player) {
        if (player.getInventory().isEmpty()) {
            player.sendMessage(color(plugin.getI18nString("commands.item.up.inventory.errors.no_items")));
            return;
        }
        double transferCost = plugin.getConfig().getDouble("settings.item.up.cost");
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack != null && !stack.getType().isAir()) {
                ShopItem shopItem = new ShopItem(shopUuid, stack.clone());
                if (plugin.getLabor_econ().has(player, transferCost)) {
                    if (shopService.addItem(shopItem)) {
                        stack.setAmount(0);
                        plugin.getLabor_econ().withdrawPlayer(player, transferCost);
                        String successMsg = plugin.getI18nString("commands.item.up.success_message").replace("%cost%", String.valueOf(transferCost));
                        player.sendMessage(color(successMsg));
                    }
                } else {
                    player.sendMessage(color(plugin.getI18nString(TXN_PREFIX + "errors.insufficient_funds").replace("%cost%", String.valueOf(transferCost))));
                }
            }
        }
    }

    @Override
    public ItemStack removeItem(Shop shop, UUID itemId) {
        return shopService.removeItem(shop, itemId);
    }

    @Override
    public List<ShopItem> getItems(UUID shopUuid) {
        return shopService.getItemsByShop(shopUuid);
    }

    @Override
    public ShopItem getItem(UUID shopUuid, UUID itemId) {
        return shopService.getItemById(shopUuid, itemId);
    }

    @Override
    public boolean updatePrice(ShopItem shopItem, Player player) {
        double price = shopItem.getPrice();
        double maxPrice = plugin.getConfig().getDouble("settings.item.max_price");
        if (price < 0 || price > maxPrice) {
            player.sendMessage(color(plugin.getI18nString("commands.item.edit.error.range_error").replace("%max%", String.valueOf(maxPrice))));
            return false;
        } else {
            return shopService.updatePrice(shopItem);
        }
    }

    @Override
    public boolean updateItemStack(ShopItem shopItem) {
        return shopService.updateItemStack(shopItem);
    }
}
