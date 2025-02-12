package org.bc.jebeMarketCore.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bc.jebeMarketCore.utils.ItemStorageUtil;
import org.bc.jebeMarketCore.dao.ShopDao;
import org.bc.jebeMarketCore.model.ShopItem;
import org.bc.jebeMarketCore.model.Shop;
import org.bc.jebeMarketCore.repository.ShopRepository;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import java.io.*;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.UUID;

public class ShopSqlite3Util implements ShopRepository {
    private final JavaPlugin plugin;
    private HikariDataSource dataSource;
    private Jdbi jdbi;

    HikariDataSource init() {
        HikariConfig config = new HikariConfig();
        // 使用插件数据文件夹的路径
        File databaseFile = new File(plugin.getDataFolder(), "database.db");
        config.setJdbcUrl("jdbc:sqlite:" + databaseFile.getAbsolutePath());
        config.setMaximumPoolSize(1); // SQLite 建议单连接
        return new HikariDataSource(config);
    }

    public ShopSqlite3Util(final JavaPlugin p_javaPlugin) throws SQLException {
        plugin = p_javaPlugin;
        // 创建数据文件夹
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        // 配置 HikariCP
        dataSource = init();
        // 初始化 JDBI
        jdbi = Jdbi.create(dataSource);
        // 在初始化 JDBI 时注册类型转换器
        jdbi.registerColumnMapper(UUID.class, (rs, columnNumber, ctx) ->
                UUID.fromString(rs.getString(columnNumber))
        );
        jdbi.registerArgument(new AbstractArgumentFactory<UUID>(Types.VARCHAR) {
            @Override
            protected Argument build(UUID value, ConfigRegistry config) {
                return (position, statement, ctx) ->
                        statement.setString(position, value.toString());
            }
        });

        jdbi.registerRowMapper(Shop.class, (rs, ctx) -> {
            UUID uuid = UUID.fromString(rs.getString("uuid"));
            String name = rs.getString("name");
            UUID owner = UUID.fromString(rs.getString("owner"));
            String lore = rs.getString("lore");
            return new Shop(uuid, name, owner, lore);
        });

        jdbi.registerRowMapper(ShopItem.class, (rs, ctx) -> {
            UUID uuid = UUID.fromString(rs.getString("uuid"));
            UUID shopuuid = UUID.fromString(rs.getString("shopuuid"));
            double price = rs.getDouble("price");
            try {
                Path dir = Paths.get(plugin.getDataFolder().getPath(), "items");
                Files.createDirectories(dir);
                Path path = dir.resolve(uuid.toString());

                try (FileInputStream fileInputStream = new FileInputStream(path.toFile());
                     ObjectInputStream in = new ObjectInputStream(fileInputStream)) {
                    byte[] data = (byte[]) in.readObject();
                    ItemStack itemStack = ItemStorageUtil.deserializeItemStack(data);
                    return new ShopItem(uuid, shopuuid, price, itemStack);
                }
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

        });

        jdbi.installPlugin(new SqlObjectPlugin());

        // 创建表
        jdbi.useHandle(handle -> {
            // 在 Sqlite3Util 的建表语句中修改主键为 uuid
            handle.execute("CREATE TABLE IF NOT EXISTS shops (\n" +
                    "  uuid TEXT NOT NULL PRIMARY KEY,\n" +  // 主键应为 uuid
                    "  name TEXT NOT NULL UNIQUE,\n" +       // 确保商铺名称唯一
                    "  owner TEXT NOT NULL,\n" +
                    "  lore TEXT NOT NULL\n" +
                    ");");
        });

        // 创建表
        jdbi.useHandle(handle -> {
            String sql = "CREATE TABLE IF NOT EXISTS shopitems (" + "uuid TEXT NOT NULL PRIMARY KEY, " + "shopuuid TEXT NOT NULL, " + "price REAL NOT NULL " + ");";
            handle.execute(sql);
        });
    }

    //    创建Shop
    public boolean createShop(Shop shop) {
        try {
            jdbi.useExtension(ShopDao.class, dao -> dao.insert(shop.getUuid(), shop.getName(), shop.getOwner(), shop.getLore()));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean updateShopName(Shop shop) {
        try {
            jdbi.useExtension(ShopDao.class, dao -> dao.updateName(shop.getUuid(), shop.getName()));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean updateShopOwner(Shop shop) {
        try {
            jdbi.useExtension(ShopDao.class, dao -> dao.updateOwner(shop.getUuid(), shop.getOwner()));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean updateShopLore(Shop shop) {
        try {
            jdbi.useExtension(ShopDao.class, dao -> dao.updateLore(shop.getUuid(), shop.getLore()));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Shop findByUuid(UUID uuid) {
        try {
            return jdbi.withExtension(ShopDao.class, dao -> dao.findByUuid(uuid));
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Shop findByName(String name) {
        try {
            return jdbi.withExtension(ShopDao.class, dao -> dao.findByName(name));
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<Shop> getShopsByOwner(UUID playerId) {
        try {
            return jdbi.withExtension(ShopDao.class, dao -> dao.findByOwner(playerId));
        } catch (Exception e) {
            e.printStackTrace();  // 打印堆栈跟踪
            return List.of();
        }
    }

    @Override
    public boolean deleteShop(UUID uuid) {
        try {
            jdbi.useExtension(ShopDao.class, dao -> dao.delete(uuid));
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    @Override
    public boolean addItem(ShopItem shopItem) {
        try {
            jdbi.useHandle(handle -> {
                jdbi.useExtension(ShopDao.class, dao -> dao.insert(shopItem.getUuid(), shopItem.getShopuuid(), shopItem.getPrice()));
            });
            saveItemStackFile(shopItem.getItemStack(), shopItem.getUuid());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 保存物品数据文件
     *
     * @param playerHead ItemStack
     * @param itemuuid   UUID
     */
    public void saveItemStackFile(ItemStack playerHead, UUID itemuuid) throws IOException {
        Path dir = Paths.get(plugin.getDataFolder().getPath(), "items");
        Files.createDirectories(dir); // 自动创建不存在的目录
        Path path = dir.resolve(itemuuid.toString());
        try (FileOutputStream fileOut = new FileOutputStream(path.toFile()); ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(ItemStorageUtil.serializeItemStack(playerHead));
            out.close();
            out.flush();
        }
    }

    @Override
    public List<ShopItem> getItemsByShop(UUID shopuuid) {
        try {
            return jdbi.withHandle(handle -> jdbi.withExtension(ShopDao.class, dao -> dao.getItemsByShop(shopuuid)));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ItemStack removeItem(Shop shop, UUID itemId) {
        Path dir = Paths.get(plugin.getDataFolder().getPath(), "items");
        Path itempath = dir.resolve(itemId.toString());

        // 先读取文件内容
        ItemStack itemStack;
        try (FileInputStream fileInputStream = new FileInputStream(itempath.toFile());
             ObjectInputStream in = new ObjectInputStream(fileInputStream)) {
            byte[] data = (byte[]) in.readObject();
            itemStack = ItemStorageUtil.deserializeItemStack(data);
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }

        // 文件流关闭后再删除文件
        try {
            Files.delete(itempath);
        } catch (FileSystemException e) {
            plugin.getLogger().severe("文件被占用");
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 删除数据库记录
        jdbi.useExtension(ShopDao.class, dao ->
                dao.delete(shop.getUuid(), itemId)
        );

        return itemStack;
    }

    @Override
    public ShopItem getItemById(UUID shopuuid, UUID itemId) {
        return jdbi.withExtension(ShopDao.class, dao -> dao.findByUuid(shopuuid, itemId));
    }

    @Override
    public boolean updatePrice(ShopItem shopItem) {
        return jdbi.withExtension(ShopDao.class, dao -> dao.updatePrice(shopItem.getUuid(), shopItem.getPrice()));
    }

    @Override
    public List<Shop> getAllShops() {
        return jdbi.withExtension(ShopDao.class, ShopDao::getAllShops);
    }

    @Override
    public boolean updateItemStack(ShopItem shopItem) {
//        删除旧文件
        try {
            Path dir = Paths.get(plugin.getDataFolder().getPath(), "items");
            Path itempath = dir.resolve(shopItem.getUuid().toString());
            Files.delete(itempath);
            saveItemStackFile(shopItem.getItemStack(), shopItem.getUuid());
            return true;

        } catch (IOException e) {
            return false;
        }
    }
}