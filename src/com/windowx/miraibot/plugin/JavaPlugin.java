package com.windowx.miraibot.plugin;

import net.mamoe.mirai.event.events.GroupMessageEvent;

public abstract class JavaPlugin {
	public boolean onCommand(String cmd) {
		return true;
	}
	
	public void onEnable() {
	}
	
	public void onDisable() {
	}
	
	public void onGroupMessage(GroupMessageEvent event) {
	}
	
	public void onFinished() {
	}
}
