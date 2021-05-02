package com.windowx.miraibot.plugin;

public interface PluginService {
	void onEnable();
	
	/**
	 * Triggered when the instruction is executed,
	 * it is not recommended to return false anyway,
	 * it will cause the user to be unable to send messages directly.
	 * @param cmd Command
	 * @return Whether to allow the message to be sent
	 */
	boolean onCommand(String cmd);
}
