package com.windowx.miraibot.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.windowx.miraibot.MiraiBot;
import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class LanguageUtil {
	public static JsonObject languages = new JsonObject();
	public static JsonObject language = new JsonObject();
	static final Gson gson = new GsonBuilder()
			.setPrettyPrinting()
			.create();

	public static void init() throws IOException {
		try (InputStream is = ClassLoader.getSystemResourceAsStream("languages.json")) {
			if (is == null) throw new IOException("InputStream is null");
			String str = new String(is.readAllBytes(), StandardCharsets.UTF_8);
			languages = gson.fromJson(str, JsonObject.class);
		}
	}
	public static void load() throws IOException {
		try (FileInputStream fis = new FileInputStream("language.json")) {
			String str = new String(fis.readAllBytes(), StandardCharsets.UTF_8);
			language = gson.fromJson(str, JsonObject.class);
		}
	}

	@NotNull
	public static String l(String key) {
		JsonElement el = language.get(key);
		if (el == null) {
			JsonObject obj = getLanguage(MiraiBot.language);
			if (obj == null) return "";
			return l(key, obj);
		}
		return el.getAsString();
	}

	@NotNull
	public static String l(String key, JsonObject obj) {
		JsonElement el = obj.get(key);
		if (el == null) return "";
		return el.getAsString();
	}

	public static JsonObject getLanguage(String lang) {
		JsonElement el = languages.get(lang);
		if (el == null) {
			return null;
		}
		return el.getAsJsonObject();
	}

	public static String languageFile(String lang) {
		return gson.toJson(getLanguage(lang));
	}
}
