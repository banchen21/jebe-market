package org.bc.jebeMarketCore.service;

import org.bc.jebeMarketCore.JebeMarket;
import org.bc.jebeMarketCore.api.ShopManager;
import org.bc.jebeMarketCore.model.Shop;
import org.bc.jebeMarketCore.model.ShopItem;
import org.bc.jebeMarketCore.repository.ShopServiceImpl;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

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
        // 获取配置参数（带默认值）
        int maxShops = plugin.getConfig().getInt(CONFIG_PREFIX + "max_shops", 5);
        double createCost = plugin.getConfig().getDouble(CONFIG_PREFIX + "create_cost", 1000.0);
        List<String> bannedWords = plugin.getConfig().getStringList("filter.banned_words");

        Player player = plugin.getServer().getPlayer(owner);

        // 使用配置消息
        if (isShopNameBanned(bannedWords, shopName)) {
            player.sendMessage(color(plugin.getString(MSG_PREFIX + "create.errors.invalid_name")));
            return null;
        }

        if (shopService.getShopsByOwner(owner).size() >= maxShops) {
            player.sendMessage(color(plugin.getString(MSG_PREFIX + "create.errors.max_limit")));
            return null;
        }

        if (plugin.getLabor_econ().has(player, createCost)) {
            plugin.getLabor_econ().withdrawPlayer(player, createCost);
            player.sendMessage(color(plugin.getString(MSG_PREFIX + "create.cost_message").replace("%cost%", String.valueOf(createCost))));
        }
        Shop shop = new Shop(shopName, owner);
        if (shopService.createShop(shop)) {
            player.sendMessage(color(plugin.getString(MSG_PREFIX + "create.success").replace("%name%", shopName).replace("%uid%", shop.getUuid().toString())));
            return shop;
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
    public boolean updateShopName(Shop shop) {
        double renameCost = plugin.getConfig().getDouble(CONFIG_PREFIX + "rename_cost", 500.0);
        Player player = plugin.getServer().getPlayer(shop.getOwner());

        if (plugin.getLabor_econ().has(player, renameCost)) {
            if (shopService.updateShopName(shop)) {
                plugin.getLabor_econ().withdrawPlayer(player, renameCost);
                player.sendMessage(color(plugin.getString(MSG_PREFIX + "edit.name.success")));
                return true;
            }
        }
        player.sendMessage(color(plugin.getString(TXN_PREFIX + "errors.insufficient_funds")));
        return false;
    }


    @Override
    public boolean updateShopOwner(Shop shop) {
        Player player = plugin.getServer().getPlayer(shop.getOwner());
        List<ShopItem> shopItems = shopService.getItemsByShop(shop.getUuid());
        if (shopItems.size() >= plugin.getConfig().getInt("shop_create_limit")) {
            player.sendMessage("§c商店添加抵达上限");
            return false;
        }
        if (plugin.getLabor_econ().has(player, plugin.getConfig().getInt("shop_transfer_cost"))) {
            if (shopService.updateShopOwner(shop)) {
                plugin.getLabor_econ().withdrawPlayer(player, plugin.getConfig().getInt("shop_transfer_cost"));
                player.sendMessage(color(plugin.getString("transaction.success.general")));
                return true;
            }
        }
        player.sendMessage(color(plugin.getString("transaction.errors.insufficient_funds")));
        return false;
    }

    @Override
    public boolean updateShopLore(Shop shop) {
        // 从配置获取参数（带默认值）
        double descCost = plugin.getConfig().getDouble("settings.shop.desc_cost", 300.0);
        int maxLoreLength = plugin.getConfig().getInt("settings.shop.max_lore_length", 256);

        Player player = plugin.getServer().getPlayer(shop.getOwner());

        // 验证描述长度
        if (shop.getLore().length() > maxLoreLength) {
            String errorMsg = plugin.getString("commands.edit.lore.errors.length")
                    .replace("%max%", String.valueOf(maxLoreLength));
            player.sendMessage(color(errorMsg));
            return false;
        }

        // 扣费逻辑
        if (plugin.getLabor_econ().has(player, descCost)) {
            if (shopService.updateShopLore(shop)) {
                plugin.getLabor_econ().withdrawPlayer(player, descCost);

                // 使用配置的成功消息
                String successMsg = plugin.getString("commands.edit.lore.success_message")
                        .replace("%cost%", String.valueOf(descCost));
                player.sendMessage(color(successMsg));
                return true;
            }
        }

        // 余额不足提示
        String fundsError = plugin.getString("transaction.errors.insufficient_funds")
                .replace("%cost%", String.valueOf(descCost));
        player.sendMessage(color(fundsError));
        return false;
    }


    @Override
    public boolean deleteShop(UUID shopUuid, boolean b) {
        return shopService.deleteShop(shopUuid);
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
    public ShopItem addItem(UUID shopUuid, @NotNull ItemStack clone) {

        Shop shop = shopService.findByUuid(shopUuid);
        Player player = plugin.getServer().getPlayer(shop.getOwner());
        List<ShopItem> shopItems = shopService.getItemsByShop(shopUuid);
        if (shopItems.size() >= plugin.getConfig().getInt("shop_item_limit")) {
            player.sendMessage("§c商店添加抵达上限");
            return null;
        }
        if (plugin.getLabor_econ().has(player, plugin.getConfig().getInt("item_list_cost"))) {
            ShopItem shopItem = new ShopItem(shopUuid, clone);
            shopItem.setPrice(9999999);
            if (shopService.addItem(shopItem)) {
                plugin.getLabor_econ().withdrawPlayer(player, plugin.getConfig().getInt("item_list_cost"));
                return shopItem;
            }
        }
        return null;
    }

    @Override
    public void addHandItem(UUID shopUuid, Player player) {
        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem.getAmount() == 0) {
            player.sendMessage(color("&c请手持要上架的商品"));
            return;
        }
        ShopItem shopItem = new ShopItem(shopUuid, handItem.clone());
        if (shopService.addItem(shopItem)) {
            handItem.setAmount(0);
            player.sendMessage(color(String.format("&a成功上架商品 %s", shopItem.getUuid().toString())));
        } else {
            player.sendMessage(color("&c添加商品失败"));
        }
    }

    @Override
    public void addInventoryItem(UUID shopUuid, Player player) {
        // 如果玩家背包为空就返回
        if (player.getInventory().isEmpty()) {
            player.sendMessage(color("&c背包为空"));
            return;
        }
        int count = 0;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack != null && !stack.getType().isAir()) {
                ShopItem shopItem = new ShopItem(shopUuid, stack.clone());
                if (shopService.addItem(shopItem)) {
                    stack.setAmount(0);
                    player.sendMessage(color(String.format("&a成功上架 %s", shopItem.getUuid().toString())));
                    count++;
                } else {
                    player.sendMessage(color("&c添加商品失败"));
                }
            }
        }
        player.sendMessage(color(String.format("&a成功上架 %d 种物品", count)));
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
    public boolean updatePrice(ShopItem shopItem) {
        Shop shop = shopService.findByUuid(shopItem.getShopuuid());
        Player player = plugin.getServer().getPlayer(shop.getOwner());
        if (shopItem.getPrice() <= plugin.getConfig().getInt("max_price")) {
            return shopService.updatePrice(shopItem);
        } else {
            player.sendMessage("§c价格超出最大价格");
            return false;
        }
    }

    @Override
    public boolean updateItemStack(ShopItem shopItem) {
        return shopService.updateItemStack(shopItem);
    }
}
