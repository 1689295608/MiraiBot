import net.mamoe.mirai.Bot;
import net.mamoe.mirai.BotFactory;
import net.mamoe.mirai.Mirai;
import net.mamoe.mirai.contact.*;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.*;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.utils.BotConfiguration;
import net.mamoe.mirai.utils.ExternalResource;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Scanner;

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
			LogUtil.log(ConfigUtil.getConfig("language.properties", "config.error"));
			System.exit(-1);
			return;
		}
		String qq = ConfigUtil.getConfig("config.properties",  "qq");
		String password = ConfigUtil.getConfig("config.properties", "password");
		String groupId = ConfigUtil.getConfig("config.properties", "group");
		if (ConfigUtil.getConfig("config.properties", "showQQ") != null) {
			EventListener.showQQ = ConfigUtil.getConfig("config.properties", "showQQ").equals("true");
		} else {
			EventListener.showQQ = false;
		}
		if (qq.isEmpty() || password.isEmpty()){
			LogUtil.log(ConfigUtil.getConfig("language.properties", "qq.password.not.exits"));
			System.exit(-1);
			return;
		}
		/*
		try {
			File messageData = new File("MessageData.data");
			if(!messageData.exists()){
				if(!messageData.createNewFile()){
					LogUtil.log("创建配置文件失败!");
				}
			} else {
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(messageData));
				EventListener.messages.set((ArrayList<MessageSource>) ois.readObject());
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		*/
		EventListener.autoRespond = new File("AutoRespond.ini");
		if (!EventListener.autoRespond.exists()) {
			try {
				if (!EventListener.autoRespond.createNewFile()) {
					LogUtil.log(ConfigUtil.getConfig("language.properties", "failed.create.config"));
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
						"# 请保持本占位符于配置文件末端！\n\n" +
						"# 自动回复支持操作：\n" +
						"# Reply=Boolean 回复\n" +
						"# Recall=Boolean 撤回\n" +
						"# Mute=Integer 禁言\n" +
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
						"# %flash_id% 闪照ID" +
						"# %image_id% 图片ID"
				)
						.getBytes(StandardCharsets.UTF_8));
				fos.flush();
				fos.close();
			} catch (IOException e) {
				LogUtil.log(ConfigUtil.getConfig("language.properties", "failed.create.config"));
				e.printStackTrace();
				System.exit(-1);
			}
		}
		IniUtil.loadData(EventListener.autoRespond);
		
		String protocol = ConfigUtil.getConfig("config.properties", "protocol") != null ?
				ConfigUtil.getConfig("config.properties", "protocol") : "";
		LogUtil.log(ConfigUtil.getConfig("language.properties", "trying.login").replaceAll("\\$1", protocol));
		try {
			BotConfiguration.MiraiProtocol miraiProtocol;
			if (protocol.equals("PAD")) { miraiProtocol = BotConfiguration.MiraiProtocol.ANDROID_PAD; }
			else if (protocol.equals("WATCH")) { miraiProtocol = BotConfiguration.MiraiProtocol.ANDROID_WATCH; }
			else { miraiProtocol = BotConfiguration.MiraiProtocol.ANDROID_PHONE; }
			Bot bot = BotFactory.INSTANCE.newBot(Long.parseLong(qq),password,new BotConfiguration(){{
				fileBasedDeviceInfo();
				setProtocol(miraiProtocol);
				noNetworkLog();
				noBotLog();
			}});
			bot.login();
			LogUtil.log(ConfigUtil.getConfig("language.properties", "registering.event"));
			GlobalEventChannel.INSTANCE.registerListenerHost(new EventListener());
			LogUtil.log(ConfigUtil.getConfig("language.properties", "login.success").replaceAll("\\$1", bot.getNick()));
			Group group = null;
			if (groupId.isEmpty()){
				LogUtil.log(ConfigUtil.getConfig("language.properties", "not.group.set"));
			} else if (!inGroup(bot, Long.parseLong(groupId))){
				LogUtil.log(ConfigUtil.getConfig("language.properties", "not.entered.group").replaceAll("\\$1", groupId));
			} else {
				group = bot.getGroupOrFail(Long.parseLong(ConfigUtil.getConfig("config.properties", "group")));
				LogUtil.log(ConfigUtil.getConfig("language.properties", "now.group")
					.replaceAll("\\$1", group.getName())
					.replaceAll("\\$2", String.valueOf(group.getId())));
			}
			EventListener.messages = new MessageData();
			while (true){
				Scanner scanner = new Scanner(System.in);
				String msg = "";
				if (scanner.hasNextLine()) {
					msg = scanner.nextLine();
				}
				String[] cmd = msg.split(" ");
				assert group != null;
				if (msg.length() > 0) {
					if (msg.equals("stop")) {
						LogUtil.log(ConfigUtil.getConfig("language.properties", "now.group")
							.replaceAll("\\$1", bot.getNick())
							.replaceAll("\\$2", String.valueOf(bot.getId())));
						scanner.close();
						bot.close();
						/*
						File messageData = new File("MessageData.data");
						if (!messageData.exists()) {
							if (!messageData.createNewFile()) {
								LogUtil.log("创建消息数据文件失败！");
							}
						}
						ObjectOutputStream ops = new ObjectOutputStream(new FileOutputStream(messageData));
						ops.writeObject(EventListener.messages);
						ops.flush();
						ops.close();
						*/
						LogUtil.Exit();
						System.exit(0);
						break;
					} else if (msg.equals("friendList")) {
						ContactList<Friend> friends = bot.getFriends();
						StringBuilder out = new StringBuilder();
						int c = 1;
						for (Friend f : friends){
							out.append(c).append(". ").append(f.getNick()).append(" (").append(f.getId()).append(")")
									.append((f.getId() == bot.getId() ? " (" +
											ConfigUtil.getConfig("language.properties", "bot") +
											")\n" : "\n"));
							c++;
						}
						LogUtil.log(out.toString());
					} else if (msg.startsWith("groupList")) {
						ContactList<NormalMember> members = group.getMembers();
						StringBuilder out = new StringBuilder();
						int c = 1;
						for (NormalMember f : members) {
							out.append(c).append(". ").append(f.getNameCard()).append(" (").append(f.getId()).append(")")
									.append((f.getId() == bot.getId() ? " (" +
											ConfigUtil.getConfig("language.properties", "bot") +
											")\n" : "\n"));
							c++;
						}
						LogUtil.log(out.toString());
					} else if (msg.equals("help")) {
						LogUtil.log("· --------====== MiraiBot ======-------- ·");
						LogUtil.log("stop");
						LogUtil.log(" - " + ConfigUtil.getConfig("language.properties", "command.stop"));
						LogUtil.log("friendList");
						LogUtil.log(" - " + ConfigUtil.getConfig("language.properties", "command.friend.list"));
						LogUtil.log("groupList");
						LogUtil.log(" - " + ConfigUtil.getConfig("language.properties", "command.group.list"));
						LogUtil.log("help");
						LogUtil.log(" - " + ConfigUtil.getConfig("language.properties", "command.help"));
						LogUtil.log("send <" + ConfigUtil.getConfig("language.properties", "qq") +
								"> <" + ConfigUtil.getConfig("language.properties", "contents") +">");
						LogUtil.log(" - " + ConfigUtil.getConfig("language.properties", "command.send"));
						LogUtil.log("reply <" + ConfigUtil.getConfig("language.properties", "message.id") +
								"> <" + ConfigUtil.getConfig("language.properties", "contents") + ">");
						LogUtil.log(" - " + ConfigUtil.getConfig("language.properties", "command.reply"));
						LogUtil.log("recall <" + ConfigUtil.getConfig("language.properties", "message.id") +">");
						LogUtil.log(" - " + ConfigUtil.getConfig("language.properties", "command.recall"));
						LogUtil.log("image <" + ConfigUtil.getConfig("language.properties", "file.path") +">");
						LogUtil.log(" - " + ConfigUtil.getConfig("language.properties", "command.image"));
						LogUtil.log("upImg <" + ConfigUtil.getConfig("language.properties", "file.path") + ">");
						LogUtil.log(" - " + ConfigUtil.getConfig("language.properties", "command.up.img"));
						LogUtil.log("upClipImg");
						LogUtil.log(" - " + ConfigUtil.getConfig("language.properties", "command.up.clip.img"));
						LogUtil.log("newImg <" + ConfigUtil.getConfig("language.properties", "width") +
								"> <" + ConfigUtil.getConfig("language.properties", "height") +
								"> <" + ConfigUtil.getConfig("language.properties", "font.size") +
								"> <" + ConfigUtil.getConfig("language.properties", "contents") +
								">");
						LogUtil.log(" - " + ConfigUtil.getConfig("language.properties", "command.new.img"));
						LogUtil.log("del <" + ConfigUtil.getConfig("language.properties", "qq") + ">");
						LogUtil.log(" - " + ConfigUtil.getConfig("language.properties", "command.del"));
						LogUtil.log("· -------------------------------------- ·");
					} else if (msg.startsWith("send")) {
						if (cmd.length >= 3) {
							StringBuilder tmp = new StringBuilder();
							for (int i = 2 ; i < cmd.length ; i ++){
								tmp.append(cmd[i]).append(i == cmd.length-1 ? "" : " ");
							}
							try { // String 转换到 Integer 如果不是数字居然还会抛出错误，气死我了！！
								Friend friend = bot.getFriend(Long.parseLong(cmd[1]));
								if (friend != null){
									friend.sendMessage(MiraiCode.deserializeMiraiCode(tmp.toString()));
								} else {
									LogUtil.log(ConfigUtil.getConfig("language.properties", "not.friend"));
								}
							} catch (NumberFormatException e) {
								LogUtil.log(ConfigUtil.getConfig("language.properties", "not.qq")
									.replaceAll("\\$1", cmd[1]));
							}
						} else {
							LogUtil.log(ConfigUtil.getConfig("language.properties", "usage") + ": send <QQ> <内容>");
						}
					} else if (msg.startsWith("image")) {
						if (cmd.length >= 2) {
							File file = new File(msg.substring(6));
							ExternalResource externalResource = ExternalResource.create(file);
							Image img = Objects.requireNonNull(bot.getGroup(Integer.parseInt(ConfigUtil.getConfig("config.properties", "group"))))
									.uploadImage(externalResource);
							Objects.requireNonNull(bot.getGroup(Integer.parseInt(ConfigUtil.getConfig("config.properties", "group")))).sendMessage(img);
							LogUtil.log(bot.getNick() + " : " +
									(ConfigUtil.getConfig("config.properties", "debug").equals("true") ? img.serializeToMiraiCode() : img.contentToString()));
							externalResource.close();
							LogUtil.log("· ------==== Image Info ====------ ·");
							LogUtil.log("I D: " + img.getImageId());
							LogUtil.log("URL: " + Mirai.getInstance().queryImageUrl(bot, img));
							LogUtil.log("MD5: " + Arrays.toString(MessageUtils.calculateImageMd5(img)));
							LogUtil.log("MiraiCode: " + img.serializeToMiraiCode());
							LogUtil.log("· -------------------------------- ·");
						} else {
							LogUtil.log(ConfigUtil.getConfig("language.properties", "usage") + ": image <" +
									ConfigUtil.getConfig("language.properties", "file.path") + ">");
						}
					} else if (msg.startsWith("upImg")) {
						if (cmd.length >= 2) {
							File file = new File(msg.substring(6));
							ExternalResource externalResource = ExternalResource.create(file);
							Image img = Objects.requireNonNull(bot.getGroup(Integer.parseInt(ConfigUtil.getConfig("config.properties", "group"))))
									.uploadImage(externalResource);
							externalResource.close();
							LogUtil.log("· ------==== Image Info ====------ ·");
							LogUtil.log("I D: " + img.getImageId());
							LogUtil.log("URL: " + Mirai.getInstance().queryImageUrl(bot, img));
							LogUtil.log("MD5: " + Arrays.toString(MessageUtils.calculateImageMd5(img)));
							LogUtil.log("MiraiCode: " + img.serializeToMiraiCode());
							LogUtil.log("· -------------------------------- ·");
						} else {
							LogUtil.log(ConfigUtil.getConfig("language.properties", "usage") + ": upImg <"+
									ConfigUtil.getConfig("language.properties", "file.path") + ">");
						}
					} else if (msg.startsWith("upClipImg")) {
						byte[] clip = ClipboardUtil.getImageFromClipboard();
						if (clip != null){
							ExternalResource externalResource = ExternalResource.create(clip);
							Image img = Objects.requireNonNull(bot.getGroup(Integer.parseInt(ConfigUtil.getConfig("config.properties", "group"))))
									.uploadImage(externalResource);
							externalResource.close();
							LogUtil.log("· ------==== Image Info ====------ ·");
							LogUtil.log("I D: " + img.getImageId());
							LogUtil.log("URL: " + Mirai.getInstance().queryImageUrl(bot, img));
							LogUtil.log("MD5: " + Arrays.toString(MessageUtils.calculateImageMd5(img)));
							LogUtil.log("MiraiCode: " + img.serializeToMiraiCode());
							LogUtil.log("· -------------------------------- ·");
						} else {
							LogUtil.log(ConfigUtil.getConfig("language.properties", "failed.clipboard"));
						}
					} else if (msg.startsWith("newImg")){
						if (cmd.length >= 5) {
							try { // String 转换到 Integer 如果不是数字居然还会抛出错误，气死我了！！
								StringBuilder content = new StringBuilder();
								for (int i = 4 ; i < cmd.length ; i ++){
									content.append(cmd[i]).append(i == cmd.length - 1 ? "" : " ");
								}
								LogUtil.log(ConfigUtil.getConfig("language.properties", "creating.word.image"));
								byte[] file = WordToImage.createImage(content.toString(),
										new Font(ConfigUtil.getConfig("config.properties", "font"), Font.PLAIN, Integer.parseInt(cmd[3])),
										Integer.parseInt(cmd[1]), Integer.parseInt(cmd[2]));
								ExternalResource externalResource = ExternalResource.create(file);
								Image img = Objects.requireNonNull(bot.getGroup(Integer.parseInt(ConfigUtil.getConfig("config.properties", "group"))))
										.uploadImage(externalResource);
								externalResource.close();
								LogUtil.log("· ------==== Image Info ====------ ·");
								LogUtil.log("I D: " + img.getImageId());
								LogUtil.log("URL: " + Mirai.getInstance().queryImageUrl(bot, img));
								LogUtil.log("MD5: " + Arrays.toString(MessageUtils.calculateImageMd5(img)));
								LogUtil.log("MiraiCode: " + img.serializeToMiraiCode());
								LogUtil.log("· -------------------------------- ·");
							} catch (NumberFormatException e) {
								LogUtil.log(ConfigUtil.getConfig("language.properties", "width.height.error"));
							}
						} else {
							LogUtil.log(ConfigUtil.getConfig("language.properties", "usage") +
									": newImg <" + ConfigUtil.getConfig("language.properties", "width") +
									"> <" + ConfigUtil.getConfig("language.properties", "height") +
									"> <" + ConfigUtil.getConfig("language.properties", "font.size") +
									"> <" + ConfigUtil.getConfig("language.properties", "contents") +
									">");
						}
					} else if (msg.startsWith("del")) {
						if (cmd.length >= 2) {
							try { // String 转换到 Integer 如果不是数字居然还会抛出错误，气死我了！！
								Friend friend = bot.getFriend(Long.parseLong(cmd[1]));
								if (friend != null){
									friend.delete();
									LogUtil.log(ConfigUtil.getConfig("language.properties", "delete.friend")
										.replaceAll("\\$1", friend.getNick())
										.replaceAll("\\$2", String.valueOf(friend.getId())));
								} else {
									LogUtil.log(ConfigUtil.getConfig("language.properties", "not.friend"));
								}
							} catch (NumberFormatException e) {
								LogUtil.log(ConfigUtil.getConfig("language.properties", "not.qq")
										.replaceAll("\\$1", cmd[1]));
							}
						} else {
							LogUtil.log(ConfigUtil.getConfig("language.properties", "usage") + ": del <QQ>");
						}
					} else if (msg.startsWith("reply")) {
						if (cmd.length >= 3) {
							try {
								MessageSource message = EventListener.messages.get(Integer.parseInt(cmd[1]) - 1);
								StringBuilder content = new StringBuilder();
								for (int i = 2 ; i < cmd.length ; i ++) {
									content.append(cmd[i]);
								}
								if (message != null) {
									group.sendMessage(new QuoteReply(message).plus(MiraiCode.deserializeMiraiCode(content.toString())));
								} else {
									LogUtil.log(ConfigUtil.getConfig("language.properties", "message.not.found"));
								}
							} catch (NumberFormatException e) {
								LogUtil.log(ConfigUtil.getConfig("language.properties", "message.id.error"));
							}
						} else {
							LogUtil.log(ConfigUtil.getConfig("language.properties", "usage") + ": reply <" +
									ConfigUtil.getConfig("language.properties", "message.id") + "> <" +
									ConfigUtil.getConfig("language.properties", "contents") + ">");
						}
					} else if (msg.startsWith("recall")) {
						if (cmd.length >= 2) {
							try {
								MessageSource message = EventListener.messages.get(Integer.parseInt(cmd[1]) - 1);
								if (message != null) {
									if (message.getFromId() == message.getBotId()) {
										try {
											Mirai.getInstance().recallMessage(bot, message);
											LogUtil.log(ConfigUtil.getConfig("language.properties", "recalled"));
										} catch (Exception e) {
											LogUtil.log(ConfigUtil.getConfig("language.properties", "failed.recall"));
										}
									} else {
										if (group.getBotPermission() != MemberPermission.MEMBER) {
											try {
												Mirai.getInstance().recallMessage(bot, message);
												LogUtil.log(ConfigUtil.getConfig("language.properties", "recalled"));
											} catch (Exception e) {
												LogUtil.log(ConfigUtil.getConfig("language.properties", "failed.recall"));
											}
										} else {
											LogUtil.log(ConfigUtil.getConfig("language.properties", "no.permission"));
										}
									}
								} else {
									LogUtil.log(ConfigUtil.getConfig("language.properties", "message.not.found"));
								}
							} catch (NumberFormatException e) {
								LogUtil.log(ConfigUtil.getConfig("language.properties", "message.id.error"));
							}
						} else {
							LogUtil.log(ConfigUtil.getConfig("language.properties", "usage") + ": recall <" +
									ConfigUtil.getConfig("language.properties", "message.id") + ">");
						}
					} else {
						MessageChain send = MiraiCode.deserializeMiraiCode(msg);
						group.sendMessage(send);
					}
				}
			}
		} catch (NumberFormatException e) {
			LogUtil.log(ConfigUtil.getConfig("language.properties", "qq.password.error"));
			e.printStackTrace();
			System.exit(-1);
		} catch (Exception e) {
			LogUtil.log(ConfigUtil.getConfig("language.properties", "unknown.error"));
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public static boolean inGroup(Bot bot, Long groupId) {
		ContactList<Group> groups = bot.getGroups();
		for (Group g : groups) {
			if (g.getId() == groupId){
				return true;
			}
		}
		return false;
	}
	
	public static boolean checkConfig(){
		File file = new File("config.properties");
		if (!file.exists()){
			try {
				if (file.createNewFile()){
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
					fos.write(config.getBytes(StandardCharsets.UTF_8));
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
