package com.windowx.miraibot;

import com.windowx.miraibot.plugin.*;
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
import net.mamoe.mirai.message.data.MessageSource;
import net.mamoe.mirai.message.data.MessageUtils;
import net.mamoe.mirai.message.data.QuoteReply;
import net.mamoe.mirai.utils.BotConfiguration;
import net.mamoe.mirai.utils.ExternalResource;
import org.json.JSONObject;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;

public class PluginMain {
	public static Group group = null;
	public static final String language = Locale.getDefault().getLanguage();
	public static Bot bot;
	public static List<Plugin> pluginList;
	public static PluginManager pluginManager;
	
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
			e.printStackTrace();
		}
		
		File languageFile = new File("language.properties");
		try {
			if (!languageFile.exists()) {
				if (!languageFile.createNewFile()) {
					LogUtil.log(language.equals("zh") ? "无法创建配置文件！" : (language.equals("tw") ? "無法創建配置文件！" : "Unable to create configuration file!"));
				} else {
					FileOutputStream fos = new FileOutputStream(languageFile);
					fos.write(LanguageUtil.languageFile(language));
					fos.flush();
					fos.close();
				}
			}
		} catch (IOException e) {
			LogUtil.log(err);
			e.printStackTrace();
			System.exit(-1);
		}
		ConfigUtil.init();
		
		loadAutoRespond();
		
		if (!checkConfig()) {
			LogUtil.log(ConfigUtil.getLanguage("config.error"));
			System.exit(-1);
			return;
		}
		if (ConfigUtil.getConfig("checkUpdate") != null && ConfigUtil.getConfig("checkUpdate").equals("true")) {
			LogUtil.log(ConfigUtil.getLanguage("checking.update"));
			checkUpdate(null);
		}
		
		String qq = ConfigUtil.getConfig("qq");
		String password = ConfigUtil.getConfig("password");
		String groupId = ConfigUtil.getConfig("group");
		if (ConfigUtil.getConfig("showQQ") != null) {
			EventListener.showQQ = Boolean.parseBoolean(ConfigUtil.getConfig("showQQ"));
		} else {
			EventListener.showQQ = false;
		}
		if ((qq == null || password == null) || (qq.isEmpty() || password.isEmpty())) {
			LogUtil.log(ConfigUtil.getLanguage("qq.password.not.exits"));
			System.exit(-1);
			return;
		}
		
		String protocol = ConfigUtil.getConfig("protocol") != null ?
				ConfigUtil.getConfig("protocol") : "";
		LogUtil.log(ConfigUtil.getLanguage("trying.login").replaceAll("\\$1", protocol));
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
			LogUtil.log(ConfigUtil.getLanguage("login.success").replaceAll("\\$1", bot.getNick()));
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
							fos.flush(); fos.close();
						}
					}
				}
				try {
					XMLParser.plugin = new File("plugins.xml");
					if (!XMLParser.plugin.exists()) {
						if (XMLParser.plugin.createNewFile()) {
							FileOutputStream fos = new FileOutputStream(XMLParser.plugin);
							fos.write(("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
									"<plugins>\n" +
									"   <plugin>\n" +
									"       <name>Demo</name>\n" +
									"       <jar>plugins/demo.jar</jar>\n" +
									"       <class>com.windowx.demo.Main</class>\n" +
									"   </plugin>\n" +
									"</plugins>").getBytes(StandardCharsets.UTF_8)
							); fos.flush(); fos.close();
						}
					}
					pluginList = XMLParser.getPluginList();
					pluginManager = new PluginManager(pluginList);
					for(Plugin plugin : pluginList) {
						try {
							PluginCore.plugin = plugin;
							JavaPlugin javaPlugin = pluginManager.getInstance(plugin.getClassName());
							LogUtil.log(ConfigUtil.getLanguage("enabling.plugin")
									.replaceAll("\\$1", plugin.getName())
							);
							javaPlugin.onEnable();
						} catch (Exception e) {
							LogUtil.log(ConfigUtil.getLanguage("failed.load.plugin")
									.replaceAll("\\$1", plugin.getName())
									.replaceAll("\\$2", e.toString())
							);
						}
					}
				} catch (Exception e) {
					LogUtil.log(ConfigUtil.getLanguage("unknown.error"));
					e.printStackTrace();
					System.exit(-1);
				}
			} catch (Exception e) {
				LogUtil.log(ConfigUtil.getLanguage("unknown.error"));
				e.printStackTrace();
				System.exit(-1);
			}
			
			if (groupId.isEmpty()) {
				LogUtil.log(ConfigUtil.getLanguage("not.group.set"));
			} else if (!bot.getGroups().contains(Long.parseLong(groupId))) {
				LogUtil.log(ConfigUtil.getLanguage("not.entered.group").replaceAll("\\$1", groupId));
			} else {
				group = bot.getGroupOrFail(Long.parseLong(ConfigUtil.getConfig("group")));
				LogUtil.log(ConfigUtil.getLanguage("now.group")
						.replaceAll("\\$1", group.getName())
						.replaceAll("\\$2", String.valueOf(group.getId())));
			}
			if (group == null) {
				LogUtil.log(ConfigUtil.getLanguage("unknown.error"));
				System.exit(-1);
			}
			
			InputStreamReader isr = new InputStreamReader(System.in);
			BufferedReader br = new BufferedReader(isr);
			do {
				String msg;
				msg = br.readLine();
				if (msg.length() <= 0) {
					System.out.print("> ");
					continue;
				}
				if (msg.equals("stop")) {
					LogUtil.log(ConfigUtil.getLanguage("stopping.bot")
							.replaceAll("\\$1", bot.getNick())
							.replaceAll("\\$2", String.valueOf(bot.getId())));
					for (Plugin plugin : pluginList) {
						try {
							PluginCore.plugin = plugin;
							JavaPlugin javaPlugin = pluginManager.getInstance(plugin.getClassName());
							javaPlugin.onDisable();
						} catch (Exception e) {
							LogUtil.log(e.toString());
						}
					}
					bot.close();
					System.out.print("\n");
					System.exit(0);
				}
				if (!runCommand(msg)) {
					try {
						boolean send = true;
						for (Plugin plugin : pluginList) {
							try {
								PluginCore.plugin = plugin;
								JavaPlugin javaPlugin = pluginManager.getInstance(plugin.getClassName());
								send = javaPlugin.onCommand(msg);
							} catch (Exception e) {
								LogUtil.log(e.toString());
							}
						}
						if (send) group.sendMessage(MiraiCode.deserializeMiraiCode(msg));
					} catch (BotIsBeingMutedException e) {
						LogUtil.log(ConfigUtil.getLanguage("bot.is.being.muted"));
					}
				}
			} while (true);
		} catch (NumberFormatException e) {
			LogUtil.log(ConfigUtil.getLanguage("qq.password.error"));
			e.printStackTrace();
			System.exit(-1);
		} catch (Exception e) {
			LogUtil.log(ConfigUtil.getLanguage("unknown.error"));
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	/**
	 * Execute an instruction.
	 * @param msg Message
	 * @throws Exception Exception
	 * @return Is it an instruction
	 */
	public static boolean runCommand(String msg) throws Exception {
		String[] cmd = msg.split(" ");
		switch (cmd[0]) {
			case "reload":
				ConfigUtil.init();
				loadAutoRespond();
				LogUtil.log(ConfigUtil.getLanguage("reloaded"));
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
					LogUtil.log(ConfigUtil.getLanguage("usage") + ": language <" + ConfigUtil.getLanguage("language") + ">");
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
						
						"mute <" + ConfigUtil.getLanguage("qq") + "> <" + ConfigUtil.getLanguage("time") + ">\n" +
						" - " + ConfigUtil.getLanguage("command.mute") + "\n" +
						
						"nameCard <" + ConfigUtil.getLanguage("qq") + "> <" + ConfigUtil.getLanguage("name.card") + ">" + ">\n" +
						" - " + ConfigUtil.getLanguage("command.name.card") + "\n" +
						
						"newImg <" + ConfigUtil.getLanguage("width") + "> <" + ConfigUtil.getLanguage("height") + "> <" +
						ConfigUtil.getLanguage("font.size") + "> <" + ConfigUtil.getLanguage("contents") + ">\n" +
						" - " + ConfigUtil.getLanguage("command.new.img") + "\n" +
						
						"reply <" + ConfigUtil.getLanguage("message.id") + "> <" + ConfigUtil.getLanguage("contents") + ">\n" +
						" - " + ConfigUtil.getLanguage("command.reply") + "\n" +
						
						"recall <" + ConfigUtil.getLanguage("message.id") + ">\n" +
						" - " + ConfigUtil.getLanguage("command.recall") + "\n" +
						
						"send <" + ConfigUtil.getLanguage("qq") + "> <" + ConfigUtil.getLanguage("contents") + ">\n" +
						" - " + ConfigUtil.getLanguage("command.send") + "\n" +
						
						"stop\n" +
						" - " + ConfigUtil.getLanguage("command.stop") + "\n" +
						
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
						LogUtil.log(ConfigUtil.getLanguage("not.qq")
								.replaceAll("\\$1", cmd[1]));
					}
				} else {
					LogUtil.log(ConfigUtil.getLanguage("usage") + ": send <" +
							ConfigUtil.getLanguage("qq") +
							"> <" + ConfigUtil.getLanguage("contents") + ">");
				}
				return true;
			case "kick":
				if (cmd.length > 2) {
					try {
						NormalMember member = group.get(Long.parseLong(cmd[1]));
						if (member != null) {
							if (group.getBotPermission() != MemberPermission.MEMBER && member.getPermission() != MemberPermission.OWNER) {
								member.kick(cmd[2]);
								LogUtil.log(ConfigUtil.getLanguage("kicked"));
							} else {
								LogUtil.log(ConfigUtil.getLanguage("no.permission"));
							}
						} else {
							LogUtil.log(ConfigUtil.getLanguage("not.user"));
						}
					} catch (NumberFormatException e) {
						LogUtil.log(ConfigUtil.getLanguage("not.qq")
								.replaceAll("\\$1", cmd[1]));
					}
				} else {
					LogUtil.log(ConfigUtil.getLanguage("usage") + ": kick <" + ConfigUtil.getLanguage("qq") + "> <" +
							ConfigUtil.getLanguage("reason") + ">");
				}
				return true;
			case "mute":
				if (cmd.length > 2) {
					try {
						NormalMember member = group.get(Long.parseLong(cmd[1]));
						if (member != null) {
							member.mute(Integer.parseInt(cmd[1]));
						} else {
							LogUtil.log(ConfigUtil.getLanguage("not.user"));
						}
					} catch (NumberFormatException e) {
						LogUtil.log(ConfigUtil.getLanguage("not.qq")
								.replaceAll("\\$1", cmd[1]));
						if (ConfigUtil.getConfig("debug").equals("true")) LogUtil.log(e.toString());
					} catch (PermissionDeniedException e) {
						LogUtil.log(ConfigUtil.getLanguage("no.permission"));
						if (ConfigUtil.getConfig("debug").equals("true")) LogUtil.log(e.toString());
					}
				} else {
					LogUtil.log(ConfigUtil.getLanguage("usage") + ": mute <" + ConfigUtil.getLanguage("qq") + "> <" +
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
							LogUtil.log(ConfigUtil.getLanguage("not.user"));
						}
					} catch (NumberFormatException e) {
						LogUtil.log(ConfigUtil.getLanguage("not.qq")
								.replaceAll("\\$1", cmd[1]));
						if (ConfigUtil.getConfig("debug").equals("true")) LogUtil.log(e.toString());
					}
				} else {
					LogUtil.log(ConfigUtil.getLanguage("usage") + ": avatar <" + ConfigUtil.getLanguage("qq") + ">");
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
						LogUtil.log(bot.getNick() + " : " +
								(ConfigUtil.getConfig("debug").equals("true") ? img.serializeToMiraiCode() : img.contentToString()));
						externalResource.close();
						imageInfo(bot, img);
					} catch (IOException e) {
						LogUtil.log(ConfigUtil.getLanguage("file.error"));
						if (ConfigUtil.getConfig("debug").equals("true")) LogUtil.log(e.toString());
					}
				} else {
					LogUtil.log(ConfigUtil.getLanguage("usage") + ": image <" +
							ConfigUtil.getLanguage("file.path") + ">");
				}
				return true;
			case "imageInfo":
				if (cmd.length > 1) {
					try {
						Image image = Image.fromId(cmd[1]);
						imageInfo(bot, image);
					} catch (IllegalArgumentException e) {
						LogUtil.log(e.toString());
					}
				} else {
					LogUtil.log(ConfigUtil.getLanguage("usage") + ": imageInfo <" +
							ConfigUtil.getLanguage("image.id") + ">");
				}
				return true;
			case "upImg":
				if (cmd.length > 1) {
					File file = new File(msg.substring(6));
					ExternalResource externalResource = ExternalResource.create(file);
					Image img = group
							.uploadImage(externalResource);
					externalResource.close();
					imageInfo(bot, img);
				} else {
					LogUtil.log(ConfigUtil.getLanguage("usage") + ": upImg <" +
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
					LogUtil.log(ConfigUtil.getLanguage("failed.clipboard"));
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
					} catch (NumberFormatException e) {
						LogUtil.log(ConfigUtil.getLanguage("width.height.error"));
						if (ConfigUtil.getConfig("debug").equals("true")) LogUtil.log(e.toString());
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
							LogUtil.log(ConfigUtil.getLanguage("deleted.friend")
									.replaceAll("\\$1", friend.getNick())
									.replaceAll("\\$2", String.valueOf(friend.getId())));
						} else {
							LogUtil.log(ConfigUtil.getLanguage("not.friend"));
						}
					} catch (NumberFormatException e) {
						LogUtil.log(ConfigUtil.getLanguage("not.qq")
								.replaceAll("\\$1", cmd[1]));
						if (ConfigUtil.getConfig("debug").equals("true")) LogUtil.log(e.toString());
					}
				} else {
					LogUtil.log(ConfigUtil.getLanguage("usage") + ": del <QQ>");
				}
				return true;
			case "reply":
				if (cmd.length > 2) {
					try {
						MessageSource message = EventListener.messages.get(Integer.parseInt(cmd[1]) - 1);
						StringBuilder content = new StringBuilder();
						for (int i = 2; i < cmd.length; i++) {
							content.append(cmd[i]);
						}
						if (message != null) {
							group.sendMessage(new QuoteReply(message).plus(MiraiCode.deserializeMiraiCode(content.toString())));
						} else {
							LogUtil.log(ConfigUtil.getLanguage("message.not.found"));
						}
					} catch (NumberFormatException e) {
						LogUtil.log(ConfigUtil.getLanguage("message.id.error"));
						if (ConfigUtil.getConfig("debug").equals("true")) LogUtil.log(e.toString());
					}
				} else {
					LogUtil.log(ConfigUtil.getLanguage("usage") + ": reply <" +
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
									LogUtil.log(ConfigUtil.getLanguage("failed.accept"));
								}
							} else {
								LogUtil.log(ConfigUtil.getLanguage("request.not.found"));
							}
						} else {
							LogUtil.log(ConfigUtil.getLanguage("request.not.found"));
						}
					} catch (NumberFormatException e) {
						LogUtil.log(ConfigUtil.getLanguage("request.id.error"));
						if (ConfigUtil.getConfig("debug").equals("true")) LogUtil.log(e.toString());
					}
				}
				return true;
			case "nameCard":
				if (cmd.length > 2) {
					try {
						NormalMember member = group.get(Long.parseLong(cmd[1]));
						if (member != null) {
							StringBuilder nameCard = new StringBuilder();
							for (int i = 2 ; i < cmd.length ; i ++) {
								nameCard.append(cmd[i]);
								if (i != cmd.length - 1) {
									nameCard.append(" ");
								}
							}
							member.setNameCard(nameCard.toString());
							LogUtil.log(ConfigUtil.getLanguage("name.card.set")
									.replaceAll("\\$1", member.getNick())
									.replaceAll("\\$2", nameCard.toString())
							);
						} else {
							LogUtil.log(ConfigUtil.getLanguage("not.user"));
						}
					} catch (NumberFormatException e) {
						LogUtil.log(ConfigUtil.getLanguage("not.qq").replaceAll("\\$1", cmd[1]));
						if (ConfigUtil.getConfig("debug").equals("true")) LogUtil.log(e.toString());
					} catch (PermissionDeniedException e) {
						LogUtil.log(ConfigUtil.getLanguage("no.permission"));
						if (ConfigUtil.getConfig("debug").equals("true")) LogUtil.log(e.toString());
					}
				} else {
					LogUtil.log(ConfigUtil.getLanguage("usage") + ": nameCard <" +
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
										LogUtil.log(ConfigUtil.getLanguage("failed.recall"));
									}
								} else {
									try {
										Mirai.getInstance().recallMessage(bot, message);
										LogUtil.log(ConfigUtil.getLanguage("recalled"));
									} catch (PermissionDeniedException e) {
										LogUtil.log(ConfigUtil.getLanguage("no.permission"));
										if (ConfigUtil.getConfig("debug").equals("true")) LogUtil.log(e.toString());
									} catch (Exception e) {
										LogUtil.log(ConfigUtil.getLanguage("failed.recall"));
									}
								}
							} else {
								LogUtil.log(ConfigUtil.getLanguage("message.not.found"));
							}
						} else {
							LogUtil.log(ConfigUtil.getLanguage("message.not.found"));
						}
					} catch (NumberFormatException e) {
						LogUtil.log(ConfigUtil.getLanguage("message.id.error"));
						if (ConfigUtil.getConfig("debug").equals("true")) LogUtil.log(e.toString());
					}
				} else {
					LogUtil.log(ConfigUtil.getLanguage("usage") + ": recall <" +
							ConfigUtil.getLanguage("message.id") + ">");
				}
				return true;
			default:
				return false;
		}
	}
	/**
	 * Check if there is a new release
	 */
	public static void checkUpdate(String u) {
		try {
			URL update;
			update = new URL(Objects.requireNonNullElse(u, "https://raw.fastgit.org/1689295608/MiraiBot/main/LatestVersion"));
			InputStream is = update.openStream();
			BufferedInputStream bis = new BufferedInputStream(is);
			String LatestVersion = new String(bis.readAllBytes()).replaceAll("\\n", "");
			URL url = ClassLoader.getSystemResource("Version");
			String version = "0.0.0";
			if (url != null) {
				version = new String(url.openStream().readAllBytes());
			}
			try {
				int nowV = Integer.parseInt(version.replaceAll("[^0-9]", ""));
				int newV = Integer.parseInt(LatestVersion.replaceAll("[^0-9]", ""));
				if (nowV < newV) {
					LogUtil.log(ConfigUtil.getLanguage("found.new.update")
							.replaceAll("\\$1", "https://github.com/1689295608/MiraiBot/releases/tag/" + LatestVersion));
				} else if (nowV == newV) {
					LogUtil.log(ConfigUtil.getLanguage("already.latest.version").replaceAll("\\$1", LatestVersion));
				} else {
					LogUtil.log(ConfigUtil.getLanguage("too.new.version"));
				}
			} catch (Exception e) {
				LogUtil.log(ConfigUtil.getLanguage("failed.check.update").replaceAll("\\$1", e.toString()));
				if (ConfigUtil.getConfig("debug").equals("true")) LogUtil.log(e.toString());
			}
		} catch (Exception e) {
			LogUtil.log(ConfigUtil.getLanguage("failed.check.update").replaceAll("\\$1", e.toString()));
			if (ConfigUtil.getConfig("debug").equals("true")) LogUtil.log(e.toString());
		}
	}
	
	public static void loadAutoRespond() {
		EventListener.autoRespond = new File("AutoRespond.json");
		if (!EventListener.autoRespond.exists()) {
			try {
				if (!EventListener.autoRespond.createNewFile()) {
					LogUtil.log(ConfigUtil.getLanguage("failed.create.config"));
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
				LogUtil.log(ConfigUtil.getLanguage("failed.create.config"));
				e.printStackTrace();
				System.exit(-1);
			}
		}
		try {
			EventListener.autoRespondConfig = new JSONObject(new String(new FileInputStream(EventListener.autoRespond).readAllBytes()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Output image info
	 * @param bot Bot
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
	 * Check config file
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
									"# 输入你要聊天的聊群\n" +
									"group=" + group + "\n" +
									"# 机器人主人 QQ 号, 即拥有一切权力的 QQ 号. 用 \",\" 分隔\n" +
									"owner=00000\n" +
									"# 每一个新消息是否都显示发送者的 QQ 号\n" +
									"showQQ=false\n" +
									"# 输入你接收的好友信息（“*” 为 全部）\n" +
									"friend=*\n" +
									"# 每次启动时都检测更新\n" +
									"checkUpdate=" + checkUpdate + "\n" +
									"# 输入使用“newImg”指令生成的字体\n" +
									"font=微软雅黑\n" +
									"# 使用的登录协议（PAD: 平板，WATCH: 手表，PHONE: 手机），默认 PHONE\n" +
									"protocol=PHONE\n" +
									"# 是否启用 Debug 模式（即显示 MiraiCode）\n" +
									"debug=false\n" +
									"\n" +
									"# ----=== MiraiBot ===----\n" +
									"# 使用“help”获取帮助！\n" +
									"# -----------------------------\n";
					fos.write(config.getBytes());
					fos.flush();
					fos.close();
					LogUtil.log(ConfigUtil.getLanguage("please.restart"));
					System.exit(0);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return false;
		}
		return true;
	}
}
