package com.windowx.miraibot.plugin;

import net.mamoe.mirai.event.events.GroupMessageEvent;

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
     * 插件加载完毕时触发
     */
    public void onFinished() { }

    /**
     * 插件禁用时触发，也会在关闭机器人时触发
     */
    public void onDisable() { }

    /**
     * 在已允许的群中有新消息时触发
     * @param event 新消息事件
     */
    public void onGroupMessage(GroupMessageEvent event) { }
}
