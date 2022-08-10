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
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageSource;
import net.mamoe.mirai.utils.BotConfiguration;
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

public class MiraiBot {
    public static final String language = Locale.getDefault().getLanguage();
    public static Group group;
    public static String[] groups;
    public static String[] allowedGroups;
    public static Bot bot;
    public static final PluginLoader loader = new PluginLoader();
    public static boolean running;
    public static final Logger logger = new Logger();
    public static final Commands commands = new Commands();
    public static ArgumentCompleter completer;
    public static LineReader reader;
    public static Terminal terminal;
    public static History history = new DefaultHistory();

    public static void main(String[] args) {
        TerminalBuilder tb = TerminalBuilder.builder()
                .encoding(Charset.defaultCharset())
                .jansi(true)
                .jna(false);
        LogUtil.init();
        try {
            terminal = tb.build();
        } catch (IOException e) {
            logger.trace(e);
            logger.error(language("unknown.error"));
            System.exit(-1);
        }
        String err = language.equals("zh") ? "出现错误！进程即将终止！" : (language.equals("tw") ? "出現錯誤！進程即將終止！" : "Unable to create configuration file!");
        try {
            URL url = ClassLoader.getSystemResource("Version");
            String version = "";
            if (url != null) {
                try (InputStream is = url.openStream()) {
                    version = new String(is.readAllBytes()).replaceAll("[^0-9.]", "");
                }
            }
            System.out.println(language.equals("zh") ? "MiraiBot " + version + " 基于 Mirai-Core. 版权所有 (C) WindowX 2021" :
                    (language.equals("tw") ? "MiraiBot " + version + " 基於 Mirai-Core. 版權所有 (C) WindowX 2021" :
                            "MiraiBot " + version + " based Mirai-Core. Copyright (C) WindowX 2021"));
        } catch (Exception e) {
            logger.trace(e);
        }
        try {
            File eula = new File("eula.txt");
            if (!eula.exists()) {
                if (eula.createNewFile()) {
                    try (FileOutputStream fos = new FileOutputStream(eula)) {
                        fos.write((
                                """
                                        # 使用本软件，您必须遵守我们的协议，一切基于本软件开发的插件都必须在项目明显位置准确提及来自 MiraiBot，不得扭曲或隐藏免费且开源的事实。
                                        # To use this software, you must abide by our agreement. All plug-ins developed based on this software must accurately mention MiraiBot in the obvious place of the project, and must not distort or hide the fact that it is free and open source
                                        # 详情请查看：https://github.com/1689295608/MiraiBot/blob/main/LICENSE
                                        # Details: https://github.com/1689295608/MiraiBot/blob/main/LICENSE
                                        eula=false"""
                        ).getBytes());
                    }
                }
            }
            Properties prop = new Properties();
            prop.load(new FileInputStream(eula));
            boolean e = Boolean.parseBoolean(prop.getProperty("eula", "false"));
            if (!e) {
                System.out.println(language.equals("zh") ? "使用本软件，您必须遵守我们的协议，请修改 eula.txt 来同意协议！" :
                        (language.equals("tw") ? "使用本軟件，您必須遵守我們的協議，請修改 eula.txt 來同意協議！" :
                                "To use this software, you must abide by our agreement, please modify eula.txt to agree to the agreement!"));
                System.exit(0);
            }
        } catch (IOException e) {
            logger.trace(e);
            System.exit(-1);
        }

        File languageFile = new File("language.properties");
        try {
            if (!languageFile.exists()) {
                if (!languageFile.createNewFile()) {
                    logger.error(language.equals("zh") ? "无法创建配置文件！" : (language.equals("tw") ? "無法創建配置文件！" : "Unable to create configuration file!"));
                } else {
                    FileOutputStream fos = new FileOutputStream(languageFile);
                    fos.write(LanguageUtil.languageFile(language));
                    fos.flush();
                    fos.close();
                }
            }
        } catch (IOException e) {
            logger.info(err);
            logger.trace(e);
            System.exit(-1);
        }
        ConfigUtil.init();
        logger.ansiColor = Boolean.parseBoolean(ConfigUtil.getConfig("ansi-console"));

        if (!checkConfig()) {
            logger.error(language("config.error"));
            System.exit(-1);
            return;
        }
        ConfigUtil.getConfig("checkUpdate");
        if (ConfigUtil.getConfig("checkUpdate").equals("true")) {
            logger.info(language("checking.update"));
            Thread thread = new Thread(() -> checkUpdate(null));
            thread.start();
        }

        String qq = ConfigUtil.getConfig("qq");
        String password = ConfigUtil.getConfig("password");
        groups = ConfigUtil.getConfig("group").split(",");
        allowedGroups = ConfigUtil.getConfig("allowedGroups").split(",");
        EventListener.showQQ = Boolean.parseBoolean(ConfigUtil.getConfig("showQQ", "false"));
        if (qq.isEmpty() || password.isEmpty()) {
            logger.error(language("qq.password.not.exists"));
            System.exit(-1);
            return;
        }

        String protocol = !ConfigUtil.getConfig("protocol").isEmpty() ? ConfigUtil.getConfig("protocol") : "";
        logger.info(language("trying.login"), protocol);
        try {
            BotConfiguration.MiraiProtocol miraiProtocol = switch (protocol) {
                case "PAD" -> BotConfiguration.MiraiProtocol.ANDROID_PAD;
                case "WATCH" -> BotConfiguration.MiraiProtocol.ANDROID_WATCH;
                default -> BotConfiguration.MiraiProtocol.ANDROID_PHONE;
            };
            bot = BotFactory.INSTANCE.newBot(Long.parseLong(qq), password, new BotConfiguration() {{
                fileBasedDeviceInfo();
                setProtocol(miraiProtocol);
                noNetworkLog();
                noBotLog();
            }});
            bot.login();

            logger.info(language("registering.event"));
            GlobalEventChannel.INSTANCE.registerListenerHost(new EventListener());

            logger.info(language("login.success"), bot.getNick());
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("windows")) {
                new ProcessBuilder("cmd", "/c", "title " + bot.getNick() + " (" + bot.getId() + ")").inheritIO().start().waitFor();
            } else if (os.contains("linux")) {
                new ProcessBuilder("echo", "-e", "\\033]0;" + bot.getNick() + " (" + bot.getId() + ")" + "\\007").inheritIO().start().waitFor();
            }

            String[] cmds = new String[] {
                    "reload", "stop",
                    "friendList", "memberList",
                    "plugins", "language",
                    "clear", "help",
                    "send", "kick",
                    "nudge", "mute",
                    "avatar", "voice",
                    "image", "imageInfo",
                    "upImg", "upClipImg",
                    "newImg", "del",
                    "reply", "checkUpdate",
                    "accept-request", "accept-invite",
                    "nameCard", "recall",
                    "group", "unload",
                    "load", "music",
                    "dice", "status",
                    "unmute"
            };
            for (String c : cmds) {
                String l = c.replaceAll("([A-Z])", ".$1").toLowerCase();
                l = l.replaceAll("-", ".");
                String de = language("command." + l);
                commands.set(c, new Command(c, de));
            }
            try {
                File pluginDir = new File("plugins");
                if (!pluginDir.exists()) {
                    if (!pluginDir.mkdirs()) {
                        logger.info(language("cannot.create.plugin.dir"));
                        System.exit(-1);
                    }
                }
                loader.initPlugins();
                for (Plugin p : loader.plugins) {
                    try {
                        logger.info(language("enabling.plugin"), p.getName());
                        p.onEnable();

                        loader.add2commands(p.getCommands());
                    } catch (Exception e) {
                        p.setEnabled(false);
                        logger.error(language("failed.load.plugin"), p.getName(), e.toString());
                        logger.trace(e);
                    }
                }
            } catch (Exception e) {
                logger.trace(e);
                logger.error(language("unknown.error"));
                System.exit(-1);
            }

            if (groups.length < 1) {
                logger.info(language("not.group.set"));
            } else if (!bot.getGroups().contains(Long.parseLong(groups[0]))) {
                logger.info(language("not.entered.group"), groups[0]);
            } else {
                for (int i = 0; i < groups.length; i++) groups[i] = groups[i].trim();
                group = bot.getGroupOrFail(Long.parseLong(groups[0]));
                logger.info(language("now.group"), group.getName(), String.valueOf(group.getId()));
            }
            if (group == null) {
                logger.error(language("unknown.error"));
                System.exit(-1);
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
            running = true;
            while (running) {
                if (reader.isReading()) continue;
                String msg = reader.readLine("> ");
                if (msg.isEmpty()) continue;
                try {
                    if (!MiraiCommand.runCommand(msg)) {
                        String decode;
                        try {
                            decode = decodeUnicode(msg);
                        } catch (Exception e) {
                            decode = msg;
                        }
                        group.sendMessage(MiraiCode.deserializeMiraiCode(msg.contains("\\u") ? decode : msg));
                    }
                } catch (BotIsBeingMutedException e) {
                    logger.error(language("bot.is.being.muted"));
                }
            }
        } catch (NumberFormatException e) {
            logger.trace(e);
            logger.error(language("qq.password.error"));
            System.exit(-1);
        } catch (UserInterruptException | EndOfFileException e) {
            System.exit(0);
        } catch (Exception e) {
            logger.trace(e);
            logger.error(language("unknown.error"));
            System.exit(-1);
        }
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
        completer = new ArgumentCompleter(
                sc,
                members,
                new Completers.FileNameCompleter()
        );
        LineReaderBuilder lrb = LineReaderBuilder.builder()
                .completer(completer)
                .history(history)
                .terminal(terminal);
        reader = lrb.build();
    }

