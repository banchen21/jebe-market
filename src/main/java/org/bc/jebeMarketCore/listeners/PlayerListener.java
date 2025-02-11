package org.bc.jebeMarketCore.listeners;

import org.bc.jebeMarketCore.JebeMarket;
import org.bc.jebeMarketCore.utils.PlayerHeadManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {


    private final JebeMarket plugin;
    private PlayerHeadManager playerHeadManager;

    public PlayerListener(JebeMarket jebeMarket, PlayerHeadManager playerHeadManager) {
        this.plugin = jebeMarket;
        this.playerHeadManager = playerHeadManager;
    }


    /**
     * 玩家进入游戏获取玩家头颅信息并存储到缓存文件中
     *
     * @param event 事件
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskAsynchronously(playerHeadManager.getPlugin(), () -> {
            ItemStack head = playerHeadManager.getPlayerHead(player.getUniqueId());
            playerHeadManager.savePlayerHead(head, player.getUniqueId());
        });
    }


}
