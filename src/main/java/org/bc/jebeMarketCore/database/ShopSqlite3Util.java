package org.bc.jebeMarketCore.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bc.jebeMarketCore.dao.ShopDao;
import org.bc.jebeMarketCore.model.Shop;
import org.bc.jebeMarketCore.repository.ShopRepository;
import org.bukkit.plugin.java.JavaPlugin;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import java.io.File;
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
            boolean shopType = rs.getBoolean("type");
            String lore = rs.getString("lore");
            return new Shop(uuid, name, owner, shopType, lore);
        });

        jdbi.installPlugin(new SqlObjectPlugin());

        // 创建表
        jdbi.useHandle(handle -> {
            // 在 Sqlite3Util 的建表语句中修改主键为 uuid
            handle.execute("CREATE TABLE IF NOT EXISTS shops (\n" +
                    "  uuid TEXT NOT NULL PRIMARY KEY,\n" +  // 主键应为 uuid
                    "  name TEXT NOT NULL UNIQUE,\n" +       // 确保商铺名称唯一
                    "  owner TEXT NOT NULL,\n" +
                    "  type BOOLEAN NOT NULL,\n" +
                    "  lore TEXT NOT NULL\n" +
                    ");");
        });
    }

    //    创建Shop
    public boolean createShop(Shop shop) {
        try {
            jdbi.useExtension(ShopDao.class, dao -> dao.upsert(shop.getUuid(), shop.getName(), shop.getOwner(), shop.isShopType(), shop.getLore()));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean updateShopName(Shop shop) {
        return false;
    }

    @Override
    public boolean updateShopOwner(Shop shop) {
        return false;
    }

    @Override
    public boolean updateShopType(Shop shop) {
        return false;
    }

    @Override
    public boolean updateShopLore(Shop shop) {
        return false;
    }

    @Override
    public Shop getShop(String uuid) {
        try {
            return jdbi.withExtension(ShopDao.class, dao -> dao.findByUuid(uuid));
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<Shop> getShopsByOwner(UUID playerId, boolean isAdmin) {
        try {
            return jdbi.withExtension(ShopDao.class, dao -> dao.findByOwner(playerId));
        } catch (Exception e) {
            e.printStackTrace();  // 打印堆栈跟踪
            return List.of();
        }
    }

    @Override
    public boolean deleteShop(UUID uuid) {
        return false;
    }

    public Shop getShop(UUID uuid) {
        try {
            return jdbi.withExtension(ShopDao.class, dao -> dao.findByUuid(uuid.toString()));
        } catch (Exception e) {
            return null;
        }
    }
}