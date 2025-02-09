package org.bc.jebeMarketCore.database;

import org.bc.jebeMarketCore.JebeMarket;
import org.bc.jebeMarketCore.dao.ShopDao;
import org.bc.jebeMarketCore.model.Shop;
import org.bc.jebeMarketCore.repository.ShopRepository;
import org.jdbi.v3.core.Jdbi;

import java.util.List;
import java.util.UUID;

public class MysqlUtil implements ShopRepository {

    private Jdbi jdbi;
    private final JebeMarket jebeMarket;

    public MysqlUtil(JebeMarket jebeMarket1) {
        this.jebeMarket = jebeMarket1;
    }

    @Override
    public boolean createShop(Shop shop) {
        return false;
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
    public Shop findByUuid(String name) {
        return null;
    }

    @Override
    public Shop findByName(String name) {
        return jdbi.withExtension(ShopDao.class, dao -> dao.findByName(name));
    }

    @Override
    public List<Shop> getShopsByOwner(UUID playerId) {
        return List.of();
    }

    @Override
    public boolean deleteShop(UUID uuid) {
        return false;
    }
}
