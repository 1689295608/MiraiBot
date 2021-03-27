import net.mamoe.mirai.Bot;
import net.mamoe.mirai.BotFactory;
import net.mamoe.mirai.Mirai;
import net.mamoe.mirai.contact.*;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageSource;
import net.mamoe.mirai.message.data.MessageUtils;
import net.mamoe.mirai.utils.BotConfiguration;
import net.mamoe.mirai.utils.ExternalResource;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

public class PluginMain {
	
	public static void main(String[] args) {
		if (!checkConfig()) {
			LogUtil.Log("配置文件出现错误，请检查配置文件后再试！");
			System.exit(-1);
			return;
		}
		String qq = ConfigUtil.getConfig("qq") != null ? ConfigUtil.getConfig("qq") : "";
		String password = ConfigUtil.getConfig("password") != null ? ConfigUtil.getConfig("password") : "";
		String groupId = ConfigUtil.getConfig("group") != null ? ConfigUtil.getConfig("group") : "";
		if (qq.equals("") || password.equals("")){
			LogUtil.Log("请填写配置文件的 QQ号 与 密码！");
			System.exit(-1);
			return;
		}
		String protocol = ConfigUtil.getConfig("protocol") != null ? ConfigUtil.getConfig("protocol") : "";
		String tmpPro;
		if (protocol.equals("PAD")){ tmpPro = "平板"; } /* 为了兼容 JDK11 而舍弃的 switch 语句 */
		else if (protocol.equals("WATCH")){ tmpPro = "手表"; } else { tmpPro = "手机"; }
		LogUtil.Log("正在尝试使用" + tmpPro + "登录, 稍后可能会出现验证码弹窗...");
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
			LogUtil.Log("正在注册事件...");
			GlobalEventChannel.INSTANCE.registerListenerHost(new EventListener());
			LogUtil.Log("登录成功，您的昵称是：" + bot.getNick());
			Group group = null;
			if (groupId.equals("")){
				LogUtil.Log("配置文件中的 聊群 项为空！您将无法发送和接收到聊群的消息！");
			}else if (!inGroup(bot, Long.parseLong(groupId))){
				LogUtil.Log("机器人并未加入聊群 \"" + groupId + "\" , 但机器人目前可以继续使用！");
			} else {
				group = bot.getGroupOrFail(Long.parseLong(ConfigUtil.getConfig("group")));
				LogUtil.Log("当前进入的聊群为：" + group.getName() + " (" + group.getId() + ")");
			}
			while (true){
				String msg = new Scanner(System.in).nextLine();
				String[] cmd = msg.split(" ");
				if (msg.length() > 0) {
					if (msg.equals("stop")) {
						LogUtil.Log("正在关闭机器人：" + bot.getNick() + " (" + bot.getId() + ")");
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
						LogUtil.Log(out.toString());
					} else if (msg.startsWith("groupList")) {
						if (group != null) {
							ContactList<NormalMember> members = group.getMembers();
							StringBuilder out = new StringBuilder();
							int c = 1;
							for (NormalMember f : members) {
								out.append(c).append(". ").append(f.getNameCard()).append(" (").append(f.getId()).append(")")
										.append((f.getId() == bot.getId() ? " (机器人)\n" : "\n"));
								c++;
							}
							LogUtil.Log(out.toString());
						} else {
							LogUtil.Log("您未配置机器人的聊群选项！请检查您的配置文件！");
						}
					} else if (msg.equals("help")) {
						LogUtil.Log("· --------====== MiraiBot ======-------- ·");
						LogUtil.Log("stop");
						LogUtil.Log(" - 关闭机器人");
						LogUtil.Log("friendList");
						LogUtil.Log(" - 获取当前机器人好友列表");
						LogUtil.Log("groupList");
						LogUtil.Log(" - 获取当前聊群成员列表");
						LogUtil.Log("help");
						LogUtil.Log(" - 显示 MiraiBot 所以指令");
						LogUtil.Log("send <qq> <Mirai码>");
						LogUtil.Log(" - 向好友发送消息（支持 Mirai码）");
						LogUtil.Log("recall <消息ID>");
						LogUtil.Log(" - 撤回一个消息");
						LogUtil.Log("image <文件路径>");
						LogUtil.Log(" - 向当前聊群发送图片");
						LogUtil.Log("upImg <文件路径>");
						LogUtil.Log(" - 上传一个图片到服务器并获取到ID");
						LogUtil.Log("upClipImg");
						LogUtil.Log(" - 上传当前剪切板的图片");
						LogUtil.Log("newImg <宽度> <高度> <字体大小> <内容>");
						LogUtil.Log(" - 创建并上传一个图片");
						LogUtil.Log("del <qq>");
						LogUtil.Log(" - 删除一个好友");
						LogUtil.Log("· -------------------------------------- ·");
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
									LogUtil.Log("你没有这个好友！");
								}
							} catch (NumberFormatException e) {
								LogUtil.Log("\"" + cmd[1] + "\" 不是一个 QQ！");
							}
						} else {
							LogUtil.Log("语法: send <QQ> <Mirai码>");
						}
					} else if (msg.startsWith("image")) {
						if (cmd.length >= 2) {
							File file = new File(msg.substring(6));
							ExternalResource externalResource = ExternalResource.create(file);
							Image img = Objects.requireNonNull(bot.getGroup(Integer.parseInt(ConfigUtil.getConfig("group"))))
									.uploadImage(externalResource);
							Objects.requireNonNull(bot.getGroup(Integer.parseInt(ConfigUtil.getConfig("group")))).sendMessage(img);
							LogUtil.Log(bot.getNick() + " : " +
									(ConfigUtil.getConfig("debug").equals("true") ? img.serializeToMiraiCode() : img.contentToString()));
							externalResource.close();
							LogUtil.Log("· ------==== Image Info ====------ ·");
							LogUtil.Log("I D: " + img.getImageId());
							LogUtil.Log("URL: " + Mirai.getInstance().queryImageUrl(bot, img));
							LogUtil.Log("MD5: " + Arrays.toString(MessageUtils.calculateImageMd5(img)));
							LogUtil.Log("MiraiCode: " + img.serializeToMiraiCode());
							LogUtil.Log("· -------------------------------- ·");
						} else {
							LogUtil.Log("语法: image <文件路径>");
						}
					} else if (msg.startsWith("upImg")) {
						if (cmd.length >= 2) {
							File file = new File(msg.substring(6));
							ExternalResource externalResource = ExternalResource.create(file);
							Image img = Objects.requireNonNull(bot.getGroup(Integer.parseInt(ConfigUtil.getConfig("group"))))
									.uploadImage(externalResource);
							externalResource.close();
							LogUtil.Log("· ------==== Image Info ====------ ·");
							LogUtil.Log("I D: " + img.getImageId());
							LogUtil.Log("URL: " + Mirai.getInstance().queryImageUrl(bot, img));
							LogUtil.Log("MD5: " + Arrays.toString(MessageUtils.calculateImageMd5(img)));
							LogUtil.Log("MiraiCode: " + img.serializeToMiraiCode());
							LogUtil.Log("· -------------------------------- ·");
						} else {
							LogUtil.Log("语法: upImg <文件路径>");
						}
					} else if (msg.startsWith("upClipImg")) {
						byte[] clip = ClipboardUtil.getImageFromClipboard();
						if (clip != null){
							ExternalResource externalResource = ExternalResource.create(clip);
							Image img = Objects.requireNonNull(bot.getGroup(Integer.parseInt(ConfigUtil.getConfig("group"))))
									.uploadImage(externalResource);
							externalResource.close();
							LogUtil.Log("· ------==== Image Info ====------ ·");
							LogUtil.Log("I D: " + img.getImageId());
							LogUtil.Log("URL: " + Mirai.getInstance().queryImageUrl(bot, img));
							LogUtil.Log("MD5: " + Arrays.toString(MessageUtils.calculateImageMd5(img)));
							LogUtil.Log("MiraiCode: " + img.serializeToMiraiCode());
							LogUtil.Log("· -------------------------------- ·");
						} else {
							LogUtil.Log("无法获取当前剪切板的图片！");
						}
					} else if (msg.startsWith("newImg")){
						if (cmd.length >= 5) {
							try { // String 转换到 Integer 如果不是数字居然还会抛出错误，气死我了！！
								StringBuilder content = new StringBuilder();
								for (int i = 4 ; i < cmd.length ; i ++){
									content.append(cmd[i]).append(i == cmd.length - 1 ? "" : " ");
								}
								LogUtil.Log("正在生成文字图片...");
								byte[] file = WordToImage.createImage(content.toString(),
										new Font(ConfigUtil.getConfig("font"), Font.PLAIN, Integer.parseInt(cmd[3])),
										Integer.parseInt(cmd[1]), Integer.parseInt(cmd[2]));
								ExternalResource externalResource = ExternalResource.create(file);
								Image img = Objects.requireNonNull(bot.getGroup(Integer.parseInt(ConfigUtil.getConfig("group"))))
										.uploadImage(externalResource);
								externalResource.close();
								LogUtil.Log("· ------==== Image Info ====------ ·");
								LogUtil.Log("I D: " + img.getImageId());
								LogUtil.Log("URL: " + Mirai.getInstance().queryImageUrl(bot, img));
								LogUtil.Log("MD5: " + Arrays.toString(MessageUtils.calculateImageMd5(img)));
								LogUtil.Log("MiraiCode: " + img.serializeToMiraiCode());
								LogUtil.Log("· -------------------------------- ·");
							} catch (NumberFormatException e) {
								LogUtil.Log("宽度、高度和字体大小必须为整数！");
							}
						} else {
							LogUtil.Log("语法： newImg <宽度> <高度> <字体大小> <内容>");
						}
					} else if (msg.startsWith("del")) {
						if (cmd.length >= 2) {
							try { // String 转换到 Integer 如果不是数字居然还会抛出错误，气死我了！！
								Friend friend = bot.getFriend(Long.parseLong(cmd[1]));
								if (friend != null){
									friend.delete();
									LogUtil.Log("已删除 " + friend.getNick() + "(" + friend.getId() + ")");
								} else {
									LogUtil.Log("你没有这个好友！");
								}
							} catch (NumberFormatException e) {
								LogUtil.Log("\"" + cmd[1] + "\" 不是一个 QQ！");
							}
						} else {
							LogUtil.Log("语法: del <QQ>");
						}
					} else if (msg.startsWith("recall")) {
						if (cmd.length >= 2) {
							try {
								MessageSource message = EventListener.getMessages(Integer.parseInt(cmd[1]) - 1);
								if (message != null) {
									if (message.getFromId() == message.getBotId()) {
										try {
											Mirai.getInstance().recallMessage(bot, message);
											LogUtil.Log("已撤回该消息！");
										} catch (Exception e) {
											LogUtil.Log("无法撤回该消息！");
										}
									} else {
										assert group != null;
										if (group.getBotPermission() != MemberPermission.MEMBER) {
											try {
												Mirai.getInstance().recallMessage(bot, message);
												LogUtil.Log("已撤回该消息！");
											} catch (Exception e) {
												LogUtil.Log("无法撤回该消息！");
											}
										} else {
											LogUtil.Log("该消息不是你发出的且你不是管理员！");
										}
									}
								} else {
									LogUtil.Log("该消息不存在！");
								}
							} catch (NumberFormatException e) {
								LogUtil.Log("消息位置必须是整数！");
							}
						} else {
							LogUtil.Log("语法：recall <消息ID>");
						}
					} else {
						MessageChain send = MiraiCode.deserializeMiraiCode(msg);
						Objects.requireNonNull(bot.getGroup(Integer.parseInt(ConfigUtil.getConfig("group")))).sendMessage(send);
					}
				}
			}
		} catch (NumberFormatException e) {
			LogUtil.Log("请检查配置文件中的 QQ号 是否正确！");
			System.exit(-1);
		} catch (Exception e) {
			LogUtil.Log("出现错误！进程即将终止！ 请检查配置文件是否正确！");
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
							"# 输入你的 QQ" + "\n" +
							"qq=" + "\n" +
							"# 输入你的 QQ 密码" + "\n" +
							"password=" + "\n" +
							"# 输入你要聊天的聊群" + "\n" +
							"group=" + "\n" +
							"# 输入你接收的好友信息（“*” 为 全部）" + "\n" +
							"friend=*" + "\n" +
							"# 输入使用“newImg”指令生成的字体" + "\n" +
							"font=微软雅黑" + "\n" +
							"# 使用的登录协议（PAD: 平板，WATCH: 手表，PHONE: 手机），默认 PHONE" + "\n" +
							"protocol=PHONE" + "\n" +
							"# 是否启用 Debug 模式（即显示 MiraiCode）" + "\n" +
							"debug=false" + "\n" +
							"\n" +
							"# ----=== MiraiBot ===----" + "\n" +
							"# 使用“help”获取帮助！" + "\n" +
							"# -----------------------------";
					fos.write(config.getBytes(StandardCharsets.UTF_8));
					fos.flush();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return false;
		}
		return true;
	}
}
