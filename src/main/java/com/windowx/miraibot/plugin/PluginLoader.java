package com.windowx.miraibot.plugin;

import com.windowx.miraibot.MiraiBot;
import com.windowx.miraibot.command.Commands;
import com.windowx.miraibot.event.EventHandler;
import com.windowx.miraibot.event.ListenerHost;
import com.windowx.miraibot.utils.Logger;
import net.mamoe.mirai.event.Event;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Properties;

import static com.windowx.miraibot.MiraiBot.logger;
import static com.windowx.miraibot.MiraiBot.reloadCommands;
import static com.windowx.miraibot.utils.LanguageUtil.l;

public class PluginLoader {
    public ArrayList<Plugin> plugins;
    public final HashMap<String, Class<?>> classes = new HashMap<>();
    public final HashMap<Plugin, PluginClassLoader> loaders = new LinkedHashMap<>();
    public final HashMap<Plugin, ArrayList<ListenerHost>> listeners = new HashMap<>();

    /**
     * 广播一个事件，使所有 Listener 都可以监听到这个事件
     *
     * @param event 事件
     */
    public void broadcastEvent(Event event) {
        for (Plugin plugin : listeners.keySet()) {
            if (!plugin.isEnabled()) continue;
            ArrayList<ListenerHost> listeners = this.listeners.get(plugin);
            for (ListenerHost listener : listeners) {
                Method[] methods = listener.getClass().getDeclaredMethods();
                for (Method method : methods) {
                    if (!method.isAnnotationPresent(EventHandler.class)) {
                        continue;
                    }
                    Class<?>[] type = method.getParameterTypes();
                    if (type.length < 1) {
                        continue;
                    }
                    Class<?> ec = event.getClass();
                    if (type[0] != event.getClass() && !ec.isAssignableFrom(type[0]) && !type[0].isAssignableFrom(ec)) {
                        continue;
                    }
                    try {
                        method.invoke(listener, event);
                    } catch (Exception e) {
                        logger.error(l("event.error"),
                                plugin.getName(),
                                className(event.getClass().getName()),
                                e.toString()
                        );
                        logger.trace(e);
                        if (e instanceof InvocationTargetException ie) {
                            logger.trace(ie.getTargetException());
                        }
                    }
                }
            }
        }
    }

    /**
     * 注册事件监听器
     *
     * @param plugin   插件
     * @param listener 监听器
     */
    public void registerListener(Plugin plugin, ListenerHost listener) {
        ArrayList<ListenerHost> list = listeners.get(plugin);
        if (list == null) {
            list = new ArrayList<>();
        }
        if (!list.contains(listener)) {
            list.add(listener);
        }
        listeners.put(plugin, list);
    }

    /**
     * 注册多个事件监听器
     *
     * @param plugin    插件
     * @param listeners 监听器数组
     */
    public void registerListeners(Plugin plugin, ListenerHost[] listeners) {
        ArrayList<ListenerHost> list = this.listeners.get(plugin);
        if (list == null) {
            list = new ArrayList<>();
        }
        for (ListenerHost l : listeners) {
            if (list.contains(l)) {
                continue;
            }
            list.add(l);
        }
        this.listeners.put(plugin, list);
    }

    /**
     * 移除一个事件监听器
     * @param plugin    插件
     * @param listener  监听器
     */
    public void removeListener(Plugin plugin, ListenerHost listener) {
        ArrayList<ListenerHost> list = this.listeners.get(plugin);
        if (list == null) {
            return;
        }
        list.remove(listener);
    }

    /**
     * 获取插件的类名（不是全写）
     *
     * @param name 全写
     * @return 类名
     */
    private String className(String name) {
        String[] split = name.split("\\.");
        return split[split.length - 1];
    }

    /**
     * 通过插件名获取插件对象
     *
     * @param name 插件名
     * @return 插件
     */
    @Nullable
    public Plugin getPlugin(String name) {
        for (Plugin p : plugins) {
            if (p.getName().equals(name)) return p;
        }
        return null;
    }

