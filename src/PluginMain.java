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
import java.util.Objects;
import java.util.Scanner;

public class PluginMain {
	
	public static void main(String[] args) {
		LogUtil.init();
		if (!checkConfig()) {
			LogUtil.log("配置文件出现错误，请检查配置文件后再试！");
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
		if (qq.isEmpty() || password.isEmpty()){
			LogUtil.log("请填写配置文件的 QQ号 与 密码！");
			System.exit(-1);
			return;
		}
		EventListener.autoRespond = new File("AutoRespond.ini");
		if (!EventListener.autoRespond.exists()) {
			try {
				if (!EventListener.autoRespond.createNewFile()) {
					LogUtil.log("创建配置文件失败！");
					System.exit(-1);
				}
				FileOutputStream fos = new FileOutputStream(EventListener.autoRespond);
				fos.write((
						"[AutoRespond]\n" +
						"Message=\\[mirai:at:%bot_id%\\](.*)?Hello!(.*)?\n" +
						"Reply=true" +
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
				LogUtil.log("创建配置文件失败！");
				e.printStackTrace();
				System.exit(-1);
			}
		}
		IniUtil.loadData(EventListener.autoRespond);
		
		String protocol = ConfigUtil.getConfig("protocol") != null ? ConfigUtil.getConfig("protocol") : "";
		String tmpPro;
		if (protocol.equals("PAD")){ tmpPro = "平板"; } /* 为了兼容 JDK11 而舍弃的 switch 语句 */
		else if (protocol.equals("WATCH")){ tmpPro = "手表"; } else { tmpPro = "手机"; }
		LogUtil.log("正在尝试使用" + tmpPro + "登录, 稍后可能会出现验证码弹窗...");
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
			LogUtil.log("正在注册事件...");
			GlobalEventChannel.INSTANCE.registerListenerHost(new EventListener());
			LogUtil.log("登录成功，您的昵称是：" + bot.getNick());
			Group group = null;
			if (groupId.equals("")){
				LogUtil.log("配置文件中的 聊群 项为空！您将无法发送和接收到聊群的消息！");
			} else if (!inGroup(bot, Long.parseLong(groupId))){
				LogUtil.log("机器人并未加入聊群 \"" + groupId + "\" , 但机器人目前可以继续使用！");
			} else {
				group = bot.getGroupOrFail(Long.parseLong(ConfigUtil.getConfig("group")));
				LogUtil.log("当前进入的聊群为：" + group.getName() + " (" + group.getId() + ")");
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
						LogUtil.log("正在关闭机器人：" + bot.getNick() + " (" + bot.getId() + ")");
						scanner.close();
						bot.close();
						LogUtil.Exit();
						System.exit(0);
						break;
					} else if (msg.equals("friendList")) {
						ContactList<Friend> friends = bot.getFriends();
						StringBuilder out = new StringBuilder();
						int c = 1;
						for (Friend f : friends){
							out.append(c).append(". ").append(f.getNick()).append(" (").append(f.getId()).append(")")
									.append((f.getId() == bot.getId() ? " (机器人)\n" : "\n"));
							c++;
						}
						LogUtil.log(out.toString());
					} else if (msg.startsWith("groupList")) {
						ContactList<NormalMember> members = group.getMembers();
						StringBuilder out = new StringBuilder();
						int c = 1;
						for (NormalMember f : members) {
							out.append(c).append(". ").append(f.getNameCard()).append(" (").append(f.getId()).append(")")
									.append((f.getId() == bot.getId() ? " (机器人)\n" : "\n"));
							c++;
						}
						LogUtil.log(out.toString());
					} else if (msg.equals("help")) {
						LogUtil.log("· --------====== MiraiBot ======-------- ·");
						LogUtil.log("stop");
						LogUtil.log(" - 关闭机器人");
						LogUtil.log("friendList");
						LogUtil.log(" - 获取当前机器人好友列表");
						LogUtil.log("groupList");
						LogUtil.log(" - 获取当前聊群成员列表");
						LogUtil.log("help");
						LogUtil.log(" - 显示 MiraiBot 所有指令");
						LogUtil.log("send <qq> <内容>");
						LogUtil.log(" - 向好友发送消息（支持 Mirai码）");
						LogUtil.log("reply <消息ID> <内容>");
						LogUtil.log(" - 回复一条消息");
						LogUtil.log("recall <消息ID>");
						LogUtil.log(" - 撤回一个消息");
						LogUtil.log("image <文件路径>");
						LogUtil.log(" - 向当前聊群发送图片");
						LogUtil.log("upImg <文件路径>");
						LogUtil.log(" - 上传一个图片到服务器并获取到ID");
						LogUtil.log("upClipImg");
						LogUtil.log(" - 上传当前剪切板的图片");
						LogUtil.log("newImg <宽度> <高度> <字体大小> <内容>");
						LogUtil.log(" - 创建并上传一个图片");
						LogUtil.log("del <qq>");
						LogUtil.log(" - 删除一个好友");
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
									LogUtil.log("你没有这个好友！");
								}
							} catch (NumberFormatException e) {
								LogUtil.log("\"" + cmd[1] + "\" 不是一个 QQ！");
							}
						} else {
							LogUtil.log("语法: send <QQ> <内容>");
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
							LogUtil.log("语法: image <文件路径>");
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
							LogUtil.log("语法: upImg <文件路径>");
						}
					} else if (msg.startsWith("upClipImg")) {
						byte[] clip = ClipboardUtil.getImageFromClipboard();
						if (clip != null){
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
							LogUtil.log("无法获取当前剪切板的图片！");
						}
					} else if (msg.startsWith("newImg")){
						if (cmd.length >= 5) {
							try { // String 转换到 Integer 如果不是数字居然还会抛出错误，气死我了！！
								StringBuilder content = new StringBuilder();
								for (int i = 4 ; i < cmd.length ; i ++){
									content.append(cmd[i]).append(i == cmd.length - 1 ? "" : " ");
								}
								LogUtil.log("正在生成文字图片...");
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
								LogUtil.log("宽度、高度和字体大小必须为整数！");
							}
						} else {
							LogUtil.log("语法： newImg <宽度> <高度> <字体大小> <内容>");
						}
					} else if (msg.startsWith("del")) {
						if (cmd.length >= 2) {
							try { // String 转换到 Integer 如果不是数字居然还会抛出错误，气死我了！！
								Friend friend = bot.getFriend(Long.parseLong(cmd[1]));
								if (friend != null){
									friend.delete();
									LogUtil.log("已删除 " + friend.getNick() + "(" + friend.getId() + ")");
								} else {
									LogUtil.log("你没有这个好友！");
								}
							} catch (NumberFormatException e) {
								LogUtil.log("\"" + cmd[1] + "\" 不是一个 QQ！");
							}
						} else {
							LogUtil.log("语法: del <QQ>");
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
									LogUtil.log("未找到该消息！");
								}
							} catch (NumberFormatException e) {
								LogUtil.log("消息位置必须是整数！");
							}
						} else {
							LogUtil.log("语法: reply <消息ID> <内容>");
						}
					} else if (msg.startsWith("recall")) {
						if (cmd.length >= 2) {
							try {
								MessageSource message = EventListener.messages.get(Integer.parseInt(cmd[1]) - 1);
								if (message != null) {
									if (message.getFromId() == message.getBotId()) {
										try {
											Mirai.getInstance().recallMessage(bot, message);
											LogUtil.log("已撤回该消息！");
										} catch (Exception e) {
											LogUtil.log("无法撤回该消息！");
										}
									} else {
										if (group.getBotPermission() != MemberPermission.MEMBER) {
											try {
												Mirai.getInstance().recallMessage(bot, message);
												LogUtil.log("已撤回该消息！");
											} catch (Exception e) {
												LogUtil.log("无法撤回该消息！");
											}
										} else {
											LogUtil.log("该消息不是你发出的且你不是管理员！");
										}
									}
								} else {
									LogUtil.log("未找到该消息！");
								}
							} catch (NumberFormatException e) {
								LogUtil.log("消息位置必须是整数！");
							}
						} else {
							LogUtil.log("语法：recall <消息ID>");
						}
					} else {
						MessageChain send = MiraiCode.deserializeMiraiCode(msg);
						group.sendMessage(send);
					}
				}
			}
		} catch (NumberFormatException e) {
			LogUtil.log("请检查配置文件中的 QQ号 是否正确！");
			e.printStackTrace();
			System.exit(-1);
		} catch (Exception e) {
			LogUtil.log("出现错误！进程即将终止！");
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
							"# 自动批准加好友/邀请进入聊群请求\n" +
							"inviteAccept=true\n" +
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
