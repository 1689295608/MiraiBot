package com.windowx.miraibot.command;

public abstract class CommandRunner {
    private String msg;
    private String label;
    private String[] args;

    public CommandRunner() {

    }

    public String label() {
        return label;
    }

    public String[] args() {
        return args;
    }

    public String msg() {
        return msg;
    }

    public String args(int index) {
        return args[index];
    }

    public void start(String label) throws Exception {
        this.label = label;
        start();
    }

    public void start(String label, String[] args) throws Exception {
        this.label = label;
        this.args = args;
        start();
    }

    public void start(String label, String[] args, String msg) throws Exception {
        this.label = label;
        this.args = args;
        this.msg = msg;
        start();
    }

    public abstract void start() throws Exception;
}
