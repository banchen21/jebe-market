package org.bc.jebeMarketCore.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;

public class MessageUtils {

    // 方案一：保留原有颜色代码转换方法
    public static String legacyColor(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    // 方案三：增强型混合方法（需要Adventure API支持）
    public static Component advancedColor(String text) {
        return Component.text(text); // 实际应包含解析逻辑
    }

    public static String color(String text) {
        return MessageUtils.legacyColor(text);
    }

    public static String color(String format, Object... args) {
        return color(String.format(format, args));
    }

}