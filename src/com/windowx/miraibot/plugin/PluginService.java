package com.windowx.miraibot.plugin;

public abstract class PluginService {
	public boolean onCommand(String cmd) {
		return true;
	}
	
	public void onEnable() { }
}
