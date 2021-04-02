import net.mamoe.mirai.Mirai;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListenerHost;
import net.mamoe.mirai.event.events.*;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.QuoteReply;

import java.io.File;

public class EventListener implements ListenerHost {
	public static boolean showQQ;
	public static File autoRespond;
	public static MessageData messages;
	
	@EventHandler
	public void onInvited(BotInvitedJoinGroupRequestEvent event){
		if (ConfigUtil.getConfig("inviteAccept").equals("true")){
			event.accept();
			LogUtil.log("机器人已接受 " + event.getInvitorNick() + showQQ(event.getInvitorId()) +
					" 的邀请，加入了聊群 " + event.getGroupName() + "(" + event.getGroupId() + ")");
		}
	}
	@EventHandler
	public void onNewFriend(NewFriendRequestEvent event){
		if (ConfigUtil.getConfig("inviteAccept").equals("true")) {
			event.accept();
			LogUtil.log("机器人已接受 " + event.getFromNick() + showQQ(event.getFromId()) + "的好友申请，");
			LogUtil.log("其添加好友的信息为：" + event.getMessage());
		}
	}
	@EventHandler
	public void onGroupRecall(MessageRecallEvent.GroupRecall event){
		if (event.getGroup().getId() != Long.parseLong(ConfigUtil.getConfig("group"))) {
			return;
		}
		Member operator = event.getOperator();
		Member sender = event.getAuthor();
		int id = -1;
		for (int i = 0; i < messages.size(); i++) {
			if (messages.get(i).getTime() == event.getMessageTime()) {
				id = i + 1;
			}
		}
		if (operator != null) {
			if (operator.getId() == sender.getId()){
				if (id != -1) {
					LogUtil.log(operator.getNameCard() + showQQ(operator.getId()) + "撤回了 [" + id + "] 消息");
				} else {
					LogUtil.log(operator.getNameCard() + showQQ(operator.getId()) + "撤回了一条消息");
				}
			} else {
				if (id != -1) {
					LogUtil.log(operator.getNameCard() + showQQ(operator.getId()) + "撤回了一条 " +
							sender.getNameCard() + showQQ(sender.getId()) + "的 [" + id + "] 消息");
				} else {
					LogUtil.log(operator.getNameCard() + showQQ(operator.getId()) + "撤回了一条 " +
						sender.getNameCard() + showQQ(sender.getId()) + "的消息");
				}
			}
		} else {
			if (id != -1) {
				LogUtil.log(event.getBot().getNick() + showQQ(event.getBot().getId()) + "撤回了 [" + id + "] 消息");
			} else {
				LogUtil.log(event.getBot().getNick() + showQQ(event.getBot().getId()) + "撤回了一条消息");
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
		for (int i = 0; i < messages.size(); i++) {
			if (messages.get(i).getTime() == event.getMessageTime()) {
				id = i + 1;
			}
		}
		if (id != -1) {
			LogUtil.log(event.getOperator().getNick() + showQQ(event.getOperator().getId()) + "撤回了 [" + id + "] 消息");
		} else {
			LogUtil.log(event.getOperator().getNick() + showQQ(event.getOperator().getId()) + "撤回了一条消息");
		}
	}
	@EventHandler
	public void onGroupPostSend(GroupMessagePostSendEvent event) {
		LogUtil.log(event.getBot().getNick() + " : " +
				(ConfigUtil.getConfig("debug").equals("true") ?
						event.getMessage().serializeToMiraiCode() : event.getMessage().contentToString()));
	}
	@EventHandler
	public void onFriendPostSend(FriendMessagePostSendEvent event) {
		LogUtil.log(event.getBot().getNick() + " -> " + event.getTarget().getNick() + showQQ(event.getTarget().getId()) +
				(ConfigUtil.getConfig("debug").equals("true") ?
					event.getMessage().serializeToMiraiCode() : event.getMessage().contentToString()));
	}
	@EventHandler
	public void onImageUpload(BeforeImageUploadEvent event){
		LogUtil.log("正在上传图片...");
	}
	@EventHandler
	public void onGroupMessage(GroupMessageEvent event) {
		if (event.getGroup().getId() != Long.parseLong(ConfigUtil.getConfig("group"))) {
			return;
		}
		String mCode = event.getMessage().serializeToMiraiCode();
		String msg = ConfigUtil.getConfig("debug").equals("true") ?
				event.getMessage().serializeToMiraiCode() : event.getMessage().contentToString();
		
		messages.add(event.getSource());
		LogUtil.log("[" + messages.size() + "] " + event.getSender().getNameCard() + showQQ(event.getSender().getId()) + ": " + msg);
		
		for (String section : IniUtil.getSectionNames()) {
			if (section != null) {
				String regex = IniUtil.getValue(section, "Message");
				String respond = IniUtil.getValue(section, "Respond");
				if (respond != null && regex != null) {
					respond = replacePlaceholder(event, respond);
					if (mCode.matches(replacePlaceholder(event, regex))) {
						boolean reply = IniUtil.getValue(section, "Reply") != null &&
								Boolean.parseBoolean(IniUtil.getValue(section, "Reply"));
						boolean recall = IniUtil.getValue(section, "Recall") != null &&
								Boolean.parseBoolean(IniUtil.getValue(section, "Recall"));
						String mute = IniUtil.getValue(section, "Mute");
						if (mute != null && !mute.equals("0")) {
							if (event.getSender().getPermission() != MemberPermission.OWNER &&
									event.getGroup().getBotPermission() != MemberPermission.MEMBER) {
								try {
									event.getSender().mute(Integer.parseInt(mute));
								} catch (NumberFormatException e) {
									e.printStackTrace();
								}
							}
						}
						if (recall) {
							Mirai.getInstance().recallMessage(event.getBot(), event.getSource());
						}
						if (reply) {
							event.getGroup().sendMessage(new QuoteReply(event.getSource()).plus(
									MiraiCode.deserializeMiraiCode(respond)));
						} else {
							event.getGroup().sendMessage(MiraiCode.deserializeMiraiCode(respond));
						}
					}
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
		LogUtil.log(event.getSender().getNick() + showQQ(event.getSender().getId()) + "-> " + event.getBot().getNick() + " " + msg);
	}
	@EventHandler
	public void onTempMessage(GroupTempMessageEvent event){
		if (!(ConfigUtil.getConfig("friend").equals("*") ||
				event.getSender().getId() == Long.parseLong(ConfigUtil.getConfig("friend")))){
			return;
		}
		String msg = ConfigUtil.getConfig("debug").equals("true") ?
				event.getMessage().plus("").serializeToMiraiCode() : event.getMessage().contentToString();
		LogUtil.log(event.getSender().getNick() + showQQ(event.getSender().getId()) + "-> " + event.getBot().getNick() + " " + msg);
	}
	@EventHandler
	public void onStrangerMessage(StrangerMessageEvent event){
		if (!(ConfigUtil.getConfig("friend").equals("*") ||
				event.getSender().getId() == Long.parseLong(ConfigUtil.getConfig("friend")))){
			return;
		}
		String msg = ConfigUtil.getConfig("debug").equals("true") ?
				event.getMessage().plus("").serializeToMiraiCode() : event.getMessage().contentToString();
		LogUtil.log(event.getSender().getNick() + showQQ(event.getSender().getId()) + "-> " + event.getBot().getNick() + " " + msg);
	}
	
	public String showQQ(Long qq){
		if (showQQ) {
			return "(" + qq + ") ";
		}
		return " ";
	}
	
	public String replacePlaceholder(GroupMessageEvent event, String str) {
		String[] spl = event.getMessage().serializeToMiraiCode().split(":");
		str = str.replaceAll("%sender_nick%", event.getSender().getNick());
		str = str.replaceAll("%sender_id%", String.valueOf(event.getSender().getId()));
		str = str.replaceAll("%sender_nameCard%", event.getSender().getNameCard());
		str = str.replaceAll("%group_name%", event.getGroup().getName());
		str = str.replaceAll("%group_id%", String.valueOf(event.getSender().getId()));
		str = str.replaceAll("%group_owner_nick%", event.getGroup().getOwner().getNick());
		str = str.replaceAll("%group_owner_id%", String.valueOf(event.getGroup().getOwner().getId()));
		str = str.replaceAll("%group_owner_nameCard%", event.getGroup().getOwner().getNameCard());
		str = str.replaceAll("%message_miraiCode%", event.getMessage().serializeToMiraiCode());
		str = str.replaceAll("%message_content%", event.getMessage().contentToString());
		str = str.replaceAll("%bot_nick%", event.getBot().getNick());
		str = str.replaceAll("%bot_id%", String.valueOf(event.getBot().getId()));
		str = str.replaceAll("%flash_id%", spl[1].equals("flash") ? spl[2].substring(0, spl[2].indexOf("]")) : "");
		str = str.replaceAll("%image_id%", spl[1].equals("image") ? spl[2].substring(0, spl[2].indexOf("]")) : "");
		return str;
	}
}