    public static String decodeUnicode(final String dataStr) {
        int start = 0;
        int end;
        final StringBuilder buffer = new StringBuilder();
        while (start > -1) {
            end = dataStr.indexOf("\\u", start + 2);
            String charStr;
            if (end == -1) {
                charStr = dataStr.substring(start + 2);
            } else {
                charStr = dataStr.substring(start + 2, end);
            }
            char letter = (char) Integer.parseInt(charStr, 16);
            buffer.append(letter);
            start = end;
        }
        return buffer.toString();
    }

    @Deprecated
    public static boolean runCommand(String msg) throws Exception {
        return MiraiCommand.runCommand(msg);
    }

    /**
     * Check if there is a new release
     */
    public static void checkUpdate(String u) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();

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
                    logger.warn(language("found.new.update"), "https://github.com/1689295608/MiraiBot/releases/tag/" + newV);
                } else {
                    logger.info(language("already.latest.version"), newV);
                }
            } catch (Exception e) {
                logger.error(language("failed.check.update"), e.toString());
                if (ConfigUtil.getConfig("debug").equals("true")) logger.info(e.toString());
            }
        } catch (Exception e) {
            logger.error(language("failed.check.update"), e.toString());
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
     * Fast to language()
     *
     * @param key key
     * @return language value
     */
    public static String language(String key) {
        return ConfigUtil.getLanguage(key);
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
                logger.info(language("before.settings"));
                logger.info(language("please.input.qq"));
                String qq = console.readLine();
                logger.info(language("please.input.password"));
                String password = new String(console.readPassword());
                logger.info(language("please.input.group.id"));
                String groups = console.readLine();
                logger.info(language("please.input.check.update.on.setup"));
                String checkUpdate = console.readLine();

                FileOutputStream fos = new FileOutputStream(file);
                InputStream is = ClassLoader.getSystemResourceAsStream("config.properties");
                if (is == null) {
                    System.out.println(language("unknown.error"));
                    System.exit(-1);
                }
                String config = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                config = config.replaceAll("%qq%", qq);
                config = config.replaceAll("%password%", password);
                config = config.replaceAll("%groups%", groups);
                config = config.replaceAll("%check\\.as\\.setup%", checkUpdate);
                fos.write(config.getBytes());
                fos.close();
                logger.warn(language("please.restart"));
                System.exit(0);
            } catch (IOException e) {
                logger.trace(e);
            }
            return false;
        }
        return true;
    }
}
