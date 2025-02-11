package org.bc.jebeMarketCore.utils;

import ca.tweetzy.skulls.flight.utils.QuickItem;
import lombok.Getter;
import org.bc.jebeMarketCore.JebeMarket;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Level;

public class PlayerHeadManager {
    @Getter
    private final JebeMarket plugin;
    private final Path cacheDirectory;

    // 使用更明确的类名
    public PlayerHeadManager(JebeMarket plugin) {
        this.plugin = plugin;
        this.cacheDirectory = Paths.get(plugin.getDataFolder().getAbsolutePath(), "PlayerHeadCache");
        initializeCacheDirectory();
    }

    private void initializeCacheDirectory() {
        try {
            Files.createDirectories(cacheDirectory);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "无法创建缓存目录", e);
        }
    }

    // 添加缓存清除方法
    public void clearPlayerCache(UUID playerId) {
        Path cacheFile = cacheDirectory.resolve(playerId.toString());
        try {
            Files.deleteIfExists(cacheFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "无法删除缓存文件: " + playerId, e);
        }
    }

    // 优化异常处理逻辑
    public void savePlayerHead(ItemStack playerHead, UUID playerId) {
        Path cacheFile = cacheDirectory.resolve(playerId.toString());

        try (OutputStream fos = Files.newOutputStream(cacheFile);
             ObjectOutputStream out = new ObjectOutputStream(fos)) {

            byte[] serialized = ItemStorageUtil.serializeItemStack(playerHead);
            out.writeObject(serialized);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "保存玩家头颅失败: " + playerId, e);
        }
    }

    // 重构获取逻辑，支持离线玩家
    public ItemStack getPlayerHead(UUID playerId) {
        // 优先尝试获取在线玩家
        Player onlinePlayer = plugin.getServer().getPlayer(playerId);
        if (onlinePlayer != null && onlinePlayer.isOnline()) {
            ItemStack head = createNewHead(onlinePlayer);
            savePlayerHead(head, playerId);
            return head;
        }

        // 离线玩家尝试读取缓存
        ItemStack cachedHead = loadCachedHead(playerId);
        if (cachedHead != null) {
            return cachedHead;
        }

        // 最终尝试获取离线玩家数据
        return createOfflineHead(playerId);
    }

    /**
     * 从缓存中加载头颅
     *
     * @param playerId UUID
     * @return ItemStack
     */
    private ItemStack loadCachedHead(UUID playerId) {
        Path cacheFile = cacheDirectory.resolve(playerId.toString());
        if (!Files.exists(cacheFile)) return null;

        try (InputStream fis = Files.newInputStream(cacheFile);
             ObjectInputStream in = new ObjectInputStream(fis)) {

            byte[] data = (byte[]) in.readObject();
            return ItemStorageUtil.deserializeItemStack(data);
        } catch (IOException | ClassNotFoundException e) {
            plugin.getLogger().log(Level.WARNING, "加载缓存头颅失败: " + playerId, e);
            return null;
        }
    }

    private ItemStack createNewHead(Player player) {
        return QuickItem.of(player)
                .amount(1)
                .make();
    }

    private ItemStack createOfflineHead(UUID playerId) {
        OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(playerId);
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(offlinePlayer);
            head.setItemMeta(meta);
        }
        return head;
    }

    // 添加定期清理任务
    public void scheduleCacheCleanup() {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(cacheDirectory)) {
                for (Path path : stream) {
                    if (Files.isRegularFile(path)) {
                        // 清理超过30天的缓存
                        if (Files.getLastModifiedTime(path).toMillis() <
                                System.currentTimeMillis() - 2592000000L) {
                            Files.delete(path);
                        }
                    }
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "清理缓存失败", e);
            }
        }, 72000L, 1728000L); // 1小时后首次执行，每天执行
    }
}