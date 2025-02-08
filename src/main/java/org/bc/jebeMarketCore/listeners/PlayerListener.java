package org.bc.jebeMarketCore.listeners;

import ca.tweetzy.skulls.flight.utils.QuickItem;
import org.bc.jebeMarketCore.JebeMarket;
import org.bc.jebeMarketCore.MarketPulseUtil.GetPlayerHeadItemStack;
import org.bc.jebeMarketCore.MarketPulseUtil.ItemStorageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PlayerListener implements Listener {


    private final JebeMarket plugin;

    public PlayerListener(JebeMarket jebeMarket) {
        this.plugin = jebeMarket;
    }


    /**
     * 玩家进入游戏获取玩家头颅信息并存储到缓存文件中
     * @param event 事件
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ItemStack playerHead = GetPlayerHeadItemStack.getPlayerHeadItemStack(player);
        try {
            // 创建文件路径时确保目录存在
            Path directory = Paths.get(plugin.getDataFolder().getAbsolutePath(), "PlayerHeadCache");
            Files.createDirectories(directory);  // 自动创建不存在的目录

            Path path = directory.resolve(String.valueOf(player.getUniqueId()));

            // 使用 try-with-resources 自动关闭流
            try (OutputStream fos = Files.newOutputStream(path);
                 ObjectOutputStream out = new ObjectOutputStream(fos)) {

                byte[] serialized = ItemStorageUtil.serializeItemStack(playerHead);
                out.writeObject(serialized);
            }
        } catch (IOException e) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "Failed to save player head for player: " + player.getName(), e);
        }
    }


}
