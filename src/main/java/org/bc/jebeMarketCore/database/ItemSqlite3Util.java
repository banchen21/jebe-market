package org.bc.jebeMarketCore.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.bc.jebeMarketCore.MarketPulseUtil.ItemStorageUtil;
import org.bc.jebeMarketCore.dao.ItemDao;
import org.bc.jebeMarketCore.model.Item;
import org.bc.jebeMarketCore.model.Shop;
import org.bc.jebeMarketCore.repository.ItemRepository;
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

public class ItemSqlite3Util implements ItemRepository {
    private final JavaPlugin plugin;
    private final HikariDataSource dataSource;
    private final Jdbi jdbi;

    HikariDataSource init() {
        HikariConfig config = new HikariConfig();
        // 使用插件数据文件夹的路径
        File databaseFile = new File(plugin.getDataFolder(), "database.db");
        config.setJdbcUrl("jdbc:sqlite:" + databaseFile.getAbsolutePath());
        config.setMaximumPoolSize(1); // SQLite 建议单连接
        return new HikariDataSource(config);
    }


    public ItemSqlite3Util(final JavaPlugin p_javaPlugin) throws SQLException {
        plugin = p_javaPlugin;
        // 创建数据文件夹
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        // 配置 HikariCP
        dataSource = init();

        //         ---------
        // 初始化 JDBI
        jdbi = Jdbi.create(dataSource);
        // 在初始化 JDBI 时注册类型转换器
        jdbi.registerColumnMapper(UUID.class, (rs, columnNumber, ctx) -> UUID.fromString(rs.getString(columnNumber)));
        jdbi.registerArgument(new AbstractArgumentFactory<UUID>(Types.VARCHAR) {
            @Override
            protected Argument build(UUID value, ConfigRegistry config) {
                return (position, statement, ctx) -> statement.setString(position, value.toString());
            }
        });

        jdbi.registerRowMapper(Item.class, (rs, ctx) -> {
            UUID uuid = UUID.fromString(rs.getString("uuid"));
            UUID shopuuid = UUID.fromString(rs.getString("shopuuid"));
            String material = rs.getString("material");
            double price = rs.getDouble("price");
            int amount = rs.getInt("amount");
            try {
                Path dir = Paths.get(plugin.getDataFolder().getPath(), "items");
                Files.createDirectories(dir);
                Path path = dir.resolve(uuid.toString());

                try (FileInputStream fileInputStream = new FileInputStream(path.toFile());
                     ObjectInputStream in = new ObjectInputStream(fileInputStream)) {
                    byte[] data = (byte[]) in.readObject();
                    ItemStack itemStack = ItemStorageUtil.deserializeItemStack(data);
                    return new Item(uuid, shopuuid, material, price, amount, itemStack);
                }
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

        });

        jdbi.installPlugin(new SqlObjectPlugin());

        // 创建表
        jdbi.useHandle(handle -> {
            String sql = "CREATE TABLE IF NOT EXISTS items (" + "uuid TEXT NOT NULL PRIMARY KEY, " + "shopuuid TEXT NOT NULL, " + "material TEXT NOT NULL, " + "price REAL NOT NULL, " + "amount INTEGER NOT NULL" + ");";
            handle.execute(sql);
        });
    }

    @Override
    public boolean addItem(Item item) {
        try {
            jdbi.useHandle(handle -> {
                jdbi.useExtension(ItemDao.class, dao -> dao.insert(item.getUuid(), item.getShopuuid(), item.getMaterial(), item.getPrice(), item.getAmount()));
            });
            saveItemStackFile(item.getItemStack(), item.getUuid());
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
    public List<Item> getItemsByShop(UUID shopuuid) {
        try {
            return jdbi.withHandle(handle -> jdbi.withExtension(ItemDao.class, dao -> dao.getItemsByShop(shopuuid)));
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
        jdbi.useExtension(ItemDao.class, dao ->
                dao.delete(shop.getUuid(), itemId)
        );

        return itemStack;
    }

    @Override
    public Item getItemById(UUID shopuuid, UUID itemId) {
        return jdbi.withExtension(ItemDao.class, dao -> dao.findByUuid(shopuuid, itemId));
    }

    @Override
    public boolean updatePrice(Item item) {
        return jdbi.withExtension(ItemDao.class, dao -> dao.updatePrice(item.getUuid(), item.getPrice()));
    }

    @Override
    public boolean updateAmount(Item item) {
        return jdbi.withExtension(ItemDao.class, dao -> dao.updateAmount(item.getUuid(), item.getAmount()));
    }
}
