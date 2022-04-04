package com.windowx.miraibot;

import com.windowx.miraibot.plugin.Plugin;
import com.windowx.miraibot.plugin.PluginLoader;
import com.windowx.miraibot.utils.*;
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
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;

public class PluginMain {
	public static final String language = Locale.getDefault().getLanguage();
	public static Group group;
	public static String[] groups;
	public static String[] allowedGroups;
	public static Bot bot;
	public static PluginLoader loader = new PluginLoader();
	public static ArrayList<String> completes = new ArrayList<>(Arrays.asList(
			"accept-request", "accept-invite", "avatar", "checkUpdate", "del", "friendList", "memberList", "help", "image", "imageInfo", "kick", "language", "load",
			"music", "mute", "nameCard", "newImg", "plugins", "reload", "reply", "recall", "send", "stop", "unload", "upClipImg", "upImg")
	);
	public static boolean running;
	public static Logger logger = new Logger();

	public static void main(String[] args) {
		String err = language.equals("zh") ? "出现错误！进程即将终止！" : (language.equals("tw") ? "出現錯誤！進程即將終止！" : "Unable to create configuration file!");
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
							"""
									# 使用本软件，您必须遵守我们的协议，一切基于本软件开发的插件都必须在项目明显位置准确提及来自 MiraiBot，不得扭曲或隐藏免费且开源的事实。
									# To use this software, you must abide by our agreement. All plug-ins developed based on this software must accurately mention MiraiBot in the obvious place of the project, and must not distort or hide the fact that it is free and open source
									# 详情请查看：https://github.com/1689295608/MiraiBot/blob/main/LICENSE
									# Details: https://github.com/1689295608/MiraiBot/blob/main/LICENSE
									eula=false"""
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
			System.out.println();
			e.printStackTrace();
			System.exit(-1);
		}
		ConfigUtil.init();
		LogUtil.ansiColor = Boolean.parseBoolean(ConfigUtil.getConfig("ansi-console"));

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
			logger.error(language("qq.password.not.exits"));
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
						completes.addAll(List.of(p.getCommands()));
					} catch (Exception e) {
						p.setEnabled(false);
						logger.error(language("failed.load.plugin"), p.getName(), e.toString());
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				logger.error(language("unknown.error"));
				e.printStackTrace();
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
					logger.error(language("bot.is.being.muted"));
				}
			}
		} catch (NumberFormatException e) {
			logger.error(language("qq.password.error"));
			e.printStackTrace();
			System.exit(-1);
		} catch (UserInterruptException | EndOfFileException e) {
			System.exit(0);
		} catch (Exception e) {
			logger.error(language("unknown.error"));
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
					logger.info(language("reloaded"));
				} else {
					try {
						Plugin plugin = loader.getPlugin(cmd[1]);
						if (plugin != null) {
							loader.unloadPlugin(cmd[1]);
							loader.loadPlugin(plugin.getFile(), plugin.getName());
						} else {
							logger.info(language("unloading.plugin"), cmd[1]);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				return true;
			case "stop":
				logger.warn(language("stopping.bot"), bot.getNick(), String.valueOf(bot.getId()));
				running = false;
				for (Plugin p : loader.plugins) {
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
				logger.info(out.toString());
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
				logger.info(out.toString());
				return true;
			}
			case "plugins":
				StringBuilder out = new StringBuilder();
				int c = 1;
				for (Plugin p : loader.plugins) {
					if (!p.isEnabled()) continue;
					out.append(c).append(". ").append(p.getName()).append(" v").append(p.getVersion()).append(" by ").append(p.getOwner()).append("\n");
					c++;
				}
				logger.info(out.toString());
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
					logger.info(language("success.change.language"));
				} else {
					logger.warn(language("usage") + ": language <" + language("language") + ">");
				}
				return true;
			case "clear":
				logger.clear();
				EventListener.messages = new ArrayList<>();
				logger.info(language("console.cleared"));
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
				logger.info(help);
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
							logger.info(language("not.friend"));
						}
					} catch (NumberFormatException e) {
						logger.info(language("not.qq"), cmd[1]);
					}
				} else {
					logger.warn(language("usage") + ": send <" +
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
				} else {
					logger.warn(language("usage") + ": kick <" + language("qq") + "> <" +
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
				} else {
					logger.warn(language("usage") + ": nudge <" + language("qq") + ">");
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
				} else {
					logger.warn(language("usage") + ": mute <" + language("qq") + "> <" +
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
				} else {
					logger.warn(language("usage") + ": avatar <" + language("qq") + ">");
				}
				return true;
			case "voice":
				if (cmd.length > 1) {
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
				} else {
					logger.warn(language("usage") + ": voice <" +
							language("file.path") + ">");
				}
				return true;
			case "image":
				if (cmd.length > 1) {
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
				} else {
					logger.warn(language("usage") + ": image <" +
							language("file.path") + ">");
				}
				return true;
			case "imageInfo":
				if (cmd.length > 1) {
					try {
						Image image = Image.fromId(cmd[1]);
						imageInfo(bot, image);
					} catch (IllegalArgumentException e) {
						logger.error(e.toString());
					}
				} else {
					logger.warn(language("usage") + ": imageInfo <" +
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
					logger.warn(language("usage") + ": upImg <" +
							language("file.path") + ">");
				}
				return true;
			case "upClipImg":
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
				return true;
			case "newImg":
				if (cmd.length > 4) {
					try {
						StringBuilder content = new StringBuilder();
						for (int i = 4; i < cmd.length; i++) {
							content.append(cmd[i]).append(i == cmd.length - 1 ? "" : " ");
						}
						logger.info(language("creating.word.image"));
						byte[] file = WordToImage.createImage(content.toString(),
								new Font(ConfigUtil.getConfig("font"), Font.PLAIN, Integer.parseInt(cmd[3])),
								Integer.parseInt(cmd[1]), Integer.parseInt(cmd[2]));
						ExternalResource externalResource = ExternalResource.create(file);
						logger.info(language("up.loading.img"));
						Image img = group.uploadImage(externalResource);
						externalResource.close();
						imageInfo(bot, img);
						group.sendMessage(img);
					} catch (NumberFormatException e) {
						logger.warn(language("width.height.error"));
						if (ConfigUtil.getConfig("debug").equals("true")) logger.warn(e.toString());
					}
				} else {
					logger.info(language("usage") +
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
							logger.info(language("deleted.friend"), friend.getNick(), String.valueOf(friend.getId()));
						} else {
							logger.error(language("not.friend"));
						}
					} catch (NumberFormatException e) {
						logger.error(language("not.qq"), cmd[1]);
						if (ConfigUtil.getConfig("debug").equals("true")) logger.error(e.toString());
					}
				} else {
					logger.warn(language("usage") + ": del <QQ>");
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
							logger.error(language("message.not.found"));
						}
					} catch (NumberFormatException e) {
						logger.error(language("message.id.error"));
						if (ConfigUtil.getConfig("debug").equals("true")) logger.error(e.toString());
					}
				} else {
					logger.warn(language("usage") + ": reply <" +
							language("message.id") + "> <" +
							language("contents") + ">");
				}
				return true;
			case "checkUpdate":
				logger.info(language("checking.update"));
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
									logger.info(language("request.accepted"));
									EventListener.joinRequest.remove(request);
								} catch (Exception e) {
									logger.error(language("failed.accept.request"));
								}
							} else {
								logger.error(language("request.not.found"));
							}
						} else {
							logger.error(language("request.not.found"));
						}
					} catch (NumberFormatException e) {
						logger.error(language("request.id.error"));
						if (ConfigUtil.getConfig("debug").equals("true")) logger.error(e.toString());
					}
				} else {
					logger.warn(language("usage") + ": accept-request <" + language("request.id") + ">");
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
									logger.info(language("invite.accepted"));
								} catch (Exception e) {
									logger.error(language("failed.accept.invite"));
								}
							} else {
								logger.error(language("invite.not.found"));
							}
						} else {
							logger.error(language("invite.not.found"));
						}
					} catch (NumberFormatException e) {
						logger.error(language("invite.id.error"));
						if (ConfigUtil.getConfig("debug").equals("true")) logger.error(e.toString());
					}
				} else {
					logger.warn(language("usage") + ": accept-invite <" + language("invite.id") + ">");
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
							logger.info(language("name.card.set"), member.getNick(), nameCard.toString());
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
				} else {
					logger.warn(language("usage") + ": nameCard <" +
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
							} else {
								logger.error(language("message.not.found"));
							}
						} else {
							logger.error(language("message.not.found"));
						}
					} catch (NumberFormatException e) {
						logger.error(language("message.id.error"));
						if (ConfigUtil.getConfig("debug").equals("true")) logger.error(e.toString());
					}
				} else {
					logger.warn(language("usage") + ": recall <" +
							language("message.id") + ">");
				}
				return true;
			case "group":
				if (cmd.length > 1) {
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
							logger.info(language("now.group"), group.getName(), String.valueOf(group.getId()));
						} else {
							logger.error(language("not.entered.group"), g);
						}
					}
				} else {
					logger.warn(language("usage") + ": group <" +
							language("group") + language("id") + ">");
				}
				return true;
			case "unload":
				if (cmd.length > 1) {
					loader.unloadPlugin(cmd[1]);
				} else {
					logger.warn(language("usage") + ": unload <" +
							language("plugin.name") + ">");
				}
				return true;
			case "load":
				if (cmd.length > 1) {
					File f = new File("plugins/" + (cmd[1].endsWith(".jar") || cmd[1].endsWith(".class") ? cmd[1] : cmd[1] + ".jar"));
					loader.loadPlugin(f, cmd[1]);
				} else {
					logger.warn(language("usage") + ": load <" +
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
				} else {
					logger.warn(language("usage") + ": music <" + language("music.id") + "> [" + language("contact") + "]");
				}
				return true;
			case "dice":
				if (cmd.length > 1) {
					int value;
					try {
						value = Integer.parseInt(cmd[1]);
					} catch (NumberFormatException exception) {
						logger.error(language("dice.not.number"));
						return true;
					}
					if (value > 0 && value <= 6) {
						Dice dice = new Dice(value);
						group.sendMessage(dice);
					} else {
						logger.error(language("dice.value.error"));
					}
				} else {
					logger.warn(language("usage") + ": dice <" + language("dice.value") + ">");
				}
				return true;
			default:
				boolean isCmd = false;
				for (Plugin p : loader.plugins) {
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
					logger.warn(language("found.new.update"), "https://github.com/1689295608/MiraiBot/releases/tag/" + LatestVersion);
				} else {
					logger.info(language("already.latest.version"), LatestVersion);
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
			return EventListener.messages.get(id - 1);
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
				Scanner scanner = new Scanner(System.in);
				logger.info(language("before.settings"));
				logger.info(language("please.input.qq"));
				String qq = scanner.nextLine();
				logger.info(language("please.input.password"));
				String password = scanner.nextLine();
				logger.info(language("please.input.group.id"));
				String groups = scanner.nextLine();
				logger.info(language("please.input.check.update.on.setup"));
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
				logger.warn(language("please.restart"));
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
