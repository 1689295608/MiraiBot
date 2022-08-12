package com.windowx.miraibot.utils;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import static com.windowx.miraibot.MiraiBot.logger;

public class ConfigUtil {
	public static final Properties config = new Properties();
	
	/**
	 * Initialize the config system
	 */
	public static void init() {
		try {
			File tmpFile = new File("config.properties");
			if (tmpFile.exists()) {
				config.load(new BufferedReader(new FileReader("config.properties")));
			}
		} catch (IOException e) {
			logger.info(LanguageUtil.l("unknown.error"));
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
					!ste.getClassName().equals("com.windowx.miraibot.MiraiBot")) {
				return true;
			}
		}
		return false;
	}
}
