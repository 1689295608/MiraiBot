package com.windowx.miraibot.utils;

import org.fusesource.jansi.AnsiConsole;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.windowx.miraibot.MiraiBot.logger;

public class LogUtil {
    public static File file;

    public static void init() {
        AnsiConsole.systemInstall();
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date date = new Date(System.currentTimeMillis());
            String time = formatter.format(date);
            File dir = new File("logs");
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    System.out.println(time + ConfigUtil.getLanguage("failed.create.config"));
                    System.exit(-1);
                    return;
                }
            }
            int i = 1;
            while (new File("logs" + File.separator + time + "-" + i + ".log").exists()) {
                i++;
            }
            file = new File("logs" + File.separator + time + "-" + i + ".log");
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    System.out.println(time + ConfigUtil.getLanguage("failed.create.config"));
                    System.exit(-1);
                }
            }
        } catch (IOException e) {
            System.out.println(ConfigUtil.getLanguage("unknown.error"));
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * 将内容写入日志
     *
     * @param str 内容
     */
    public static void write(String str) throws IOException {
        if (str == null) return;
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] all = fis.readAllBytes();
            try (FileOutputStream fos = new FileOutputStream(file)) {
                byte[] add = new byte[0];
                add = byteMerger(add, (str + "\n").getBytes());
                fos.write(byteMerger(all, add));
            }
        }
    }

    /**
     * Combine two byte[] into one byte[]
     *
     * @param byte1 Byte[] to be merged at the beginning
     * @param byte2 Byte[] to be merged to the end
     * @return Merged result
     */
    public static byte[] byteMerger(byte[] byte1, byte[] byte2) {
        byte[] byte3 = new byte[byte1.length + byte2.length];
        System.arraycopy(byte1, 0, byte3, 0, byte1.length);
        System.arraycopy(byte2, 0, byte3, byte1.length, byte2.length);
        return byte3;
    }
}
