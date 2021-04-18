package com.windowx.miraibot.plugin;

public class Plugin {
	private String name;
	private String jar;
	private String className;
	public String getName() {
		return this.name;
	}
	public String getJar() {
		return this.jar;
	}
	public String getClassName() {
		return this.className;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setJar(String jar) {
		this.jar = jar;
	}
	public void setClassName(String className) {
		this.className = className;
	}
}
