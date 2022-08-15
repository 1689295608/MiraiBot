package com.windowx.miraibot;

import com.windowx.miraibot.command.Command;
import com.windowx.miraibot.command.Commands;
import com.windowx.miraibot.plugin.Plugin;
import com.windowx.miraibot.plugin.PluginLoader;
import com.windowx.miraibot.utils.ConfigUtil;
import com.windowx.miraibot.utils.LanguageUtil;
import com.windowx.miraibot.utils.LogUtil;
import com.windowx.miraibot.utils.Logger;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.BotFactory;
import net.mamoe.mirai.Mirai;
import net.mamoe.mirai.contact.BotIsBeingMutedException;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageSource;
import net.mamoe.mirai.utils.BotConfiguration;
import org.fusesource.jansi.AnsiConsole;
import org.jline.builtins.Completers;
import org.jline.reader.*;
import org.jline.reader.impl.completer.ArgumentCompleter;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;

import static com.windowx.miraibot.EventListener.messages;
import static com.windowx.miraibot.utils.LanguageUtil.l;

public class MiraiBot {
    public static final String language = Locale.getDefault().getLanguage();
    public static final PluginLoader loader = new PluginLoader();
    public static final Logger logger = new Logger();
    public static final Commands commands = new Commands();
    public static Group group;
    public static String[] groups;
    public static String[] allowedGroups;
    public static Bot bot;
    public static ArgumentCompleter completer;
    public static LineReader reader;
    public static Terminal terminal;
    public static final History history = new DefaultHistory();
    public static Thread lineThread;

