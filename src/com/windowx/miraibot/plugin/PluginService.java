package com.windowx.miraibot.plugin;

import com.windowx.miraibot.utils.LogUtil;
import net.mamoe.mirai.event.events.GroupMessageEvent;

public abstract class PluginService extends Plugin {
	public boolean onCommand(String cmd) {
		return true;
	}
	
	public void onEnable() {
		LogUtil.log("[" + this.getName() + "] Enabling " + this.getName() + "...");
	}
	
	public void onGroupMessage(GroupMessageEvent event) { }
}
