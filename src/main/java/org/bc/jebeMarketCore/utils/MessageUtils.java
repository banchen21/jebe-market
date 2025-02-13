package org.bc.jebeMarketCore.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

public class MessageUtils {
    // 环境检测标志
    private static final boolean ADVENTURE_AVAILABLE = checkAdventure();

    private static boolean checkAdventure() {
        try {
            Class.forName("net.kyori.adventure.text.Component");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * 通用颜色代码处理方法（最终保证输出含 § 符号）
     * @param text 原始带 & 符号的字符串
     * @return 处理后的字符串
     */
    public static String color(String text) {
        if (text == null || text.isEmpty()) return "";

        // 统一处理颜色代码转换
        String processed = ADVENTURE_AVAILABLE ?
                processWithAdventure(text) :
                processWithLegacy(text);

        // 最终强制转换 & 为 §（确保万无一失）
        return processed.replace('&', '§');
    }

    /**
     * 使用 AdventureAPI 处理
     */
    private static String processWithAdventure(String text) {
        Component component = LegacyComponentSerializer.legacySection().deserialize(text);
        return LegacyComponentSerializer.legacySection().serialize(component);
    }

    /**
     * 使用传统 Bukkit 方法处理
     */
    private static String processWithLegacy(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    /**
     * 保留原始方法用于特殊场景
     */
    public static Component adventureColor(String text) {
        if (!ADVENTURE_AVAILABLE) {
            throw new RuntimeException("Adventure API 不可用");
        }
        return LegacyComponentSerializer.legacySection().deserialize(text);
    }
}