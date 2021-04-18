package com.windowx.miraibot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class ConfigUtil {
	public static final Properties config = new Properties();
	public static final Properties language = new Properties();
	
	/**
	 * Initialize the config system
	 */
	public static void init() {
		try {
			File tmpFile = new File("config.properties");
			if (tmpFile.exists()) {
				config.load(new BufferedReader(new FileReader("config.properties")));
			}
			tmpFile = new File("language.properties");
			if (tmpFile.exists()) {
				language.load(new BufferedReader(new FileReader("language.properties")));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Get the value of key in the configuration file
	 * @param key Key
	 * @return Config value
	 */
	public static String getConfig(String key) {
		return config.getProperty(key);
	}
	
	/**
	 * Get the value of key in the language file
	 * @param key Key
	 * @return Language value
	 */
	public static String getLanguage(String key) {
		return language.getProperty(key);
	}
}
