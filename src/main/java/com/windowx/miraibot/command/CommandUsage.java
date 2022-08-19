package com.windowx.miraibot.command;

import java.util.ArrayList;

public class CommandUsage {
    // TODO: 为插件命令增加 Usage 检测，便于开发时更快的 Usage 帮助

    private String cmd;
    private final ArrayList<String> args = new ArrayList<>();
    private final ArrayList<String> means = new ArrayList<>();
    private final ArrayList<String> def = new ArrayList<>();

    public CommandUsage() {

    }

    public CommandUsage add(String arg, String mean) {
        args.add(arg);
        means.add(mean);
        def.add(null);
        return this;
    }

    public CommandUsage add(String arg, String mean, String def) {
        args.add(arg);
        means.add(mean);
        this.def.add(def);
        return this;
    }

    public ArrayList<String> args() {
        return args;
    }

    public ArrayList<String> means() {
        return means;
    }

    public ArrayList<String> def() {
        return def;
    }
}
