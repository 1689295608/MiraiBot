package com.windowx.miraibot.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Set;

public class ConfigSection extends ConfigElement {
    private final JsonObject config;

    public ConfigSection() {
        config = new JsonObject();
    }
    public ConfigSection(JsonObject object) {
        config = object;
    }

    public boolean has(String key) {
        return config.has(key);
    }

    public ConfigSection getSection(String key) {
        if (!config.has(key)) {
            return new ConfigSection();
        }
        return new ConfigSection(config.getAsJsonObject(key));
    }

    public ConfigElement get(String key) {
        return new ConfigElement(config.get(key));
    }

    public ConfigElement getOrDefault(String key, ConfigElement def) {
        if (!config.has(key)) {
            return def;
        }
        return new ConfigElement(config.get(key));
    }

    public ConfigElement getOrDefault(String key, String def) {
        return getOrDefault(key, new ConfigElement(def));
    }
    public ConfigElement getOrDefault(String key, Boolean def) {
        return getOrDefault(key, new ConfigElement(def));
    }
    public ConfigElement getOrDefault(String key, Number def) {
        return getOrDefault(key, new ConfigElement(def));
    }
    public ConfigElement getOrDefault(String key, Character def) {
        return getOrDefault(key, new ConfigElement(def));
    }

    public void set(String key, ConfigElement element) {
        config.add(key, element);
    }
    public void set(String key, JsonElement element) {
        config.add(key, element);
    }

    public void set(String key, String val) {
        config.addProperty(key, val);
    }

    public void set(String key, Boolean val) {
        config.addProperty(key, val);
    }

    public void set(String key, Number val) {
        config.addProperty(key, val);
    }

    public void set(String key, Character val) {
        config.addProperty(key, val);
    }

    public Set<String> keySet() {
        return config.keySet();
    }
}
