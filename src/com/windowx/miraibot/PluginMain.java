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
import net.mamoe.mirai.event.events.MemberJoinRequestEvent;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.*;
import net.mamoe.mirai.utils.BotConfiguration;
import net.mamoe.mirai.utils.ExternalResource;
import org.jetbrains.annotations.Nullable;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
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
import java.util.*;
import java.util.List;

public class PluginMain {
	public static final String language = Locale.getDefault().getLanguage();
	public static Group group = null;
	public static String[] groups = null;
	public static String[] allowedGroups = null;
	public static Bot bot;
	public static ArrayList<String> commands = new ArrayList<>(Arrays.asList(
			"accept", "avatar", "checkUpdate", "del", "friendList", "help", "image", "imageInfo", "kick", "language", "load",
			"music", "mute", "nameCard", "newImg", "plugins", "reload", "reply", "recall", "send", "stop", "unload", "upClipImg", "upImg")
	);
	protected static ArrayList<Plugin> plugins;
	public static boolean running;
	
	public static void main(String[] args) {
		String err = language.equals("zh") ? "出现错误！进程即将终止！" : (language.equals("tw") ? "出現錯誤！進程即將終止！" : "Unable to create configuration file!");
		LogUtil.init();
		try {
			URL url = ClassLoader.getSystemResource("Version");
			String version = "ERROR";
			if (url != null) {
				version = new String(url.openStream().readAllBytes());
			}
			LogUtil.log(language.equals("zh") ? "MiraiBot " + version + " 基于 Mirai-Core. 版权所有 (C) WindowX 2021" :
					(language.equals("tw") ? "MiraiBot " + version + " 基於 Mirai-Core. 版權所有 (C) WindowX 2021" :
							"MiraiBot " + version + " based Mirai-Core. Copyright (C) WindowX 2021"));
		} catch (Exception e) {
			System.out.println();
			e.printStackTrace();
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
			LogUtil.error(ConfigUtil.getLanguage("config.error"));
			System.exit(-1);
			return;
		}
		ConfigUtil.getConfig("checkUpdate");
		if (ConfigUtil.getConfig("checkUpdate").equals("true")) {
			LogUtil.log(ConfigUtil.getLanguage("checking.update"));
			Thread thread = new Thread(() -> checkUpdate(null));
			thread.start();
		}
		
		String qq = ConfigUtil.getConfig("qq");
		String password = ConfigUtil.getConfig("password");
		groups = ConfigUtil.getConfig("group").split(",");
		allowedGroups = ConfigUtil.getConfig("allowedGroups").split(",");
		EventListener.showQQ = Boolean.parseBoolean(ConfigUtil.getConfig("showQQ", "false"));
		if (qq.isEmpty() || password.isEmpty()) {
			LogUtil.error(ConfigUtil.getLanguage("qq.password.not.exits"));
			System.exit(-1);
			return;
		}
		
		String protocol = !ConfigUtil.getConfig("protocol").isEmpty() ? ConfigUtil.getConfig("protocol") : "";
		LogUtil.log(ConfigUtil.getLanguage("trying.login"), protocol);
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
			LogUtil.log(ConfigUtil.getLanguage("registering.event"));
			GlobalEventChannel.INSTANCE.registerListenerHost(new EventListener());
			LogUtil.log(ConfigUtil.getLanguage("login.success"), bot.getNick());
			String os = System.getProperty("os.name").toLowerCase();
			if (os.contains("windows")) {
				new ProcessBuilder("cmd", "/c", "title " + bot.getNick() + " (" + bot.getId() + ")").inheritIO().start().waitFor();
			} else if (os.contains("linux")) {
				new ProcessBuilder("echo", "-e", "\\033]0;" + bot.getNick() + " (" + bot.getId() + ")" + "\\007").inheritIO().start().waitFor();
			}
			try {
				File demo = new File("plugins");
				if (!demo.exists()) {
					if (demo.mkdirs()) {
						demo = new File("plugins" + File.separator + "demo.jar");
						if (demo.createNewFile()) {
							FileOutputStream fos = new FileOutputStream(demo);
							URL url = ClassLoader.getSystemResource("demo.jar");
							if (url != null) fos.write(url.openStream().readAllBytes());
							fos.flush();
							fos.close();
						}
					}
				}
				File pluginsDir = new File("plugins");
				plugins = new ArrayList<>();
				try {
					File[] pluginsFile = pluginsDir.listFiles();
					if (pluginsFile != null) {
						for (File f : pluginsFile) {
							if (!f.getName().endsWith(".jar") && !f.getName().endsWith(".class")) continue;
							Plugin plugin = null;
							if (f.getName().endsWith(".jar")) {
								URLClassLoader u = new URLClassLoader(new URL[]{ f.toURI().toURL() });
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
									LogUtil.error(ConfigUtil.getLanguage("failed.load.plugin"), f.getName(), "\"plugin.ini\" not found");
								}
							} else if (f.getName().endsWith(".class")) {
								try {
									MyClassLoader myClassLoader = new MyClassLoader();
									Class<?> clazz = myClassLoader.findClass(f);
									plugin = (Plugin) clazz.getDeclaredConstructor().newInstance();
									plugin.setName(f.getName().substring(0, f.getName().length() - 6));
									plugin.setClassName(f.getName().substring(0, f.getName().length() - 6));
									plugin.setClassLoader(myClassLoader);
								} catch (Exception e) {
									e.printStackTrace();
									LogUtil.error(ConfigUtil.getLanguage("failed.load.plugin"), f.getName(), e.toString());
								}
							}
							if (plugin != null) {
								plugin.setFile(f);
								plugin.setEnabled(true);
								plugins.add(plugin);
							} else {
								LogUtil.error(ConfigUtil.getLanguage("failed.load.plugin"), f.getName(), "unknown error");
							}
						}
					}
					for (Plugin p : plugins) {
						try {
							LogUtil.log(ConfigUtil.getLanguage("enabling.plugin"), p.getName());
							p.onEnable();
							commands.addAll(List.of(p.getCommands()));
						} catch (Exception e) {
							p.setEnabled(false);
							LogUtil.error(ConfigUtil.getLanguage("failed.load.plugin"), p.getName(), e.toString());
							e.printStackTrace();
						}
					}
				} catch (Exception e) {
					LogUtil.error(ConfigUtil.getLanguage("unknown.error"));
					e.printStackTrace();
					System.exit(-1);
				}
			} catch (Exception e) {
				LogUtil.error(ConfigUtil.getLanguage("unknown.error"));
				e.printStackTrace();
				System.exit(-1);
			}
			
