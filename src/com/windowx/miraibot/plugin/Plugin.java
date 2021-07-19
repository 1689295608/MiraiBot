package com.windowx.miraibot.plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Plugin extends JavaPlugin {
	private String name;
	private String owner;
	private String className;
	private String version;
	private Properties config;
	public File file;
	public void setConfig(Properties properties) {
		this.config = properties;
	}
	public Properties getConfig() {
		return this.config;
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
	public void setVersion(String version) {
		this.version = version;
	}
	public String getVersion() {
		return this.version;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return this.name;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getOwner() {
		return this.owner;
	}
	public void setClassName(String className) {
		this.className = name;
	}
	public String getClassName() {
		return this.className;
	}
}
