// MemoryShopRepository.java
package org.bc.jebeMarketCore.repository;

import ca.tweetzy.skulls.flight.utils.QuickItem;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import lombok.Getter;
import org.bc.jebeMarketCore.JebeMarket;
import org.bc.jebeMarketCore.MarketPulseUtil.ItemStorageUtil;
import org.bc.jebeMarketCore.i18n.JebeMarketTranslations;
import org.bc.jebeMarketCore.model.Shop;
import org.bc.jebeMarketCore.model.ShopItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

import static org.bc.jebeMarketCore.JebeMarket.JEBEMARKETPATH;

/**
 * 内存实现的商店仓库
 */
public class FileShopRepository implements ShopRepository {

    private final String shopSPath = JEBEMARKETPATH + "shops.json";
    private final String shopDataSPath = JEBEMARKETPATH + "shops/";
    private final String shopDataPath = JEBEMARKETPATH + "shopData/";

    private final JebeMarket plugin;
    @Getter
    List<ItemStack> shops = new ArrayList<>();
    @Getter
    LinkedHashMap<UUID, UUID> uuiduuidLinkedHashMap = new LinkedHashMap<>();

    public FileShopRepository(JebeMarket plugin) {
        this.plugin = plugin;
        loadPlayerShop();
    }

    /**
     * 清除缓存重加载玩家商店
     */
    public void loadPlayerShop() {
        shops.clear();
        uuiduuidLinkedHashMap.clear();
        File dataFile = new File(shopSPath);
        try (FileReader reader = new FileReader(dataFile)) {
            Gson gson = new Gson();
//            商铺、玩家
            LinkedHashMap<UUID, UUID> fileShops = gson.fromJson(reader, new TypeToken<LinkedHashMap<UUID, UUID>>() {
            }.getType());
            fileShops.forEach((shop, player) -> {
                ItemStack itemStack = checkData(shop);
                if (itemStack != null) {
                    shops.add(itemStack);
                    uuiduuidLinkedHashMap.put(shop, player);
                } else {
                    plugin.getLogger().severe(JebeMarketTranslations.getLocalizedMessage("file.read.failed"));
                }
            });
        } catch (IOException ignored) {
        }
    }

    /**
     * 尝试加载商店数据
     *
     * @param shopUuid shopUuid
     * @return ItemStack
     */
    public ItemStack checkData(UUID shopUuid) {
        File file1 = new File(shopDataSPath + shopUuid + ".dat");
        try {
            return getItemStack(file1);
        } catch (ClassNotFoundException | IOException e) {
            plugin.getLogger().severe(JebeMarketTranslations.getLocalizedMessage("file.read.failed") + ":" + file1.getName());
            return null;
        }
    }

    /**
     * 获取物品
     *
     * @param file1 file1
     * @return ItemStack
     */
    @Nullable
    private static ItemStack getItemStack(File file1) throws IOException, ClassNotFoundException {
        if (file1.exists()) {
            FileInputStream fileIn = new FileInputStream(file1);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            byte[] data = (byte[]) in.readObject();
            return ItemStorageUtil.deserializeItemStack(data);
        } else {
            return null;
        }
    }

    @Override
    public Shop AddShop(Shop shop) {
//        获取玩家是否在线
        OfflinePlayer player = Bukkit.getOfflinePlayer(shop.getOwner());
        ItemStack playerHandItemStack = getPlayerHandItemStack(player.getPlayer(), shop);

        saveShopDataItemMetaStr(playerHandItemStack, "owner", shop.getOwner().toString());
        saveShopDataItemMetaBoolean(playerHandItemStack, "type", shop.isType());
        saveShopDataItemMetaName(playerHandItemStack, shop.getName());
        saveShopDataItemMetaLore(playerHandItemStack, shop.getLore());

        uuiduuidLinkedHashMap.put(shop.getId(), shop.getOwner());
        saveShopJson();
        try {
            saveShopDataFile(playerHandItemStack, shop.getId());
        } catch (IOException e) {
            // 使用插件Logger记录异常信息（带堆栈跟踪）
            plugin.getLogger().log(
                    Level.SEVERE,
                    "Failed to save shop data for shop ID: " + shop.getId(),
                    e
            );
        }

        shops.add(playerHandItemStack);
        return shop;
    }

    // 名称清洗工具方法
    private String cleanShopName(String rawName) {
        // 去除颜色代码
        String withoutColor = ChatColor.stripColor(rawName);
        // 去除前后空格
        return withoutColor.trim().toLowerCase();
    }

