import net.mamoe.mirai.Mirai;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListenerHost;
import net.mamoe.mirai.event.events.*;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageSource;
import net.mamoe.mirai.message.data.QuoteReply;

import java.util.Objects;
import java.util.regex.Pattern;

public class EventListener implements ListenerHost {
	public static final MessageSource[] messages = new MessageSource[1024];
	private final int[] o = {0};
	private final String[] oldImg = {""};
	private final String[] old = {""};
	
	public static MessageSource getMessages(int key){
		return messages[key];
	}
	
	@EventHandler
	public void onInvited(BotInvitedJoinGroupRequestEvent event){
		event.accept();
		LogUtil.Log("机器人已接受 " + event.getInvitorNick() + "(" + event.getInvitorId() + ")" +
				" 的邀请，加入了聊群 " + event.getGroupName() + "(" + event.getGroupId() + ")");
	}
	@EventHandler
	public void onNewFriend(NewFriendRequestEvent event){
		event.accept();
		LogUtil.Log("机器人已接受 " + event.getFromNick() + "(" + event.getFromId() + ")" + " 的好友申请，");
		LogUtil.Log("其添加好友的信息为：" + event.getMessage());
	}
	@EventHandler
	public void onUnmute(BotUnmuteEvent event){
		if (event.getGroup().getId() != Long.parseLong(ConfigUtil.getConfig("group"))) {
			return;
		}
		Objects.requireNonNull(event.getBot().getGroup(Long.parseLong(ConfigUtil.getConfig("group")))).sendMessage(
				new At(event.getOperator().getId()).plus("终于活过来了..."));
	}
	@EventHandler
	public void onPermissionChange(BotGroupPermissionChangeEvent event){
		if (event.getGroup().getId() != Long.parseLong(ConfigUtil.getConfig("group"))) {
			return;
		}
		Objects.requireNonNull(event.getBot().getGroup(Long.parseLong(ConfigUtil.getConfig("group"))))
				.sendMessage("我的权限从" + event.getOrigin() + "被修改成了" + event.getGroup().getBotPermission() + "！");
	}
	@EventHandler
	public void onJoinGroup(BotJoinGroupEvent event){
		event.getGroup().sendMessage(new At(event.getGroup().getOwner().getId()).plus("机器人已成功加入本群！"));
	}
	@EventHandler
	public void onImageUpload(BeforeImageUploadEvent event){
		LogUtil.Log("正在上传图片...");
	}
	@EventHandler
	public void onNameChange(GroupNameChangeEvent event){
		if (event.getGroup().getId() != Long.parseLong(ConfigUtil.getConfig("group"))) {
			return;
		}
		if (event.getOperator() != null){
			event.getGroup().sendMessage(new At(event.getOperator().getId()).plus("改我群昵称干嘛？"));
			event.getGroup().getBotAsMember().setNameCard(event.getOrigin());
			if (event.getOperator().getPermission() != MemberPermission.OWNER &&
					event.getGroup().getBotPermission() != MemberPermission.MEMBER){
				event.getOperator().setNameCard(event.getNew());
			}
		}
	}
	@EventHandler
	public void onGroupMessage(GroupMessageEvent event) {
		if (event.getGroup().getId() != Long.parseLong(ConfigUtil.getConfig("group"))) {
			return;
		}
		String mCode = event.getMessage().serializeToMiraiCode();
		String msg = ConfigUtil.getConfig("debug").equals("true") ?
				event.getMessage().serializeToMiraiCode() : event.getMessage().contentToString();
		
		messages[o[0]] = event.getSource();
		o[0]++;
		if (o[0] == 1024){
			o[0] = 0;
		}
		
		if((msg + "\\*/" + event.getSender().getId()).equals(old[0]) && !mCode.startsWith("[mirai:image:")){
			if (event.getSender().getPermission() != MemberPermission.OWNER &&
					event.getGroup().getBotPermission() != MemberPermission.MEMBER) {
				Mirai.getInstance().recallMessage(event.getBot(), event.getSource());
				event.getSender().mute(10);
				event.getGroup().sendMessage(new At(event.getSender().getId()).plus("禁 止 复 读！！"));
			}
		} else if (mCode.split(":").length >= 3 && mCode.split(":")[1].equals("image")){
			if (mCode.split(":")[2].equals(oldImg[0])){
				if (event.getSender().getPermission() == MemberPermission.MEMBER &&
						event.getGroup().getBotPermission() != MemberPermission.MEMBER) {
					Mirai.getInstance().recallMessage(event.getBot(), event.getSource());
					event.getSender().mute(10);
				}
				event.getGroup().sendMessage(new At(event.getSender().getId()).plus("发完全一样的图片干嘛？"));
			}
			oldImg[0] = mCode.split(":")[2];
		}
		
		old[0] = msg + "\\*/" + event.getSender().getId();
		oldImg[0] = "";
		
		LogUtil.Log("[" + o[0] + "] " + event.getSender().getNameCard() + " : " + msg);
		if (mCode.split(":").length >= 3 && mCode.split(":")[1].equals("flash")){
			if (event.getSender().getPermission() != MemberPermission.OWNER &&
					event.getGroup().getBotPermission() != MemberPermission.MEMBER) {
				Mirai.getInstance().recallMessage(event.getBot(), event.getSource());
			}
			MessageChain send = MiraiCode.deserializeMiraiCode(mCode.replace("flash", "image"));
			event.getGroup().sendMessage(send);
		}
		String miraiCode = "^(\\[mirai\\\\:)(image|face|flash)(\\\\:[\\w\\W]+)?(\\\\])$";
		if (Pattern.matches(miraiCode, mCode)){
			event.getGroup().sendMessage(
					new QuoteReply(event.getSource())
							.plus("检测到 MiraiCode ：\n")
							.plus(MiraiCode.deserializeMiraiCode(mCode
									.replace("\\[", "[")
									.replace("\\]", "]")
									.replace("\\r", "\r")
									.replace("\\n", "\n")
									.replace("\\:", ":")
									.replace("\\,", ",")
							))
			);
		}
		
		if (mCode.startsWith("[mirai:at:" + event.getBot().getId() + "] ")){
			String[] cmd = mCode.split(" ");
			if (cmd[1].equals("帮助") || cmd[1].equals("help")) {
				String help =
						"""
								· --------====== MiraiBot ======-------- ·
								1. 禁言 <秒>
								 - 禁言自己一段时间
								· -------------------------------------- ·
								""";
				event.getGroup().sendMessage(help);
			} else if (cmd[1].equals("禁言") && cmd.length > 2){
				try {
					if (event.getSender().getPermission() != MemberPermission.OWNER &&
							event.getGroup().getBotPermission() != MemberPermission.MEMBER){
						if (Integer.parseInt(cmd[2]) > 0 && Integer.parseInt(cmd[2]) < 2592000){
							event.getSender().mute(Integer.parseInt(cmd[2]));
							event.getGroup().sendMessage(new QuoteReply(event.getSource()).plus(
									new At(event.getSender().getId()).plus("头一次听说这么奇怪的要求...")));
						} else {
							event.getGroup().sendMessage(new QuoteReply(event.getSource()).plus(
									new At(event.getSender().getId()).plus("这数字不河里啊...!")));
						}
					} else {
						event.getGroup().sendMessage(new QuoteReply(event.getSource()).plus(
								new At(event.getSender().getId()).plus("俺 莫 得 权 限")));
					}
				} catch (NumberFormatException e) {
					event.getGroup().sendMessage(new QuoteReply(event.getSource()).plus(
							new At(event.getSender().getId()).plus("\"" + cmd[2] + "\" 是多少秒...")));
				}
			}
		}
		
		if (msg.startsWith("冒泡")) {
			event.getGroup().sendMessage("戳泡");
			LogUtil.Log(event.getBot().getNick() + " : " + "戳泡");
		} else if (msg.startsWith("戳") && msg.length() >= 2) {
			String stamp = "戳“" + event.getMessage().serializeToMiraiCode() + "”";
			event.getGroup().sendMessage(MiraiCode.deserializeMiraiCode(stamp));
			LogUtil.Log(event.getBot().getNick() + " : " + "戳 “" + msg + "”");
		}
	}
	@EventHandler
	public void onFriendMessage(FriendMessageEvent event){
		if (!(ConfigUtil.getConfig("friend").equals("*") ||
				event.getSender().getId() == Long.parseLong(ConfigUtil.getConfig("friend")))) {
			return;
		}
		String msg = ConfigUtil.getConfig("debug").equals("true") ?
				event.getMessage().plus("").serializeToMiraiCode() : event.getMessage().contentToString();
		LogUtil.Log(event.getSender().getNick() + " -> " + event.getBot().getNick() + " " + msg);
	}
	@EventHandler
	public void onTempMessage(GroupTempMessageEvent event){
		if (!(ConfigUtil.getConfig("friend").equals("*") ||
				event.getSender().getId() == Long.parseLong(ConfigUtil.getConfig("friend")))){
			return;
		}
		String msg = ConfigUtil.getConfig("debug").equals("true") ?
				event.getMessage().plus("").serializeToMiraiCode() : event.getMessage().contentToString();
		LogUtil.Log(event.getSender().getNick() + "(" + event.getSender().getId() + ")" + " -> " + event.getBot().getNick() + " " + msg);
	}
	@EventHandler
	public void onStrangerMessage(StrangerMessageEvent event){
		if (!(ConfigUtil.getConfig("friend").equals("*") ||
				event.getSender().getId() == Long.parseLong(ConfigUtil.getConfig("friend")))){
			return;
		}
		String msg = ConfigUtil.getConfig("debug").equals("true") ?
				event.getMessage().plus("").serializeToMiraiCode() : event.getMessage().contentToString();
		LogUtil.Log(event.getSender().getNick() + "(" + event.getSender().getId() + ")" + " -> " + event.getBot().getNick() + " " + msg);
	}
	@EventHandler
	public void onJoinRequest(MemberJoinRequestEvent event){
		if (Objects.requireNonNull(event.getGroup()).getId() != Long.parseLong(ConfigUtil.getConfig("group"))){
			return;
		}
		Group group = Objects.requireNonNull(event.getBot().getGroup(Long.parseLong(ConfigUtil.getConfig("group"))));
		Objects.requireNonNull(group.sendMessage(
				new At(group.getOwner().getId()) +
						"有新人申请加入本群！\n" +
						"QQ: " + event.getFromId() +
						"昵称: " + event.getFromNick() +
						"问答: \n   " + event.getMessage()
		));
	}
	
	@EventHandler
	public void onJoin(MemberJoinEvent event){
		if (Objects.requireNonNull(event.getGroup()).getId() != Long.parseLong(ConfigUtil.getConfig("group"))){
			return;
		}
		event.getMember().setNameCard("[新人]" + event.getMember().getNick());
	}
}
