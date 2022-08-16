package com.windowx.miraibot;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.windowx.miraibot.command.Command;
import com.windowx.miraibot.command.CommandExecutor;
import com.windowx.miraibot.command.CommandRunner;
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
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.*;
import net.mamoe.mirai.utils.ExternalResource;

import java.awt.*;
import java.io.*;
import java.lang.management.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.windowx.miraibot.EventListener.*;
import static com.windowx.miraibot.MiraiBot.*;
import static com.windowx.miraibot.utils.LanguageUtil.l;

public class MiraiCommand {
    public static final HashMap<String, CommandRunner> cmds = new HashMap<>();

    public static void init() {
        cmds.put("reload", new CommandRunner() {
            @Override
            public void start() {
                ConfigUtil.init();
                try {
                    LanguageUtil.init();
                    LanguageUtil.load();
                } catch (IOException e) {
                    logger.trace(e);
                    return;
                }
                logger.info(l("reloaded"));
            }
        });
        cmds.put("stop", new CommandRunner() {
            public void start() {
                logger.warn(l("stopping.bot"), bot.getNick(), String.valueOf(bot.getId()));
                bot.close();
                stop();
            }
        });
        cmds.put("friendList", new CommandRunner() {
            @Override
            public void start() {
                ContactList<Friend> friends = bot.getFriends();
                StringBuilder out = new StringBuilder();
                int c = 1;
                for (Friend f : friends) {
                    out.append(c)
                            .append(". ")
                            .append(f.getNick())
                            .append(" (")
                            .append(f.getId())
                            .append(")")
                            .append((f.getId() == bot.getId() ? " (" + l("bot") + ")\n" : "\n"));
                    c++;
                }
                logger.info(out.toString());
            }
        });
        cmds.put("memberList", new CommandRunner() {
            @Override
            public void start() {
                ContactList<NormalMember> members = group.getMembers();
                StringBuilder out = new StringBuilder();
                int c = 1;
                for (NormalMember f : members) {
                    out.append(c).append(". ").append((f.getNameCard().isEmpty() ? f.getNick() : f.getNameCard()))
                            .append(" (").append(f.getId()).append(")").append("\n");
                    c++;
                }
                out.append(c).append(". ").append(bot.getNick()).append(" (").append(bot.getId()).append(")")
                        .append(" (").append(l("bot")).append(")\n");
                logger.info(out.toString());
            }
        });
        cmds.put("plugins", new CommandRunner() {
            @Override
            public void start() {
                StringBuilder out = new StringBuilder();
                int c = 1;
                for (Plugin p : loader.plugins) {
                    if (!p.isEnabled()) continue;
                    out.append(c).append(". ").append(p.getName()).append(" v").append(p.getVersion()).append(" by ").append(p.getOwner()).append("\n");
                    c++;
                }
                logger.info(out.toString());
            }
        });
        cmds.put("language", new CommandRunner() {
            @Override
            public void start() throws Exception {
                if (this.args().length < 1) {
                    logger.warn("%s: language <%s>", l("usage"), l("language"));
                    return;
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
                try (FileOutputStream fos = new FileOutputStream(now)) {
                    fos.write(LanguageUtil.languageFile(this.args(0)).getBytes());
                }
                LanguageUtil.init();
                logger.info(l("success.change.language"));
            }
        });
        cmds.put("clear", new CommandRunner() {
            @Override
            public void start() {
                logger.clear();
                messages = new ArrayList<>();
                logger.info(l("console.cleared"));
            }
        });
        cmds.put("help", new CommandRunner() {
            @Override
            public void start() {
                int page = 1;
                if (args().length > 0) {
                    try {
                        page = Integer.parseInt(args(0));
                    } catch (NumberFormatException e) {
                        logger.warn(l("page.not.exists"));
                        return;
                    }
                }
                String[] key = commands.keys();
                int pages = (int) Math.floor(key.length / 10F) + 1;
                if (page > pages) {
                    logger.warn(l("page.not.exists"));
                    return;
                }
                StringBuilder help = new StringBuilder();
                int index = (page - 1) * 10;
                help.append(String.format(l("help.header"), page, pages));
                for (int i = index; i < index + 10; i++) {
                    if (i >= key.length) break;
                    Command c = commands.get(key[i]);
                    String name = c.getName();
                    help.append(name)
                            .append(" ".repeat(16 - name.length()));
                    help.append(c.getDescription())
                            .append("\n");
                }
                help.append(l("help.footer"));
                logger.info(help.toString());
            }
        });
        cmds.put("send", new CommandRunner() {
            @Override
            public void start() {
                if (args().length < 2) {
                    logger.warn("%s: send <%s> <%s...>", l("usage"), l("qq"), l("contents"));
                    return;
                }
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < args().length; i++) {
                    sb.append(args(i)).append(i == args().length - 1 ? "" : " ");
                }
                String msgs = sb.toString();
                try {
                    Friend friend = bot.getFriend(Long.parseLong(args(0)));
                    if (friend != null) {
                        friend.sendMessage(MiraiCode.deserializeMiraiCode(msgs));
                    } else {
                        logger.info(l("not.friend"));
                    }
                } catch (NumberFormatException e) {
                    logger.info(l("not.qq"), args(0));
                }
            }
        });
        cmds.put("kick", new CommandRunner() {
            @Override
            public void start() {
                if (args().length < 2) {
                    logger.warn("%s: kick <%s> <%s>", l("usage"), l("qq"), l("reason"));
                    return;
                }
                NormalMember member = null;
                try {
                    member = group.get(Long.parseLong(args(0)));
                } catch (NumberFormatException e) {
                    logger.error(l("not.qq"), args(0));
                }
                if (member != null) {
                    try {
                        member.kick(args(1));
                        logger.info(l("kicked"));
                    } catch (Exception e) {
                        logger.error(l("no.permission"));
                    }
                } else {
                    logger.error(l("not.user"));
                }
            }
        });
        cmds.put("nudge", new CommandRunner() {
            @Override
            public void start() {
                if (args().length < 1) {
                    logger.warn("%s: nudge <%s>", l("usage"), l("qq"));
                    return;
                }
                NormalMember member = null;
                try {
                    member = group.get(Long.parseLong(args(0)));
                } catch (NumberFormatException e) {
                    logger.error(l("not.qq"), args(0));
                }
                if (member != null) {
                    try {
                        member.nudge().sendTo(group);
                        logger.info(l("nudged"));
                    } catch (Exception e) {
                        logger.error(l("no.permission"));
                    }
                } else {
                    logger.error(l("not.user"));
                }
            }
        });
        cmds.put("mute", new CommandRunner() {
            @Override
            public void start() {
                if (args().length < 2) {
                    logger.warn("%s: mute <%s> <%s>", l("usage"), l("qq"), l("time"));
                    return;
                }
                try {
                    long qq = Long.parseLong(args(0));
                    NormalMember member = group.get(qq);
                    if (member != null) {
                        try {
                            member.mute(Integer.parseInt(args(1)));
                        } catch (NumberFormatException e) {
                            logger.warn(l("time.too.long"), args(1));
                        }
                    } else {
                        logger.error(l("not.user"));
                    }
                } catch (NumberFormatException e) {
                    logger.error(l("not.qq"), args(0));
                    logger.trace(e);
                } catch (PermissionDeniedException e) {
                    logger.error(l("no.permission"));
                    logger.trace(e);
                }
            }
        });
        cmds.put("unmute", new CommandRunner() {
            @Override
            public void start() {
                if (args().length < 1) {
                    logger.warn("%s: mute <%s>", l("usage"), l("qq"));
                    return;
                }
                try {
                    long qq = Long.parseLong(args(0));
                    NormalMember member = group.get(qq);
                    if (member != null) {
                        member.unmute();
                    } else {
                        logger.error(l("not.user"));
                    }
                } catch (NumberFormatException e) {
                    logger.error(l("not.qq"), args(0));
                    logger.trace(e);
                } catch (PermissionDeniedException e) {
                    logger.error(l("no.permission"));
                    logger.trace(e);
                }
            }
        });
        cmds.put("avatar", new CommandRunner() {
            @Override
            public void start() throws Exception {
                if (args().length < 1) {
                    logger.warn("%s: avatar <%s>", l("usage"), l("qq"));
                    return;
                }
                Stranger stranger = null;
                try {
                    stranger = bot.getStranger(Long.parseLong(args(0)));
                } catch (NumberFormatException e) {
                    logger.error(l("not.qq"), args(0));
                    logger.trace(e);
                }
                for (Member m : group.getMembers()) {
                    if (String.valueOf(m.getId()).equals(args(0))) {
                        stranger = m.getBot().getAsStranger();
                    }
                }
                if (stranger != null || args(0).equals(String.valueOf(bot.getId()))) {
                    URL url = new URL((stranger != null ? stranger : bot).getAvatarUrl());
                    InputStream is = url.openStream();
                    BufferedInputStream bis = new BufferedInputStream(is);
                    byte[] avatar = bis.readAllBytes();
                    ExternalResource externalResource = ExternalResource.create(avatar);
                    logger.info(l("up.loading.img"));
                    Image img = group.uploadImage(externalResource);
                    externalResource.close();
                    imageInfo(bot, img);
                } else {
                    logger.error(l("not.user"));
                }
            }
        });
        cmds.put("voice", new CommandRunner() {
            @Override
            public void start() {
                if (args().length < 2) {
                    logger.warn(
                            "%s: voice <%s> <%s> [%s = false]",
                            l("usage"),
                            l("file.path"),
                            l("send")
                    );
                    return;
                }
                AtomicBoolean send = new AtomicBoolean(false);
                if (args().length > 2) {
                    send.set(Boolean.parseBoolean(args(2)));
                }
                Thread upVoice = new Thread(() -> {
                    try {
                        File file = new File(msg().substring(6));
                        ExternalResource externalResource = ExternalResource.create(file);
                        logger.info(l("up.loading.voice"));
                        Audio audio = group.uploadAudio(externalResource);
                        if (send.get()) {
                            group.sendMessage(audio);
                        }
                        externalResource.close();
                    } catch (IOException e) {
                        logger.error(l("file.error"));
                        logger.trace(e);
                    }
                });
                upVoice.start();
            }
        });
        cmds.put("image", new CommandRunner() {
            @Override
            public void start() {
                if (args().length < 1) {
                    logger.warn("%s: image <%s>", l("usage"), l("file.path"));
                    return;
                }
                Thread upImg = new Thread(() -> {
                    try {
                        File file = new File(msg().substring(label().length()));
                        ExternalResource externalResource = ExternalResource.create(file);
                        logger.info(l("up.loading.img"));
                        Image img = group.uploadImage(externalResource);
                        group.sendMessage(img);
                        externalResource.close();
                        imageInfo(bot, img);
                    } catch (IOException e) {
                        logger.error(l("file.error"));
                        logger.trace(e);
                    }
                });
                upImg.start();
            }
        });
        cmds.put("imageInfo", new CommandRunner() {
            @Override
            public void start() {
                if (args().length < 1) {
                    logger.warn("%s: imageInfo <%s>", l("usage"), l("image.id"));
                    return;
                }
                try {
                    Image image = Image.fromId(args(0));
                    imageInfo(bot, image);
                } catch (IllegalArgumentException e) {
                    logger.error(e.toString());
                }
            }
        });
        cmds.put("upImg", new CommandRunner() {
            @Override
            public void start() throws Exception {
                if (args().length < 1) {
                    logger.warn("%s: upImg <%s>", l("usage"), l("file.path"));
                    return;
                }
                File file = new File(msg().substring(label().length()));
                ExternalResource externalResource = ExternalResource.create(file);
                Image img = group.uploadImage(externalResource);
                externalResource.close();
                imageInfo(bot, img);
            }
        });
        cmds.put("upClipImg", new CommandRunner() {
            @Override
            public void start() throws Exception {
                byte[] clip = ClipboardUtil.getImageFromClipboard();
                if (clip != null) {
                    ExternalResource externalResource = ExternalResource.create(clip);
                    logger.info(l("up.loading.img"));
                    Image img = group.uploadImage(externalResource);
                    externalResource.close();
                    imageInfo(bot, img);
                } else {
                    logger.error(l("failed.clipboard"));
                }
            }
        });
        cmds.put("newImg", new CommandRunner() {
            @Override
            public void start() throws Exception {
                if (args().length < 4) {
                    logger.warn(
                            "%s: newImg <%s> <%s> <%s> <%s>",
                            l("usage"),
                            l("width"),
                            l("height"),
                            l("font.size"),
                            l("contents")
                    );
                    return;
                }
                int width, height, size;
                try {
                    width = Integer.parseInt(args(0));
                    height = Integer.parseInt(args(1));
                    size = Integer.parseInt(args(2));
                } catch (NumberFormatException e) {
                    logger.warn(l("width.height.error"));
                    logger.trace(e);
                    return;
                }
                StringBuilder content = new StringBuilder();
                for (int i = 4; i < args().length; i++) {
                    content.append(args(i)).append(i == args().length - 1 ? "" : " ");
                }
                logger.info(l("creating.word.image"));
                byte[] bytes = WordToImage.createImage(content.toString(),
                        new Font(ConfigUtil.getConfig("font"), Font.PLAIN, size),
                        width, height
                );
                ExternalResource externalResource = ExternalResource.create(bytes);
                logger.info(l("up.loading.img"));
                Image img = group.uploadImage(externalResource);
                externalResource.close();
                imageInfo(bot, img);
                group.sendMessage(img);
            }
        });
        cmds.put("del", new CommandRunner() {
            @Override
            public void start() {
                if (args().length < 1) {
                    logger.warn("%s: del <%s>", l("usage"), l("qq"));
                    return;
                }
                try {
                    Friend friend = bot.getFriend(Long.parseLong(args(0)));
                    if (friend != null) {
                        friend.delete();
                        logger.info(l("deleted.friend"), friend.getNick(), String.valueOf(friend.getId()));
                    } else {
                        logger.error(l("not.friend"));
                    }
                } catch (NumberFormatException e) {
                    logger.error(l("not.qq"), args(0));
                    logger.trace(e);
                }
            }
        });
        cmds.put("reply", new CommandRunner() {
            @Override
            public void start() {
                if (args().length < 2) {
                    logger.warn("%s: reply <%s> <%s>", l("usage"), l("message.id"), l("contents"));
                    return;
                }
                MessageSource message = null;
                try {
                    message = getMessageById(Integer.parseInt(args(0)));
                } catch (NumberFormatException e) {
                    logger.error(l("message.id.error"));
                    logger.trace(e);
                }
                StringBuilder content = new StringBuilder();
                for (int i = 2; i < args().length; i++) {
                    content.append(args(i));
                }
                if (message != null) {
                    group.sendMessage(new QuoteReply(message).plus(MiraiCode.deserializeMiraiCode(content.toString())));
                } else {
                    logger.error(l("message.not.found"));
                }
            }
        });
        cmds.put("checkUpdate", new CommandRunner() {
            @Override
            public void start() {
                logger.info(l("checking.update"));
                checkUpdate(null);
            }
        });
        cmds.put("accept-request", new CommandRunner() {
            @Override
            public void start() {
                if (args().length < 1) {
                    logger.warn("%s: accept-request <%s>", l("usage"), l("request.id"));
                    return;
                }
                int index = -1;
                try {
                    index = Integer.parseInt(args(0)) - 1;
                } catch (NumberFormatException e) {
                    logger.error(l("request.id.error"));
                    logger.trace(e);
                }
                if (index < 0 || joinRequest.size() <= index) {
                    logger.error(l("request.not.found"));
                    return;
                }
                MemberJoinRequestEvent request = joinRequest.get(index);
                if (request == null) {
                    logger.error(l("request.not.found"));
                    return;
                }
                try {
                    request.accept();
                    logger.info(l("request.accepted"));
                    joinRequest.remove(request);
                } catch (Exception e) {
                    logger.error(l("failed.accept.request"));
                }
            }
        });
        cmds.put("accept-invite", new CommandRunner() {
            @Override
            public void start() {
                if (args().length < 1) {
                    logger.warn("%s: accept-invite <%s>", l("usage"), l("invite.id"));
                    return;
                }
                int index = -1;
                try {
                    index = Integer.parseInt(args(0)) - 1;
                } catch (NumberFormatException e) {
                    logger.error(l("invite.id.error"));
                    logger.trace(e);
                }
                if (index < 0 || inviteRequest.size() <= index) {
                    logger.error(l("invite.not.found"));
                    return;
                }
                BotInvitedJoinGroupRequestEvent request = inviteRequest.get(index);
                if (request == null) {
                    logger.error(l("invite.not.found"));
                    return;
                }
                try {
                    request.accept();
                    logger.info(l("invite.accepted"));
                } catch (Exception e) {
                    logger.error(l("failed.accept.invite"));
                }
            }
        });
        cmds.put("nameCard", new CommandRunner() {
            @Override
            public void start() {
                if (args().length < 2) {
                    logger.warn("%s: nameCard <%s> <%s>", l("usage"), l("qq"), l("name.card"));
                    return;
                }
                try {
                    NormalMember member = group.get(Long.parseLong(args(0)));
                    if (member == null) {
                        logger.error(l("not.user"));
                        return;
                    }
                    StringBuilder nameCard = new StringBuilder();
                    for (int i = 2; i < args().length; i++) {
                        nameCard.append(args(i));
                        if (i != args().length - 1) {
                            nameCard.append(" ");
                        }
                    }
                    member.setNameCard(nameCard.toString());
                    logger.info(l("name.card.set"), member.getNick(), nameCard.toString());
                } catch (NumberFormatException e) {
                    logger.error(l("not.qq"), args(0));
                    logger.trace(e);
                } catch (PermissionDeniedException e) {
                    logger.error(l("no.permission"));
                    logger.trace(e);
                }
            }
        });
        cmds.put("recall", new CommandRunner() {
            @Override
            public void start() {
                if (args().length < 1) {
                    logger.warn("%s: recall <%s>", l("usage"), l("message.id"));
                    return;
                }
                int index = -1;
                try {
                    index = Integer.parseInt(args(0)) - 1;
                } catch (NumberFormatException e) {
                    logger.error(l("message.id.error"));
                    logger.trace(e);
                }
                if (index < 0 || messages.size() <= index) {
                    logger.error(l("message.not.found"));
                    return;
                }
                MessageSource message = messages.get(index);
                if (message == null) {
                    logger.error(l("message.not.found"));
                    return;
                }
                if (message.getFromId() == message.getBotId()) {
                    try {
                        Mirai.getInstance().recallMessage(bot, message);
                        logger.info(l("recalled"));
                    } catch (Exception e) {
                        logger.error(l("failed.recall"));
                    }
                } else {
                    try {
                        Mirai.getInstance().recallMessage(bot, message);
                        logger.info(l("recalled"));
                    } catch (PermissionDeniedException e) {
                        logger.error(l("no.permission"));
                        logger.trace(e);
                    } catch (Exception e) {
                        logger.error(l("failed.recall"));
                    }
                }
            }
        });
        cmds.put("group", new CommandRunner() {
            @Override
            public void start() {
                if (args().length < 1) {
                    logger.warn("%s: group <%s>", l("usage"), l("group") + l("id"));
                    return;
                }
                int id = 0;
                try {
                    id = Integer.parseInt(args(0)) - 1;
                } catch (NumberFormatException e) {
                    logger.error(l("group.id.not.found"), String.valueOf(id));
                }
                String g = "";
                try {
                    g = groups[id];
                } catch (Exception e) {
                    logger.error(l("group.id.not.found"), args(0));
                }
                if (!g.isEmpty()) {
                    if (bot.getGroups().contains(Long.parseLong(g))) {
                        group = bot.getGroup(Long.parseLong(groups[id]));
                        reloadCommands();
                        logger.info(l("now.group"), group.getName(), String.valueOf(group.getId()));
                    } else {
                        logger.error(l("not.entered.group"), g);
                    }
                }
            }
        });
        cmds.put("unload", new CommandRunner() {
            @Override
            public void start() {
                if (args().length < 1) {
                    logger.warn("%s: unload <%s>", l("usage"), l("plugin.name"));
                    return;
                }
                loader.unloadPlugin(args(0));
            }
        });
        cmds.put("load", new CommandRunner() {
            @Override
            public void start() throws Exception {
                if (args().length < 1) {
                    logger.warn("%s: load <%s>", l("usage"), l("file.name"));
                    return;
                }
                File f = new File("plugins/" +
                        (args(0).endsWith(".jar") ? args(0) : args(0) + ".jar"));
                loader.loadPlugin(f, args(0));
            }
        });
        cmds.put("music", new CommandRunner() {
            @Override
            public void start() {
                if (args().length < 1) {
                    logger.warn("%s: music <%s> [%s]", l("usage"), l("music.id"), l("contact"));
                    return;
                }
                long l;
                try {
                    l = Long.parseLong(args(0));
                } catch (NumberFormatException e) {
                    logger.error(l("contact.id.error"));
                    return;
                }
                MusicShare share;
                try {
                    share = neteaseMusic(l);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    return;
                }
                if (args().length < 2) {
                    group.sendMessage(share);
                    return;
                }
                try {
                    long tid = Long.parseLong(args(1));
                    Group group = bot.getGroup(tid);
                    if (group != null) {
                        group.sendMessage(share);
                    } else {
                        Friend friend = bot.getFriend(tid);
                        if (friend != null) {
                            friend.sendMessage(share);
                        }
                    }
                } catch (Exception e) {
                    logger.error(e.toString());
                }
            }
        });
        cmds.put("dice", new CommandRunner() {
            @Override
            public void start() {
                if (args().length < 1) {
                    logger.warn("%s: dice <%s>", l("usage"), l("dice.value"));
                    return;
                }
                int value;
                try {
                    value = Integer.parseInt(args(0));
                } catch (NumberFormatException exception) {
                    logger.error(l("dice.not.number"));
                    return;
                }
                if (value > 0 && value <= 6) {
                    Dice dice = new Dice(value);
                    group.sendMessage(dice);
                } else {
                    logger.error(l("dice.value.error"));
                }
            }
        });
        cmds.put("status", new CommandRunner() {
            @Override
            public void start() {
                long MB = 1024 * 1024;

                ClassLoadingMXBean classLoad = ManagementFactory.getClassLoadingMXBean();

                MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
                MemoryUsage headMemory = memory.getHeapMemoryUsage();

                ThreadMXBean thread = ManagementFactory.getThreadMXBean();

                StringBuilder format = new StringBuilder();
                int l = 1;
                while (!l("status.format.l" + l).isEmpty()) {
                    format.append(l("status.format.l" + l)).append("\n");
                    l++;
                }
                logger.info(format.toString()
                        , headMemory.getUsed() / MB
                        , thread.getThreadCount()
                        , loader.getPlugins().size()
                        , classLoad.getLoadedClassCount()
                        , classLoad.getUnloadedClassCount()
                );
            }
        });
    }

