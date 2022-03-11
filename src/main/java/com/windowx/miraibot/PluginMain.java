package com.windowx.miraibot;

import com.windowx.miraibot.plugin.Plugin;
import com.windowx.miraibot.utils.ClipboardUtil;
import com.windowx.miraibot.utils.ConfigUtil;
import com.windowx.miraibot.utils.LanguageUtil;
import com.windowx.miraibot.utils.LogUtil;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.BotFactory;
import net.mamoe.mirai.Mirai;
import net.mamoe.mirai.contact.*;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent;
import net.mamoe.mirai.event.events.MemberJoinRequestEvent;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.*;
import net.mamoe.mirai.utils.BotConfiguration;
import net.mamoe.mirai.utils.ExternalResource;
import org.fusesource.jansi.AnsiConsole;
import org.jetbrains.annotations.Nullable;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.completer.StringsCompleter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.*;

public class PluginMain {
	public static final String language = Locale.getDefault().getLanguage();
	public static Group group;
	public static String[] groups;
	public static String[] allowedGroups;
	public static Bot bot;
	public static ArrayList<String> completes = new ArrayList<>(Arrays.asList(
			"accept-request", "accept-invite", "avatar", "checkUpdate", "del", "friendList", "memberList", "help", "image", "imageInfo", "kick", "language", "load",
			"music", "mute", "nameCard", "newImg", "plugins", "reload", "reply", "recall", "send", "stop", "unload", "upClipImg", "upImg")
	);
	public static boolean running;
	protected static ArrayList<Plugin> plugins;
	
