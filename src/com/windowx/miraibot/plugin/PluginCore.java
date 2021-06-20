package com.windowx.miraibot.plugin;

import java.io.*;
import java.util.Properties;

public class PluginCore {
	static Properties properties = new Properties();
	public static Plugin plugin;
	public void setConfig(String key, String value) throws IOException {
		File config = new File("plugins/" + plugin.getName() + "/");
		if (!config.exists()) {
			if (!config.mkdirs()) {
				throw new IOException("cloud not create dirs: " + config);
			}
		}
		config = new File("plugins/" + plugin.getName() + "/config.ini");
		if (!config.exists()) {
			if (!config.createNewFile()) {
				throw new IOException("cloud not create config file: " + config);
			}
		}
		properties.load(new FileReader(config));
		properties.setProperty(key, value);
	}
	
	public String getConfig(String key, String defaultValue) throws IOException {
		File config = new File("plugins/" + plugin.getName() + "/config.ini");
		if (!config.exists()) {
			return defaultValue;
		}
		properties.load(new FileReader(config));
		return properties.getProperty(key, defaultValue);
	}
	
	public void saveConfig() throws IOException {
		File config = new File("plugins/" + plugin.getName() + "/");
		if (!config.exists()) {
			if (!config.mkdirs()) {
				throw new IOException("cloud not create dirs: " + config);
			}
		}
		config = new File("plugins/" + plugin.getName() + "/config.ini");
		if (!config.exists()) {
			if (!config.createNewFile()) {
				throw new IOException("cloud not create config file: " + config);
			}
		}
		FileOutputStream fos = new FileOutputStream(config);
		properties.store(fos, null);
		fos.close();
	}
	
	public boolean hasKey(String key) {
		return properties.containsKey(key);
	}
	
	public boolean hasValue(String value) {
		return properties.containsValue(value);
	}
	
	public Properties getProperties() {
		return properties;
	}
}
