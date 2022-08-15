package com.windowx.miraibot.utils;

import org.fusesource.jansi.AnsiConsole;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public class LogUtil {
    public static Path path;

    public static void init() {
        AnsiConsole.systemInstall();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(System.currentTimeMillis());
        String time = formatter.format(date);
        File logs = new File("logs");
        int i = 1;
        while (new File(logs, time + "-" + i + ".log").exists()) {
            i++;
        }
        File file = new File(logs, time + "-" + i + ".log");
        path = file.toPath();
    }

    /**
     * 将内容写入日志
     *
     * @param str 内容
     */
    public static void write(String str) throws IOException {
        if (str == null) return;
        Files.writeString(path, str + "\n", APPEND, CREATE);
    }
}