	public static void main(String[] args) {
		String err = language.equals("zh") ? "出现错误！进程即将终止！" : (language.equals("tw") ? "出現錯誤！進程即將終止！" : "Unable to create configuration file!");
		LogUtil.init();
		try {
			URL url = ClassLoader.getSystemResource("Version");
			String version = "";
			if (url != null) {
				version = new String(url.openStream().readAllBytes()).replaceAll("[^0-9.]", "");
			}
			System.out.println(language.equals("zh") ? "MiraiBot " + version + " 基于 Mirai-Core. 版权所有 (C) WindowX 2021" :
					(language.equals("tw") ? "MiraiBot " + version + " 基於 Mirai-Core. 版權所有 (C) WindowX 2021" :
							"MiraiBot " + version + " based Mirai-Core. Copyright (C) WindowX 2021"));
		} catch (Exception e) {
			System.out.println();
			e.printStackTrace();
		}
		try {
			File eula = new File("eula.txt");
			if (!eula.exists()) {
				if (eula.createNewFile()) {
					FileOutputStream fos = new FileOutputStream(eula);
					fos.write((
							"# 使用本软件，您必须遵守我们的协议，一切基于本软件开发的插件都必须在项目明显位置准确提及来自 MiraiBot，不得扭曲或隐藏免费且开源的事实。\n" +
									"# To use this software, you must abide by our agreement." +
									" All plug-ins developed based on this software must accurately mention MiraiBot in the obvious place of the project," +
									" and must not distort or hide the fact that it is free and open source\n" +
									"# 详情请查看：https://github.com/1689295608/MiraiBot/blob/main/LICENSE\n" +
									"# Details: https://github.com/1689295608/MiraiBot/blob/main/LICENSE\n" +
									"eula=false"
					).getBytes());
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
			e.printStackTrace();
			System.exit(-1);
		}
		
		File languageFile = new File("language.properties");
		try {
			if (!languageFile.exists()) {
				if (!languageFile.createNewFile()) {
					LogUtil.error(language.equals("zh") ? "无法创建配置文件！" : (language.equals("tw") ? "無法創建配置文件！" : "Unable to create configuration file!"));
				} else {
					FileOutputStream fos = new FileOutputStream(languageFile);
					fos.write(LanguageUtil.languageFile(language));
					fos.flush();
					fos.close();
				}
			}
		} catch (IOException e) {
			LogUtil.log(err);
			System.out.println();
			e.printStackTrace();
			System.exit(-1);
		}
		ConfigUtil.init();
		ConfigUtil.getConfig("ansiColor");
		LogUtil.ansiColor = Boolean.parseBoolean(ConfigUtil.getConfig("ansiColor"));
		
		loadAutoRespond();
		
		if (!checkConfig()) {
			LogUtil.error(language("config.error"));
			System.exit(-1);
			return;
		}
		ConfigUtil.getConfig("checkUpdate");
		if (ConfigUtil.getConfig("checkUpdate").equals("true")) {
			LogUtil.log(language("checking.update"));
			Thread thread = new Thread(() -> checkUpdate(null));
			thread.start();
		}
		
		String qq = ConfigUtil.getConfig("qq");
		String password = ConfigUtil.getConfig("password");
		groups = ConfigUtil.getConfig("group").split(",");
		allowedGroups = ConfigUtil.getConfig("allowedGroups").split(",");
		EventListener.showQQ = Boolean.parseBoolean(ConfigUtil.getConfig("showQQ", "false"));
		if (qq.isEmpty() || password.isEmpty()) {
			LogUtil.error(language("qq.password.not.exits"));
			System.exit(-1);
			return;
		}
		
		String protocol = !ConfigUtil.getConfig("protocol").isEmpty() ? ConfigUtil.getConfig("protocol") : "";
		LogUtil.log(language("trying.login"), protocol);
		try {
			BotConfiguration.MiraiProtocol miraiProtocol;
			switch (protocol) {
				case "PAD":
					miraiProtocol = BotConfiguration.MiraiProtocol.ANDROID_PAD;
					break;
				case "WATCH":
					miraiProtocol = BotConfiguration.MiraiProtocol.ANDROID_WATCH;
					break;
				default:
					miraiProtocol = BotConfiguration.MiraiProtocol.ANDROID_PHONE;
					break;
			}
			bot = BotFactory.INSTANCE.newBot(Long.parseLong(qq), password, new BotConfiguration() {{
				fileBasedDeviceInfo();
				setProtocol(miraiProtocol);
				noNetworkLog();
				noBotLog();
			}});
			bot.login();
			LogUtil.log(language("registering.event"));
			GlobalEventChannel.INSTANCE.registerListenerHost(new EventListener());
			LogUtil.log(language("login.success"), bot.getNick());
			String os = System.getProperty("os.name").toLowerCase();
			if (os.contains("windows")) {
				new ProcessBuilder("cmd", "/c", "title " + bot.getNick() + " (" + bot.getId() + ")").inheritIO().start().waitFor();
			} else if (os.contains("linux")) {
				new ProcessBuilder("echo", "-e", "\\033]0;" + bot.getNick() + " (" + bot.getId() + ")" + "\\007").inheritIO().start().waitFor();
			}
			try {
				File pluginDir = new File("plugins");
				if (!pluginDir.exists()) {
					if (!pluginDir.mkdirs()) {
						LogUtil.log(language("cannot.create.plugin.dir"));
						System.exit(-1);
					}
				}
				initPlugins();
				for (Plugin p : plugins) {
					try {
						LogUtil.log(language("enabling.plugin"), p.getName());
						p.onEnable();
						completes.addAll(List.of(p.getCommands()));
					} catch (Exception e) {
						p.setEnabled(false);
						LogUtil.error(language("failed.load.plugin"), p.getName(), e.toString());
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				LogUtil.error(language("unknown.error"));
				e.printStackTrace();
				System.exit(-1);
			}
			
			if (groups.length < 1) {
				LogUtil.log(language("not.group.set"));
			} else if (!bot.getGroups().contains(Long.parseLong(groups[0]))) {
				LogUtil.log(language("not.entered.group"), groups[0]);
			} else {
				for (int i = 0; i < groups.length; i++) groups[i] = groups[i].trim();
				group = bot.getGroupOrFail(Long.parseLong(groups[0]));
				LogUtil.log(language("now.group"), group.getName(), String.valueOf(group.getId()));
			}
			if (group == null) {
				LogUtil.error(language("unknown.error"));
				System.exit(-1);
			}
			
			for (Plugin p : plugins) {
				if (!p.isEnabled()) continue;
				try {
					p.onFinished();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			running = true;
			LineReaderBuilder builder = LineReaderBuilder.builder();
			builder.completer(new StringsCompleter(completes));
			LineReader reader = builder.build();
			while (running) {
				if (reader.isReading()) continue;
				String msg = reader.readLine();
				if (msg.isEmpty()) {
					System.out.print("> ");
					continue;
				}
				try {
					if (!runCommand(msg)) {
						String decode;
						try {
							decode = decodeUnicode(msg);
						} catch (Exception e) {
							decode = msg;
						}
						group.sendMessage(MiraiCode.deserializeMiraiCode(msg.contains("\\u") ? decode : msg));
					}
				} catch (BotIsBeingMutedException e) {
					LogUtil.error(language("bot.is.being.muted"));
				}
			}
		} catch (NumberFormatException e) {
			LogUtil.error(language("qq.password.error"));
			e.printStackTrace();
			System.exit(-1);
		} catch(UserInterruptException | EndOfFileException e) {
			System.exit(0);
		} catch (Exception e) {
			LogUtil.error(language("unknown.error"));
			e.printStackTrace();
			System.exit(-1);
		}
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
	
	/**
	 * Execute an instruction.
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
		switch (cmd[0]) {
			case "reload":
				if (cmd.length == 1) {
					ConfigUtil.init();
					loadAutoRespond();
					LogUtil.log(language("reloaded"));
				} else {
					try {
						Plugin plugin = getPlugin(cmd[1]);
						if (plugin != null) {
							unloadPlugin(cmd[1]);
							loadPlugin(plugin.getFile(), plugin.getName());
						} else {
							LogUtil.log(language("unloading.plugin"), cmd[1]);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				return true;
			case "stop":
				LogUtil.warn(language("stopping.bot"), bot.getNick(), String.valueOf(bot.getId()));
				running = false;
				for (Plugin p : plugins) {
					try {
						p.onDisable();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				bot.close();
				System.out.print("\n");
				AnsiConsole.systemUninstall();
				System.exit(0);
				return true;
			case "friendList": {
				ContactList<Friend> friends = bot.getFriends();
				StringBuilder out = new StringBuilder();
				int c = 1;
				for (Friend f : friends) {
					out.append(c).append(". ").append(f.getNick()).append(" (").append(f.getId()).append(")")
							.append((f.getId() == bot.getId() ? " (" + language("bot") + ")\n" : "\n"));
					c++;
				}
				LogUtil.log(out.toString());
				return true;
			}
			case "memberList": {
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
				LogUtil.log(out.toString());
				return true;
			}
			case "plugins":
				StringBuilder out = new StringBuilder();
				int c = 1;
				for (Plugin p : plugins) {
					if (!p.isEnabled()) continue;
					out.append(c).append(". ").append(p.getName()).append(" v").append(p.getVersion()).append(" by ").append(p.getOwner()).append("\n");
					c++;
				}
				LogUtil.log(out.toString());
				return true;
			case "language":
				if (cmd.length > 1) {
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
							fos.write(new FileInputStream(now).readAllBytes());
							fos.flush();
							fos.close();
						}
					}
					FileOutputStream fos = new FileOutputStream(now);
					fos.write(LanguageUtil.languageFile(cmd[1]));
					fos.close();
					ConfigUtil.init();
					LogUtil.log(language("success.change.language"));
				} else {
					LogUtil.warn(language("usage") + ": language <" + language("language") + ">");
				}
				return true;
			case "clear":
				LogUtil.clear();
				LogUtil.messages = new StringBuilder();
				EventListener.messages = new ArrayList<>();
				LogUtil.log(language("console.cleared"));
				return true;
			case "help":
				String help = "· --------====== MiraiBot ======-------- ·\n" +
						"accept-invite <" + language("invite.id") + ">\n" +
						" - " + language("command.accept.invite") + "\n" +
						
						"accept-request <" + language("request.id") + ">\n" +
						" - " + language("command.accept.request") + "\n" +
						
						"avatar <" + language("qq") + ">\n" +
						" - " + language("command.avatar") + "\n" +
						
						"checkUpdate \n" +
						" - " + language("command.check.update") + "\n" +
						
						"del <" + language("qq") + ">\n" +
						" - " + language("command.del") + "\n" +
						
						"friendList\n" +
						" - " + language("command.friend.list") + "\n" +
						
						"group\n" +
						" - " + language("command.group") + "\n" +

						"help\n" +
						" - " + language("command.help") + "\n" +
						
						"image <" + language("file.path") + ">\n" +
						" - " + language("command.image") + "\n" +
						
						"imageInfo <" + language("image.id") + ">\n" +
						" - " + language("command.image.info") + "\n" +
						
						"kick <" + language("qq") + "> <" + language("reason") + ">\n" +
						" - " + language("command.kick") + "\n" +
						
						"language <" + language("language") + ">\n" +
						" - " + language("command.language") + "\n" +
						
						"load <" + language("file.name") + ">\n" +
						" - " + language("command.load") + "\n" +

						"memberList\n" +
						" - " + language("command.member.list") + "\n" +
						
						"music <" + language("music.id") + "> [" + language("contact") + "]\n" +
						" - " + language("command.music") + "\n" +
						
						"mute <" + language("qq") + "> <" + language("time") + ">\n" +
						" - " + language("command.mute") + "\n" +
						
						"nameCard <" + language("qq") + "> <" + language("name.card") + ">" + ">\n" +
						" - " + language("command.name.card") + "\n" +
						
						"newImg <" + language("width") + "> <" + language("height") + "> <" +
						language("font.size") + "> <" + language("contents") + ">\n" +
						" - " + language("command.new.img") + "\n" +
						
						"nudge <" + language("qq") + ">\n" +
						" - " + language("command.nudge") + "\n" +
						
						"plugins\n" +
						" - " + language("command.plugins") + "\n" +
						
						"reload <" + language("plugin.name") + ">\n" +
						" - " + language("command.reload") + "\n" +
						
						"reply <" + language("message.id") + "> <" + language("contents") + ">\n" +
						" - " + language("command.reply") + "\n" +
						
						"recall <" + language("message.id") + ">\n" +
						" - " + language("command.recall") + "\n" +
						
						"send <" + language("qq") + "> <" + language("contents") + ">\n" +
						" - " + language("command.send") + "\n" +
						
						"stop\n" +
						" - " + language("command.stop") + "\n" +
						
						"unload <" + language("plugin.name") + ">\n" +
						" - " + language("command.unload") + "\n" +
						
						"upClipImg\n" +
						" - " + language("command.up.clip.img") + "\n" +
						
						"upImg <" + language("file.path") + ">\n" +
						" - " + language("command.up.img") + "\n" +
						
						"voice <" + language("file.path") + ">\n" +
						" - " + language("command.voice") + "\n" +
						"· -------------------------------------- ·\n";
				LogUtil.log(help);
				return true;
			case "send":
				if (cmd.length > 2) {
					StringBuilder tmp = new StringBuilder();
					for (int i = 2; i < cmd.length; i++) {
						tmp.append(cmd[i]).append(i == cmd.length - 1 ? "" : " ");
					}
					try {
						Friend friend = bot.getFriend(Long.parseLong(cmd[1]));
						if (friend != null) {
							friend.sendMessage(MiraiCode.deserializeMiraiCode(tmp.toString()));
						} else {
							LogUtil.log(language("not.friend"));
						}
					} catch (NumberFormatException e) {
						LogUtil.log(language("not.qq"), cmd[1]);
					}
				} else {
					LogUtil.warn(language("usage") + ": send <" +
							language("qq") +
							"> <" + language("contents") + ">");
				}
				return true;
			case "kick":
				if (cmd.length > 2) {
					try {
						NormalMember member = group.get(Long.parseLong(cmd[1]));
						if (member != null) {
							try {
								member.kick(cmd[2]);
								LogUtil.log(language("kicked"));
							} catch (Exception e) {
								LogUtil.error(language("no.permission"));
							}
						} else {
							LogUtil.error(language("not.user"));
						}
					} catch (NumberFormatException e) {
						LogUtil.error(language("not.qq"), cmd[1]);
					}
				} else {
					LogUtil.warn(language("usage") + ": kick <" + language("qq") + "> <" +
							language("reason") + ">");
				}
				return true;
			case "nudge":
				if (cmd.length > 1) {
					try {
						NormalMember member = group.get(Long.parseLong(cmd[1]));
						if (member != null) {
							try {
								member.nudge().sendTo(group);
								LogUtil.log(language("nudged"));
							} catch (Exception e) {
								LogUtil.error(language("no.permission"));
							}
						} else {
							LogUtil.error(language("not.user"));
						}
					} catch (NumberFormatException e) {
						LogUtil.error(language("not.qq"), cmd[1]);
					}
				} else {
					LogUtil.warn(language("usage") + ": nudge <" + language("qq") + ">");
				}
				return true;
			case "mute":
				if (cmd.length > 2) {
					try {
						long qq = Long.parseLong(cmd[1]);
						NormalMember member = group.get(qq);
						if (member != null) {
							try {
								member.mute(Integer.parseInt(cmd[2]));
							} catch (NumberFormatException e) {
								LogUtil.warn(language("time.too.long"), cmd[2]);
							}
						} else {
							LogUtil.error(language("not.user"));
						}
					} catch (NumberFormatException e) {
						LogUtil.error(language("not.qq"), cmd[1]);
						if (ConfigUtil.getConfig("debug").equals("true")) LogUtil.error(e.toString());
					} catch (PermissionDeniedException e) {
						LogUtil.error(language("no.permission"));
						if (ConfigUtil.getConfig("debug").equals("true")) LogUtil.error(e.toString());
					}
				} else {
					LogUtil.warn(language("usage") + ": mute <" + language("qq") + "> <" +
							language("time") + ">");
				}
				return true;
			case "avatar":
				if (cmd.length > 1) {
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
							LogUtil.log(language("up.loading.img"));
							Image img = group.uploadImage(externalResource);
							externalResource.close();
							imageInfo(bot, img);
						} else {
							LogUtil.error(language("not.user"));
						}
					} catch (NumberFormatException e) {
						LogUtil.error(language("not.qq"), cmd[1]);
						if (ConfigUtil.getConfig("debug").equals("true")) LogUtil.error(e.toString());
					}
				} else {
					LogUtil.warn(language("usage") + ": avatar <" + language("qq") + ">");
				}
				return true;
			case "voice":
				if (cmd.length > 1) {
					Thread upVoice = new Thread(() -> {
						try {
							File file = new File(msg.substring(6));
							ExternalResource externalResource = ExternalResource.create(file);
							LogUtil.log(language("up.loading.voice"));
							Audio audio = group.uploadAudio(externalResource);
							group.sendMessage(audio);
							externalResource.close();
						} catch (IOException e) {
							LogUtil.error(language("file.error"));
							if (ConfigUtil.getConfig("debug").equals("true")) LogUtil.error(e.toString());
						}
					});
					upVoice.start();
				} else {
					LogUtil.warn(language("usage") + ": voice <" +
							language("file.path") + ">");
				}
				return true;
			case "image":
				if (cmd.length > 1) {
					Thread upImg = new Thread(() -> {
						try {
							File file = new File(msg.substring(6));
							ExternalResource externalResource = ExternalResource.create(file);
							LogUtil.log(language("up.loading.img"));
							Image img = group.uploadImage(externalResource);
							group.sendMessage(img);
							externalResource.close();
							imageInfo(bot, img);
						} catch (IOException e) {
							LogUtil.error(language("file.error"));
							if (ConfigUtil.getConfig("debug").equals("true")) LogUtil.error(e.toString());
						}
					});
					upImg.start();
				} else {
					LogUtil.warn(language("usage") + ": image <" +
							language("file.path") + ">");
				}
				return true;
			case "imageInfo":
				if (cmd.length > 1) {
					try {
						Image image = Image.fromId(cmd[1]);
						imageInfo(bot, image);
					} catch (IllegalArgumentException e) {
						LogUtil.error(e.toString());
					}
				} else {
					LogUtil.warn(language("usage") + ": imageInfo <" +
							language("image.id") + ">");
				}
				return true;
			case "upImg":
				if (cmd.length > 1) {
					File file = new File(msg.substring(6));
					ExternalResource externalResource = ExternalResource.create(file);
					Image img = group.uploadImage(externalResource);
					externalResource.close();
					imageInfo(bot, img);
				} else {
					LogUtil.warn(language("usage") + ": upImg <" +
							language("file.path") + ">");
				}
				return true;
			case "upClipImg":
				byte[] clip = ClipboardUtil.getImageFromClipboard();
				if (clip != null) {
					ExternalResource externalResource = ExternalResource.create(clip);
					LogUtil.log(language("up.loading.img"));
					Image img = group.uploadImage(externalResource);
					externalResource.close();
					imageInfo(bot, img);
				} else {
					LogUtil.error(language("failed.clipboard"));
				}
				return true;
			case "newImg":
				if (cmd.length > 4) {
					try {
						StringBuilder content = new StringBuilder();
						for (int i = 4; i < cmd.length; i++) {
							content.append(cmd[i]).append(i == cmd.length - 1 ? "" : " ");
						}
						LogUtil.log(language("creating.word.image"));
						byte[] file = WordToImage.createImage(content.toString(),
								new Font(ConfigUtil.getConfig("font"), Font.PLAIN, Integer.parseInt(cmd[3])),
								Integer.parseInt(cmd[1]), Integer.parseInt(cmd[2]));
						ExternalResource externalResource = ExternalResource.create(file);
						LogUtil.log(language("up.loading.img"));
						Image img = group.uploadImage(externalResource);
						externalResource.close();
						imageInfo(bot, img);
						group.sendMessage(img);
					} catch (NumberFormatException e) {
						LogUtil.warn(language("width.height.error"));
						if (ConfigUtil.getConfig("debug").equals("true")) LogUtil.warn(e.toString());
					}
				} else {
					LogUtil.log(language("usage") +
							": newImg <" + language("width") +
							"> <" + language("height") +
							"> <" + language("font.size") +
							"> <" + language("contents") +
							">");
				}
				return true;
			case "del":
				if (cmd.length > 1) {
					try {
						Friend friend = bot.getFriend(Long.parseLong(cmd[1]));
						if (friend != null) {
							friend.delete();
							LogUtil.log(language("deleted.friend"), friend.getNick(), String.valueOf(friend.getId()));
						} else {
							LogUtil.error(language("not.friend"));
						}
					} catch (NumberFormatException e) {
						LogUtil.error(language("not.qq"), cmd[1]);
						if (ConfigUtil.getConfig("debug").equals("true")) LogUtil.error(e.toString());
					}
				} else {
					LogUtil.warn(language("usage") + ": del <QQ>");
				}
				return true;
			case "reply":
				if (cmd.length > 2) {
					try {
						MessageSource message = getMessageById(Integer.parseInt(cmd[1]));
						StringBuilder content = new StringBuilder();
						for (int i = 2; i < cmd.length; i++) {
							content.append(cmd[i]);
						}
						if (message != null) {
							group.sendMessage(new QuoteReply(message).plus(MiraiCode.deserializeMiraiCode(content.toString())));
						} else {
							LogUtil.error(language("message.not.found"));
						}
					} catch (NumberFormatException e) {
						LogUtil.error(language("message.id.error"));
						if (ConfigUtil.getConfig("debug").equals("true")) LogUtil.error(e.toString());
					}
				} else {
					LogUtil.warn(language("usage") + ": reply <" +
							language("message.id") + "> <" +
							language("contents") + ">");
				}
				return true;
			case "checkUpdate":
				LogUtil.log(language("checking.update"));
				checkUpdate(null);
				return true;
			case "accept-request":
				if (cmd.length > 1) {
					try {
						if (Integer.parseInt(cmd[1]) - 1 >= 0) {
							MemberJoinRequestEvent request = EventListener.joinRequest.get(Integer.parseInt(cmd[1]) - 1);
							if (request != null) {
								try {
									request.accept();
									LogUtil.log(language("request.accepted"));
									EventListener.joinRequest.remove(request);
								} catch (Exception e) {
									LogUtil.error(language("failed.accept.request"));
								}
							} else {
								LogUtil.error(language("request.not.found"));
							}
						} else {
							LogUtil.error(language("request.not.found"));
						}
					} catch (NumberFormatException e) {
						LogUtil.error(language("request.id.error"));
						if (ConfigUtil.getConfig("debug").equals("true")) LogUtil.error(e.toString());
					}
				} else {
					LogUtil.warn(language("usage") + ": accept-request <" + language("request.id") + ">");
				}
				return true;
			case "accept-invite":
				if (cmd.length > 1) {
					try {
						if (Integer.parseInt(cmd[1]) - 1 >= 0) {
							BotInvitedJoinGroupRequestEvent request = EventListener.inviteRequest.get(Integer.parseInt(cmd[1]) - 1);
							if (request != null) {
								try {
									request.accept();
									LogUtil.log(language("invite.accepted"));
								} catch (Exception e) {
									LogUtil.error(language("failed.accept.invite"));
								}
							} else {
								LogUtil.error(language("invite.not.found"));
							}
						} else {
							LogUtil.error(language("invite.not.found"));
						}
					} catch (NumberFormatException e) {
						LogUtil.error(language("invite.id.error"));
						if (ConfigUtil.getConfig("debug").equals("true")) LogUtil.error(e.toString());
					}
				} else {
					LogUtil.warn(language("usage") + ": accept-invite <" + language("invite.id") + ">");
				}
			case "nameCard":
				if (cmd.length > 2) {
					try {
						NormalMember member = group.get(Long.parseLong(cmd[1]));
						if (member != null) {
							StringBuilder nameCard = new StringBuilder();
							for (int i = 2; i < cmd.length; i++) {
								nameCard.append(cmd[i]);
								if (i != cmd.length - 1) {
									nameCard.append(" ");
								}
							}
							member.setNameCard(nameCard.toString());
							LogUtil.log(language("name.card.set"), member.getNick(), nameCard.toString());
						} else {
							LogUtil.error(language("not.user"));
						}
					} catch (NumberFormatException e) {
						LogUtil.error(language("not.qq"), cmd[1]);
						if (ConfigUtil.getConfig("debug").equals("true")) LogUtil.error(e.toString());
					} catch (PermissionDeniedException e) {
						LogUtil.error(language("no.permission"));
						if (ConfigUtil.getConfig("debug").equals("true")) LogUtil.error(e.toString());
					}
				} else {
					LogUtil.warn(language("usage") + ": nameCard <" +
							language("qq") + "> <" + language("name.card") + ">");
				}
				return true;
			case "recall":
				if (cmd.length > 1) {
					try {
						if (Integer.parseInt(cmd[1]) - 1 >= 0) {
							MessageSource message = EventListener.messages.get(Integer.parseInt(cmd[1]) - 1);
							if (message != null) {
								if (message.getFromId() == message.getBotId()) {
									try {
										Mirai.getInstance().recallMessage(bot, message);
										LogUtil.log(language("recalled"));
									} catch (Exception e) {
										LogUtil.error(language("failed.recall"));
									}
								} else {
									try {
										Mirai.getInstance().recallMessage(bot, message);
										LogUtil.log(language("recalled"));
									} catch (PermissionDeniedException e) {
										LogUtil.error(language("no.permission"));
										if (ConfigUtil.getConfig("debug").equals("true")) LogUtil.error(e.toString());
									} catch (Exception e) {
										LogUtil.error(language("failed.recall"));
									}
								}
							} else {
								LogUtil.error(language("message.not.found"));
							}
						} else {
							LogUtil.error(language("message.not.found"));
						}
					} catch (NumberFormatException e) {
						LogUtil.error(language("message.id.error"));
						if (ConfigUtil.getConfig("debug").equals("true")) LogUtil.error(e.toString());
					}
				} else {
					LogUtil.warn(language("usage") + ": recall <" +
							language("message.id") + ">");
				}
				return true;
			case "group":
				if (cmd.length > 1) {
					int id = 0;
					try {
						id = Integer.parseInt(cmd[1]) - 1;
					} catch (NumberFormatException e) {
						LogUtil.error(language("group.id.not.found"), String.valueOf(id));
					}
					String g = "";
					try {
						g = groups[id];
					} catch (Exception e) {
						LogUtil.error(language("group.id.not.found"), cmd[1]);
					}
					if (!g.isEmpty()) {
						if (bot.getGroups().contains(Long.parseLong(g))) {
							group = bot.getGroup(Long.parseLong(groups[id]));
							LogUtil.log(language("now.group"), group.getName(), String.valueOf(group.getId()));
						} else {
							LogUtil.error(language("not.entered.group"), g);
						}
					}
				} else {
					LogUtil.warn(language("usage") + ": group <" +
							language("group") + language("id") + ">");
				}
				return true;
			case "unload":
				if (cmd.length > 1) {
					unloadPlugin(cmd[1]);
				} else {
					LogUtil.warn(language("usage") + ": unload <" +
							language("plugin.name") + ">");
				}
				return true;
			case "load":
				if (cmd.length > 1) {
					File f = new File("plugins/" + (cmd[1].endsWith(".jar") || cmd[1].endsWith(".class") ? cmd[1] : cmd[1] + ".jar"));
					loadPlugin(f, cmd[1]);
				} else {
					LogUtil.warn(language("usage") + ": load <" +
							language("file.name") + ">");
				}
				return true;
			case "music":
				if (cmd.length > 1) {
					try {
						long id = Long.parseLong(cmd[1]);
						URL url = new URL("http://music.163.com/api/song/detail/?id=" + id + "&ids=[" + id + "]");
						InputStream is = url.openStream();
						JSONObject json = new JSONObject(new String(is.readAllBytes(), StandardCharsets.UTF_8));
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
									"http://music.163.com/song/" + id,
									songs.getJSONObject(0).getJSONObject("album").getString("picUrl"),
									"http://music.163.com/song/media/outer/url?id=" + id,
									"[音乐] " + songs.getJSONObject(0).getString("name") + " - " + artists
							);
							if (cmd.length < 3) {
								group.sendMessage(share);
							} else {
								try {
									long tid = Long.parseLong(cmd[2]);
									Group g = bot.getGroup(tid);
									if (g != null) {
										g.sendMessage(share);
									} else {
										Friend f = bot.getFriend(tid);
										if (f != null) {
											f.sendMessage(share);
										}
									}
								} catch (NumberFormatException e) {
									LogUtil.error(language("contact.id.error"));
								} catch (Exception e) {
									LogUtil.error(e.toString());
								}
							}
						} else {
							LogUtil.error(language("music.code.error"));
						}
					} catch (NumberFormatException e) {
						LogUtil.error(language("music.id.error"));
					}
				} else {
					LogUtil.warn(language("usage") + ": music <" + language("music.id") + "> [" + language("contact") + "]");
				}
				return true;
			case "dice":
				if (cmd.length > 1) {
					int value;
					try {
						value = Integer.parseInt(cmd[1]);
					} catch (NumberFormatException exception) {
						LogUtil.error(language("dice.not.number"));
						return true;
					}
					if (value > 0 && value <= 6) {
						Dice dice = new Dice(value);
						group.sendMessage(dice);
					} else {
						LogUtil.error(language("dice.value.error"));
					}
				} else {
					LogUtil.warn(language("usage") + ": dice <" + language("dice.value") + ">");
				}
				return true;
			default:
				boolean isCmd = false;
				for (Plugin p : plugins) {
					if (!p.isEnabled()) continue;
					try {
						boolean s = p.onCommand(msg);
						if (!s) isCmd = true;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				return isCmd;
		}
	}
	
	/**
	 * 通过插件名获取插件对象
	 *
	 * @param name 插件名
	 * @return 插件
	 */
	@Nullable
	public static Plugin getPlugin(String name) {
		for (Plugin p : plugins) {
			if (p.getName().equals(name)) return p;
		}
		return null;
	}
	
	/**
	 * 卸载某个插件
	 *
	 * @param name 插件名
	 */
	public static void unloadPlugin(String name) {
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
	
	private static Plugin initPlugin(InputStream is, URLClassLoader u) throws Exception {
		Properties plugin = new Properties();
		plugin.load(is);
		Class<?> clazz = u.loadClass(plugin.getProperty("main"));
		Plugin p = (Plugin) clazz.getDeclaredConstructor().newInstance();
		p.setName(plugin.getProperty("name", "Untitled"));
		p.setOwner(plugin.getProperty("owner", "Unnamed"));
		p.setClassName(plugin.getProperty("main"));
		p.setVersion(plugin.getProperty("version", "1.0.0"));
		p.setDescription(plugin.getProperty("description", "A Plugin For MiraiBot."));
		p.setCommands(plugin.getProperty("commands", "").split(","));
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
	public static void loadPlugin(File file, String name) throws Exception {
		if (file.exists()) {
			Plugin plugin = null;
			if (file.getName().endsWith(".class")) {
				MyClassLoader myClassLoader = new MyClassLoader();
				Class<?> clazz = myClassLoader.findClass(file);
				plugin = (Plugin) clazz.getDeclaredConstructor().newInstance();
				plugin.setName(file.getName().substring(0, file.getName().length() - 6));
				plugin.setClassName(file.getName().substring(6));
				plugin.setClassLoader(myClassLoader);
			} else {
				URLClassLoader u = new URLClassLoader(new URL[]{file.toURI().toURL()});
				InputStream is = u.getResourceAsStream("plugin.ini");
				if (is != null) {
					LogUtil.log(language("loading.plugin"), name);
					plugin = initPlugin(is, u);
				} else {
					LogUtil.error(language("failed.load.plugin"), file.getName(), "\"plugin.ini\" not found");
				}
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
	 * 初始化插件，将所有 plugins 文件夹内的 .jar 和 .class 文件加载到插件列表
	 */
	public static void initPlugins() {
		File pluginsDir = new File("plugins");
		plugins = new ArrayList<>();
		try {
			File[] pluginsFile = pluginsDir.listFiles();
			if (pluginsFile != null) {
				for (File f : pluginsFile) {
					if (!f.getName().endsWith(".jar") && !f.getName().endsWith(".class")) continue;
					Plugin plugin = null;
					if (f.getName().endsWith(".jar")) {
						URLClassLoader u = new URLClassLoader(new URL[]{f.toURI().toURL()});
						InputStream is = u.getResourceAsStream("plugin.ini");
						if (is != null) {
							try {
								plugin = initPlugin(is, u);
								plugin.setClassLoader(u);
							} catch (Exception e) {
								System.out.println();
								e.printStackTrace();
							}
						} else {
							LogUtil.error(language("failed.load.plugin"), f.getName(), "\"plugin.ini\" not found");
						}
					} else if (f.getName().endsWith(".class")) { // .class 插件文件随时可能被弃用
						try {
							MyClassLoader myClassLoader = new MyClassLoader();
							Class<?> clazz = myClassLoader.findClass(f);
							plugin = (Plugin) clazz.getDeclaredConstructor().newInstance();
							plugin.setName(f.getName().substring(0, f.getName().length() - 6));
							plugin.setClassName(f.getName().substring(0, f.getName().length() - 6));
							plugin.setClassLoader(myClassLoader);
						} catch (Exception e) {
							e.printStackTrace();
							LogUtil.error(language("failed.load.plugin"), f.getName(), e.toString());
						}
					}
					if (plugin != null) {
						plugin.setFile(f);
						plugin.setEnabled(true);
						plugins.add(plugin);
					} else {
						LogUtil.error(language("failed.load.plugin"), f.getName(), "unknown error");
					}
				}
			}
		} catch (Exception e) {
			LogUtil.error(language("unknown.error"));
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	/**
	 * Check if there is a new release
	 */
	public static void checkUpdate(String u) {
		try {
			URL update = new URL(Objects.requireNonNullElse(u, "https://ghproxy.com/https://raw.githubusercontent.com/1689295608/MiraiBot/main/LatestVersion"));
			InputStream is = update.openStream();
			BufferedInputStream bis = new BufferedInputStream(is);
			String LatestVersion = new String(bis.readAllBytes()).replaceAll("\\n", "");
			URL url = ClassLoader.getSystemResource("Version");
			String version = "0.0.0";
			if (url != null) {
				version = new String(url.openStream().readAllBytes());
			}
			try {
				String nowV = version.replaceAll("[^0-9.]", "");
				String newV = LatestVersion.replaceAll("[^0-9.]", "");
				if (!nowV.equals(newV)) {
					LogUtil.warn(language("found.new.update"), "https://github.com/1689295608/MiraiBot/releases/tag/" + LatestVersion);
				} else {
					LogUtil.log(language("already.latest.version"), LatestVersion);
				}
			} catch (Exception e) {
				LogUtil.error(language("failed.check.update"), e.toString());
				if (ConfigUtil.getConfig("debug").equals("true")) LogUtil.log(e.toString());
			}
		} catch (Exception e) {
			LogUtil.error(language("failed.check.update"), e.toString());
			if (ConfigUtil.getConfig("debug").equals("true")) LogUtil.log(e.toString());
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
			return EventListener.messages.get(id - 1);
		}
		return null;
	}
	
	/**
	 * 加载自动回复
	 */
	public static void loadAutoRespond() {
		EventListener.autoRespond = new File("AutoRespond.json");
		if (!EventListener.autoRespond.exists()) {
			try {
				if (!EventListener.autoRespond.createNewFile()) {
					LogUtil.error(language("failed.create.config"));
					System.exit(-1);
				}
				FileOutputStream fos = new FileOutputStream(EventListener.autoRespond);
				URL url = ClassLoader.getSystemResource("AutoRespond.json");
				if (url != null) {
					fos.write(url.openStream().readAllBytes());
				}
				fos.flush();
				fos.close();
			} catch (IOException e) {
				LogUtil.error(language("failed.create.config"));
				System.out.println();
				e.printStackTrace();
				System.exit(-1);
			}
		}
		try {
			EventListener.autoRespondConfig = new JSONObject(new String(new FileInputStream(EventListener.autoRespond).readAllBytes()));
		} catch (IOException e) {
			System.out.println();
			e.printStackTrace();
		}
	}
	
	/**
	 * Output image info
	 *
	 * @param bot   Bot
	 * @param image Image
	 */
	public static void imageInfo(Bot bot, Image image) {
		LogUtil.log("· ------==== Image Info ====------ ·");
		LogUtil.log("I D: " + image.getImageId());
		LogUtil.log("URL: " + Mirai.getInstance().queryImageUrl(bot, image));
		LogUtil.log("MD5: " + Arrays.toString(image.getMd5()));
		LogUtil.log("MiraiCode: " + image.serializeToMiraiCode());
		LogUtil.log("· -------------------------------- ·");
	}
	
	/**
	 * Fast to language()
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
				Scanner scanner = new Scanner(System.in);
				LogUtil.log(language("before.settings"));
				LogUtil.log(language("please.input.qq"));
				String qq = scanner.nextLine();
				LogUtil.log(language("please.input.password"));
				String password = scanner.nextLine();
				LogUtil.log(language("please.input.group.id"));
				String groups = scanner.nextLine();
				LogUtil.log(language("please.input.check.update.on.setup"));
				String checkUpdate = scanner.nextLine();
				
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
				config = config.replaceAll("%check.as.setup%", checkUpdate);
				fos.write(config.getBytes());
				fos.close();
				LogUtil.warn(language("please.restart"));
				System.exit(0);
			} catch (IOException e) {
				System.out.println();
				e.printStackTrace();
			}
			return false;
		}
		return true;
	}
}

class MyClassLoader extends ClassLoader {
	public Class<?> findClass(File file) throws IOException {
		Path path = file.toPath();
		byte[] bytes = Files.readAllBytes(path);
		return defineClass(null, bytes, 0, bytes.length, null);
	}
}