    public String[] getPluginNames() {
        ArrayList<String> al = new ArrayList<>();
        for (Plugin p : plugins) {
            al.add(p.getName());
        }
        return al.toArray(new String[0]);
    }

    /**
     * 通过插件名获取插件是否存在
     *
     * @param name 插件名
     * @return 是否存在
     */
    public boolean hasPlugin(String name) {
        for (Plugin p : plugins) {
            if (p.getName().equals(name)) return true;
        }
        return false;
    }

    /**
     * 获取所有插件列表
     *
     * @return 所有插件
     */
    public ArrayList<Plugin> getPlugins() {
        return plugins;
    }

    /**
     * 卸载某个插件
     *
     * @param name 插件名
     */
    public void unloadPlugin(String name) {
        Plugin plugin = getPlugin(name);
        if (plugin == null) {
            logger.error(l("plugin.not.exists"), name);
            return;
        }
        logger.info(l("unloading.plugin"), plugin.getName());
        try {
            plugin.onDisable();
        } catch (Exception e) {
            logger.trace(e);
        }
        plugin.setEnabled(false);
        listeners.remove(plugin);
        removeCommands(plugin.getCommands());
        clearCaches();
        loaders.remove(plugin);
        plugins.remove(plugin);
        System.gc();
        logger.info(l("unloaded.plugin"), name);
    }

    private Plugin init(Properties plugin, PluginClassLoader u) throws Exception {
        Class<?> clazz = u.loadClass(plugin.getProperty("main"));
        Class<? extends Plugin> pc;
        try {
            pc = clazz.asSubclass(Plugin.class);
        } catch (ClassCastException e) {
            logger.trace(e);
            logger.error(l("not.extends.plugin"), clazz.getName());
            return null;
        }
        Plugin p = pc.getDeclaredConstructor().newInstance();
        p.setName(plugin.getProperty("name"));
        p.setOwner(plugin.getProperty("owner"));
        p.setClassName(plugin.getProperty("main"));
        p.setVersion(plugin.getProperty("version"));
        p.setDescription(plugin.getProperty("description"));
        p.setPluginClassLoader(u);
        p.setPlugin(plugin);
        p.setCommands(new Commands());
        p.setPluginLoader(MiraiBot.loader);
        p.setLogger(new Logger("[" + p.getName() + "] "));
        File file = new File(p.getDataFolder(), "/config.json");
        if (file.exists()) {
            p.loadConfig(new FileInputStream(file));
        }
        loaders.put(p, u);
        return p;
    }

    Class<?> getClassByName(String name) {
        Class<?> cachedClass = classes.get(name);

        if (cachedClass != null) {
            return cachedClass;
        }
        for (Plugin current : loaders.keySet()) {
            PluginClassLoader loader = loaders.get(current);

            try {
                cachedClass = loader.findClass(name, false);
            } catch (ClassNotFoundException ignored) {
                return null;
            }
            if (cachedClass != null) {
                return cachedClass;
            }
        }
        return null;
    }

    void setClass(final String name, final Class<?> clazz) {
        if (!classes.containsKey(name)) {
            classes.put(name, clazz);
        }
    }

    void removeClass(final String name) {
        classes.remove(name);
    }

    void clearCaches() {
        classes.clear();
    }

    /**
     * 加载某个插件
     *
     * @param file 文件
     * @param name 名称
     * @throws Exception 报错
     */
    public void loadPlugin(File file, String name) throws Exception {
        if (!file.exists()) {
            logger.error(l("plugin.file.not.exists")
                    , name
            );
            return;
        }
        ArrayList<File> files = new ArrayList<>();
        files.add(file);
        loadPlugins(files.toArray(new File[0]));
    }

    public void add2commands(Commands commands) {
        for (String command : commands.keys()) {
            MiraiBot.commands.set(command, commands.get(command));
        }
        reloadCommands();
    }

    public void removeCommands(Commands commands) {
        for (String command : commands.keys()) {
            MiraiBot.commands.remove(command);
        }
        reloadCommands();
    }

