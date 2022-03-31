package com.windowx.miraibot.plugin;

import java.net.URL;
import java.net.URLClassLoader;

public class PluginClassLoader extends URLClassLoader {
    public PluginClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public Class<?> findClass(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }
}
