package com.windowx.miraibot.plugin;

import com.windowx.miraibot.utils.LogUtil;
import net.mamoe.mirai.event.events.GroupMessageEvent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Plugin extends JavaPlugin {
	private File file;
	private String name;
	private String owner;
	private String className;
	private String version;
	private String description;
	private Properties config;
	private ClassLoader classLoader;
	private boolean isEnabled;
	private Properties plugin;
	private String[] commands;
	
	public String[] getCommands() {
		return commands;
	}
	
	public void setCommands(String[] commands) {
		this.commands = commands;
	}
	
	public ClassLoader getMyClassLoader() {
		return classLoader;
	}
	
	public void setMyClassLoader(ClassLoader classLoader) {
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		for (StackTraceElement ste : stack) {
			if (!ste.getClassName().equals("java.lang.Thread") && !ste.getClassName().equals("com.windowx.miraibot.PluginMain") &&
					!ste.getClassName().equals("com.windowx.miraibot.plugin.Plugin")) {
				return;
			}
		}
		this.classLoader = classLoader;
	}
	
	public Properties getPlugin() {
		return plugin;
	}
	
	public void setPlugin(Properties plugin) {
		this.plugin = plugin;
	}
	
	public Properties getConfig() {
		return this.config;
	}
	
	public void setConfig(Properties properties) {
		this.config = properties;
	}
	
	public void saveConfig() throws IOException {
		File file = new File("plugins/" + name + "/");
		if (!file.exists()) {
			if (!file.mkdirs()) {
				throw new IOException("Cloud not create dirs: " + config);
			}
		}
		file = new File("plugins/" + name + "/config.ini");
		if (!file.exists()) {
			if (!file.createNewFile()) {
				throw new IOException("Cloud not create config file: " + config);
			}
		}
		config.store(new FileOutputStream(file), null);
	}
	
	public File getFile() {
		return file;
	}
	
	public void setFile(File file) {
		this.file = file;
	}
	
	public String getVersion() {
		return this.version;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getOwner() {
		return this.owner;
	}
	
	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	public String getClassName() {
		return this.className;
	}
	
	public boolean isEnabled() {
		return isEnabled;
	}
	
	public void setEnabled(boolean enabled) {
		isEnabled = enabled;
	}
	
	public void setClassName(String className) {
		this.className = className;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public void info(String info, Object... args) {
		LogUtil.log("[" + name + "] " + info, args);
	}
	
	public void error(String error, Object... args) {
		LogUtil.error("[" + name + "] " + error, args);
	}
	
	public void warn(String warn, Object... args) {
		LogUtil.warn("[" + name + "] " + warn, args);
	}
	
	public void infop(String prefix, String info, Object... args) {
		LogUtil.logp(prefix, "[" + name + "] " + info, args);
	}
	
	public void errorp(String prefix, String error, Object... args) {
		LogUtil.errorp(prefix, "[" + name + "] " + error, args);
	}
	
	public void warnp(String prefix, String warn, Object... args) {
		LogUtil.warnp(prefix, "[" + name + "] " + warn, args);
	}
	
	public void info(String info) {
		LogUtil.log("[" + name + "] " + info);
	}
	
	public void error(String error) {
		LogUtil.error("[" + name + "] " + error);
	}
	
	public void warn(String warn) {
		LogUtil.warn("[" + name + "] " + warn);
	}
}

abstract class JavaPlugin {
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
