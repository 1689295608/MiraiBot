package com.windowx.miraibot.utils;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.windowx.miraibot.PluginMain.language;
import static org.fusesource.jansi.Ansi.ansi;

public class LogUtil {
    public static boolean ansiColor;
    static byte[] all = new byte[0];
    static File file;

    /**
     * Initialize the log system
     */
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
     * 获取多行优化后的内容
     *
     * @param str    内容
     * @param prefix 前缀
     * @return 优化后的内容
     */
    public static String formatStr(String prefix, String str) {
        String[] spl = str.split("\n");
        if (spl.length == 1) return replaceColor(prefix + str);
        StringBuilder sb = new StringBuilder();
        for (String s : spl) {
            s = prefix + s;
            s = replaceColor(s);
            if (sb.toString().isEmpty()) {
                sb.append(s).append("\n");
                continue;
            }
            sb.append(formatTime()).append(s).append("\n");
        }
        return sb.toString();
    }

    public static String replaceColor(String str) {
        if (!ansiColor) return str.replaceAll("&[0-9a-f]", "");
        return str.replaceAll("&0", ansi().fgRgb(12, 12, 12).toString())
                .replaceAll("&1", ansi().fgRgb(0, 55, 218).toString())
                .replaceAll("&2", ansi().fgRgb(19, 161, 14).toString())
                .replaceAll("&3", ansi().fgRgb(58, 150, 221).toString())
                .replaceAll("&4", ansi().fgRgb(197, 15, 31).toString())
                .replaceAll("&5", ansi().fgRgb(136, 23, 152).toString())
                .replaceAll("&6", ansi().fgRgb(193, 156, 0).toString())
                .replaceAll("&7", ansi().fgRgb(204, 204, 204).toString())
                .replaceAll("&8", ansi().fgRgb(118, 118, 118).toString())
                .replaceAll("&9", ansi().fgRgb(59, 120, 255).toString())
                .replaceAll("&a", ansi().fgRgb(22, 198, 12).toString())
                .replaceAll("&b", ansi().fgRgb(97, 214, 214).toString())
                .replaceAll("&c", ansi().fgRgb(231, 72, 86).toString())
                .replaceAll("&d", ansi().fgRgb(180, 0, 165).toString())
                .replaceAll("&e", ansi().fgRgb(249, 241, 165).toString())
                .replaceAll("&f", ansi().fgRgb(242, 242, 242).toString())
                .replaceAll("&l", ansi().a(Ansi.Attribute.INTENSITY_BOLD).toString())
                .replaceAll("&m", ansi().a(Ansi.Attribute.STRIKETHROUGH_ON).toString())
                .replaceAll("&n", ansi().a(Ansi.Attribute.UNDERLINE).toString())
                .replaceAll("&o", ansi().a(Ansi.Attribute.ITALIC).toString())
                .replaceAll("&r", ansi().reset().toString()) + ansi().reset();
    }

    /**
     * 无前缀的优化
     *
     * @param str 内容
     * @return 优化后的内容
     */
    public static String formatStr(String str) {
        return formatStr("", str);
    }

    /**
     * 获取格式化后的时间
     *
     * @return 格式化后的时间
     */
    public static String formatTime() {
        SimpleDateFormat formatter = new SimpleDateFormat(ConfigUtil.getLanguage("format.time"));
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date);
    }

    /**
     * 获取格式化后的时间并将内容写入日志
     *
     * @param str  内容
     * @param args 参数
     * @return 格式化后的时间
     */
    public static String write(String str, Object... args) {
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
     * Output a message and record it in the log file
     *
     * @param str What to output
     */
    public static void log(String str, Object... args) {
        if (str == null) return;
        AnsiConsole.out().printf("\r" + write(str, args) + formatStr(str) + "\n> ", args);
    }

    public static void log(String str) {
        if (str == null) return;
        AnsiConsole.out().print("\r" + write(str) + formatStr(str) + "\n> ");
    }

    public static void log_p(String prefix, String str, Object... args) {
        if (str == null) return;
        AnsiConsole.out().printf("\r" + write(str, args) + formatStr(prefix, str) + "\n> ", args);
    }

    /**
     * Output an error message and record it in the log file
     *
     * @param str What to output
     */
    public static void error(String str, Object... args) {
        if (str == null) return;
        if (ansiColor) {
            str = ansi().fgBright(Ansi.Color.RED).a(str).reset().toString();
        }
        AnsiConsole.out().printf("\r" + write(str, args) + formatStr(str) + "\n> ", args);
    }

    public static void error(String str) {
        if (str == null) return;
        if (ansiColor) {
            str = ansi().fgBright(Ansi.Color.RED).a(str).reset().toString();
        }
        AnsiConsole.out().print("\r" + write(str) + formatStr(str) + "\n> ");
    }

    public static void error_p(String prefix, String str, Object... args) {
        if (str == null) return;
        if (ansiColor) {
            str = ansi().fgBright(Ansi.Color.RED).a(str).reset().toString();
        }
        AnsiConsole.out().printf("\r" + write(str, args) + formatStr(prefix, str) + "\n> ", args);
    }

    /**
     * Output an error message and record it in the log file
     *
     * @param str What to output
     */
    public static void warn(String str, Object... args) {
        if (str == null) return;
        if (ansiColor) {
            str = ansi().fgBright(Ansi.Color.YELLOW).a(str).reset().toString();
        }
        AnsiConsole.out().printf("\r" + write(str, args) + formatStr(str) + "\n> ", args);
    }

    public static void warn(String str) {
        if (str == null) return;
        if (ansiColor) {
            str = ansi().fgBright(Ansi.Color.YELLOW).a(str).reset().toString();
        }
        AnsiConsole.out().print("\r" + write(str) + formatStr(str) + "\n> ");
    }

    public static void warn_p(String prefix, String str, Object... args) {
        if (str == null) return;
        if (ansiColor) {
            str = ansi().fgBright(Ansi.Color.YELLOW).a(str).reset().toString();
        }
        AnsiConsole.out().printf("\r" + write(str, args) + formatStr(prefix, str) + "\n> ", args);
    }

    public static void trace(Exception e) {
        String name = e.toString().split(":")[0];
        error(language("exception.string"),
                name,
                e.getMessage()
        );
        StackTraceElement[] ste = e.getStackTrace();
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

    /**
     * Clear the console text
     */
    public static void clear() {
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
}
