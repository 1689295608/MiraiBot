package com.windowx.miraibot.command;

public interface CommandExecutor {
    void onCommand(String label, String[] args);
}
