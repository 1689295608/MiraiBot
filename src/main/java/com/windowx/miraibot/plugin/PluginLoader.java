package com.windowx.miraibot.plugin;

import com.windowx.miraibot.PluginMain;
import com.windowx.miraibot.utils.LogUtil;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.windowx.miraibot.PluginMain.completes;
import static com.windowx.miraibot.PluginMain.language;

public class PluginLoader {
    public ArrayList<Plugin> plugins;

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
     * 卸载某个插件
     *
     * @param name 插件名
     */
    public void unloadPlugin(String name) {
        Plugin plugin = null;
        for (Plugin value : plugins) {
            if (value.getName().equals(name)) {
                plugin = value;
                break;
            }
        }
        if (plugin != null) {
            LogUtil.log(language("unloading.plugin"), plugin.getName());
            try {
                plugin.onDisable();
            } catch (Exception e) {
                e.printStackTrace();
            }
            plugin.setEnabled(false);
            System.gc();
            LogUtil.log(language("unloaded.plugin"), plugin.getName());
        } else {
            LogUtil.error(language("plugin.not.exits"), name);
        }
    }

    private Plugin init(Properties plugin, PluginClassLoader u) throws Exception {
        Class<?> clazz = u.loadClass(plugin.getProperty("main"));
        Plugin p = (Plugin) clazz.getDeclaredConstructor().newInstance();
        p.setName(plugin.getProperty("name", "Untitled"));
        p.setOwner(plugin.getProperty("owner", "Unnamed"));
        p.setClassName(plugin.getProperty("main"));
        p.setVersion(plugin.getProperty("version", "1.0.0"));
        p.setDescription(plugin.getProperty("description", "A Plugin For MiraiBot."));
        p.setCommands(plugin.getProperty("commands", "").split(","));
        p.setPluginLoader(u);
        p.setPlugin(plugin);
        Properties config = new Properties();
        File file = new File("plugins/" + plugin.getProperty("name") + "/config.ini");
        if (file.exists()) config.load(new FileReader(file));
        p.setConfig(config);
        return p;
    }

    /**
     * 加载某个插件
     *
     * @param file 文件
     * @param name 名称
     * @throws Exception 报错
     */
    public void loadPlugin(File file, String name) throws Exception {
        if (file.exists()) {
            Plugin plugin = null;
            PluginClassLoader u = new PluginClassLoader(new URL[]{file.toURI().toURL()}, getClass().getClassLoader());
            InputStream is = u.getResourceAsStream("plugin.ini");
            if (is != null) {
                LogUtil.log(language("loading.plugin"), name);
                Properties prop = new Properties();
                prop.load(is);
                if (prop.containsKey("depend")) {
                    String[] split = prop.getProperty("depend").split(",");
                    for (String s : split) {
                        if (!hasPlugin(s)) {
                            LogUtil.error(language("depend.not.exits"), s);
                            return;
                        }
                    }
                }
                plugin = init(prop, u);
            } else {
                LogUtil.error(language("failed.load.plugin"), file.getName(), "\"plugin.ini\" not found");
            }
            if (plugin != null) {
                plugin.setFile(file);
                Plugin p = getPlugin(plugin.getName());
                if (p != null) {
                    if (p.isEnabled()) {
                        LogUtil.warn(language("plugin.already.loaded"), plugin.getName());
                        plugins.remove(p);
                        return;
                    }
                }
                plugin.setEnabled(true);
                plugins.add(plugin);
                try {
                    plugin.onEnable();
                    completes.addAll(List.of(plugin.getCommands()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                LogUtil.log(language("loaded.plugin"), plugin.getName());
            } else {
                LogUtil.error(language("failed.load.plugin"), file.getName(), "unknown error");
            }
        } else {
            LogUtil.error(language("plugin.file.not.exits")
                    , name
            );
        }
    }

    /**
     * 初始化插件，将所有 plugins 文件夹内的 .jar 文件加载到插件列表
     */
    public void initPlugins() {
        File pluginsDir = new File("plugins");
        plugins = new ArrayList<>();
        try {
            File[] pluginsFile = pluginsDir.listFiles();
            if (pluginsFile == null) {
                return;
            }
            ArrayList<File> after = new ArrayList<>();
            for (File f : pluginsFile) {
                if (!f.getName().endsWith(".jar")) continue;
                Plugin plugin = null;
                PluginClassLoader u = new PluginClassLoader(new URL[]{f.toURI().toURL()}, getClass().getClassLoader());
                InputStream is = u.getResourceAsStream("plugin.ini");
                if (is == null) {
                    LogUtil.error(language("failed.load.plugin"), f.getName(), "\"plugin.ini\" not found");
                    continue;
                }
                try {
                    Properties prop = new Properties();
                    prop.load(is);
                    if (prop.containsKey("depend")) {
                        String[] split = prop.getProperty("depend").split(",");
                        boolean con = false;
                        for (String s : split) {
                            if (getPlugin(s) == null) {
                                after.add(f);
                                con = true;
                                break;
                            }
                        }
                        if (con) {
                            continue;
                        }
                    }
                    plugin = init(prop, u);
                    plugin.setPluginLoader(u);
                } catch (Exception e) {
                    System.out.println();
                    e.printStackTrace();
                }
                if (plugin != null) {
                    plugin.setFile(f);
                    plugin.setEnabled(true);
                    plugins.add(plugin);
                } else {
                    LogUtil.error(language("failed.load.plugin"), f.getName(), "unknown error");
                }
            }
            if (after.size() < 1) {
                return;
            }
            for (File f : after) {
                Plugin plugin = null;
                PluginClassLoader u = new PluginClassLoader(new URL[]{f.toURI().toURL()}, getClass().getClassLoader());
                InputStream is = u.getResourceAsStream("plugin.ini");
                assert is != null;

                try {
                    Properties prop = new Properties();
                    prop.load(is);
                    if (prop.containsKey("depend")) {
                        String[] split = prop.getProperty("depend").split(",");
                        for (String s : split) {
                            if (!hasPlugin(s)) {
                                LogUtil.error(language("depend.not.exits"), s);
                                return;
                            }
                        }
                    }
                    plugin = init(prop, u);
                    plugin.setPluginLoader(u);
                } catch (Exception e) {
                    System.out.println();
                    e.printStackTrace();
                }

                if (plugin != null) {
                    plugin.setFile(f);
                    plugin.setEnabled(true);
                    plugins.add(plugin);
                } else {
                    LogUtil.error(language("failed.load.plugin"), f.getName(), "unknown error");
                }
            }
        } catch (Exception e) {
            LogUtil.error(language("unknown.error"));
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
