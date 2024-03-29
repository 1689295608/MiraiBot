package com.windowx.miraibot.plugin;

abstract class PluginBase {
    /**
     * 用户输入内容并回车时触发
     * @param cmd 用户输入的内容
     * @return 若为 false 则阻止这条消息被发送
     */
    public boolean onCommand(String cmd) {
        return true;
    }

    /**
     * 插件加载时触发
     */
    public void onEnable() { }

    /**
     * 插件禁用时触发，也会在关闭机器人时触发
     */
    public void onDisable() { }
}
