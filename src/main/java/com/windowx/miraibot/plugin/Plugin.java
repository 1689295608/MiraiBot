package com.windowx.miraibot.plugin;

import com.windowx.miraibot.utils.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Plugin extends PluginBase {
	private File file;
	private String name;
	private String owner;
	private String className;
	private String version;
	private String description;
	private Properties config;
	private PluginClassLoader classLoader;
	private boolean isEnabled;
	private Properties plugin;
	private String[] commands;
	private PluginLoader loader;
	private Logger logger;

	public PluginLoader getPluginLoader() {
		return loader;
	}

	public void setPluginLoader(PluginLoader loader) {
		this.loader = loader;
	}

	public String[] getCommands() {
		return commands;
	}
	
	public void setCommands(String[] commands) {
		this.commands = commands;
	}
	
	public PluginClassLoader getPluginClassLoader() {
		return classLoader;
	}
	
	public void setPluginClassLoader(PluginClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public InputStream getResourceAsStream(String name) {
		return classLoader.getResourceAsStream(name);
	}

	public Properties getPlugin() {
		return plugin;
	}
	
	public void setPlugin(Properties plugin) {
		this.plugin = plugin;
	}
	
	public Properties getConfig() {
		return this.config;
	}
	
	public void setConfig(Properties properties) {
		this.config = properties;
	}

	public File getDataFolder() {
		return new File("plugins" + File.separator + name);
	}

	public void saveDefaultConfig() {
		InputStream is = getResourceAsStream("config.ini");
		if (is == null) return;
		try {
			config.load(is);
			saveConfig();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void saveConfig() throws IOException {
		File file = getDataFolder();
		if (!file.exists()) {
			if (!file.mkdirs()) {
				throw new IOException("Cloud not create dirs: " + config);
			}
		}
		file = new File(file, "config.ini");
		if (!file.exists()) {
			if (!file.createNewFile()) {
				throw new IOException("Cloud not create config file: " + config);
			}
		}
		config.store(new FileOutputStream(file), null);
	}
	
	public File getFile() {
		return file;
	}
	
	public void setFile(File file) {
		this.file = file;
	}
	
	public String getVersion() {
		return this.version;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getOwner() {
		return this.owner;
	}
	
	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	public String getClassName() {
		return this.className;
	}
	
	public boolean isEnabled() {
		return isEnabled;
	}
	
	public void setEnabled(boolean enabled) {
		isEnabled = enabled;
	}
	
	public void setClassName(String className) {
		this.className = className;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public Logger getLogger() {
		return logger;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public void info(String info, Object... args) {
		logger.info("[" + name + "] " + info, args);
	}

	public void error(String error, Object... args) {
		logger.error("[" + name + "] " + error, args);
	}

	public void warn(String warn, Object... args) {
		logger.warn("[" + name + "] " + warn, args);
	}

	public void info(String info) {
		logger.info("[" + name + "] " + info);
	}

	public void error(String error) {
		logger.error("[" + name + "] " + error);
	}

	public void warn(String warn) {
		logger.warn("[" + name + "] " + warn);
	}
}
