package com.windowx.miraibot.utils;

import org.fusesource.jansi.AnsiConsole;
import org.jline.utils.AttributedString;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.windowx.miraibot.MiraiBot.reader;
import static com.windowx.miraibot.utils.LanguageUtil.l;
import static org.fusesource.jansi.Ansi.ansi;

public class Logger {
    private String prefix = "";
    public boolean ansiColor;

    public Logger() {

    }

    public Logger(String prefix) {

        this.prefix = prefix;
    }

    /**
     * 获取格式化后的时间
     *
     * @return 格式化后的时间
     */
    public String formatTime(String level) {
        String f = String.format(l("format.time"), level);
        SimpleDateFormat formatter = new SimpleDateFormat(f);
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
            sb /*.append(formatTime()) */
                    .append(s)
                    .append(ansi().reset().toString())
                    .append("\n");
        }
        return sb.toString();
    }

    /**
     * 输出文本到控制台并记录到日志
     *
     * @param str 输出的内容
     */
    public void print(String str, String level, Object... args) {
        if (str == null) str = "";
        String opt = String.format(formatStr(formatTime(level), str), args) + "\n";
        try {
            LogUtil.write(opt.substring(0, opt.length() - 1));
        } catch (IOException e) {
            trace(e);
        }
        if (reader == null) {
            AnsiConsole.out().print(opt);
        } else {
            reader.printAbove(AttributedString.fromAnsi(opt));
        }
    }

    public void info(String str, Object... args) {
        print(formatStr(prefix, str), "I", args);
    }

    public void warn(String str, Object... args) {
        print(formatStr(ansi().fgBrightYellow().toString() + prefix, str), "W", args);
    }

    public void error(String str, Object... args) {
        print(formatStr(ansi().fgBrightRed().toString() + prefix, str), "E", args);
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
        String name = str.split(":")[0];
        error(l("exception.string"),
                name,
                msg
        );
        for (StackTraceElement s : ste) {
            String className = s.getClassName();
            String methodName = s.getMethodName();
            String fileName = s.getFileName();
            int lineNum = s.getLineNumber();
            error(l("exception.details"),
                    className,
                    methodName,
                    fileName,
                    lineNum
            );
        }
    }
}
