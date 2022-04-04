package com.windowx.miraibot.command;

import java.util.Set;
import java.util.TreeMap;

public class Commands {
    private final TreeMap<String, Command> commands = new TreeMap<>();

    public Command get(String name) {
        return commands.get(name);
    }

    public void set(String name, Command command) {
        commands.put(name, command);
    }

    public void remove(String name) {
        commands.remove(name);
    }

    public String[] keys() {
        Set<String> strings = commands.keySet();
        return strings.toArray(new String[0]);
    }

    public int size() {
        return commands.size();
    }
}
