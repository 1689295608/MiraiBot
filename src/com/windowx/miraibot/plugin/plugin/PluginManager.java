package com.windowx.miraibot.plugin;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

public class PluginManager {
	private URLClassLoader urlClassLoader;
	
	public PluginManager(List<Plugin> plugins) throws MalformedURLException {
		init(plugins);
	}
	
	private void init(List<Plugin> plugins) throws MalformedURLException {
		int size = plugins.size();
		URL[] urls = new URL[size];
		
		for(int i = 0; i < size; i++) {
			Plugin plugin = plugins.get(i);
			String filePath = plugin.getJar();
			
			urls[i] = new File(filePath).toURI().toURL();
		}
		
		// 将jar文件组成数组，来创建一个URLClassLoader
		urlClassLoader = new URLClassLoader(urls);
	}
	
	public PluginService getInstance(String className)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		// 插件实例化对象，插件都是实现PluginService接口
		Class<?> clazz = urlClassLoader.loadClass(className);
		Object instance = clazz.getDeclaredConstructor().newInstance();
		
		return (PluginService) instance;
	}
}
