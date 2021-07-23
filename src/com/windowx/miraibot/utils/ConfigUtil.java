package com.windowx.miraibot.utils;

import org.jetbrains.annotations.NotNull;

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
			LogUtil.log(ConfigUtil.getLanguage("unknown.error"));
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	/**
	 * Get the value of key in the configuration file
	 *
	 * @param key Key
	 * @return Config value
	 */
	@NotNull public static String getConfig(String key) {
		if (key.equals("password")) {
			StackTraceElement[] stack = Thread.currentThread().getStackTrace();
			for (StackTraceElement ste : stack) {
				if (!ste.getClassName().equals("java.lang.Thread") &&
						!ste.getClassName().equals("com.windowx.miraibot.utils.ConfigUtil") &&
						!ste.getClassName().equals("com.windowx.miraibot.PluginMain")) {
					return "";
				}
			}
		}
		return config.getProperty(key) != null ? config.getProperty(key) : "";
	}
	
	/**
	 * Get the value of key in the language file
	 *
	 * @param key Key
	 * @return Language value
	 */
	public static String getLanguage(String key) {
		return language.getProperty(key);
	}
}
