package com.windowx.miraibot.plugin;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;

public class PluginClassLoader extends URLClassLoader {
    private final HashMap<String, Class<?>> classes = new HashMap<>();
    private final PluginLoader loader;

    public PluginClassLoader(URL[] urls, ClassLoader parent, PluginLoader loader) {
        super(urls, parent);
        this.loader = loader;
    }

    public Class<?> findClass(String name) throws ClassNotFoundException {
        return findClass(name, true);
    }

    public Class<?> findClass(String name, boolean global) throws ClassNotFoundException {
        Class<?> result = classes.get(name);

        if (result == null) {
            if (global) {
                result = loader.getClassByName(name);
            }

            if (result == null) {
                result = super.findClass(name);

                if (result != null) {
                    loader.setClass(name, result);
                }
            }
            classes.put(name, result);
        }
        return result;
    }
}
