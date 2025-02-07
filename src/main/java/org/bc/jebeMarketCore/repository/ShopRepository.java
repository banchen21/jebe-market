// ShopRepository.java
package org.bc.jebeMarketCore.repository;

import org.bc.jebeMarketCore.model.Shop;

import java.io.IOException;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface ShopRepository {
    Shop AddShop(Shop shop);
    Optional<Shop> findById(UUID id) throws IOException, ClassNotFoundException;
    List<Shop> findByOwner(UUID ownerId) throws IOException, ClassNotFoundException;
    boolean delete(UUID id);
}