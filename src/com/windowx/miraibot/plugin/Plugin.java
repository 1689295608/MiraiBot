package com.windowx.miraibot.plugin;

public class Plugin extends JavaPlugin {
	private String name;
	private String className;
	private Plugin instance;
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return this.name;
	}
	public void setClassName(String className) {
		this.className = name;
	}
	public String getClassName() {
		return this.className;
	}
	public void setInstance(Plugin instance) {
		this.instance = instance;
	}
	public Plugin getInstance() {
		return this.instance;
	}
}
