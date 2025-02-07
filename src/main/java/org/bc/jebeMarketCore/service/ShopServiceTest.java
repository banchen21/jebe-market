package org.bc.jebeMarketCore.service;

import org.bc.jebeMarketCore.JebeMarket;
import org.bc.jebeMarketCore.api.ShopManager;
import org.bc.jebeMarketCore.model.Shop;
import org.bc.jebeMarketCore.repository.FileShopRepository;
import org.bc.jebeMarketCore.repository.ShopRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class ShopServiceTest {

    private ShopManager shopManager;
    private ShopRepository repository;

    @BeforeEach
    void setUp() {
        repository = new FileShopRepository(new JebeMarket());
        shopManager = new ShopServiceImpl(repository);
    }

    //    测试翻译
    @Test
    void testTranslation() {

    }

    @Test
    void shouldCreateShopSuccessfully(){
        UUID owner = UUID.randomUUID();
        Shop shop = shopManager.createShop(owner, "测试商店");


    }
}