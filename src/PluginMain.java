import net.mamoe.mirai.Bot;
import net.mamoe.mirai.BotFactory;
import net.mamoe.mirai.Mirai;
import net.mamoe.mirai.contact.*;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.*;
import net.mamoe.mirai.utils.BotConfiguration;
import net.mamoe.mirai.utils.ExternalResource;

import java.awt.*;
import java.io.*;
import java.util.*;

public class PluginMain {
	public static String language = Locale.getDefault().getLanguage();
	
	public static void main(String[] args) {
		LogUtil.init();
		File lang = new File("language.properties");
		try {
			if (!lang.exists()) {
				if (!lang.createNewFile()) {
					LogUtil.log(language.equals("zh") ? "无法创建配置文件！" : "Unable to create configuration file!");
				} else {
					FileOutputStream fos = new FileOutputStream(lang);
					fos.write(LanguageUtil.languageFile(language));
					fos.flush();
					fos.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (!checkConfig()) {
			LogUtil.log(ConfigUtil.getLanguage("config.error"));
			System.exit(-1);
			return;
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
				fos.write((
						"[AutoRespond]\n" +
								"Message=\\[mirai:at:%bot_id%\\](.*)?Hello!(.*)?\n" +
								"Reply=true\n" +
								"Respond=[mirai:at:%sender_id%] Hello!\n" +
								"\n" +
								"[Placeholder|占位符]\n" +
								"# 由于技术原因，请勿删除本占位符。\n" +
								"# For technical reasons, please do not delete this placeholder.\n" +
								"# 请保持本占位符位于配置文件末端！\n" +
								"# Please keep this placeholder at the end of the configuration file!\n\n" +
								"# 自动回复支持操作：\n" +
								"# Reply=Boolean 回复\n" +
								"# Recall=Boolean 撤回\n" +
								"# Mute=Integer 禁言\n" +
								"# \n" +
								"# 自动回复支持变量：\n" +
								"# %sender_nick% 发送者昵称\n" +
								"# %sender_id% 发送者QQ号\n" +
								"# %sender_nameCard% 发送者群昵称\n" +
								"# %group_name% 本群名称\n" +
								"# %group_id% 本群群号\n" +
								"# %group_owner_nick% 本群群主昵称\n" +
								"# %group_owner_id% 本群群主QQ号\n" +
								"# %group_owner_nameCard% 本群群主群昵称\n" +
								"# %message_miraiCode% 消息的 Mirai码\n" +
								"# %message_content% 消息的内容\n" +
								"# %bot_nick% 机器人昵称\n" +
								"# %bot_id% 机器人QQ号\n" +
								"# %flash_id% 闪照ID\n" +
								"# %image_id% 图片ID\n" +
								"# %file_id% 文件ID"
				)
						.getBytes());
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
			Group group = null;
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
			Scanner scanner = new Scanner(System.in);
			while (true) {
				String msg;
				if (!scanner.hasNextLine()) {
					continue;
				}
				msg = scanner.nextLine();
				LogUtil.log("> " + msg);
				String[] cmd = msg.split(" ");
				assert group != null;
				if (msg.length() > 0) {
					if (msg.equals("stop")) {
						LogUtil.log(ConfigUtil.getLanguage("stopping.bot")
								.replaceAll("\\$1", bot.getNick())
								.replaceAll("\\$2", String.valueOf(bot.getId())));
						scanner.close();
						bot.close();
						LogUtil.Exit();
						System.out.println();
						System.exit(0);
						break;
					} else if (msg.equals("friendList")) {
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
					} else if (msg.startsWith("groupList")) {
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
					} else if (msg.equals("help")) {
						LogUtil.log("· --------====== MiraiBot ======-------- ·");
						LogUtil.log("stop");
						LogUtil.log(" - " + ConfigUtil.getLanguage("command.stop"));
						LogUtil.log("friendList");
						LogUtil.log(" - " + ConfigUtil.getLanguage("command.friend.list"));
						LogUtil.log("groupList");
						LogUtil.log(" - " + ConfigUtil.getLanguage("command.group.list"));
						LogUtil.log("help");
						LogUtil.log(" - " + ConfigUtil.getLanguage("command.help"));
						LogUtil.log("send <" + ConfigUtil.getLanguage("qq") + "> <" +
								ConfigUtil.getLanguage("contents") + ">");
						LogUtil.log(" - " + ConfigUtil.getLanguage("command.send"));
						LogUtil.log("reply <" + ConfigUtil.getLanguage("message.id") + "> <" +
								ConfigUtil.getLanguage("contents") + ">");
						LogUtil.log(" - " + ConfigUtil.getLanguage("command.reply"));
						LogUtil.log("recall <" + ConfigUtil.getLanguage("message.id") + ">");
						LogUtil.log(" - " + ConfigUtil.getLanguage("command.recall"));
						LogUtil.log("image <" + ConfigUtil.getLanguage("file.path") + ">");
						LogUtil.log(" - " + ConfigUtil.getLanguage("command.image"));
						LogUtil.log("upImg <" + ConfigUtil.getLanguage("file.path") + ">");
						LogUtil.log(" - " + ConfigUtil.getLanguage("command.up.img"));
						LogUtil.log("upClipImg");
						LogUtil.log(" - " + ConfigUtil.getLanguage("command.up.clip.img"));
						LogUtil.log("newImg <" + ConfigUtil.getLanguage("width") + "> <" +
								ConfigUtil.getLanguage("height") + "> <" +
								ConfigUtil.getLanguage("font.size") + "> <" +
								ConfigUtil.getLanguage("contents") + ">");
						LogUtil.log(" - " + ConfigUtil.getLanguage("command.new.img"));
						LogUtil.log("del <" + ConfigUtil.getLanguage("qq") + ">");
						LogUtil.log(" - " + ConfigUtil.getLanguage("command.del"));
						LogUtil.log("· -------------------------------------- ·");
					} else if (msg.startsWith("send")) {
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
							LogUtil.log(ConfigUtil.getLanguage("usage") + ": send <QQ> <内容>");
						}
					} else if (msg.startsWith("image")) {
						if (cmd.length >= 2) {
							File file = new File(msg.substring(6));
							ExternalResource externalResource = ExternalResource.create(file);
							Image img = Objects.requireNonNull(bot.getGroup(Integer.parseInt(ConfigUtil.getConfig("group"))))
									.uploadImage(externalResource);
							Objects.requireNonNull(bot.getGroup(Integer.parseInt(ConfigUtil.getConfig("group")))).sendMessage(img);
							LogUtil.log(bot.getNick() + " : " +
									(ConfigUtil.getConfig("debug").equals("true") ? img.serializeToMiraiCode() : img.contentToString()));
							externalResource.close();
							LogUtil.log("· ------==== Image Info ====------ ·");
							LogUtil.log("I D: " + img.getImageId());
							LogUtil.log("URL: " + Mirai.getInstance().queryImageUrl(bot, img));
							LogUtil.log("MD5: " + Arrays.toString(MessageUtils.calculateImageMd5(img)));
							LogUtil.log("MiraiCode: " + img.serializeToMiraiCode());
							LogUtil.log("· -------------------------------- ·");
						} else {
							LogUtil.log(ConfigUtil.getLanguage("usage") + ": image <" +
									ConfigUtil.getLanguage("file.path") + ">");
						}
					} else if (msg.startsWith("upImg")) {
						if (cmd.length >= 2) {
							File file = new File(msg.substring(6));
							ExternalResource externalResource = ExternalResource.create(file);
							Image img = Objects.requireNonNull(bot.getGroup(Integer.parseInt(ConfigUtil.getConfig("group"))))
									.uploadImage(externalResource);
							externalResource.close();
							LogUtil.log("· ------==== Image Info ====------ ·");
							LogUtil.log("I D: " + img.getImageId());
							LogUtil.log("URL: " + Mirai.getInstance().queryImageUrl(bot, img));
							LogUtil.log("MD5: " + Arrays.toString(MessageUtils.calculateImageMd5(img)));
							LogUtil.log("MiraiCode: " + img.serializeToMiraiCode());
							LogUtil.log("· -------------------------------- ·");
						} else {
							LogUtil.log(ConfigUtil.getLanguage("usage") + ": upImg <" +
									ConfigUtil.getLanguage("file.path") + ">");
						}
					} else if (msg.startsWith("upClipImg")) {
						byte[] clip = ClipboardUtil.getImageFromClipboard();
						if (clip != null) {
							ExternalResource externalResource = ExternalResource.create(clip);
							Image img = Objects.requireNonNull(bot.getGroup(Integer.parseInt(ConfigUtil.getConfig("group"))))
									.uploadImage(externalResource);
							externalResource.close();
							LogUtil.log("· ------==== Image Info ====------ ·");
							LogUtil.log("I D: " + img.getImageId());
							LogUtil.log("URL: " + Mirai.getInstance().queryImageUrl(bot, img));
							LogUtil.log("MD5: " + Arrays.toString(MessageUtils.calculateImageMd5(img)));
							LogUtil.log("MiraiCode: " + img.serializeToMiraiCode());
							LogUtil.log("· -------------------------------- ·");
						} else {
							LogUtil.log(ConfigUtil.getLanguage("failed.clipboard"));
						}
					} else if (msg.startsWith("newImg")) {
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
								Image img = Objects.requireNonNull(bot.getGroup(Integer.parseInt(ConfigUtil.getConfig("group"))))
										.uploadImage(externalResource);
								externalResource.close();
								LogUtil.log("· ------==== Image Info ====------ ·");
								LogUtil.log("I D: " + img.getImageId());
								LogUtil.log("URL: " + Mirai.getInstance().queryImageUrl(bot, img));
								LogUtil.log("MD5: " + Arrays.toString(MessageUtils.calculateImageMd5(img)));
								LogUtil.log("MiraiCode: " + img.serializeToMiraiCode());
								LogUtil.log("· -------------------------------- ·");
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
					} else if (msg.startsWith("del")) {
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
					} else if (msg.startsWith("reply")) {
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
					} else if (msg.startsWith("recall")) {
						if (cmd.length >= 2) {
							try {
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
							} catch (NumberFormatException e) {
								LogUtil.log(ConfigUtil.getLanguage("message.id.error"));
							}
						} else {
							LogUtil.log(ConfigUtil.getLanguage("usage") + ": recall <" +
									ConfigUtil.getLanguage("message.id") + ">");
						}
					} else {
						MessageChain send = MiraiCode.deserializeMiraiCode(msg);
						group.sendMessage(send);
					}
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
	
	public static boolean inGroup(Bot bot, Long groupId) {
		ContactList<Group> groups = bot.getGroups();
		for (Group g : groups) {
			if (g.getId() == groupId) {
				return true;
			}
		}
		return false;
	}
	
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
}
