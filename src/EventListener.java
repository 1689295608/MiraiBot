import net.mamoe.mirai.Mirai;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListenerHost;
import net.mamoe.mirai.event.events.*;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageSource;
import net.mamoe.mirai.message.data.QuoteReply;
import net.mamoe.mirai.utils.ExternalResource;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

public class EventListener implements ListenerHost {
	public static boolean showQQ;
	public static File autoRespond;
	public static ArrayList<MessageSource> messages = new ArrayList<>();
	public static ArrayList<MemberJoinRequestEvent> requests = new ArrayList<>();
	
	@EventHandler
	public void onGroupJoin(MemberJoinEvent event) {
		if (event.getGroup().getId() != Long.parseLong(ConfigUtil.getConfig("group"))) {
			return;
		}
		LogUtil.log(ConfigUtil.getLanguage("joined.group")
				.replaceAll("\\$1", event.getMember().getNameCard())
				.replaceAll("\\$2", String.valueOf(event.getMember().getId()))
				.replaceAll("\\$3", event.getGroup().getName())
				.replaceAll("\\$4", String.valueOf(event.getGroupId()))
		);
	}
	
	@EventHandler
	public void onGroupLeave(MemberLeaveEvent.Quit event) {
		if (event.getGroup().getId() != Long.parseLong(ConfigUtil.getConfig("group"))) {
			return;
		}
		LogUtil.log(ConfigUtil.getLanguage("left.group")
				.replaceAll("\\$1", event.getMember().getNick())
				.replaceAll("\\$2", String.valueOf(event.getMember().getId()))
				.replaceAll("\\$3", event.getGroup().getName())
				.replaceAll("\\$4", String.valueOf(event.getGroupId()))
		);
	}
	
	@EventHandler
	public void onGroupKick(MemberLeaveEvent.Kick event) {
		if (event.getGroup().getId() != Long.parseLong(ConfigUtil.getConfig("group"))) {
			return;
		}
		Member operator = event.getOperator();
		LogUtil.log(ConfigUtil.getLanguage("kick.group")
				.replaceAll("\\$1", event.getMember().getNick())
				.replaceAll("\\$2", String.valueOf(event.getMember().getId()))
				.replaceAll("\\$3", operator != null ? operator.getNameCard() : event.getBot().getNick())
				.replaceAll("\\$4", String.valueOf(operator != null ? operator.getId() : event.getBot().getId()))
				.replaceAll("\\$5", event.getGroup().getName())
				.replaceAll("\\$6", String.valueOf(event.getGroupId()))
		);
	}
	
	@EventHandler
	public void onJoinRequest(MemberJoinRequestEvent event) {
		if (event.getGroup() == null) return;
		if (event.getGroup().getId() != Long.parseLong(ConfigUtil.getConfig("group"))) {
			return;
		}
		requests.add(event);
		LogUtil.log("[" + requests.size() + "] " + ConfigUtil.getLanguage("join.request.group")
				.replaceAll("\\$1", event.getFromNick())
				.replaceAll("\\$2", String.valueOf(event.getFromId()))
				.replaceAll("\\$3", event.getGroup().getName())
				.replaceAll("\\$4", String.valueOf(event.getGroupId()))
		);
	}
	
