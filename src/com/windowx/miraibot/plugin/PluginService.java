package com.windowx.miraibot.plugin;

import net.mamoe.mirai.event.events.GroupMessageEvent;

public abstract class PluginService extends Plugin {
	public boolean onCommand(String cmd) {
		return true;
	}
	
	public void onEnable() { }
	
	public void onGroupMessage(GroupMessageEvent event) { }
}
