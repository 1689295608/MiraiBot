package com.windowx.miraibot;

import com.windowx.miraibot.command.Command;
import com.windowx.miraibot.command.CommandExecutor;
import com.windowx.miraibot.command.Commands;
import com.windowx.miraibot.plugin.Plugin;
import com.windowx.miraibot.utils.ClipboardUtil;
import com.windowx.miraibot.utils.ConfigUtil;
import com.windowx.miraibot.utils.LanguageUtil;
import com.windowx.miraibot.utils.WordToImage;
import net.mamoe.mirai.Mirai;
import net.mamoe.mirai.contact.*;
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent;
import net.mamoe.mirai.event.events.MemberJoinRequestEvent;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.*;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.utils.ExternalResource;
import org.fusesource.jansi.AnsiConsole;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.io.*;
import java.lang.management.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

import static com.windowx.miraibot.EventListener.*;
import static com.windowx.miraibot.EventListener.messages;
import static com.windowx.miraibot.MiraiBot.*;

public class MiraiCommand {

    /**
     * 运行命令执行
     *
     * @param msg Message
     * @return Is it an instruction
     * @throws Exception Exception
     */
    public static boolean runCommand(String msg) throws Exception {
        String[] cmd = msg.split(" ");
        if (cmd.length < 1) {
            return false;
        }
        String label = cmd[0];
        String[] args = Arrays.copyOfRange(cmd, 1, cmd.length);
        boolean rt = true;
        switch (label) {
            case "reload" -> {
                if (cmd.length == 1) {
                    ConfigUtil.init();
                    logger.info(language("reloaded"));
                    break;
                }
                try {
                    Plugin plugin = loader.getPlugin(cmd[1]);
                    if (plugin != null) {
                        loader.unloadPlugin(cmd[1]);
                        loader.loadPlugin(plugin.getFile(), plugin.getName());
                    } else {
                        logger.info(language("unloading.plugin"), cmd[1]);
                    }
                } catch (Exception e) {
                    logger.trace(e);
                }
            }
            case "stop" -> {
                logger.warn(language("stopping.bot"), bot.getNick(), String.valueOf(bot.getId()));
                running = false;
                for (Plugin p : loader.plugins) {
                    try {
                        p.onDisable();
                    } catch (Exception e) {
                        logger.trace(e);
                    }
                }
                bot.close();
                System.out.print("\n");
                AnsiConsole.systemUninstall();
                System.exit(0);
            }
            case "friendList" -> {
                {
                    ContactList<Friend> friends = bot.getFriends();
                    StringBuilder out = new StringBuilder();
                    int c = 1;
                    for (Friend f : friends) {
                        out.append(c).append(". ").append(f.getNick()).append(" (").append(f.getId()).append(")")
                                .append((f.getId() == bot.getId() ? " (" + language("bot") + ")\n" : "\n"));
                        c++;
                    }
                    logger.info(out.toString());
                }
            }
            case "memberList" -> {
                {
                    ContactList<NormalMember> members = group.getMembers();
                    StringBuilder out = new StringBuilder();
                    int c = 1;
                    for (NormalMember f : members) {
                        out.append(c).append(". ").append((f.getNameCard().isEmpty() ? f.getNick() : f.getNameCard()))
                                .append(" (").append(f.getId()).append(")").append("\n");
                        c++;
                    }
                    out.append(c).append(". ").append(bot.getNick()).append(" (").append(bot.getId()).append(")")
                            .append(" (").append(language("bot")).append(")\n");
                    logger.info(out.toString());
                }
            }
            case "plugins" -> {
                StringBuilder out = new StringBuilder();
                int c = 1;
                for (Plugin p : loader.plugins) {
                    if (!p.isEnabled()) continue;
                    out.append(c).append(". ").append(p.getName()).append(" v").append(p.getVersion()).append(" by ").append(p.getOwner()).append("\n");
                    c++;
                }
                logger.info(out.toString());
            }
            case "language" -> {
                if (cmd.length < 2) {
                    logger.warn(language("usage") + ": language <" + language("language") + ">");
                    break;
                }
                File now = new File("language.properties");
                if (now.exists()) {
                    File bak = new File("language.properties.bak");
                    if (bak.exists()) {
                        if (!bak.delete()) {
                            bak.deleteOnExit();
                        }
                    }
                    if (bak.createNewFile()) {
                        FileOutputStream fos = new FileOutputStream(bak);
                        try (FileInputStream fis = new FileInputStream(now)) {
                            fos.write(fis.readAllBytes());
                            fos.flush();
                            fos.close();
                        }
                    }
                }
                FileOutputStream fos = new FileOutputStream(now);
                fos.write(LanguageUtil.languageFile(cmd[1]));
                fos.close();
                ConfigUtil.init();
                logger.info(language("success.change.language"));
            }
            case "clear" -> {
                logger.clear();
                messages = new ArrayList<>();
                logger.info(language("console.cleared"));
            }
            case "help" -> {
                int page = 1;
                if (cmd.length > 1) {
                    try {
                        page = Integer.parseInt(cmd[1]);
                    } catch (NumberFormatException e) {
                        logger.warn(language("page.not.exists"));
                        break;
                    }
                }
                String[] key = commands.keys();
                int pages = (int) Math.floor(key.length / 10F) + 1;
                if (page > pages) {
                    logger.warn(language("page.not.exists"));
                    break;
                }
                StringBuilder help = new StringBuilder();
                int index = (page - 1) * 10;
                help.append(String.format(language("help.header"), page, pages));
                for (int i = index ; i < index + 10 ; i ++) {
                    if(i >= key.length) break;
                    Command c = commands.get(key[i]);
                    String name = c.getName();
                    help.append(name)
                            .append(" ".repeat(16 - name.length()));
                    help.append(c.getDescription())
                            .append("\n");
                }
                help.append(language("help.footer"));
                logger.info(help.toString());
            }
            case "send" -> {
                if (cmd.length <= 2) {
                    logger.warn(language("usage") + ": send <" +
                            language("qq") +
                            "> <" + language("contents") + ">");
                    break;
                }
                StringBuilder tmp = new StringBuilder();
                for (int i = 2; i < cmd.length; i++) {
                    tmp.append(cmd[i]).append(i == cmd.length - 1 ? "" : " ");
                }
                try {
                    Friend friend = bot.getFriend(Long.parseLong(cmd[1]));
                    if (friend != null) {
                        friend.sendMessage(MiraiCode.deserializeMiraiCode(tmp.toString()));
                    } else {
                        logger.info(language("not.friend"));
                    }
                } catch (NumberFormatException e) {
                    logger.info(language("not.qq"), cmd[1]);
                }
            }
            case "kick" -> {
                if (cmd.length <= 2) {
                    logger.warn(language("usage") + ": kick <" + language("qq") + "> <" +
                            language("reason") + ">");
                    break;
                }
                try {
                    NormalMember member = group.get(Long.parseLong(cmd[1]));
                    if (member != null) {
                        try {
                            member.kick(cmd[2]);
                            logger.info(language("kicked"));
                        } catch (Exception e) {
                            logger.error(language("no.permission"));
                        }
                    } else {
                        logger.error(language("not.user"));
                    }
                } catch (NumberFormatException e) {
                    logger.error(language("not.qq"), cmd[1]);
                }
            }
            case "nudge" -> {
                if (cmd.length < 2) {
                    logger.warn(language("usage") + ": nudge <" + language("qq") + ">");
                    break;
                }
                try {
                    NormalMember member = group.get(Long.parseLong(cmd[1]));
                    if (member != null) {
                        try {
                            member.nudge().sendTo(group);
                            logger.info(language("nudged"));
                        } catch (Exception e) {
                            logger.error(language("no.permission"));
                        }
                    } else {
                        logger.error(language("not.user"));
                    }
                } catch (NumberFormatException e) {
                    logger.error(language("not.qq"), cmd[1]);
                }
            }
            case "mute" -> {
                if (cmd.length <= 2) {
                    logger.warn(language("usage") + ": mute <" + language("qq") + "> <" +
                            language("time") + ">");
                    break;
                }
                try {
                    long qq = Long.parseLong(cmd[1]);
                    NormalMember member = group.get(qq);
                    if (member != null) {
                        try {
                            member.mute(Integer.parseInt(cmd[2]));
                        } catch (NumberFormatException e) {
                            logger.warn(language("time.too.long"), cmd[2]);
                        }
                    } else {
                        logger.error(language("not.user"));
                    }
                } catch (NumberFormatException e) {
                    logger.error(language("not.qq"), cmd[1]);
                    if (ConfigUtil.getConfig("debug").equals("true")) logger.error(e.toString());
                } catch (PermissionDeniedException e) {
                    logger.error(language("no.permission"));
                    if (ConfigUtil.getConfig("debug").equals("true")) logger.error(e.toString());
                }
            }
            case "unmute" -> {
                if (cmd.length < 2) {logger.warn(language("usage") + ": mute <" + language("qq") + ">");
                    break;
                }
                try {
                    long qq = Long.parseLong(cmd[1]);
                    NormalMember member = group.get(qq);
                    if (member != null) {
                        try {
                            member.unmute();
                        } catch (NumberFormatException e) {
                            logger.warn(language("time.too.long"), cmd[2]);
                        }
                    } else {
                        logger.error(language("not.user"));
                    }
                } catch (NumberFormatException e) {
                    logger.error(language("not.qq"), cmd[1]);
                    if (ConfigUtil.getConfig("debug").equals("true")) logger.error(e.toString());
                } catch (PermissionDeniedException e) {
                    logger.error(language("no.permission"));
                    if (ConfigUtil.getConfig("debug").equals("true")) logger.error(e.toString());
                }
            }
            case "avatar" -> {
                if (cmd.length < 2) {
                    logger.warn(language("usage") + ": avatar <" + language("qq") + ">");
                    break;
                }
                try {
                    Stranger stranger = bot.getStranger(Long.parseLong(cmd[1]));
                    for (Member m : group.getMembers()) {
                        if (String.valueOf(m.getId()).equals(cmd[1])) {
                            stranger = m.getBot().getAsStranger();
                        }
                    }
                    if (stranger != null || cmd[1].equals(String.valueOf(bot.getId()))) {
                        URL url = new URL((stranger != null ? stranger : bot).getAvatarUrl());
                        InputStream is = url.openStream();
                        BufferedInputStream bis = new BufferedInputStream(is);
                        byte[] avatar = bis.readAllBytes();
                        ExternalResource externalResource = ExternalResource.create(avatar);
                        logger.info(language("up.loading.img"));
                        Image img = group.uploadImage(externalResource);
                        externalResource.close();
                        imageInfo(bot, img);
                    } else {
                        logger.error(language("not.user"));
                    }
                } catch (NumberFormatException e) {
                    logger.error(language("not.qq"), cmd[1]);
                    if (ConfigUtil.getConfig("debug").equals("true")) logger.error(e.toString());
                }
            }
            case "voice" -> {
                if (cmd.length < 2) {
                    logger.warn(language("usage") + ": voice <" +
                            language("file.path") + ">");
                    break;
                }
                Thread upVoice = new Thread(() -> {
                    try {
                        File file = new File(msg.substring(6));
                        ExternalResource externalResource = ExternalResource.create(file);
                        logger.info(language("up.loading.voice"));
                        Audio audio = group.uploadAudio(externalResource);
                        group.sendMessage(audio);
                        externalResource.close();
                    } catch (IOException e) {
                        logger.error(language("file.error"));
                        if (ConfigUtil.getConfig("debug").equals("true")) logger.error(e.toString());
                    }
                });
                upVoice.start();
            }
            case "image" -> {
                if (cmd.length < 2) {
                    logger.warn(language("usage") + ": image <" +
                            language("file.path") + ">");
                    break;
                }
                Thread upImg = new Thread(() -> {
                    try {
                        File file = new File(msg.substring(6));
                        ExternalResource externalResource = ExternalResource.create(file);
                        logger.info(language("up.loading.img"));
                        Image img = group.uploadImage(externalResource);
                        group.sendMessage(img);
                        externalResource.close();
                        imageInfo(bot, img);
                    } catch (IOException e) {
                        logger.error(language("file.error"));
                        if (ConfigUtil.getConfig("debug").equals("true")) logger.error(e.toString());
                    }
                });
                upImg.start();
            }
            case "imageInfo" -> {
                if (cmd.length < 2) {
                    logger.warn(language("usage") + ": imageInfo <" +
                            language("image.id") + ">");
                    break;
                }
                try {
                    Image image = Image.fromId(cmd[1]);
                    imageInfo(bot, image);
                } catch (IllegalArgumentException e) {
                    logger.error(e.toString());
                }
            }
            case "upImg" -> {
                if (cmd.length < 2) {
                    logger.warn(language("usage") + ": upImg <" +
                            language("file.path") + ">");
                    break;
                }
                File file = new File(msg.substring(6));
                ExternalResource externalResource = ExternalResource.create(file);
                Image img = group.uploadImage(externalResource);
                externalResource.close();
                imageInfo(bot, img);
            }
            case "upClipImg" -> {
                byte[] clip = ClipboardUtil.getImageFromClipboard();
                if (clip != null) {
                    ExternalResource externalResource = ExternalResource.create(clip);
                    logger.info(language("up.loading.img"));
                    Image img = group.uploadImage(externalResource);
                    externalResource.close();
                    imageInfo(bot, img);
                } else {
                    logger.error(language("failed.clipboard"));
                }
            }
            case "newImg" -> {
                if (cmd.length <= 4) {
                    logger.info(language("usage") +
                            ": newImg <" + language("width") +
                            "> <" + language("height") +
                            "> <" + language("font.size") +
                            "> <" + language("contents") +
                            ">");
                    break;
                }
                try {
                    StringBuilder content = new StringBuilder();
                    for (int i = 4; i < cmd.length; i++) {
                        content.append(cmd[i]).append(i == cmd.length - 1 ? "" : " ");
                    }
                    logger.info(language("creating.word.image"));
                    byte[] bytes = WordToImage.createImage(content.toString(),
                            new Font(ConfigUtil.getConfig("font"), Font.PLAIN, Integer.parseInt(cmd[3])),
                            Integer.parseInt(cmd[1]), Integer.parseInt(cmd[2]));
                    ExternalResource externalResource = ExternalResource.create(bytes);
                    logger.info(language("up.loading.img"));
                    Image img = group.uploadImage(externalResource);
                    externalResource.close();
                    imageInfo(bot, img);
                    group.sendMessage(img);
                } catch (NumberFormatException e) {
                    logger.warn(language("width.height.error"));
                    if (ConfigUtil.getConfig("debug").equals("true")) logger.warn(e.toString());
                }
            }
            case "del" -> {
                if (cmd.length < 2) {
                    logger.warn(language("usage") + ": del <QQ>");
                    break;
                }
                try {
                    Friend friend = bot.getFriend(Long.parseLong(cmd[1]));
                    if (friend != null) {
                        friend.delete();
                        logger.info(language("deleted.friend"), friend.getNick(), String.valueOf(friend.getId()));
                    } else {
                        logger.error(language("not.friend"));
                    }
                } catch (NumberFormatException e) {
                    logger.error(language("not.qq"), cmd[1]);
                    if (ConfigUtil.getConfig("debug").equals("true")) logger.error(e.toString());
                }
            }
            case "reply" -> {
                if (cmd.length <= 2) {
                    logger.warn(language("usage") + ": reply <" +
                            language("message.id") + "> <" +
                            language("contents") + ">");
                    break;
                }
                try {
                    MessageSource message = getMessageById(Integer.parseInt(cmd[1]));
                    StringBuilder content = new StringBuilder();
                    for (int i = 2; i < cmd.length; i++) {
                        content.append(cmd[i]);
                    }
                    if (message != null) {
                        group.sendMessage(new QuoteReply(message).plus(MiraiCode.deserializeMiraiCode(content.toString())));
                    } else {
                        logger.error(language("message.not.found"));
                    }
                } catch (NumberFormatException e) {
                    logger.error(language("message.id.error"));
                    if (ConfigUtil.getConfig("debug").equals("true")) logger.error(e.toString());
                }
            }
            case "checkUpdate" -> {
                logger.info(language("checking.update"));
                checkUpdate(null);
            }
            case "accept-request" -> {
                if (cmd.length < 2) {
                    logger.warn(language("usage") + ": accept-request <" + language("request.id") + ">");
                    break;
                }
                try {
                    int index = Integer.parseInt(cmd[1]) - 1;
                    if (index < 0 || joinRequest.size() <= index) {
                        logger.error(language("request.not.found"));
                        break;
                    }
                    MemberJoinRequestEvent request = joinRequest.get(Integer.parseInt(cmd[1]) - 1);
                    if (request == null) {
                        logger.error(language("request.not.found"));
                        break;
                    }
                    try {
                        request.accept();
                        logger.info(language("request.accepted"));
                        joinRequest.remove(request);
                    } catch (Exception e) {
                        logger.error(language("failed.accept.request"));
                    }
                } catch (NumberFormatException e) {
                    logger.error(language("request.id.error"));
                    if (ConfigUtil.getConfig("debug").equals("true")) logger.error(e.toString());
                }
            }
            case "accept-invite" -> {
                if (cmd.length < 2) {
                    logger.warn(language("usage") + ": accept-invite <" + language("invite.id") + ">");
                    break;
                }
                try {
                    int index = Integer.parseInt(cmd[1]) - 1;
                    if (index < 0 || inviteRequest.size() <= index) {
                        logger.error(language("invite.not.found"));
                        break;
                    }
                    BotInvitedJoinGroupRequestEvent request = inviteRequest.get(Integer.parseInt(cmd[1]) - 1);
                    if (request == null) {
                        logger.error(language("invite.not.found"));
                        break;
                    }
                    try {
                        request.accept();
                        logger.info(language("invite.accepted"));
                    } catch (Exception e) {
                        logger.error(language("failed.accept.invite"));
                    }
                } catch (NumberFormatException e) {
                    logger.error(language("invite.id.error"));
                    if (ConfigUtil.getConfig("debug").equals("true")) logger.error(e.toString());
                }
            }
            case "nameCard" -> {
                if (cmd.length <= 2) {
                    logger.warn(language("usage") + ": nameCard <" +
                            language("qq") + "> <" + language("name.card") + ">");
                    break;
                }
                try {
                    NormalMember member = group.get(Long.parseLong(cmd[1]));
                    if (member == null) {
                        logger.error(language("not.user"));
                        break;
                    }
                    StringBuilder nameCard = new StringBuilder();
                    for (int i = 2; i < cmd.length; i++) {
                        nameCard.append(cmd[i]);
                        if (i != cmd.length - 1) {
                            nameCard.append(" ");
                        }
                    }
                    member.setNameCard(nameCard.toString());
                    logger.info(language("name.card.set"), member.getNick(), nameCard.toString());
                } catch (NumberFormatException e) {
                    logger.error(language("not.qq"), cmd[1]);
                    if (ConfigUtil.getConfig("debug").equals("true")) logger.error(e.toString());
                } catch (PermissionDeniedException e) {
                    logger.error(language("no.permission"));
                    if (ConfigUtil.getConfig("debug").equals("true")) logger.error(e.toString());
                }
            }
            case "recall" -> {
                if (cmd.length < 2) {
                    logger.warn(language("usage") + ": recall <" +
                            language("message.id") + ">");
                    break;
                }
                try {
                    int index = Integer.parseInt(cmd[1]) - 1;
                    if (index < 0 || messages.size() <= index) {
                        logger.error(language("message.not.found"));
                        break;
                    }
                    MessageSource message = messages.get(Integer.parseInt(cmd[1]) - 1);
                    if (message == null) {
                        logger.error(language("message.not.found"));
                        break;
                    }
                    if (message.getFromId() == message.getBotId()) {
                        try {
                            Mirai.getInstance().recallMessage(bot, message);
                            logger.info(language("recalled"));
                        } catch (Exception e) {
                            logger.error(language("failed.recall"));
                        }
                    } else {
                        try {
                            Mirai.getInstance().recallMessage(bot, message);
                            logger.info(language("recalled"));
                        } catch (PermissionDeniedException e) {
                            logger.error(language("no.permission"));
                            if (ConfigUtil.getConfig("debug").equals("true")) logger.error(e.toString());
                        } catch (Exception e) {
                            logger.error(language("failed.recall"));
                        }
                    }
                } catch (NumberFormatException e) {
                    logger.error(language("message.id.error"));
                    if (ConfigUtil.getConfig("debug").equals("true")) logger.error(e.toString());
                }
            }
            case "group" -> {
                if (cmd.length < 2) {
                    logger.warn(language("usage") + ": group <" +
                            language("group") + language("id") + ">");
                    break;
                }
                int id = 0;
                try {
                    id = Integer.parseInt(cmd[1]) - 1;
                } catch (NumberFormatException e) {
                    logger.error(language("group.id.not.found"), String.valueOf(id));
                }
                String g = "";
                try {
                    g = groups[id];
                } catch (Exception e) {
                    logger.error(language("group.id.not.found"), cmd[1]);
                }
                if (!g.isEmpty()) {
                    if (bot.getGroups().contains(Long.parseLong(g))) {
                        group = bot.getGroup(Long.parseLong(groups[id]));
                        reloadCommands();
                        logger.info(language("now.group"), group.getName(), String.valueOf(group.getId()));
                    } else {
                        logger.error(language("not.entered.group"), g);
                    }
                }
            }
            case "unload" -> {
                if (cmd.length < 2) {
                    logger.warn(language("usage") + ": unload <" +
                            language("plugin.name") + ">");
                    break;
                }
                loader.unloadPlugin(cmd[1]);
            }
            case "load" -> {
                if (cmd.length < 2) {
                    logger.warn(language("usage") + ": load <" +
                            language("file.name") + ">");
                    break;
                }
                File f = new File("plugins/" + (cmd[1].endsWith(".jar") || cmd[1].endsWith(".class") ? cmd[1] : cmd[1] + ".jar"));
                loader.loadPlugin(f, cmd[1]);
            }
            case "music" -> {
                if (cmd.length < 2) {
                    logger.warn(language("usage") + ": music <" + language("music.id") + "> [" + language("contact") + "]");
                    break;
                }
                try {
                    long l = Long.parseLong(cmd[1]);
                    URL url = new URL("http://music.163.com/api/song/detail/?id=" + l + "&ids=[" + l + "]");
                    JSONObject json;
                    try (InputStream is = url.openStream()) {
                        json = new JSONObject(new String(is.readAllBytes(), StandardCharsets.UTF_8));
                    }
                    JSONArray songs = json.getJSONArray("songs");
                    StringBuilder artists = new StringBuilder();
                    JSONArray artistsA = songs.getJSONObject(0).getJSONArray("artists");
                    for (int i = 0; i < artistsA.length(); i++) {
                        artists.append(artistsA.getJSONObject(i).getString("name")).append(i != artistsA.length() - 1 ? " / " : "");
                    }
                    if (json.getInt("code") == 200) {
                        MusicShare share = new MusicShare(MusicKind.NeteaseCloudMusic,
                                songs.getJSONObject(0).getString("name"),
                                artists.toString(),
                                "http://music.163.com/song/" + l,
                                songs.getJSONObject(0).getJSONObject("album").getString("picUrl"),
                                "http://music.163.com/song/media/outer/url?id=" + l,
                                "[音乐] " + songs.getJSONObject(0).getString("name") + " - " + artists
                        );
                        if (cmd.length < 3) {
                            group.sendMessage(share);
                        } else {
                            try {
                                long tid = Long.parseLong(cmd[2]);
                                Group group = bot.getGroup(tid);
                                if (group != null) {
                                    group.sendMessage(share);
                                } else {
                                    Friend friend = bot.getFriend(tid);
                                    if (friend != null) {
                                        friend.sendMessage(share);
                                    }
                                }
                            } catch (NumberFormatException e) {
                                logger.error(language("contact.id.error"));
                            } catch (Exception e) {
                                logger.error(e.toString());
                            }
                        }
                    } else {
                        logger.error(language("music.code.error"));
                    }
                } catch (NumberFormatException e) {
                    logger.error(language("music.id.error"));
                }
            }
            case "dice" -> {
                if (cmd.length < 2) {
                    logger.warn(language("usage") + ": dice <" + language("dice.value") + ">");
                    break;
                }
                int value;
                try {
                    value = Integer.parseInt(cmd[1]);
                } catch (NumberFormatException exception) {
                    logger.error(language("dice.not.number"));
                    break;
                }
                if (value > 0 && value <= 6) {
                    Dice dice = new Dice(value);
                    group.sendMessage(dice);
                } else {
                    logger.error(language("dice.value.error"));
                }
            }
            case "status" -> {
                long MB = 1024 * 1024;

                ClassLoadingMXBean classLoad = ManagementFactory.getClassLoadingMXBean();

                MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
                MemoryUsage headMemory = memory.getHeapMemoryUsage();

                ThreadMXBean thread = ManagementFactory.getThreadMXBean();

                StringBuilder format = new StringBuilder();
                int l = 1;
                while(!language("status.format.l" + l).isEmpty()) {
                    format.append(language("status.format.l" + l)).append("\n");
                    l ++;
                }
                logger.info(format.toString()
                        , headMemory.getUsed() / MB
                        , thread.getThreadCount()
                        , loader.getPlugins().size()
                        , classLoad.getLoadedClassCount()
                        , classLoad.getUnloadedClassCount()
                );
            }
            default -> {
                boolean isCmd = false;
                for (Plugin p : loader.plugins) {
                    if (!p.isEnabled()) continue;
                    try {
                        boolean s = p.onCommand(msg);
                        if (!s) isCmd = true;
                    } catch (Exception e) {
                        logger.error(language("plugin.command.error"),
                                p.getName(),
                                label,
                                e.toString()
                        );
                        logger.trace(e);
                    }
                    Commands cmds = p.getCommands();
                    for(String name : cmds.keys()) {
                        Command c = cmds.get(name);
                        CommandExecutor executor = c.getExecutor();
                        if (executor == null) continue;
                        if (!label.equals(c.getName())) {
                            continue;
                        }
                        isCmd = true;
                        try {
                            executor.onCommand(label, args);
                        } catch (Exception e) {
                            logger.error(language("plugin.command.error"),
                                    p.getName(),
                                    c.getName(),
                                    e.toString()
                            );
                            logger.trace(e);
                        }
                    }
                }
                rt = isCmd;
            }
        }
        return rt;
    }
}
