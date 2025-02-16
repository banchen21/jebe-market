package org.bc.jebeMarketCore.gui.be;

import org.bc.jebeMarketCore.JebeMarket;
import org.bc.jebeMarketCore.model.Shop;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.player.FloodgatePlayer;

import static org.bc.jebeMarketCore.utils.MessageUtils.color;

public class ShopMainForm {

    private final JebeMarket plugin;
    String browse_shop_name;
    String my_shop_name;
    String edit_shop_name;  // 修改名称
    String edit_shop_desc; // 修改描述
    String edit_shop_items; // 管理商铺
    String edit_shop_sell_hand; // 卖出手持物品
    String edit_shop_bulk_sell; // 背包卖出

    int shop_name_min_name_length;
    int shop_name_max_name_length;

    int lore_max_length;

    public ShopMainForm(JebeMarket plugin) {
        this.plugin = plugin;
        init();
        shop_name_min_name_length = plugin.getConfig().getInt("settings.shop.min_name_length");
        shop_name_max_name_length = plugin.getConfig().getInt("settings.shop.max_name_length");
        lore_max_length = plugin.getConfig().getInt("settings.shop.lore.max_length");
    }


    void init() {
        browse_shop_name = color_i18n("ui.main.buttons.browse_shop.name");
        my_shop_name = color_i18n("ui.main.buttons.my_shop.name");

        edit_shop_desc = color_i18n("ui.edit.desc_button");
        edit_shop_name = color_i18n("ui.edit.name_button");
        edit_shop_items = color_i18n("ui.edit.items_button");
        edit_shop_sell_hand = color_i18n("ui.edit.sell_hand_button");
        edit_shop_bulk_sell = color_i18n("ui.edit.bulk_sell_button");
    }

    private String color_i18n(String key) {
        return color(plugin.getI18nString(key));
    }

    //    main
    public void toMainForm(Player player) {


        SimpleForm.Builder form = SimpleForm.builder().title(color(color_i18n("ui.main.title")));
        form.button(browse_shop_name);
        form.button(my_shop_name);
        form.build();
        FloodgatePlayer floodgatePlayer = FloodgateApi.getInstance().getPlayer(player.getUniqueId());

        form.validResultHandler((player1, response) -> {
            if (floodgatePlayer == null) return;
            String buttonS = response.clickedButton().text();
            if (buttonS.equals(my_shop_name)) {
                toMyShopListForm(player);
            }
        });

        floodgatePlayer.sendForm(form);
    }

    private void toMyShopListForm(Player player) {
        SimpleForm.Builder form = SimpleForm.builder().title(my_shop_name);
        plugin.getShopManager().getShopsByOwner(player.getUniqueId()).forEach(shop -> {
            form.button(shop.getName());
        });
        FloodgatePlayer floodgatePlayer = FloodgateApi.getInstance().getPlayer(player.getUniqueId());

        form.validResultHandler((player1, response) -> {
            Shop shop = plugin.getShopManager().getShop(response.clickedButton().text());
            MyEditShopForm(player, shop);
        });

        floodgatePlayer.sendForm(form);
    }

    private void MyEditShopForm(Player player, Shop shop) {
        SimpleForm.Builder form = SimpleForm.builder().title(color_i18n("ui.edit.title") + shop.getName());
        form.button(edit_shop_name);
        form.button(edit_shop_desc);
        form.button(edit_shop_items);
        form.button(edit_shop_sell_hand);
        form.button(edit_shop_bulk_sell);

        FloodgatePlayer floodgatePlayer = FloodgateApi.getInstance().getPlayer(player.getUniqueId());

        form.validResultHandler((player1, response) -> {
            String buttonS = response.clickedButton().text();
            if (buttonS.equals(edit_shop_name) || buttonS.equals(edit_shop_desc)) {
                toEditShopForm(player, shop, buttonS);
            }
        });

        floodgatePlayer.sendForm(form);

    }

    //    编辑页面
    public void toEditShopForm(Player player, Shop shop, String buttonS) {
//        修改什么？ 1.名称 2.描述
        CustomForm.Builder form = CustomForm.builder().title(shop.getName());
        if (buttonS.equals(edit_shop_name)) {
            form.label(color_i18n("ui.edit.current_name").replace("%name%", shop.getName()));
        } else if (buttonS.equals(edit_shop_desc)) {
            form.label(color_i18n("ui.edit.current_desc").replace("%lore%", shop.getLore()));
        }

        form.input(buttonS);
        form.validResultHandler((player1, response) -> {
            String text = response.asInput();
//            todo 空格、长度
            if (buttonS.equals(edit_shop_name)) {
                plugin.getShopManager().updateShopName(shop, text, player);
            } else if (buttonS.equals(edit_shop_desc)) {
                shop.setLore(text);
                plugin.getShopManager().updateShopLore(shop);
            }
        });

        FloodgatePlayer floodgatePlayer = FloodgateApi.getInstance().getPlayer(player.getUniqueId());
        floodgatePlayer.sendForm(form);
    }
}
