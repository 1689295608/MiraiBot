import net.mamoe.mirai.Mirai;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListenerHost;
import net.mamoe.mirai.event.events.*;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageSource;
import net.mamoe.mirai.message.data.QuoteReply;

public class EventListener implements ListenerHost {
	public static final MessageSource[] messages = new MessageSource[1024];
	public static final int[] messageI = {0};
	public static boolean showQQ;
	
	@EventHandler
	public void onInvited(BotInvitedJoinGroupRequestEvent event){
		if (ConfigUtil.getConfig("inviteAccept").equals("true")){
			event.accept();
			LogUtil.Log("机器人已接受 " + event.getInvitorNick() + showQQ(event.getInvitorId()) +
					" 的邀请，加入了聊群 " + event.getGroupName() + "(" + event.getGroupId() + ")");
		}
	}
	@EventHandler
	public void onNewFriend(NewFriendRequestEvent event){
		if (ConfigUtil.getConfig("inviteAccept").equals("true")) {
			event.accept();
			LogUtil.Log("机器人已接受 " + event.getFromNick() + showQQ(event.getFromId()) + "的好友申请，");
			LogUtil.Log("其添加好友的信息为：" + event.getMessage());
		}
	}
	@EventHandler
	public void onGroupRecall(MessageRecallEvent.GroupRecall event){
		if (event.getGroup().getId() != Long.parseLong(ConfigUtil.getConfig("group"))) {
			return;
		}
		Member operator = event.getOperator();
		Member sender = event.getAuthor();
		if (operator != null) {
			int id = -1;
			for (int i = 0; i < messageI[0]; i++) {
				if (messages[i].getTime() == event.getMessageTime()) {
					id = i + 1;
				}
			}
			if (operator.getId() == sender.getId()){
				if (id != -1) {
					LogUtil.Log(operator.getNameCard() + showQQ(operator.getId()) + "撤回了 [" + id + "] 消息");
				} else {
					LogUtil.Log(operator.getNameCard() + showQQ(operator.getId()) + "撤回了一条消息");
				}
			} else {
				if (id != -1) {
					LogUtil.Log(operator.getNameCard() + showQQ(operator.getId()) + "撤回了一条 " +
							sender.getNameCard() + showQQ(sender.getId()) + "的 [" + id + "] 消息");
				} else {
					LogUtil.Log(operator.getNameCard() + showQQ(operator.getId()) + "撤回了一条 " +
						sender.getNameCard() + showQQ(sender.getId()) + "的消息");
				}
			}
		}
	}
	@EventHandler
	public void onFriendRecall(MessageRecallEvent.FriendRecall event){
		if (!(ConfigUtil.getConfig("friend").equals("*") ||
				event.getOperator().getId() == Long.parseLong(ConfigUtil.getConfig("friend")))) {
			return;
		}
		int id = -1;
		for (int i = 0; i < messageI[0]; i++) {
			if (messages[i].getTime() == event.getMessageTime()) {
				id = i + 1;
			}
		}
		if (id != -1) {
			LogUtil.Log(event.getOperator().getNick() + showQQ(event.getOperator().getId()) + "撤回了 [" + id + "] 消息");
		} else {
			LogUtil.Log(event.getOperator().getNick() + showQQ(event.getOperator().getId()) + "撤回了一条消息");
		}
	}
	@EventHandler
	public void onGroupPostSend(GroupMessagePostSendEvent event) {
		LogUtil.Log(event.getBot().getNick() + " : " +
				(ConfigUtil.getConfig("debug").equals("true") ?
						event.getMessage().serializeToMiraiCode() : event.getMessage().contentToString()));
	}
	@EventHandler
	public void onFriendPostSend(FriendMessagePostSendEvent event) {
		LogUtil.Log(event.getBot().getNick() + " -> " + event.getTarget().getNick() + showQQ(event.getTarget().getId()) +
				(ConfigUtil.getConfig("debug").equals("true") ?
					event.getMessage().serializeToMiraiCode() : event.getMessage().contentToString()));
	}
	@EventHandler
	public void onImageUpload(BeforeImageUploadEvent event){
		LogUtil.Log("正在上传图片...");
	}
	@EventHandler
	public void onGroupMessage(GroupMessageEvent event) {
		if (event.getGroup().getId() != Long.parseLong(ConfigUtil.getConfig("group"))) {
			return;
		}
		String mCode = event.getMessage().serializeToMiraiCode();
		String msg = ConfigUtil.getConfig("debug").equals("true") ?
				event.getMessage().serializeToMiraiCode() : event.getMessage().contentToString();
		
		messages[messageI[0]] = event.getSource();
		messageI[0]++;
		if (messageI[0] == 1024){
			messageI[0] = 0;
		}
		LogUtil.Log("[" + messageI[0] + "] " + event.getSender().getNameCard() + showQQ(event.getSender().getId()) + ": " + msg);
		
		if (mCode.split(":").length >= 3 && mCode.split(":")[1].equals("flash")){
			if (ConfigUtil.getConfig("autoFlash").equals("true")){
				if (event.getSender().getPermission() != MemberPermission.OWNER &&
						event.getGroup().getBotPermission() != MemberPermission.MEMBER) {
					Mirai.getInstance().recallMessage(event.getBot(), event.getSource());
				}
				MessageChain send = MiraiCode.deserializeMiraiCode(mCode.replace("flash", "image"));
				event.getGroup().sendMessage(send);
			}
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
	}
	@EventHandler
	public void onFriendMessage(FriendMessageEvent event){
		if (!(ConfigUtil.getConfig("friend").equals("*") ||
				event.getSender().getId() == Long.parseLong(ConfigUtil.getConfig("friend")))) {
			return;
		}
		String msg = ConfigUtil.getConfig("debug").equals("true") ?
				event.getMessage().plus("").serializeToMiraiCode() : event.getMessage().contentToString();
		LogUtil.Log(event.getSender().getNick() + showQQ(event.getSender().getId()) + "-> " + event.getBot().getNick() + " " + msg);
	}
	@EventHandler
	public void onTempMessage(GroupTempMessageEvent event){
		if (!(ConfigUtil.getConfig("friend").equals("*") ||
				event.getSender().getId() == Long.parseLong(ConfigUtil.getConfig("friend")))){
			return;
		}
		String msg = ConfigUtil.getConfig("debug").equals("true") ?
				event.getMessage().plus("").serializeToMiraiCode() : event.getMessage().contentToString();
		LogUtil.Log(event.getSender().getNick() + showQQ(event.getSender().getId()) + "-> " + event.getBot().getNick() + " " + msg);
	}
	@EventHandler
	public void onStrangerMessage(StrangerMessageEvent event){
		if (!(ConfigUtil.getConfig("friend").equals("*") ||
				event.getSender().getId() == Long.parseLong(ConfigUtil.getConfig("friend")))){
			return;
		}
		String msg = ConfigUtil.getConfig("debug").equals("true") ?
				event.getMessage().plus("").serializeToMiraiCode() : event.getMessage().contentToString();
		LogUtil.Log(event.getSender().getNick() + showQQ(event.getSender().getId()) + "-> " + event.getBot().getNick() + " " + msg);
	}
	
	public String showQQ(Long qq){
		if (showQQ) {
			return "(" + qq + ") ";
		}
		return " ";
	}
}