    public void saveShopJson() {
        File dataFile = new File(shopSPath);
        try (FileWriter writer = new FileWriter(dataFile)) {
            Gson gson = new Gson();
            gson.toJson(uuiduuidLinkedHashMap, writer);
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //    保存物品别名
    private void saveShopDataItemMetaName(ItemStack playerHead, String name) {
        ItemMeta itemMeta = playerHead.getItemMeta();
        itemMeta.setDisplayName(name);
        playerHead.setItemMeta(itemMeta);
    }

    //    保存物品lore
    private void saveShopDataItemMetaLore(ItemStack playerHead, List<String> data) {
        ItemMeta itemMeta = playerHead.getItemMeta();
        itemMeta.setLore(data);
        playerHead.setItemMeta(itemMeta);
    }

    //    保存数据持久化到物品中
    private void saveShopDataItemMetaStr(ItemStack playerHead, String key, String data) {
        ItemMeta itemMeta = playerHead.getItemMeta();
        NamespacedKey namespacedKey = new NamespacedKey(plugin, key);
        itemMeta.getPersistentDataContainer().set(namespacedKey, PersistentDataType.STRING, data);
        playerHead.setItemMeta(itemMeta);
    }

    private void saveShopDataItemMetaBoolean(ItemStack playerHead, String key, boolean data) {
        ItemMeta itemMeta = playerHead.getItemMeta();
        NamespacedKey namespacedKey = new NamespacedKey(plugin, key);
        itemMeta.getPersistentDataContainer().set(namespacedKey, PersistentDataType.BOOLEAN, data);
        playerHead.setItemMeta(itemMeta);
    }

    // 从物品中读取数据
    private String getItemStackString(ItemStack playerHead, String key) {
        NamespacedKey statusKey = new NamespacedKey(plugin, key);
        PersistentDataContainer container = playerHead.getItemMeta().getPersistentDataContainer();
        if (container.has(statusKey, PersistentDataType.STRING)) {
            PersistentDataContainer pdc = playerHead.getItemMeta().getPersistentDataContainer();
            return pdc.get(statusKey, PersistentDataType.STRING);
        }
        return null;
    }

    // 从物品中读取数据
    private Boolean getItemStackBoolean(ItemStack playerHead, String key) {
        NamespacedKey statusKey = new NamespacedKey(plugin, key);
        PersistentDataContainer container = playerHead.getItemMeta().getPersistentDataContainer();
        if (container.has(statusKey, PersistentDataType.BOOLEAN)) {
            PersistentDataContainer pdc = playerHead.getItemMeta().getPersistentDataContainer();
            return pdc.get(statusKey, PersistentDataType.BOOLEAN);
        }
        return null;
    }

    //    读取物品中的数据
    private Double getItemStackDouble(ItemStack playerHead, String key) {

        NamespacedKey statusKey = new NamespacedKey(plugin, key);
        PersistentDataContainer container = playerHead.getItemMeta().getPersistentDataContainer();
        if (container.has(statusKey, PersistentDataType.DOUBLE)) {
            PersistentDataContainer pdc = playerHead.getItemMeta().getPersistentDataContainer();
            return pdc.get(statusKey, PersistentDataType.DOUBLE);
        }
        return null;
    }

    /**
     * 获取玩家头颅
     *
     * @param player Player
     * @param shop   Shop
     * @return ItemStack
     */
    private static @NotNull ItemStack getPlayerHandItemStack(Player player, Shop shop) {
        ItemStack playerHead = QuickItem.of(player).name(player.getName()).amount(1).make();
        ItemMeta meta = playerHead.getItemMeta();
        meta.setDisplayName(shop.getName());
        meta.setLore(shop.getLore());
        playerHead.setItemMeta(meta);
        return playerHead;
    }

    /**
     * 保存商店数据文件
     *
     * @param playerHead ItemStack
     * @param uuid       UUID
     */
    public void saveShopDataFile(ItemStack playerHead, UUID uuid) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(shopDataSPath + uuid + ".dat");
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(ItemStorageUtil.serializeItemStack(playerHead));
        out.flush();
        out.close();
        loadPlayerShop();
    }

    public List<ShopItem> getShopItem(UUID shopId) throws IOException, ClassNotFoundException {
        List<ShopItem> shops = new ArrayList<>();
        File file = new File(shopDataPath + shopId + "/");
        if (file.exists()) {
            for (File file1 : Objects.requireNonNull(file.listFiles())) {
                ItemStack itemStack = getItemStack(file1);
                ShopItem shopItem = new ShopItem();
                shopItem.setId(UUID.fromString(file1.getName().replace(".dat", "")));
                if (itemStack != null) {
                    shopItem.setName(itemStack.getItemMeta().getDisplayName());
                    shopItem.setPrice(getItemStackDouble(itemStack, "price"));
                    shopItem.setIcon_type(getItemStackString(itemStack, "icon_type"));
                    shopItem.setIcon_url(getItemStackString(itemStack, "icon_url"));
                }
                shops.add(shopItem);
            }
        }
        return shops;
    }

    @Override
    public Optional<Shop> findById(UUID id) throws IOException, ClassNotFoundException {
        ItemStack itemStack = checkData(id);
        String owner = getItemStackString(itemStack, "owner");
        boolean type = getItemStackBoolean(itemStack, "type");
        return Optional.of(new Shop(id, id, itemStack, owner, type, getShopItem(id)));
    }

    @Override
    public List<Shop> findByOwner(UUID ownerId) throws IOException, ClassNotFoundException {
        List<Shop> shops = new ArrayList<>();
        for (UUID shopuuid : uuiduuidLinkedHashMap.keySet()) {
            ItemStack itemStack = checkData(shopuuid);
            String owner = getItemStackString(itemStack, "owner");
            boolean type = Boolean.TRUE.equals(getItemStackBoolean(itemStack, "type"));
            if (owner != null && UUID.fromString(owner).equals(ownerId)) {
                Shop shop = new Shop(ownerId, shopuuid, itemStack, owner, type, getShopItem(shopuuid));
                shops.add(shop);
            }
        }
        return shops;
    }

    @Override
    public boolean delete(UUID id) {
        shops.remove(checkData(id));
        uuiduuidLinkedHashMap.remove(id);
        File dir = new File(shopDataPath + File.separator + id);
        if (!dir.exists() || !dir.isDirectory()) {
            return true;
        }
        File[] files = dir.listFiles();
        if (files == null) {
            return true;
        }

        for (File f : files) {
            if (f.isFile()) {
                return false;
            }
        }

        return true;
    }

}