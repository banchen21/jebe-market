package org.bc.jebeMarketCore.MarketPulseUtil;

import ca.tweetzy.skulls.flight.utils.QuickItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.Nullable;

import java.io.*;

public class ItemStorageUtil {
    /**
     * 将 ItemStack 对象序列化为字节数组
     *
     * @param itemStack 要序列化存储的物品数据
     * @return 序列化后的字节数组
     * @throws IOException 序列化过程中可能抛出的输入输出异常
     */
    public static byte[] serializeItemStack(ItemStack itemStack) throws IOException {
        // 创建一个字节数组输出流，用于临时存储序列化后的数据
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        // 使用 BukkitObjectOutputStream 处理 Bukkit 特有对象的序列化
        BukkitObjectOutputStream bukkitStream = new BukkitObjectOutputStream(byteStream);
        try {
            // 将物品对象写入流中
            bukkitStream.writeObject(itemStack);
        } finally {
            // 确保流被正确关闭，释放资源
            bukkitStream.close();
        }
        // 返回序列化后的字节数组
        return byteStream.toByteArray();
    }

    /**
     * 将字节数组反序列化为 ItemStack 对象
     *
     * @param data
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static ItemStack deserializeItemStack(byte[] data) throws IOException, ClassNotFoundException {
        // 创建一个字节数组输入流，用于读取存储的数据
        ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
        // 使用 BukkitObjectInputStream 处理 Bukkit 特有对象的反序列化
        BukkitObjectInputStream bukkitStream = new BukkitObjectInputStream(byteStream);
        try {
            // 从流中读取对象并转换为 ItemStack 类型
            return (ItemStack) bukkitStream.readObject();
        } finally {
            // 确保流被正确关闭，释放资源
            bukkitStream.close();
        }
    }

    /**
     * 获取物品
     *
     * @param file1 File
     * @return ItemStack
     */
    @Nullable
    public static ItemStack getItemStack(File file1) throws IOException, ClassNotFoundException {
        if (file1.exists()) {
            FileInputStream fileIn = new FileInputStream(file1);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            byte[] data = (byte[]) in.readObject();
            return ItemStorageUtil.deserializeItemStack(data);
        } else {
            return null;
        }
    }


}
