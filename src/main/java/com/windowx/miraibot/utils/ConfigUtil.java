package com.windowx.miraibot.utils;

import com.windowx.miraibot.MiraiBot;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Properties;

import static com.windowx.miraibot.MiraiBot.logger;

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
			logger.info(ConfigUtil.getLanguage("unknown.error"));
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
			if (isPrivate(stack)) return "";
		}
		return config.getProperty(key) != null ? config.getProperty(key) : "";
	}
	@NotNull public static String getConfig(String key, String defaultValue) {
		if (key.equals("password")) {
			StackTraceElement[] stack = Thread.currentThread().getStackTrace();
			if (isPrivate(stack)) return "";
		}
		return config.getProperty(key) != null ? config.getProperty(key) : defaultValue;
	}
	
	private static boolean isPrivate(StackTraceElement[] stack) {
		for (StackTraceElement ste : stack) {
			if (!ste.getClassName().equals("java.lang.Thread") &&
					!ste.getClassName().equals("com.windowx.miraibot.utils.ConfigUtil") &&
					!ste.getClassName().equals("com.windowx.miraibot.PluginMain")) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Get the value of key in the language file
	 *
	 * @param key Key
	 * @return Language value
	 */
	@NotNull public static String getLanguage(String key) {
		String lang = language.getProperty(key);
		if (lang == null) {
			Properties tmpLanguage = new Properties();
			try {
				tmpLanguage.load(new StringReader(new String(LanguageUtil.languageFile(MiraiBot.language))));
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (tmpLanguage.containsKey(key)) {
				return tmpLanguage.getProperty(key);
			} else {
				return "";
			}
		}
		return language.getProperty(key);
	}
}