    public static void main(String[] args) {
        String err = language.equals("zh") ? "出现错误！进程即将终止！" : "Unable to create configuration file!";
        TerminalBuilder tb = TerminalBuilder.builder().encoding(Charset.defaultCharset()).jansi(true).jna(false);
        LogUtil.init();
        try {
            terminal = tb.build();
        } catch (IOException e) {
            logger.trace(e);
            logger.error(err);
            System.exit(-1);
        }
        try {
            URL url = ClassLoader.getSystemResource("Version");
            String version = "";
            if (url != null) {
                try (InputStream is = url.openStream()) {
                    version = new String(is.readAllBytes()).replaceAll("[^0-9.]", "");
                }
            }
            logger.info(language.equals("zh") ? "MiraiBot " + version + " 基于 Mirai-Core. 版权所有 (C) WindowX 2021" : "MiraiBot " + version + " based Mirai-Core. Copyright (C) WindowX 2021");
        } catch (Exception e) {
            logger.trace(e);
        }
        try {
            File eula = new File("eula.txt");
            if (!eula.exists()) {
                if (eula.createNewFile()) {
                    try (FileOutputStream fos = new FileOutputStream(eula)) {
                        fos.write(("""
                                # 使用本软件，您必须遵守我们的协议，一切基于本软件开发的插件都必须在项目明显位置准确提及来自 MiraiBot，不得扭曲或隐藏免费且开源的事实。
                                # To use this software, you must abide by our agreement. All plug-ins developed based on this software must accurately mention MiraiBot in the obvious place of the project, and must not distort or hide the fact that it is free and open source
                                # 详情请查看：https://github.com/1689295608/MiraiBot/blob/main/LICENSE
                                # Details: https://github.com/1689295608/MiraiBot/blob/main/LICENSE
                                eula=false""").getBytes());
                    }
                }
            }
            Properties prop = new Properties();
            prop.load(new FileInputStream(eula));
            boolean e = Boolean.parseBoolean(prop.getProperty("eula", "false"));
            if (!e) {
                System.out.println(language.equals("zh") ? "使用本软件，您必须遵守我们的协议，请修改 eula.txt 来同意协议！" : "To use this software, you must abide by our agreement, please modify eula.txt to agree to the agreement!");
                System.exit(0);
            }
        } catch (IOException e) {
            logger.trace(e);
            System.exit(-1);
        }

        try {
            LanguageUtil.init();

            File languageFile = new File("language.json");
            if (!languageFile.exists()) {
                if (!languageFile.createNewFile()) {
                    logger.error(language.equals("zh") ? "无法创建配置文件！" : "Unable to create configuration file!");
                } else {
                    try (FileOutputStream fos = new FileOutputStream(languageFile)) {
                        fos.write(LanguageUtil.languageFile(language).getBytes(StandardCharsets.UTF_8));
                    }
                }
            }

            LanguageUtil.load();
        } catch (IOException e) {
            logger.info(err);
            logger.trace(e);
            System.exit(-1);
        }

        ConfigUtil.init();
        logger.ansiColor = Boolean.parseBoolean(ConfigUtil.getConfig("ansi-console"));

        if (!checkConfig()) {
            logger.error(l("config.error"));
            System.exit(-1);
            return;
        }
        ConfigUtil.getConfig("checkUpdate");
        if (ConfigUtil.getConfig("checkUpdate").equals("true")) {
            logger.info(l("checking.update"));
            Thread thread = new Thread(() -> checkUpdate(null));
            thread.start();
        }

        String qq = ConfigUtil.getConfig("qq");
        String password = ConfigUtil.getConfig("password");
        groups = ConfigUtil.getConfig("group").split(",");
        allowedGroups = ConfigUtil.getConfig("allowedGroups").split(",");
        EventListener.showQQ = Boolean.parseBoolean(ConfigUtil.getConfig("showQQ", "false"));
        if (qq.isEmpty() || password.isEmpty()) {
            logger.error(l("qq.password.not.exists"));
            System.exit(-1);
            return;
        }

        String protocol = !ConfigUtil.getConfig("protocol").isEmpty() ? ConfigUtil.getConfig("protocol") : "";
        logger.info(l("trying.login"), protocol);
        try {
            BotConfiguration.MiraiProtocol miraiProtocol = switch (protocol) {
                case "PAD" -> BotConfiguration.MiraiProtocol.ANDROID_PAD;
                case "WATCH" -> BotConfiguration.MiraiProtocol.ANDROID_WATCH;
                case "IPAD" -> BotConfiguration.MiraiProtocol.IPAD;
                case "MACOS" -> BotConfiguration.MiraiProtocol.MACOS;
                default -> BotConfiguration.MiraiProtocol.ANDROID_PHONE;
            };
            bot = BotFactory.INSTANCE.newBot(Long.parseLong(qq), password, new BotConfiguration() {{
                fileBasedDeviceInfo();
                setProtocol(miraiProtocol);
                noNetworkLog();
                noBotLog();
            }});
            bot.login();

            logger.info(l("registering.event"));
            bot.getEventChannel().registerListenerHost(new EventListener());

            logger.info(l("login.success"), bot.getNick());
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("windows")) {
                new ProcessBuilder("cmd", "/c", "title " + bot.getNick() + " (" + bot.getId() + ")").inheritIO().start().waitFor();
            } else if (os.contains("linux")) {
                new ProcessBuilder("echo", "-e", "\\033]0;" + bot.getNick() + " (" + bot.getId() + ")" + "\\007").inheritIO().start().waitFor();
            }

            MiraiCommand.init();
            String[] cmds = MiraiCommand.cmds.keySet().toArray(new String[0]);
            for (String c : cmds) {
                String l = c.replaceAll("([A-Z])", ".$1").toLowerCase();
                l = l.replaceAll("-", ".");
                String de = l("command." + l);
                commands.set(c, new Command(c, de));
            }
            try {
                File pluginDir = new File("plugins");
                if (!pluginDir.exists()) {
                    if (!pluginDir.mkdirs()) {
                        logger.info(l("cannot.create.plugin.dir"));
                        System.exit(-1);
                    }
                }
                loader.initPlugins();
                for (Plugin p : loader.plugins) {
                    try {
                        logger.info(l("enabling.plugin"), p.getName());
                        p.onEnable();

                        loader.add2commands(p.getCommands());
                    } catch (Exception e) {
                        p.setEnabled(false);
                        logger.error(l("failed.load.plugin"), p.getName(), e.toString());
                        logger.trace(e);
                    }
                }
            } catch (Exception e) {
                logger.trace(e);
                logger.error(l("unknown.error"));
                System.exit(-1);
            }

            if (groups.length < 1) {
                logger.info(l("not.group.set"));
            } else if (!bot.getGroups().contains(Long.parseLong(groups[0]))) {
                logger.info(l("not.entered.group"), groups[0]);
            } else {
                for (int i = 0; i < groups.length; i++) groups[i] = groups[i].trim();
                group = bot.getGroupOrFail(Long.parseLong(groups[0]));
                logger.info(l("now.group"), group.getName(), String.valueOf(group.getId()));
            }

            for (Plugin p : loader.plugins) {
                if (!p.isEnabled()) continue;
                try {
                    p.onFinished();
                } catch (Exception e) {
                    logger.trace(e);
                }
            }
            reloadCommands();

            lineThread = runLinerReader();
        } catch (NumberFormatException e) {
            logger.trace(e);
            logger.error(l("qq.password.error"));
            System.exit(-1);
        } catch (UserInterruptException | EndOfFileException e) {
            System.exit(0);
        } catch (Exception e) {
            logger.trace(e);
            logger.error(l("unknown.error"));
            System.exit(-1);
        }
    }

    public static Thread runLinerReader() {
        Thread thread = new Thread(() -> {
            while (!Thread.interrupted()) {
                if (reader.isReading()) continue;
                String msg = reader.readLine("> ");
                if (msg.isEmpty()) continue;
                if (MiraiCommand.runCommand(msg)) {
                    continue;
                }
                if (group == null) {
                    logger.warn(l("not.group.set"));
                    continue;
                }
                try {
                    group.sendMessage(MiraiCode.deserializeMiraiCode(msg));
                } catch (BotIsBeingMutedException e) {
                    logger.error(l("bot.is.being.muted"));
                }
            }
        });
        thread.start();
        return thread;
    }

    public static void stop() {
        lineThread.interrupt();
        for (Plugin p : loader.plugins) {
            try {
                p.onDisable();
            } catch (Exception e) {
                logger.trace(e);
            }
        }
        System.out.print("\n");
        AnsiConsole.systemUninstall();
        System.exit(0);
    }

    public static void reloadCommands() {
        StringsCompleter sc = new StringsCompleter(commands.keys());
        ArrayList<String> names = new ArrayList<>();
        if (group != null) {
            Member[] mbs = group.getMembers().toArray(new Member[0]);
            for (Member m : mbs) {
                names.add(String.valueOf(m.getId()));
                names.add(m.getNick());
            }
        }
        StringsCompleter members = new StringsCompleter(names);
        completer = new ArgumentCompleter(sc, members, new Completers.FileNameCompleter());
        LineReaderBuilder lrb = LineReaderBuilder.builder().completer(completer).history(history).terminal(terminal);
        reader = lrb.build();
    }

    @Deprecated
    public static boolean runCommand(String msg) {
        return MiraiCommand.runCommand(msg);
    }

    /**
     * Check if there is a new release
     */
    public static void checkUpdate(String u) {
        try {
            HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).followRedirects(HttpClient.Redirect.NORMAL).build();

            String def = "https://ghproxy.com/https://raw.githubusercontent.com/1689295608/MiraiBot/main/LatestVersion";
            URI uri = new URI(u == null ? def : u);
            HttpRequest request = HttpRequest.newBuilder(uri).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new Exception("status code is not 200");
            }
            String v = response.body();

            URL url = ClassLoader.getSystemResource("Version");
            String version = "0.0.0";
            if (url != null) {
                try (InputStream is = url.openStream()) {
                    version = new String(is.readAllBytes());
                }
            }
            try {
                String nowV = version.replaceAll("[^0-9.]", "");
                String newV = v.replaceAll("[^0-9.]", "");
                if (!nowV.equals(newV)) {
                    logger.warn(l("found.new.update"), "https://github.com/1689295608/MiraiBot/releases/tag/" + newV);
                } else {
                    logger.info(l("already.latest.version"), newV);
                }
            } catch (Exception e) {
                logger.error(l("failed.check.update"), e.toString());
                if (ConfigUtil.getConfig("debug").equals("true")) logger.info(e.toString());
            }
        } catch (Exception e) {
            logger.error(l("failed.check.update"), e.toString());
            if (ConfigUtil.getConfig("debug").equals("true")) logger.info(e.toString());
        }
    }

    /**
     * Get MessageSource by Message ID
     *
     * @param id Message ID
     * @return MessageSource
     */
    public static MessageSource getMessageById(int id) {
        if (id > 0) {
            return messages.get(id - 1);
        }
        return null;
    }

    /**
     * Output image info
     *
     * @param bot   Bot
     * @param image Image
     */
    public static void imageInfo(Bot bot, Image image) {
        logger.info("· ------==== Image Info ====------ ·");
        logger.info("I D: " + image.getImageId());
        logger.info("URL: " + Mirai.getInstance().queryImageUrl(bot, image));
        logger.info("MiraiCode: " + image.serializeToMiraiCode());
        logger.info("· -------------------------------- ·");
    }

    /**
     * Determine whether the group is allowed
     *
     * @param id GroupID
     * @return Is Not Allowed Group
     */
    public static boolean isNotAllowedGroup(long id) {
        for (String s : allowedGroups) {
            if (Long.parseLong(s) == id) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check config file
     *
     * @return Whether the config file is exists
     */
    public static boolean checkConfig() {
        File file = new File("config.properties");
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    return false;
                }
                Console console = System.console();
                logger.info(l("before.settings"));
                logger.info(l("please.input.qq"));
                String qq = console.readLine();
                logger.info(l("please.input.password"));
                String password = new String(console.readPassword());
                logger.info(l("please.input.group.id"));
                String groups = console.readLine();
                logger.info(l("please.input.check.update.on.setup"));
                String checkUpdate = console.readLine();

                FileOutputStream fos = new FileOutputStream(file);
                InputStream is = ClassLoader.getSystemResourceAsStream("config.properties");
                if (is == null) {
                    System.out.println(l("unknown.error"));
                    System.exit(-1);
                }
                String config = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                config = config.replaceAll("%qq%", qq);
                config = config.replaceAll("%password%", password);
                config = config.replaceAll("%groups%", groups);
                config = config.replaceAll("%check\\.as\\.setup%", checkUpdate);
                fos.write(config.getBytes());
                fos.close();
                logger.warn(l("please.restart"));
                System.exit(0);
            } catch (IOException e) {
                logger.trace(e);
            }
            return false;
        }
        return true;
    }
}
