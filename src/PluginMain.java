import net.mamoe.mirai.Bot;
import net.mamoe.mirai.BotFactory;
import net.mamoe.mirai.Mirai;
import net.mamoe.mirai.contact.*;
import net.mamoe.mirai.event.GlobalEventChannel;
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
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class PluginMain {
	public static Group group;
	
	public static void main(String[] args) {
		LogUtil.init();
		LogUtil.messages.append(Locale.getDefault().getLanguage().equals("zh") ?
				"MiraiBot 基于 Mirai-Core. 版权所有 (C) WindowX 2021" : "MiraiBot based Mirai-Core. Copyright (C) WindowX 2021");
		File languageFile = new File("language.properties");
		try {
			if (!languageFile.exists()) {
				if (!languageFile.createNewFile()) {
					LogUtil.log(Locale.getDefault().getLanguage().equals("zh") ? "无法创建配置文件！" : "Unable to create configuration file!");
				} else {
					FileOutputStream fos = new FileOutputStream(languageFile);
					fos.write(LanguageUtil.languageFile(Locale.getDefault().getLanguage()));
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
			checkUpdate();
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
				new ProcessBuilder("cmd", "/c", "title", bot.getNick() + " (" + bot.getId() + ")").inheritIO().start().waitFor();
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
			
			command:
			while (true) {
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
						LogUtil.Exit();
						System.out.println();
						System.exit(0);
						break command;
					case "friendList": {
						ContactList<Friend> friends = bot.getFriends();
						StringBuilder out = new StringBuilder();
						int c = 1;
						for (Friend f : friends) {
							out.append(c).append(". ").append(f.getNick()).append(" (").append(f.getId()).append(")")
									.append((f.getId() == bot.getId() ? " (" +
											ConfigUtil.getLanguage("bot") +
											")\n" : "\n"));
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
					case "short":
						if (cmd.length > 1) {
							try {
								String API = ConfigUtil.getConfig("shortUrlApi");
								String Param = ConfigUtil.getConfig("shortUrlParam");
								if (API != null && Param != null) {
									LogUtil.log(sendPost(API, Param.replaceAll("\\$1", cmd[1]), null));
								} else {
									LogUtil.log(ConfigUtil.getLanguage("api.param.not.exits"));
								}
							} catch (IOException e) {
								LogUtil.log(ConfigUtil.getLanguage("failed.post.url"));
							}
						} else {
							LogUtil.log(ConfigUtil.getLanguage("usage") + ": short <" + ConfigUtil.getLanguage("url") + ">");
						}
						break;
					case "clear":
						LogUtil.clear();
						LogUtil.messages = new StringBuilder();
						EventListener.messages = new ArrayList<>();
						LogUtil.log("Console cleared");
						break;
					case "help":
						LogUtil.log("· --------====== MiraiBot ======-------- ·");
						LogUtil.log("stop");
						LogUtil.log(" - " + ConfigUtil.getLanguage("command.stop"));
						LogUtil.log("friendList");
						LogUtil.log(" - " + ConfigUtil.getLanguage("command.friend.list"));
						LogUtil.log("groupList");
						LogUtil.log(" - " + ConfigUtil.getLanguage("command.group.list"));
						LogUtil.log("help");
						LogUtil.log(" - " + ConfigUtil.getLanguage("command.help"));
						LogUtil.log("send <" + ConfigUtil.getLanguage("qq") + "> <" + ConfigUtil.getLanguage("contents") + ">");
						LogUtil.log(" - " + ConfigUtil.getLanguage("command.send"));
						LogUtil.log("reply <" + ConfigUtil.getLanguage("message.id") + "> <" + ConfigUtil.getLanguage("contents") + ">");
						LogUtil.log(" - " + ConfigUtil.getLanguage("command.reply"));
						LogUtil.log("recall <" + ConfigUtil.getLanguage("message.id") + ">");
						LogUtil.log(" - " + ConfigUtil.getLanguage("command.recall"));
						LogUtil.log("image <" + ConfigUtil.getLanguage("file.path") + ">");
						LogUtil.log(" - " + ConfigUtil.getLanguage("command.image"));
						LogUtil.log("upImg <" + ConfigUtil.getLanguage("file.path") + ">");
						LogUtil.log(" - " + ConfigUtil.getLanguage("command.up.img"));
						LogUtil.log("upClipImg");
						LogUtil.log(" - " + ConfigUtil.getLanguage("command.up.clip.img"));
						LogUtil.log("newImg <" + ConfigUtil.getLanguage("width") + "> <" + ConfigUtil.getLanguage("height") + "> <" +
								ConfigUtil.getLanguage("font.size") + "> <" + ConfigUtil.getLanguage("contents") + ">");
						LogUtil.log(" - " + ConfigUtil.getLanguage("command.new.img"));
						LogUtil.log("del <" + ConfigUtil.getLanguage("qq") + ">");
						LogUtil.log(" - " + ConfigUtil.getLanguage("command.del"));
						LogUtil.log("avatar <" + ConfigUtil.getLanguage("qq") + ">");
						LogUtil.log(" - " + ConfigUtil.getLanguage("command.avatar"));
						LogUtil.log("kick <" + ConfigUtil.getLanguage("qq") + "> <" + ConfigUtil.getLanguage("reason") + ">");
						LogUtil.log(" - " + ConfigUtil.getLanguage("command.avatar"));
						LogUtil.log("· -------------------------------------- ·");
						break;
					case "send":
						if (cmd.length >= 3) {
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
					case "image":
						if (cmd.length >= 2) {
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
						if (cmd.length >= 2) {
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
						if (cmd.length >= 5) {
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
						if (cmd.length >= 2) {
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
						if (cmd.length >= 3) {
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
					case "recall":
						if (cmd.length >= 2) {
							try {
								if (Integer.parseInt(cmd[1]) - 1 > 0) {
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
	 */
	public static void checkUpdate() {
		try {
			HttpURLConnection connection;
			URL url;
			try {
				url = new URL("https://raw.githubusercontent.com/1689295608/MiraiBot/main/LatestVersion");
				connection = getHttpURLConnection(url);
				connection.connect();
			} catch (UnknownHostException e) {
				url = new URL("https://ghproxy.com/https://raw.githubusercontent.com/1689295608/MiraiBot/main/LatestVersion");
				connection = getHttpURLConnection(url);
				connection.connect();
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = in.readLine()) != null) {
				sb.append(line);
			}
			int LatestVersion = Integer.parseInt(sb.toString().replaceAll("\\.", ""));
			int ThisVersion = Integer.parseInt(new String(PluginMain.class.getResourceAsStream("Version").readAllBytes()).replaceAll("\\.", ""));
			if (ThisVersion < LatestVersion) {
				LogUtil.log(ConfigUtil.getLanguage("found.new.update")
						.replaceAll("\\$1", "https://github.com/1689295608/MiraiBot/releases/tag/" + sb)
				);
			} else {
				LogUtil.log(ConfigUtil.getLanguage("already.latest.version"));
			}
		} catch (Exception e) {
			LogUtil.log(ConfigUtil.getLanguage("failed.check.update"));
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
		connection.setConnectTimeout(5000);
		connection.setReadTimeout(15000);
		connection.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
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
					FileOutputStream fos = new FileOutputStream(file);
					String config =
							"# 输入你的 QQ\n" +
									"qq=\n" +
									"# 输入你的 QQ 密码\n" +
									"password=\n" +
									"# 输入你要聊天的聊群\n" +
									"group=\n" +
									"# 每一个新消息是否都显示发送者QQ号\n" +
									"showQQ=false\n" +
									"# 输入你接收的好友信息（“*” 为 全部）\n" +
									"friend=*\n" +
									"# 每次启动时都检测更新\n" +
									"checkUpdate=true\n" +
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
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return false;
		}
		return true;
	}
	/**
	 * Send a POST request
	 * @param url URL
	 * @param param Parameters (Looks like this:"username=xxx&password=xxx")
	 * @param header (May be null) header
	 * @return The content obtained
	 * @throws IOException IOException
	 */
	public static String sendPost(String url, String param, Map<String, String> header) throws IOException {
		PrintWriter out;
		BufferedReader in;
		StringBuilder result = new StringBuilder();
		URL realUrl = new URL(url);
		// 打开和URL之间的连接
		URLConnection conn = realUrl.openConnection();
		//设置超时时间
		conn.setConnectTimeout(5000);
		conn.setReadTimeout(15000);
		// 设置通用的请求属性
		if (header!=null)
			for (Map.Entry<String, String> entry : header.entrySet()) conn.setRequestProperty(entry.getKey(), entry.getValue());
		conn.setRequestProperty("accept", "*/*");
		conn.setRequestProperty("connection", "Keep-Alive");
		conn.setRequestProperty("content-type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
		// 发送POST请求必须设置如下两行
		conn.setDoOutput(true);
		conn.setDoInput(true);
		// 获取URLConnection对象对应的输出流
		out = new PrintWriter(conn.getOutputStream());
		// 发送请求参数
		out.print(param);
		// flush输出流的缓冲
		out.flush();
		// 定义BufferedReader输入流来读取URL的响应
		in = new BufferedReader(
				new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
		String line;
		while ((line = in.readLine()) != null) {
			result.append(line);
		}
		out.close();
		in.close();
		return result.toString();
	}
}
