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

import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Scanner;

public class PluginMain {
	public static Group group;
	public static final String language = Locale.getDefault().getLanguage();
	
	public static void main(String[] args) {
		LogUtil.init();
		try {
			String version = new String(PluginMain.class.getResourceAsStream("Version").readAllBytes());
			LogUtil.log(language.equals("zh") ?
					"MiraiBot " + version + " 基于 Mirai-Core. 版权所有 (C) WindowX 2021" : "MiraiBot " + version + " based Mirai-Core. Copyright (C) WindowX 2021");
		} catch (Exception e) {
			e.printStackTrace();
		}
		File languageFile = new File("language.properties");
		try {
			if (!languageFile.exists()) {
				if (!languageFile.createNewFile()) {
					LogUtil.log(language.equals("zh") ? "无法创建配置文件！" : "Unable to create configuration file!");
				} else {
					FileOutputStream fos = new FileOutputStream(languageFile);
					fos.write(LanguageUtil.languageFile(language));
					fos.flush();
					fos.close();
				}
			}
		} catch (IOException e) {
			LogUtil.log(ConfigUtil.getLanguage("unknown.error"));
			e.printStackTrace();
			System.exit(-1);
		}
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
			EventListener.showQQ = ConfigUtil.getConfig("showQQ").equals("true");
		} else {
			EventListener.showQQ = false;
		}
		if (qq.isEmpty() || password.isEmpty()) {
			LogUtil.log(ConfigUtil.getLanguage("qq.password.not.exits"));
			System.exit(-1);
			return;
		}
		EventListener.autoRespond = new File("AutoRespond.ini");
		if (!EventListener.autoRespond.exists()) {
			try {
				if (!EventListener.autoRespond.createNewFile()) {
					LogUtil.log(ConfigUtil.getLanguage("failed.create.config"));
					System.exit(-1);
				}
				FileOutputStream fos = new FileOutputStream(EventListener.autoRespond);
				fos.write(PluginMain.class.getResourceAsStream("AutoRespond.ini").readAllBytes());
				fos.flush();
				fos.close();
			} catch (IOException e) {
				LogUtil.log(ConfigUtil.getLanguage("failed.create.config"));
				e.printStackTrace();
				System.exit(-1);
			}
		}
		IniUtil.loadData(EventListener.autoRespond);
		