    /**
     * 初始化插件，将所有 plugins 文件夹内的 .jar 文件加载到插件列表
     */
    public void initPlugins() throws IOException {
        File pluginsDir = new File("plugins");
        plugins = new ArrayList<>();
        File[] pluginsFile = pluginsDir.listFiles();
        if (pluginsFile == null) {
            return;
        }
        loadPlugins(pluginsFile);
    }

    /**
     * 通过文件数组加载插件，返回缺失前置插件的文件数组
     *
     * @param files 插件文件数组
     * @return 缺失前置插件的插件文件数组
     */
    public ArrayList<File> loadPluginFiles(File[] files) {
        ArrayList<File> after = new ArrayList<>();
        for (File f : files) {
            if (!f.getName().endsWith(".jar")) continue;
            Plugin plugin = null;
            PluginClassLoader u;
            try {
                u = getLoader(f);
            } catch (IOException e) {
                logger.error(l("failed.load.plugin"), f.getName(), e.toString());
                continue;
            }
            try {
                Properties info = getPluginInfo(u);
                if (info == null) {
                    logger.error(l("failed.load.plugin"), f.getName(), "'plugin.ini' does not exist");
                    continue;
                }
                String[] depends = getDepends(info);
                if (!loadedDepends(depends)) {
                    after.add(f);
                    continue;
                }
                plugin = init(info, u);
            } catch (Exception e) {
                logger.trace(e);
            }
            if (plugin != null) {
                plugin.setFile(f);
                plugin.setEnabled(true);
                plugins.add(plugin);
                add2commands(plugin.getCommands());
                try {
                    plugin.onEnable();
                } catch (Exception e) {
                    plugin.setEnabled(false);
                    unloadPlugin(plugin.getName());
                    logger.error(l("failed.load.plugin"), plugin.getName(), e.toString());
                    logger.trace(e);
                }
                logger.info(l("loaded.plugin"), plugin.getName());
            } else {
                logger.error(l("failed.load.plugin"), f.getName(), "unknown error");
            }
        }
        return after;
    }

    /**
     * 获取是否已加载前置插件列表
     * @param depends 前置插件列表
     * @return 是否已全部加载
     */
    public boolean loadedDepends(String[] depends) {
        for (String s : depends) {
            if (getPlugin(s) == null && !s.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 通过文件数组加载插件
     *
     * @param files 文件数组
     */
    public void loadPlugins(File[] files) throws IOException {
        ArrayList<File> after = loadPluginFiles(files);
        if (after.size() > 0) {
            ArrayList<String> names = getNames(after.toArray(new File[0]));
            for (File f : after) {
                PluginClassLoader u = getLoader(f);
                Properties info = getPluginInfo(u);
                String[] depends = getDepends(info);
                for (String s : depends) {
                    if (!names.contains(s)) {
                        logger.error(l("failed.load.plugin"),
                                f.getName(),
                                "missing depend plugin '" + s + "'"
                        );
                        // 缺失前置插件时报错并移除该插件
                        after.remove(f);
                    }
                }
            }
            // 递归加载插件
            loadPlugins(after.toArray(new File[0]));
        }
    }

    public Properties getPluginInfo(PluginClassLoader loader) throws IOException {
        InputStream is = loader.getResourceAsStream("plugin.ini");
        if (is == null) return null;
        Properties properties = new Properties();
        properties.load(is);
        return properties;
    }

    public PluginClassLoader getLoader(File file) throws IOException {
        URL[] urls = new URL[]{
                file.toURI().toURL()
        };
        return new PluginClassLoader(urls, getClass().getClassLoader(), this);
    }

    public String[] getDepends(Properties info) {
        String depends = info.getProperty("depend", "");
        return depends.split(",");
    }

    public ArrayList<String> getNames(File[] files) throws IOException {
        ArrayList<String> back = new ArrayList<>();
        for (File f : files) {
            PluginClassLoader u = getLoader(f);
            Properties info = getPluginInfo(u);
            if (!info.containsKey("name")) continue;
            back.add(info.getProperty("name"));
        }
        return back;
    }
}
