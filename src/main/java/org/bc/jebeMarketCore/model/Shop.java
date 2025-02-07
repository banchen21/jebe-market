package org.bc.jebeMarketCore.model;


import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
public class Shop {
    private UUID id;
    private UUID owner;
    private String name;
    private boolean type;
    private List<String> lore;
    private List<ShopItem> items;

    public Shop(UUID owner, String name) {
        this.id = UUID.randomUUID();
        this.owner = owner;
        this.name = name;
        this.type = false;
        this.lore = new ArrayList<>();
        this.items = new ArrayList<>();
    }

    public Shop(UUID ownerId, UUID shopuuid, ItemStack itemStack, String owner, boolean type, List<ShopItem> items) {
        this.id = shopuuid;
        this.owner = ownerId;
        this.name = itemStack.getItemMeta().getDisplayName();
        this.type = type;
        this.lore = itemStack.getItemMeta().getLore();
        this.items = items;
    }


    private String formatLoreLine(String text) {
        return text + "§r"; // 确保颜色重置
    }

    // 无参构造器（必须存在）
    public Shop() {
    }
}