	@EventHandler
	public void onGroupRecall(MessageRecallEvent.GroupRecall event) {
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
			if (operator.getId() == sender.getId()) {
				if (id != -1) {
					LogUtil.log(ConfigUtil.getLanguage("recall.message")
							.replaceAll("\\$1", operator.getNameCard() + showQQ(operator.getId()))
							.replaceAll("\\$2", String.valueOf(id)));
				} else {
					LogUtil.log(ConfigUtil.getLanguage("recall.unknown.message")
							.replaceAll("\\$1", operator.getNameCard() + showQQ(operator.getId())));
				}
			} else {
				if (id != -1) {
					LogUtil.log(ConfigUtil.getLanguage("recall.others.message")
							.replaceAll("\\$1", operator.getNameCard() + showQQ(operator.getId()))
							.replaceAll("\\$2", sender.getNameCard() + showQQ(sender.getId()))
							.replaceAll("\\$3", String.valueOf(id)));
				} else {
					LogUtil.log(ConfigUtil.getLanguage("recall.others.unknown.message")
							.replaceAll("\\$1", operator.getNameCard() + showQQ(operator.getId()))
							.replaceAll("\\$2", String.valueOf(id)));
				}
			}
		} else {
			if (id != -1) {
				LogUtil.log(ConfigUtil.getLanguage("recall.message")
						.replaceAll("\\$1", event.getBot().getNick() + showQQ(event.getBot().getId()))
						.replaceAll("\\$2", String.valueOf(id)));
			} else {
				LogUtil.log(ConfigUtil.getLanguage("recall.unknown.message")
						.replaceAll("\\$1", event.getBot().getNick() + showQQ(event.getBot().getId())));
			}
		}
	}
	
	@EventHandler
	public void onFriendRecall(MessageRecallEvent.FriendRecall event) {
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
		Friend operator = event.getOperator();
		if (id != -1) {
			LogUtil.log(ConfigUtil.getLanguage("recall.message")
					.replaceAll("\\$1", operator.getNick() + showQQ(operator.getId()))
					.replaceAll("\\$2", String.valueOf(id)));
		} else {
			LogUtil.log(ConfigUtil.getLanguage("recall.unknown.message")
					.replaceAll("\\$1", operator.getNick() + showQQ(operator.getId())));
		}
	}
	
	@EventHandler
	public void onGroupPostSend(GroupMessagePostSendEvent event) {
		MessageReceipt<Group> receipt = event.getReceipt();
		if (receipt != null) {
			messages.add(receipt.getSource());
			LogUtil.log("[" + messages.size() + "] " + event.getBot().getNick() + showQQ(event.getBot().getId()) + " : " +
					(ConfigUtil.getConfig("debug").equals("true") ?
							event.getMessage().serializeToMiraiCode() : event.getMessage().contentToString()));
		} else {
			LogUtil.log(event.getBot().getNick() + showQQ(event.getBot().getId()) + " : " +
					(ConfigUtil.getConfig("debug").equals("true") ?
							event.getMessage().serializeToMiraiCode() : event.getMessage().contentToString()));
		}
	}
	
	@EventHandler
	public void onFriendPostSend(FriendMessagePostSendEvent event) {
		if (event.getReceipt() != null) {
			messages.add(event.getReceipt().getSource());
			LogUtil.log("[" + messages.size() + "] " + event.getBot().getNick() + " -> " +
					event.getTarget().getNick() + showQQ(event.getTarget().getId()) +
					(ConfigUtil.getConfig("debug").equals("true") ?
							event.getMessage().serializeToMiraiCode() : event.getMessage().contentToString()));
		} else {
			LogUtil.log(event.getBot().getNick() + " -> " + event.getTarget().getNick() + showQQ(event.getTarget().getId()) +
					(ConfigUtil.getConfig("debug").equals("true") ?
							event.getMessage().serializeToMiraiCode() : event.getMessage().contentToString()));
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
	public void onFriendMessage(FriendMessageEvent event) {
		if (!(ConfigUtil.getConfig("friend").equals("*") ||
				event.getSender().getId() == Long.parseLong(ConfigUtil.getConfig("friend")))) {
			return;
		}
		String msg = ConfigUtil.getConfig("debug").equals("true") ?
				event.getMessage().plus("").serializeToMiraiCode() : event.getMessage().contentToString();
		LogUtil.log(event.getSender().getNick() + showQQ(event.getSender().getId()) + "-> " + event.getBot().getNick() + " " + msg);
	}
	
	@EventHandler
	public void onTempMessage(GroupTempMessageEvent event) {
		if (!(ConfigUtil.getConfig("friend").equals("*") ||
				event.getSender().getId() == Long.parseLong(ConfigUtil.getConfig("friend")))) {
			return;
		}
		String msg = ConfigUtil.getConfig("debug").equals("true") ?
				event.getMessage().plus("").serializeToMiraiCode() : event.getMessage().contentToString();
		LogUtil.log(event.getSender().getNick() + showQQ(event.getSender().getId()) + "-> " + event.getBot().getNick() + " " + msg);
	}
	
	@EventHandler
	public void onStrangerMessage(StrangerMessageEvent event) {
		if (!(ConfigUtil.getConfig("friend").equals("*") ||
				event.getSender().getId() == Long.parseLong(ConfigUtil.getConfig("friend")))) {
			return;
		}
		String msg = ConfigUtil.getConfig("debug").equals("true") ?
				event.getMessage().plus("").serializeToMiraiCode() : event.getMessage().contentToString();
		LogUtil.log(event.getSender().getNick() + showQQ(event.getSender().getId()) + "-> " + event.getBot().getNick() + " " + msg);
	}
	
	public String showQQ(Long qq) {
		return showQQ ? "(" + qq + ")" : "";
	}
	
	/**
	 * Replace all placeholders with corresponding values
	 * @param event GroupMessageEvent
	 * @param str Text to replace
	 * @return Replaced text
	 */
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
		str = str.replaceAll("%file_id%", spl[1].equals("file") ? spl[2].substring(0, spl[2].indexOf("]")) : "");
		try {
			URL url = new URL(event.getSender().getAvatarUrl());
			InputStream is = url.openStream();
			BufferedInputStream bis = new BufferedInputStream(is);
			byte[] avatar = bis.readAllBytes();
			ExternalResource externalResource = ExternalResource.create(avatar);
			Image img = event.getGroup().uploadImage(externalResource);
			externalResource.close();
			String[] imgSpl = img.serializeToMiraiCode().split(":");
			str = str.replaceAll("%sender_avatarId%", imgSpl[2].substring(0, imgSpl[2].indexOf("]")));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return str;
	}
}
