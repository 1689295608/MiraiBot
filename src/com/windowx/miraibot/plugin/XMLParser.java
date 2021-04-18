package com.windowx.miraibot.plugin;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class XMLParser {
	public static File plugin;
	public static List<Plugin> getPluginList() throws DocumentException {
		List<Plugin> list = new ArrayList<>();
		
		SAXReader saxReader =new SAXReader();
		Document document = saxReader.read(plugin);
		Element root = document.getRootElement();
		List<?> plugins = root.elements("plugin");
		for(Object pluginObj : plugins) {
			Element pluginEle = (Element)pluginObj;
			Plugin plugin = new Plugin();
			plugin.setName(pluginEle.elementText("name"));
			plugin.setJar(pluginEle.elementText("jar"));
			plugin.setClassName(pluginEle.elementText("class"));
			list.add(plugin);
		}
		return list;
	}
}