    public static String[] parseArgs(String[] args) {
        if (args.length < 1) return args;
        StringBuilder sb = new StringBuilder();
        for (String s : args) {
            sb.append(s).append(" ");
        }
        sb.delete(sb.length() - 1, sb.length());
        String merge = sb.toString();
        int from = 0,
                index;
        boolean merging = false;
        // 使用了两个占位符：\uFFFC 和 \uFFFB
        String space = "\uFFFC";
        String escape = "\uFFFB";
        merge = merge.replaceAll("\\\\\"", escape);
        while ((index = merge.indexOf("\"", from + 1)) != -1) {
            int f = from;
            from = index;
            if (merging) {
                String me = merge.substring(f, index + 1);
                me = me.replaceAll(" ", space);
                me = me.substring(1, me.length() - 1);
                String left = merge.substring(0, f);
                String right = merge.substring(index + 1);
                merge = left + me + right;
                merging = false;
                continue;
            }
            merging = true;
        }
        ArrayList<String> out = new ArrayList<>();
        for (String s : merge.split(" ")) {
            s = s.replaceAll(space, " ");
            s = s.replaceAll(escape, "\"");
            out.add(s);
        }
        return out.toArray(new String[0]);
    }

    /**
     * 运行命令执行
     *
     * @param msg 信息内容
     * @return 是否是一个可执行命令
     */
    public static boolean runCommand(String msg) {
        String[] cmd = msg.split(" ");
        if (cmd.length < 1) {
            return false;
        }
        String label = cmd[0];
        String[] args = Arrays.copyOfRange(cmd, 1, cmd.length);
        args = parseArgs(args);
        CommandRunner runner = cmds.get(label);
        if (runner != null) {
            try {
                runner.start(label, args, msg);
            } catch (Exception e) {
                logger.trace(e);
                logger.error(l("some.error"), e.toString());
            }
            return true;
        }
        boolean isCmd = false;
        for (Plugin p : loader.plugins) {
            if (!p.isEnabled()) continue;
            try {
                boolean s = p.onCommand(msg);
                if (!s) isCmd = true;
            } catch (Exception e) {
                logger.error(l("plugin.command.error"),
                        p.getName(),
                        label,
                        e.toString()
                );
                logger.trace(e);
            }
            Commands cmds = p.getCommands();
            for (String name : cmds.keys()) {
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
                    logger.error(l("plugin.command.error"),
                            p.getName(),
                            c.getName(),
                            e.toString()
                    );
                    logger.trace(e);
                }
            }
        }
        return isCmd;
    }

    public static MusicShare neteaseMusic(long id) throws Exception {
        URL url = new URL("http://music.163.com/api/song/detail/?id=" + id + "&ids=[" + id + "]");
        JsonObject json;
        try (InputStream is = url.openStream()) {
            json = new Gson().fromJson(
                    new String(is.readAllBytes(), StandardCharsets.UTF_8),
                    JsonObject.class
            );
        }
        JsonArray songs = json.get("songs").getAsJsonArray();
        StringBuilder artists = new StringBuilder();
        JsonArray artistsA = songs.get(0).getAsJsonObject()
                .get("artists").getAsJsonArray();
        for (int i = 0; i < artistsA.size(); i++) {
            artists.append(
                    artistsA.get(i).getAsJsonObject()
                            .get("name").getAsString()
            ).append(
                    i != artistsA.size() - 1 ? " / " : ""
            );
        }
        if (json.get("code").getAsInt() != 200) {
            throw new Exception(l("music.code.error"));
        }
        return new MusicShare(MusicKind.NeteaseCloudMusic,
                songs.get(0).getAsJsonObject()
                        .get("name").getAsString(),
                artists.toString(),
                "http://music.163.com/song/" + id,
                songs.get(0).getAsJsonObject()
                        .get("album").getAsJsonObject()
                        .get("picUrl").getAsString(),
                "http://music.163.com/song/media/outer/url?id=" + id,
                "[音乐] " + songs.get(0).getAsJsonObject()
                        .get("name").getAsString() + " - " + artists
        );
    }
}