			if (groups.length < 1) {
				LogUtil.log(ConfigUtil.getLanguage("not.group.set"));
			} else if (!bot.getGroups().contains(Long.parseLong(groups[0]))) {
				LogUtil.log(ConfigUtil.getLanguage("not.entered.group"), groups[0]);
			} else {
				for (int i = 0; i < groups.length; i++) groups[i] = groups[i].trim();
				group = bot.getGroupOrFail(Long.parseLong(groups[0]));
				LogUtil.log(ConfigUtil.getLanguage("now.group"), group.getName(), String.valueOf(group.getId()));
			}
			if (group == null) {
				LogUtil.error(ConfigUtil.getLanguage("unknown.error"));
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
			builder.completer(new StringsCompleter(commands));
			LineReader reader = builder.build();
			while (running) {
				if (reader.isReading()) continue;
				String msg = reader.readLine();
				if (msg.isEmpty()) {
					System.out.print("> ");
					continue;
				}
				if (!runCommand(msg)) {
					try {
						String decode;
						try {
							decode = decodeUnicode(msg);
						} catch (Exception e) {
							decode = msg;
						}
						group.sendMessage(MiraiCode.deserializeMiraiCode(msg.contains("\\u") ? decode : msg));
					} catch (BotIsBeingMutedException e) {
						LogUtil.error(ConfigUtil.getLanguage("bot.is.being.muted"));
					}
				}
			}
		} catch (NumberFormatException e) {
			LogUtil.error(ConfigUtil.getLanguage("qq.password.error"));
			e.printStackTrace();
			System.exit(-1);
		} catch (Exception e) {
			LogUtil.error(ConfigUtil.getLanguage("unknown.error"));
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
		switch (cmd[0]) {
			case "reload":
				if (cmd.length == 1) {
					ConfigUtil.init();
					loadAutoRespond();
					LogUtil.log(ConfigUtil.getLanguage("reloaded"));
				} else {
					try {
						Plugin plugin = getPlugin(cmd[1]);
						if (plugin != null) {
							unloadPlugin(cmd[1]);
							loadPlugin(plugin.getFile(), plugin.getName());
						} else {
							LogUtil.log(ConfigUtil.getLanguage("unloading.plugin"), cmd[1]);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				return true;
			case "stop":
				LogUtil.warn(ConfigUtil.getLanguage("stopping.bot"), bot.getNick(), String.valueOf(bot.getId()));
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
				System.exit(0);
				return true;
			case "friendList": {
				ContactList<Friend> friends = bot.getFriends();
				StringBuilder out = new StringBuilder();
				int c = 1;
				for (Friend f : friends) {
					out.append(c).append(". ").append(f.getNick()).append(" (").append(f.getId()).append(")")
							.append((f.getId() == bot.getId() ? " (" + ConfigUtil.getLanguage("bot") + ")\n" : "\n"));
					c++;
				}
				LogUtil.log(out.toString());
				return true;
			}
			case "groupList": {
				ContactList<NormalMember> members = group.getMembers();
				StringBuilder out = new StringBuilder();
				int c = 1;
				for (NormalMember f : members) {
					out.append(c).append(". ").append((f.getNameCard().isEmpty() ? f.getNick() : f.getNameCard()))
							.append(" (").append(f.getId()).append(")").append("\n");
					c++;
				}
				out.append(c).append(". ").append(bot.getNick()).append(" (").append(bot.getId()).append(")")
						.append(" (").append(ConfigUtil.getLanguage("bot")).append(")\n");
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
					LogUtil.log(ConfigUtil.getLanguage("success.change.language"));
				} else {
					LogUtil.warn(ConfigUtil.getLanguage("usage") + ": language <" + ConfigUtil.getLanguage("language") + ">");
				}
				return true;
			case "clear":
				LogUtil.clear();
				LogUtil.messages = new StringBuilder();
				EventListener.messages = new ArrayList<>();
				LogUtil.log(ConfigUtil.getLanguage("console.cleared"));
				return true;
			case "help":
				String help = "· --------====== MiraiBot ======-------- ·\n" +
						"accept <" + ConfigUtil.getLanguage("request.id") + ">\n" +
						" - " + ConfigUtil.getLanguage("command.accept") + "\n" +
						
						"avatar <" + ConfigUtil.getLanguage("qq") + ">\n" +
						" - " + ConfigUtil.getLanguage("command.avatar") + "\n" +
						
						"checkUpdate \n" +
						" - " + ConfigUtil.getLanguage("command.check.update") + "\n" +
						
						"del <" + ConfigUtil.getLanguage("qq") + ">\n" +
						" - " + ConfigUtil.getLanguage("command.del") + "\n" +
						
						"friendList\n" +
						" - " + ConfigUtil.getLanguage("command.friend.list") + "\n" +
						
						"group\n" +
						" - " + ConfigUtil.getLanguage("command.group") + "\n" +
						
						"groupList\n" +
						" - " + ConfigUtil.getLanguage("command.group.list") + "\n" +
						
						"help\n" +
						" - " + ConfigUtil.getLanguage("command.help") + "\n" +
						
						"image <" + ConfigUtil.getLanguage("file.path") + ">\n" +
						" - " + ConfigUtil.getLanguage("command.image") + "\n" +
						
						"imageInfo <" + ConfigUtil.getLanguage("image.id") + ">\n" +
						" - " + ConfigUtil.getLanguage("command.image.info") + "\n" +
						
						"kick <" + ConfigUtil.getLanguage("qq") + "> <" + ConfigUtil.getLanguage("reason") + ">\n" +
						" - " + ConfigUtil.getLanguage("command.kick") + "\n" +
						
						"language <" + ConfigUtil.getLanguage("language") + ">\n" +
						" - " + ConfigUtil.getLanguage("command.language") + "\n" +
						
						"load <" + ConfigUtil.getLanguage("file.name") + ">\n" +
						" - " + ConfigUtil.getLanguage("command.load") + "\n" +
						
						"music <" + ConfigUtil.getLanguage("music.id") + ">\n" +
						" - " + ConfigUtil.getLanguage("command.music") + "\n" +
						
						"mute <" + ConfigUtil.getLanguage("qq") + "> <" + ConfigUtil.getLanguage("time") + ">\n" +
						" - " + ConfigUtil.getLanguage("command.mute") + "\n" +
						
						"nameCard <" + ConfigUtil.getLanguage("qq") + "> <" + ConfigUtil.getLanguage("name.card") + ">" + ">\n" +
						" - " + ConfigUtil.getLanguage("command.name.card") + "\n" +
						
						"newImg <" + ConfigUtil.getLanguage("width") + "> <" + ConfigUtil.getLanguage("height") + "> <" +
						ConfigUtil.getLanguage("font.size") + "> <" + ConfigUtil.getLanguage("contents") + ">\n" +
						" - " + ConfigUtil.getLanguage("command.new.img") + "\n" +
						
						"plugins\n" +
						" - " + ConfigUtil.getLanguage("command.plugins") + "\n" +
						
						"reload <" + ConfigUtil.getLanguage("plugin.name") + ">\n" +
						" - " + ConfigUtil.getLanguage("command.reload") + "\n" +
						
						"reply <" + ConfigUtil.getLanguage("message.id") + "> <" + ConfigUtil.getLanguage("contents") + ">\n" +
						" - " + ConfigUtil.getLanguage("command.reply") + "\n" +
						
						"recall <" + ConfigUtil.getLanguage("message.id") + ">\n" +
						" - " + ConfigUtil.getLanguage("command.recall") + "\n" +
						
						"send <" + ConfigUtil.getLanguage("qq") + "> <" + ConfigUtil.getLanguage("contents") + ">\n" +
						" - " + ConfigUtil.getLanguage("command.send") + "\n" +
						
						"stop\n" +
						" - " + ConfigUtil.getLanguage("command.stop") + "\n" +
						
						"unload <" + ConfigUtil.getLanguage("plugin.name") + ">\n" +
						" - " + ConfigUtil.getLanguage("command.unload") + "\n" +
						
						"upClipImg\n" +
						" - " + ConfigUtil.getLanguage("command.up.clip.img") + "\n" +
						
						"upImg <" + ConfigUtil.getLanguage("file.path") + ">\n" +
						" - " + ConfigUtil.getLanguage("command.up.img") + "\n" +
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
							LogUtil.log(ConfigUtil.getLanguage("not.friend"));
						}
					} catch (NumberFormatException e) {
						LogUtil.log(ConfigUtil.getLanguage("not.qq"), cmd[1]);
					}
				} else {
					LogUtil.warn(ConfigUtil.getLanguage("usage") + ": send <" +
							ConfigUtil.getLanguage("qq") +
							"> <" + ConfigUtil.getLanguage("contents") + ">");
				}
				return true;
			case "kick":
				if (cmd.length > 2) {
					try {
						NormalMember member = group.get(Long.parseLong(cmd[1]));
						if (member != null) {
							try {
								member.kick(cmd[2]);
								LogUtil.log(ConfigUtil.getLanguage("kicked"));
							} catch (Exception e) {
								LogUtil.error(ConfigUtil.getLanguage("no.permission"));
							}
						} else {
							LogUtil.error(ConfigUtil.getLanguage("not.user"));
						}
					} catch (NumberFormatException e) {
						LogUtil.error(ConfigUtil.getLanguage("not.qq"), cmd[1]);
					}
				} else {
					LogUtil.warn(ConfigUtil.getLanguage("usage") + ": kick <" + ConfigUtil.getLanguage("qq") + "> <" +
							ConfigUtil.getLanguage("reason") + ">");
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
								LogUtil.warn(ConfigUtil.getLanguage("time.too.long"), cmd[2]);
							}
						} else {
							LogUtil.error(ConfigUtil.getLanguage("not.user"));
						}
					} catch (NumberFormatException e) {
						LogUtil.error(ConfigUtil.getLanguage("not.qq"), cmd[1]);
						if (ConfigUtil.getConfig("debug").equals("true")) LogUtil.error(e.toString());
					} catch (PermissionDeniedException e) {
						LogUtil.error(ConfigUtil.getLanguage("no.permission"));
						if (ConfigUtil.getConfig("debug").equals("true")) LogUtil.error(e.toString());
					}
				} else {
					LogUtil.warn(ConfigUtil.getLanguage("usage") + ": mute <" + ConfigUtil.getLanguage("qq") + "> <" +
							ConfigUtil.getLanguage("time") + ">");
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
							LogUtil.log(ConfigUtil.getLanguage("up.loading.img"));
							Image img = group.uploadImage(externalResource);
							externalResource.close();
							imageInfo(bot, img);
						} else {
							LogUtil.error(ConfigUtil.getLanguage("not.user"));
						}
					} catch (NumberFormatException e) {
						LogUtil.error(ConfigUtil.getLanguage("not.qq"), cmd[1]);
						if (ConfigUtil.getConfig("debug").equals("true")) LogUtil.error(e.toString());
					}
				} else {
					LogUtil.warn(ConfigUtil.getLanguage("usage") + ": avatar <" + ConfigUtil.getLanguage("qq") + ">");
				}
				return true;
			case "image":
				if (cmd.length > 1) {
					try {
						File file = new File(msg.substring(6));
						ExternalResource externalResource = ExternalResource.create(file);
						LogUtil.log(ConfigUtil.getLanguage("up.loading.img"));
						Image img = group.uploadImage(externalResource);
						group.sendMessage(img);
						externalResource.close();
						imageInfo(bot, img);
					} catch (IOException e) {
						LogUtil.error(ConfigUtil.getLanguage("file.error"));
						if (ConfigUtil.getConfig("debug").equals("true")) LogUtil.error(e.toString());
					}
				} else {
					LogUtil.warn(ConfigUtil.getLanguage("usage") + ": image <" +
							ConfigUtil.getLanguage("file.path") + ">");
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
					LogUtil.warn(ConfigUtil.getLanguage("usage") + ": imageInfo <" +
							ConfigUtil.getLanguage("image.id") + ">");
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
					LogUtil.warn(ConfigUtil.getLanguage("usage") + ": upImg <" +
							ConfigUtil.getLanguage("file.path") + ">");
				}
				return true;
			case "upClipImg":
				byte[] clip = ClipboardUtil.getImageFromClipboard();
				if (clip != null) {
					ExternalResource externalResource = ExternalResource.create(clip);
					LogUtil.log(ConfigUtil.getLanguage("up.loading.img"));
					Image img = group.uploadImage(externalResource);
					externalResource.close();
					imageInfo(bot, img);
				} else {
					LogUtil.error(ConfigUtil.getLanguage("failed.clipboard"));
				}
				return true;
			case "newImg":
				if (cmd.length > 4) {
					try {
						StringBuilder content = new StringBuilder();
						for (int i = 4; i < cmd.length; i++) {
							content.append(cmd[i]).append(i == cmd.length - 1 ? "" : " ");
						}
						LogUtil.log(ConfigUtil.getLanguage("creating.word.image"));
						byte[] file = WordToImage.createImage(content.toString(),
								new Font(ConfigUtil.getConfig("font"), Font.PLAIN, Integer.parseInt(cmd[3])),
								Integer.parseInt(cmd[1]), Integer.parseInt(cmd[2]));
						ExternalResource externalResource = ExternalResource.create(file);
						LogUtil.log(ConfigUtil.getLanguage("up.loading.img"));
						Image img = group.uploadImage(externalResource);
						externalResource.close();
						imageInfo(bot, img);
						group.sendMessage(img);
					} catch (NumberFormatException e) {
						LogUtil.warn(ConfigUtil.getLanguage("width.height.error"));
						if (ConfigUtil.getConfig("debug").equals("true")) LogUtil.warn(e.toString());
					}
				} else {
					LogUtil.log(ConfigUtil.getLanguage("usage") +
							": newImg <" + ConfigUtil.getLanguage("width") +
							"> <" + ConfigUtil.getLanguage("height") +
							"> <" + ConfigUtil.getLanguage("font.size") +
							"> <" + ConfigUtil.getLanguage("contents") +
							">");
				}
				return true;
			case "del":
				if (cmd.length > 1) {
					try {
						Friend friend = bot.getFriend(Long.parseLong(cmd[1]));
						if (friend != null) {
							friend.delete();
							LogUtil.log(ConfigUtil.getLanguage("deleted.friend"), friend.getNick(), String.valueOf(friend.getId()));
						} else {
							LogUtil.error(ConfigUtil.getLanguage("not.friend"));
						}
					} catch (NumberFormatException e) {
						LogUtil.error(ConfigUtil.getLanguage("not.qq"), cmd[1]);
						if (ConfigUtil.getConfig("debug").equals("true")) LogUtil.error(e.toString());
					}
				} else {
					LogUtil.warn(ConfigUtil.getLanguage("usage") + ": del <QQ>");
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
							LogUtil.error(ConfigUtil.getLanguage("message.not.found"));
						}
					} catch (NumberFormatException e) {
						LogUtil.error(ConfigUtil.getLanguage("message.id.error"));
						if (ConfigUtil.getConfig("debug").equals("true")) LogUtil.error(e.toString());
					}
				} else {
					LogUtil.warn(ConfigUtil.getLanguage("usage") + ": reply <" +
							ConfigUtil.getLanguage("message.id") + "> <" +
							ConfigUtil.getLanguage("contents") + ">");
				}
				return true;
			case "checkUpdate":
				LogUtil.log(ConfigUtil.getLanguage("checking.update"));
				checkUpdate(null);
				return true;
			case "accept":
				if (cmd.length > 1) {
					try {
						if (Integer.parseInt(cmd[1]) - 1 >= 0) {
							MemberJoinRequestEvent request = EventListener.requests.get(Integer.parseInt(cmd[1]) - 1);
							if (request != null) {
								try {
									request.accept();
									LogUtil.log(ConfigUtil.getLanguage("accepted"));
								} catch (Exception e) {
									LogUtil.error(ConfigUtil.getLanguage("failed.accept"));
								}
							} else {
								LogUtil.error(ConfigUtil.getLanguage("request.not.found"));
							}
						} else {
							LogUtil.error(ConfigUtil.getLanguage("request.not.found"));
						}
					} catch (NumberFormatException e) {
						LogUtil.error(ConfigUtil.getLanguage("request.id.error"));
						if (ConfigUtil.getConfig("debug").equals("true")) LogUtil.error(e.toString());
					}
				}
				return true;
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
							LogUtil.log(ConfigUtil.getLanguage("name.card.set"), member.getNick(), nameCard.toString());
						} else {
							LogUtil.error(ConfigUtil.getLanguage("not.user"));
						}
					} catch (NumberFormatException e) {
						LogUtil.error(ConfigUtil.getLanguage("not.qq"), cmd[1]);
						if (ConfigUtil.getConfig("debug").equals("true")) LogUtil.error(e.toString());
					} catch (PermissionDeniedException e) {
						LogUtil.error(ConfigUtil.getLanguage("no.permission"));
						if (ConfigUtil.getConfig("debug").equals("true")) LogUtil.error(e.toString());
					}
				} else {
					LogUtil.warn(ConfigUtil.getLanguage("usage") + ": nameCard <" +
							ConfigUtil.getLanguage("qq") + "> <" + ConfigUtil.getLanguage("name.card") + ">");
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
										LogUtil.log(ConfigUtil.getLanguage("recalled"));
									} catch (Exception e) {
										LogUtil.error(ConfigUtil.getLanguage("failed.recall"));
									}
								} else {
									try {
										Mirai.getInstance().recallMessage(bot, message);
										LogUtil.log(ConfigUtil.getLanguage("recalled"));
									} catch (PermissionDeniedException e) {
										LogUtil.error(ConfigUtil.getLanguage("no.permission"));
										if (ConfigUtil.getConfig("debug").equals("true")) LogUtil.error(e.toString());
									} catch (Exception e) {
										LogUtil.error(ConfigUtil.getLanguage("failed.recall"));
									}
								}
							} else {
								LogUtil.error(ConfigUtil.getLanguage("message.not.found"));
							}
						} else {
							LogUtil.error(ConfigUtil.getLanguage("message.not.found"));
						}
					} catch (NumberFormatException e) {
						LogUtil.error(ConfigUtil.getLanguage("message.id.error"));
						if (ConfigUtil.getConfig("debug").equals("true")) LogUtil.error(e.toString());
					}
				} else {
					LogUtil.warn(ConfigUtil.getLanguage("usage") + ": recall <" +
							ConfigUtil.getLanguage("message.id") + ">");
				}
				return true;
			case "group":
				if (cmd.length > 1) {
					int id = 0;
					try {
						id = Integer.parseInt(cmd[1]) - 1;
					} catch (NumberFormatException e) {
						LogUtil.error(ConfigUtil.getLanguage("group.id.not.found"), String.valueOf(id));
					}
					String g = "";
					try {
						g = groups[id];
					} catch (Exception e) {
						LogUtil.error(ConfigUtil.getLanguage("group.id.not.found"), cmd[1]);
					}
					if (!g.isEmpty()) {
						if (bot.getGroups().contains(Long.parseLong(g))) {
							group = bot.getGroup(Long.parseLong(groups[id]));
							LogUtil.log(ConfigUtil.getLanguage("now.group"), group.getName(), String.valueOf(group.getId()));
						} else {
							LogUtil.error(ConfigUtil.getLanguage("not.entered.group"), g);
						}
					}
				} else {
					LogUtil.warn(ConfigUtil.getLanguage("usage") + ": group <" +
							ConfigUtil.getLanguage("group") + ConfigUtil.getLanguage("id") + ">");
				}
				return true;
			case "unload":
				if (cmd.length > 1) {
					unloadPlugin(cmd[1]);
				} else {
					LogUtil.warn(ConfigUtil.getLanguage("usage") + ": unload <" +
							ConfigUtil.getLanguage("plugin.name") + ">");
				}
				return true;
			case "load":
				if (cmd.length > 1) {
					File f = new File("plugins/" + (cmd[1].endsWith(".jar") || cmd[1].endsWith(".class") ? cmd[1] : cmd[1] + ".jar"));
					loadPlugin(f, cmd[1]);
				} else {
					LogUtil.warn(ConfigUtil.getLanguage("usage") + ": load <" +
							ConfigUtil.getLanguage("file.name") + ">");
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
							group.sendMessage(share);
						} else {
							LogUtil.error(ConfigUtil.getLanguage("music.code.error"));
						}
					} catch (NumberFormatException e) {
						LogUtil.error(ConfigUtil.getLanguage("music.id.error"));
					}
				} else {
					LogUtil.warn(ConfigUtil.getLanguage("usage") + ": music <" + ConfigUtil.getLanguage("music.id") + ">");
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
	@Nullable public static Plugin getPlugin(String name) {
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
			LogUtil.log(ConfigUtil.getLanguage("unloading.plugin"), plugin.getName());
			try {
				plugin.onDisable();
			} catch (Exception e) {
				e.printStackTrace();
			}
			plugin.setEnabled(false);
			System.gc();
			LogUtil.log(ConfigUtil.getLanguage("unloaded.plugin"), plugin.getName());
		} else {
			LogUtil.error(ConfigUtil.getLanguage("plugin.not.exits"), name);
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
					LogUtil.log(ConfigUtil.getLanguage("loading.plugin"), name);
					plugin = initPlugin(is, u);
				} else {
					LogUtil.error(ConfigUtil.getLanguage("failed.load.plugin"), file.getName(), "\"plugin.ini\" not found");
				}
			}
			if (plugin != null) {
				plugin.setFile(file);
				Plugin p = getPlugin(plugin.getName());
				if (p != null) {
					if (p.isEnabled()) {
						LogUtil.warn(ConfigUtil.getLanguage("plugin.already.loaded"), plugin.getName());
						plugins.remove(p);
						return;
					}
				}
				plugin.setEnabled(true);
				plugins.add(plugin);
				try {
					plugin.onEnable();
					commands.addAll(List.of(plugin.getCommands()));
				} catch (Exception e) {
					e.printStackTrace();
				}
				LogUtil.log(ConfigUtil.getLanguage("loaded.plugin"), plugin.getName());
			} else {
				LogUtil.error(ConfigUtil.getLanguage("failed.load.plugin"), file.getName(), "unknown error");
			}
		} else {
			LogUtil.error(ConfigUtil.getLanguage("plugin.file.not.exits")
					.replaceAll("\\$1", name)
			);
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
				String[] nowV = version.replaceAll("[^0-9.]", "").split("\\.");
				String[] newV = LatestVersion.replaceAll("[^0-9.]", "").split("\\.");
				int now = 0, ne = 0;
				for (String s : nowV) now += Integer.parseInt(s);
				for (String s : newV) now += Integer.parseInt(s);
				if (now < ne) {
					LogUtil.log(ConfigUtil.getLanguage("found.new.update"), "https://github.com/1689295608/MiraiBot/releases/tag/" + LatestVersion);
				} else if (nowV == newV) {
					LogUtil.log(ConfigUtil.getLanguage("already.latest.version"), LatestVersion);
				} else {
					LogUtil.warn(ConfigUtil.getLanguage("too.new.version"));
				}
			} catch (Exception e) {
				LogUtil.error(ConfigUtil.getLanguage("failed.check.update"), e.toString());
				if (ConfigUtil.getConfig("debug").equals("true")) LogUtil.log(e.toString());
			}
		} catch (Exception e) {
			LogUtil.error(ConfigUtil.getLanguage("failed.check.update"), e.toString());
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
					LogUtil.error(ConfigUtil.getLanguage("failed.create.config"));
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
				LogUtil.error(ConfigUtil.getLanguage("failed.create.config"));
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
		LogUtil.log("MD5: " + Arrays.toString(MessageUtils.calculateImageMd5(image)));
		LogUtil.log("MiraiCode: " + image.serializeToMiraiCode());
		LogUtil.log("· -------------------------------- ·");
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
				if (file.createNewFile()) {
					Scanner scanner = new Scanner(System.in);
					LogUtil.log(ConfigUtil.getLanguage("please.input") + ConfigUtil.getLanguage("qq"));
					String qq = scanner.nextLine();
					LogUtil.log(ConfigUtil.getLanguage("please.input") + ConfigUtil.getLanguage("password"));
					String password = scanner.nextLine();
					LogUtil.log(ConfigUtil.getLanguage("please.input") + ConfigUtil.getLanguage("group"));
					String group = scanner.nextLine();
					LogUtil.log(ConfigUtil.getLanguage("please.input") + ConfigUtil.getLanguage("true.or.false") + ConfigUtil.getLanguage("check.update"));
					String checkUpdate = scanner.nextLine();
					
					FileOutputStream fos = new FileOutputStream(file);
					String config =
							"# 输入你的 QQ\n" +
									"qq=" + qq + "\n" +
									"# 输入你的 QQ 密码\n" +
									"password=" + password + "\n" +
									"# 输入你要聊天的聊群, 用 “,” 分隔\n" +
									"group=" + group + "\n" +
									"# 允许运行机器人的聊群, 用 “,” 分隔\n" +
									"allowedGroups=" + group + "\n" +
									"# 机器人主人 QQ 号, 即拥有一切权力的 QQ 号. 用 \",\" 分隔\n" +
									"owner=\n" +
									"# 每一个新消息是否都显示发送者的 QQ 号\n" +
									"showQQ=false\n" +
									"# 输入你接收的好友信息（“*” 为 全部\n" +
									"friend=*\n" +
									"# 每次启动时都检测更新 (true / false)\n" +
									"checkUpdate=" + checkUpdate + "\n" +
									"# 输入使用 “newImg” 指令生成的字体\n" +
									"font=微软雅黑\n" +
									"# 输入使用 “newImg” 指令生成的字体颜色\n" +
									"font-color=#ffffff\n" +
									"# 输入使用 “newImg” 指令生成的背景颜色\n" +
									"background-color=#000000\n" +
									"# 使用的登录协议（PAD: 平板，WATCH: 手表，PHONE: 手机），默认 PHONE\n" +
									"protocol=PHONE\n" +
									"# 是否启用 Debug 模式（即显示 MiraiCode）(true / false)\n" +
									"debug=false\n" +
									"# 是否强制使用 AnsiConsole 渲染颜色 (true / false)\n" +
									"ansiColor=false\n" +
									"\n" +
									"# ----=== MiraiBot ===----\n" +
									"# 使用“help”获取帮助！\n" +
									"# -----------------------------\n";
					fos.write(config.getBytes());
					fos.flush();
					fos.close();
					LogUtil.warn(ConfigUtil.getLanguage("please.restart"));
					System.exit(0);
				}
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
