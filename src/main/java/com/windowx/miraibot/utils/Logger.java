package com.windowx.miraibot.utils;

public class Logger {
    private String prefix = "";

    public Logger() {
        LogUtil.init();
    }
    public Logger(String prefix) {
        this.prefix = prefix;
        LogUtil.init();
    }

    public void info(String str) {
        LogUtil.log_p(prefix, str);
    }
    public void info(String str, Object... args) {
        LogUtil.log_p(prefix, str, args);
    }

    public void warn(String str) {
        LogUtil.warn_p(prefix, str);
    }
    public void warn(String str, Object... args) {
        LogUtil.warn_p(prefix, str, args);
    }

    public void error(String str) {
        LogUtil.error_p(prefix, str);
    }
    public void error(String str, Object... args) {
        LogUtil.error_p(prefix, str, args);
    }

    public void clear() {
        LogUtil.clear();
    }

    public void trace(Exception e) {
        LogUtil.trace(e);
    }
}
