package com.windowx.miraibot.command;

public class Command {
    private String name;
    private String description;
    private CommandUsage usage = new CommandUsage();
    private CommandExecutor executor = null;

    public Command(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CommandExecutor getExecutor() {
        return executor;
    }

    public void setExecutor(CommandExecutor executor) {
        this.executor = executor;
    }

    public CommandUsage getUsage() {
        return usage;
    }

    public void setUsage(CommandUsage usage) {
        this.usage = usage;
    }
}
