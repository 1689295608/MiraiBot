package com.windowx.miraibot.plugin;

import java.io.*;
import java.util.Properties;

public class ConfigCore {
	static Properties properties = new Properties();
	/**
	 * Modify the value of key in the configuration file
	 * @param key Key
	 * @param value Value
	 */
	public void setConfig(String key, String value) {
		properties.setProperty(key, value);
	}
	
	/**
	 * Get the value of key in the configuration file
	 * @param key Key
	 * @param defaultValue Default Value
	 * @return Value
	 */
	public String getConfig(String key, String defaultValue) {
		return properties.getProperty(key, defaultValue);
	}
	
	/**
	 * Load configuration file
	 * @param name Name
	 * @throws IOException IOException
	 */
	public void loadConfig(String name) throws IOException {
		File config = new File("plugins/" + name + "/");
		if (!config.exists()) {
			if (!config.mkdirs()) {
				throw new IOException("Cloud not create dirs: " + config);
			}
		}
		config = new File("plugins/" + name + "/config.ini");
		if (!config.exists()) {
			if (!config.createNewFile()) {
				throw new IOException("Cloud not create config file: " + config);
			}
		}
		properties.load(new FileReader(config));
	}
	
	public void saveConfig(String name) throws IOException {
		File config = new File("plugins/" + name + "/");
		if (!config.exists()) {
			if (!config.mkdirs()) {
				throw new IOException("Cloud not create dirs: " + config);
			}
		}
		config = new File("plugins/" + name + "/config.ini");
		if (!config.exists()) {
			if (!config.createNewFile()) {
				throw new IOException("Cloud not create config file: " + config);
			}
		}
		properties.store(new FileOutputStream(config), null);
	}
	/**
	 * Get whether the key is included in the configuration file
	 * @param key Key
	 * @return Whether to include the key
	 */
	public boolean hasKey(String key) {
		return properties.containsKey(key);
	}
	
	/**
	 * Get whether the value is included in the configuration file
	 * @param value Value
	 * @return Whether to include the value
	 */
	public boolean hasValue(String value) {
		return properties.containsValue(value);
	}
	
	/**
	 * Get current properties
	 * @return Current properties
	 */
	public Properties getProperties() {
		return properties;
	}
	
	/**
	 * Set current properties
	 * @param properties Properties
	 */
	public void setProperties(Properties properties) {
		ConfigCore.properties = properties;
	}
}
