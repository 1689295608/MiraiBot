package com.windowx.miraibot.plugin;

import java.io.*;
import java.util.Properties;

public class PluginCore {
	static Properties properties = new Properties();
	public static Plugin plugin;
	
	/**
	 * Modify the value of key in the configuration file
	 * @param key Key
	 * @param value Value
	 * @throws IOException IOException
	 */
	@Deprecated public void setConfig(String key, String value) throws IOException {
		File config = new File("plugins/" + plugin.getName() + "/");
		if (!config.exists()) {
			if (!config.mkdirs()) {
				throw new IOException("Cloud not create dirs: " + config);
			}
		}
		config = new File("plugins/" + plugin.getName() + "/config.ini");
		if (!config.exists()) {
			if (!config.createNewFile()) {
				throw new IOException("Cloud not create config file: " + config);
			}
		}
		properties.load(new FileReader(config));
		properties.setProperty(key, value);
		FileOutputStream fos = new FileOutputStream(config);
		properties.store(fos, null);
		fos.close();
	}
	
	public void setConfigByName(String name, String key, String value) throws IOException {
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
		properties.setProperty(key, value);
		FileOutputStream fos = new FileOutputStream(config);
		properties.store(fos, null);
		fos.close();
	}
	
	/**
	 * Get the value of key in the configuration file
	 * @param key Key
	 * @param defaultValue Default Value
	 * @return Value
	 * @throws IOException IOException
	 */
	@Deprecated public String getConfig(String key, String defaultValue) throws IOException {
		File config = new File("plugins/" + plugin.getName() + "/config.ini");
		if (!config.exists()) {
			return defaultValue;
		}
		properties.load(new FileReader(config));
		return properties.getProperty(key, defaultValue);
	}
	
	public String getConfigByName(String name, String key, String defaultValue) throws IOException {
		File config = new File("plugins/" + name + "/config.ini");
		if (!config.exists()) {
			return defaultValue;
		}
		properties.load(new FileReader(config));
		return properties.getProperty(key, defaultValue);
	}
	
	public void loadConfigByName(String name) throws IOException {
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
	
	public Properties getPropertiesByName(String name) throws IOException {
		File config = new File("plugins/" + name + "/config.ini");
		properties.load(new FileReader(config));
		return properties;
	}
	
	/**
	 * Set current properties
	 * @param properties Properties
	 */
	public void setProperties(Properties properties) {
		PluginCore.properties = properties;
	}
	
	@Deprecated public String getName() {
		return plugin.getName();
	}
	@Deprecated public String getClassName() {
		return plugin.getClassName();
	}
	@Deprecated public String getJarPath() {
		return plugin.getJar();
	}
}
