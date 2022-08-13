package com.windowx.miraibot.plugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.windowx.miraibot.command.Command;
import com.windowx.miraibot.command.Commands;
import com.windowx.miraibot.utils.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class Plugin extends PluginBase {
	private File file;
	private String name;
	private String owner;
	private String className;
	private String version;
	private String description;
	private JsonObject config = new JsonObject();
	private PluginClassLoader classLoader;
	private boolean isEnabled;
	private Properties plugin;
	private Commands commands;
	private PluginLoader loader;
	private Logger logger;

	public PluginLoader getPluginLoader() {
		return loader;
	}

	public void setPluginLoader(PluginLoader loader) {
		this.loader = loader;
	}

	public Commands getCommands() {
		return commands;
	}
	
	public void setCommands(Commands commands) {
		this.commands = commands;
	}
	
	public void registerCommand(String name, Command command) {
		this.commands.set(name, command);
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

	public JsonObject config() {
		return config;
	}

	public JsonElement config(String key) {
		return config.get(key);
	}

	public JsonElement config(String key, JsonElement def) {
		if (!config.has(key)) {
			return def;
		}
		return config.get(key);
	}

	public void config(JsonObject c) {
		this.config = c;
	}

	public void loadConfig(InputStream is) throws IOException {
		Gson gson = new Gson();
		String s = new String(is.readAllBytes(), StandardCharsets.UTF_8);
		config = gson.fromJson(s, JsonObject.class);
	}

	public File getDataFolder() {
		return new File("plugins" + File.separator + name);
	}

	public void saveDefaultConfig() throws IOException {
		try (InputStream is = getResourceAsStream("config.json")) {
			if (is == null) return;
			loadConfig(is);
		}
	}

	public void saveConfig() throws IOException {
		File file = getDataFolder();
		if (!file.exists()) {
			if (!file.mkdirs()) {
				throw new IOException("Cloud not create dirs: " + config);
			}
		}
		file = new File(file, "config.json");
		if (!file.exists()) {
			if (!file.createNewFile()) {
				throw new IOException("Cloud not create config file: " + config);
			}
		}
		Gson gson = new GsonBuilder()
				.setPrettyPrinting()
				.create();
		String str = gson.toJson(config);
		try (FileOutputStream fos = new FileOutputStream(file)) {
			fos.write(str.getBytes(StandardCharsets.UTF_8));
		}
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
		logger.info(info, args);
	}

	public void error(String error, Object... args) {
		logger.error(error, args);
	}

	public void warn(String warn, Object... args) {
		logger.warn(warn, args);
	}

	public void info(String info) {
		logger.info(info);
	}

	public void error(String error) {
		logger.error(error);
	}

	public void warn(String warn) {
		logger.warn(warn);
	}
}
