package org.bc.jebeMarketCore.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;


/**
 * Player player = (Player) sender;
 * <p>
 * inputHandler.requestInput(
 * player,
 * "§a请在聊天栏输入新的名称（最多16个字符）：",
 * input -> {
 * // 此处处理输入内容（异步线程）
 * if (input.length() > 16) {
 * player.sendMessage("§c名称过长！");
 * return;
 * }
 * <p>
 * // 同步执行Bukkit API操作
 * Bukkit.getScheduler().runTask(plugin, () -> {
 * player.setDisplayName(input);
 * player.sendMessage("§a名称已修改为: " + input);
 * });
 * }, 30);
 */
public class PlayerInputHandler implements Listener {
    private final Map<UUID, Consumer<String>> inputCallbacks = new HashMap<>();
    private final Plugin plugin;
    private int timeout;

    public PlayerInputHandler(Plugin plugin) {
        this.plugin = plugin;
        this.timeout=plugin.getConfig().getInt("interaction.input_timeout");
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * 请求玩家输入
     *
     * @param player   目标玩家
     * @param prompt   提示信息
     * @param callback 输入回调（异步）
     */
    public void requestInput(Player player, String prompt, Consumer<String> callback) {
        UUID uuid = player.getUniqueId();

        // 发送提示信息
        player.sendMessage(prompt);
        player.sendMessage("§e输入 'T' 取消操作");

        // 注册回调
        inputCallbacks.put(uuid, callback);

        // 设置超时任务
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (inputCallbacks.remove(uuid) != null) {
                player.sendMessage("§c输入超时，操作已取消");
            }
        }, timeout * 20L);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String message = event.getMessage();

        // 检查是否在等待输入状态
        if (inputCallbacks.containsKey(uuid)) {
            event.setCancelled(true); // 阻止正常聊天

            // 处理取消操作
            if (message.equalsIgnoreCase("T")) {
                inputCallbacks.remove(uuid);
                player.sendMessage("§c已取消输入");
                return;
            }

            // 异步执行回调
            Consumer<String> callback = inputCallbacks.remove(uuid);
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    if (plugin.getConfig().getBoolean("allow_color_code") && checkColorCode(message)) {
                        player.sendMessage("§c输入包含颜色代码，请重新输入");
                        return;
                    }
                    callback.accept(message);
                } catch (Exception e) {
                    player.sendMessage("§c处理输入时发生错误: " + e.getMessage());
                }
            });
        }
    }

    //    颜色代码检测
    public static boolean checkColorCode(String text) {
        String regex = "§[0-9a-fA-Fk-oK-OrR]";
        return text.matches(regex);
    }
}