		String protocol = ConfigUtil.getConfig("protocol") != null ?
				ConfigUtil.getConfig("protocol") : "";
		LogUtil.log(ConfigUtil.getLanguage("trying.login").replaceAll("\\$1", protocol));
		try {
			BotConfiguration.MiraiProtocol miraiProtocol;
			if (protocol.equals("PAD")) {
				miraiProtocol = BotConfiguration.MiraiProtocol.ANDROID_PAD;
			} else if (protocol.equals("WATCH")) {
				miraiProtocol = BotConfiguration.MiraiProtocol.ANDROID_WATCH;
			} else {
				miraiProtocol = BotConfiguration.MiraiProtocol.ANDROID_PHONE;
			}
			Bot bot = BotFactory.INSTANCE.newBot(Long.parseLong(qq), password, new BotConfiguration() {{
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
			group = null;
			if (groupId.isEmpty()) {
				LogUtil.log(ConfigUtil.getLanguage("not.group.set"));
			} else if (!inGroup(bot, Long.parseLong(groupId))) {
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
			Scanner scanner = new Scanner(System.in);
			command: while (true) {
				String msg;
				if (!scanner.hasNextLine()) continue;
				msg = scanner.nextLine();
				LogUtil.log("> " + msg);
				String[] cmd = msg.split(" ");
				if (msg.length() <= 0) continue;
				switch (cmd[0]) {
					case "stop":
						LogUtil.log(ConfigUtil.getLanguage("stopping.bot")
								.replaceAll("\\$1", bot.getNick())
								.replaceAll("\\$2", String.valueOf(bot.getId())));
						scanner.close();
						bot.close();
						System.out.println();
						System.exit(0);
						break command;
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
						break;
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
						break;
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
							fos.flush();
							fos.close();
							LogUtil.log(ConfigUtil.getLanguage("success.change.language"));
						} else {
							LogUtil.log(ConfigUtil.getLanguage("usage") + ": language <" +
									ConfigUtil.getLanguage("language") + ">");
						}
						break;
					case "clear":
						LogUtil.clear();
						LogUtil.messages = new StringBuilder();
						EventListener.messages = new ArrayList<>();
						LogUtil.log(ConfigUtil.getLanguage("console.cleared"));
						break;
					case "help":
						String help = "· --------====== MiraiBot ======-------- ·\n" +
								"accept <" + ConfigUtil.getLanguage("request.id") + ">\n" +
								" - " + ConfigUtil.getLanguage("command.accept") + "\n" +
								"avatar <" + ConfigUtil.getLanguage("qq") + ">\n" +
								" - " + ConfigUtil.getLanguage("command.avatar") + "\n" +
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
								"kick <" + ConfigUtil.getLanguage("qq") + "> <" + ConfigUtil.getLanguage("reason") + ">\n" +
								" - " + ConfigUtil.getLanguage("command.avatar") + "\n" +
								"language <" + ConfigUtil.getLanguage("language") + ">\n" +
								" - " + ConfigUtil.getLanguage("command.language") + "\n" +
								"newImg <" + ConfigUtil.getLanguage("width") + "> <" + ConfigUtil.getLanguage("height") + "> <" +
								ConfigUtil.getLanguage("font.size") + "> <" + ConfigUtil.getLanguage("contents") + ">" +
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
						break;
					case "send":
						if (cmd.length > 2) {
							StringBuilder tmp = new StringBuilder();
							for (int i = 2; i < cmd.length; i++) {
								tmp.append(cmd[i]).append(i == cmd.length - 1 ? "" : " ");
							}
							try { // String 转换到 Integer 如果不是数字居然还会抛出错误，气死我了！！
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
						break;
					case "kick":
						if (cmd.length > 2) {
							try {
								NormalMember member = null;
								for (NormalMember m : group.getMembers()) {
									if (String.valueOf(m.getId()).equals(cmd[1])) {
										member = m;
									}
								}
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
						break;
					case "avatar":
						if (cmd.length > 1) {
							try {
								Stranger stranger = bot.getStranger(Integer.parseInt(cmd[1]));
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
							}
						} else {
							LogUtil.log(ConfigUtil.getLanguage("usage") + ": avatar <" + ConfigUtil.getLanguage("qq") + ">");
						}
						break;
						/*
					case "nudge":
						if (cmd.length > 1) {
							try {
								Friend friend = bot.getFriend(Long.parseLong(cmd[1]));
								if (friend != null) {
									friend.nudge().sendTo(friend);
								} else {
									LogUtil.log(ConfigUtil.getLanguage("not.friend"));
								}
							} catch (NumberFormatException e) {
								LogUtil.log(ConfigUtil.getLanguage("not.qq"));
							}
						}
						break;
						
						暂不使用，等以后再测试
						
						 */
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
							}
						} else {
							LogUtil.log(ConfigUtil.getLanguage("usage") + ": image <" +
									ConfigUtil.getLanguage("file.path") + ">");
						}
						break;
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
						break;
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
						break;
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
							}
						} else {
							LogUtil.log(ConfigUtil.getLanguage("usage") +
									": newImg <" + ConfigUtil.getLanguage("width") +
									"> <" + ConfigUtil.getLanguage("height") +
									"> <" + ConfigUtil.getLanguage("font.size") +
									"> <" + ConfigUtil.getLanguage("contents") +
									">");
						}
						break;
					case "del":
						if (cmd.length > 1) {
							try {
								Friend friend = bot.getFriend(Long.parseLong(cmd[1]));
								if (friend != null) {
									friend.delete();
									LogUtil.log(ConfigUtil.getLanguage("delete.friend")
											.replaceAll("\\$1", friend.getNick())
											.replaceAll("\\$2", String.valueOf(friend.getId())));
								} else {
									LogUtil.log(ConfigUtil.getLanguage("not.friend"));
								}
							} catch (NumberFormatException e) {
								LogUtil.log(ConfigUtil.getLanguage("not.qq")
										.replaceAll("\\$1", cmd[1]));
							}
						} else {
							LogUtil.log(ConfigUtil.getLanguage("usage") + ": del <QQ>");
						}
						break;
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
							}
						} else {
							LogUtil.log(ConfigUtil.getLanguage("usage") + ": reply <" +
									ConfigUtil.getLanguage("message.id") + "> <" +
									ConfigUtil.getLanguage("contents") + ">");
						}
						break;
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
							}
						}
						break;
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
											if (group.getBotPermission() != MemberPermission.MEMBER) {
												try {
													Mirai.getInstance().recallMessage(bot, message);
													LogUtil.log(ConfigUtil.getLanguage("recalled"));
												} catch (Exception e) {
													LogUtil.log(ConfigUtil.getLanguage("failed.recall"));
												}
											} else {
												LogUtil.log(ConfigUtil.getLanguage("no.permission"));
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
							}
						} else {
							LogUtil.log(ConfigUtil.getLanguage("usage") + ": recall <" +
									ConfigUtil.getLanguage("message.id") + ">");
						}
						break;
					default:
						group.sendMessage(MiraiCode.deserializeMiraiCode(msg));
						break;
				}
			}
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
	 * Check if there is a new release
	 * @param u URL
	 */
	public static void checkUpdate(String u) {
		try {
			HttpURLConnection connection;
			URL url;
			if (u == null) {
				try {
					url = new URL("https://raw.githubusercontent.com/1689295608/MiraiBot/main/LatestVersion");
					connection = getHttpURLConnection(url);
					connection.connect();
				} catch (IOException e) {
					url = new URL("https://ghproxy.com/https://raw.githubusercontent.com/1689295608/MiraiBot/main/LatestVersion");
					connection = getHttpURLConnection(url);
					connection.connect();
				}
			} else {
				url = new URL(u);
				connection = getHttpURLConnection(url);
				connection.connect();
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = in.readLine()) != null)
				sb.append(line);
			int LatestVersion = Integer.parseInt(sb.toString().replaceAll("\\.", ""));
			/* 注：这里的 ”Version“ 文件是打包时放入的当前版本文件，并未更新于 Github，特此申明！ */
			int ThisVersion = Integer.parseInt(new String(PluginMain.class.getResourceAsStream("Version").readAllBytes()).replaceAll("\\.", ""));
			if (ThisVersion < LatestVersion) {
				LogUtil.log(ConfigUtil.getLanguage("found.new.update")
						.replaceAll("\\$1", "https://github.com/1689295608/MiraiBot/releases/tag/" + sb)
				);
			} else {
				LogUtil.log(ConfigUtil.getLanguage("already.latest.version").replaceAll("\\$1", sb.toString()));
			}
		} catch (Exception e) {
			if (e.getMessage().equals("Read timed out")) {
				checkUpdate("https://ghproxy.com/https://raw.githubusercontent.com/1689295608/MiraiBot/main/LatestVersion");
			} else {
				LogUtil.log(ConfigUtil.getLanguage("failed.check.update")
						.replaceAll("\\$1", e.toString())
				);
			}
		}
	}
	
	/**
	 * Quickly get an HttpURLConnection through the URL object
	 * @param url URL
	 * @return HttpURLConnection
	 * @throws IOException IOException
	 */
	public static HttpURLConnection getHttpURLConnection(URL url) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setConnectTimeout(3000);
		connection.setReadTimeout(5000);
		return connection;
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
	 * Whether the robot is in the group
	 * @param bot Bot
	 * @param groupId Group ID
	 * @return Whether the robot is in the group
	 */
	public static boolean inGroup(Bot bot, Long groupId) {
		ContactList<Group> groups = bot.getGroups();
		for (Group g : groups) {
			if (g.getId() == groupId) {
				return true;
			}
		}
		return false;
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
					System.exit(-1);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return false;
		}
		return true;
	}
}
