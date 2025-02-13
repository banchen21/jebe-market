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
            plugin.getLogger().log(
                    Level.SEVERE,
                    plugin.getString("filesystem.cache.create_failed.ncreate_failed"),
                    e
            );
        }
    }

    // 添加缓存清除方法
    public void clearPlayerCache(UUID playerId) {
        Path cacheFile = cacheDirectory.resolve(playerId.toString());
        try {
            Files.deleteIfExists(cacheFile);
        } catch (IOException e) {
            String message = plugin.getString("filesystem.cache.delete_failed")
                    .replace("%player%", playerId.toString());
            plugin.getLogger().log(Level.WARNING, message, e);
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
            String message = plugin.getString("filesystem.cache.save_failed")
                    .replace("%player%", playerId.toString());
            plugin.getLogger().log(Level.SEVERE, message, e);
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
            String message = plugin.getString("filesystem.cache.load_failed")
                    .replace("%player%", playerId.toString());
            plugin.getLogger().log(Level.WARNING, message, e);
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
            int deletedCount = 0;
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(cacheDirectory)) {
                for (Path path : stream) {
                    if (Files.isRegularFile(path)) {
                        if (Files.getLastModifiedTime(path).toMillis() <
                                System.currentTimeMillis() - 2592000000L) {
                            Files.delete(path);
                            deletedCount++;
                        }
                    }
                }
                // 记录清理结果
                if (deletedCount > 0) {
                    String message = plugin.getString("plugin.maintenance.cache_cleanup")
                            .replace("%count%", String.valueOf(deletedCount));
                    plugin.getLogger().info(message);
                }
            } catch (IOException e) {
                plugin.getLogger().log(
                        Level.WARNING,
                        plugin.getString("filesystem.cache.cleanup_failed"),
                        e
                );
            }
        }, 72000L, 1728000L);
    }
}