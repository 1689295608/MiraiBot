package com.windowx.miraibot.utils;

import org.fusesource.jansi.AnsiConsole;
import org.jline.utils.AttributedString;
import org.jline.utils.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.windowx.miraibot.MiraiBot.language;
import static com.windowx.miraibot.MiraiBot.reader;
import static org.fusesource.jansi.Ansi.ansi;

public class Logger {
    private String prefix = "";
    public boolean ansiColor;
    byte[] all = new byte[0];
    File file;

    public Logger() {
        init();
    }

    public Logger(String prefix) {
        init();
        this.prefix = prefix;
    }

    public void init() {
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
                    return;
                }
            }
            FileInputStream fis = new FileInputStream(file);
            all = fis.readAllBytes();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(all);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            System.out.println(ConfigUtil.getLanguage("unknown.error"));
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * 获取格式化后的时间
     *
     * @return 格式化后的时间
     */
    public String formatTime() {
        SimpleDateFormat formatter = new SimpleDateFormat(ConfigUtil.getLanguage("format.time"));
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date);
    }
    /**
     * 获取多行优化后的内容
     *
     * @param str    内容
     * @param prefix 前缀
     * @return 优化后的内容
     */
    public String formatStr(String prefix, String str) {
        String[] spl = str.split("\n");
        if (spl.length == 1) return (prefix + str + ansi().reset().toString());
        StringBuilder sb = new StringBuilder();
        for (String s : spl) {
            s = prefix + s;
            if (sb.toString().isEmpty()) {
                sb.append(s)
                        .append(ansi().reset().toString())
                        .append("\n");
                continue;
            }
            sb.append(formatTime())
                    .append(s)
                    .append(ansi().reset().toString())
                    .append("\n");
        }
        return sb.toString();
    }
    /**
     * 获取格式化后的时间并将内容写入日志
     *
     * @param str  内容
     * @param args 参数
     * @return 格式化后的时间
     */
    public String write(String str, Object... args) {
        if (str == null) return null;
        str = String.format(str, args);
        String time = formatTime();
        try {
            FileInputStream fis = new FileInputStream(file);
            all = fis.readAllBytes();
            FileOutputStream fos = new FileOutputStream(file);
            String[] allLine = str.split("\n");
            byte[] add = new byte[0];
            for (String s : allLine) {
                add = byteMerger(add, (time + s + "\n").getBytes());
            }
            fos.write(byteMerger(all, add));
            fos.flush();
            fos.close();
        } catch (IOException e) {
            System.out.println(ConfigUtil.getLanguage("unknown.error"));
            System.out.println("(" + e.getCause() + " : " + e.getMessage() + ")");
            System.exit(-1);
        }
        return time;
    }

    /**
     * Combine two byte[] into one byte[]
     *
     * @param byte1 Byte[] to be merged at the beginning
     * @param byte2 Byte[] to be merged to the end
     * @return Merged result
     */
    public byte[] byteMerger(byte[] byte1, byte[] byte2) {
        byte[] byte3 = new byte[byte1.length + byte2.length];
        System.arraycopy(byte1, 0, byte3, 0, byte1.length);
        System.arraycopy(byte2, 0, byte3, byte1.length, byte2.length);
        return byte3;
    }

    /**
     * Output a message and record it in the log file
     *
     * @param str What to output
     */
    public void print(String str, Object... args) {
        if (str == null) str = "";
        String opt = String.format(formatStr(formatTime(), str), args) + "\n";
        if (reader == null) {
            AnsiConsole.out().print(opt);
        } else {
            reader.printAbove(AttributedString.fromAnsi(opt));
        }
    }

    public void info(String str, Object... args) {
        print(formatStr(prefix, str), args);
    }

    public void warn(String str, Object... args) {
        print(formatStr(prefix + ansi().fgBrightYellow().toString(), str), args);
    }

    public void error(String str, Object... args) {
        print(formatStr(prefix + ansi().fgBrightRed().toString(), str), args);
    }

    /**
     * Clear the console text
     */
    public void clear() {
        try {
            System.console().flush();
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else if (os.contains("linux")) {
                new ProcessBuilder("clear").inheritIO().start().waitFor();
            }
        } catch (Exception ignored) {
        }
    }

    public void trace(Exception e) {
        trace(e.toString(), e.getMessage(), e.getStackTrace());
    }
    public void trace(Throwable t) {
        trace(t.toString(), t.getMessage(), t.getStackTrace());
    }
    public void trace(String str, String msg, StackTraceElement[] ste) {
        String name = str.toString().split(":")[0];
        error(language("exception.string"),
                name,
                msg
        );
        for (StackTraceElement s : ste) {
            String className = s.getClassName();
            String methodName = s.getMethodName();
            String fileName = s.getFileName();
            int lineNum = s.getLineNumber();
            error(language("exception.details"),
                    className,
                    methodName,
                    fileName,
                    lineNum
            );
        }
    }
}
