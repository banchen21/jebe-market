package org.bc.jebeMarketCore.model;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class ShopItem {
    private UUID id;
    private String name;
    private double price;
    private String icon_type;
    private String icon_url;
    // 构造函数/getters/setters

}