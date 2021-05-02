package com.windowx.miraibot;

import net.mamoe.mirai.Mirai;
import net.mamoe.mirai.contact.*;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListenerHost;
import net.mamoe.mirai.event.events.*;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageSource;
import net.mamoe.mirai.message.data.QuoteReply;
import net.mamoe.mirai.utils.ExternalResource;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Pattern;

public class EventListener implements ListenerHost {
	public static boolean showQQ;
	public static File autoRespond;
	public static ArrayList<MessageSource> messages = new ArrayList<>();
	public static final ArrayList<MemberJoinRequestEvent> requests = new ArrayList<>();
	public static JSONObject autoRespondConfig;
	
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
	
	private void logMute(Member op, Member member, Group group, int time) {
		if (op == null) {
			op = group.getBotAsMember();
		}
		if (member == null) {
			member = group.getBotAsMember();
		}
		LogUtil.log(ConfigUtil.getLanguage("member.mute")
				.replaceAll("\\$1", member.getNameCard())
				.replaceAll("\\$2", String.valueOf(member.getId()))
				.replaceAll("\\$3", op.getNameCard())
				.replaceAll("\\$4", String.valueOf(op.getId()))
				.replaceAll("\\$5", String.valueOf(time))
		);
	}
	
	@EventHandler
	public void onMemberMute(MemberMuteEvent event) {
		if (event.getGroup().getId() != Long.parseLong(ConfigUtil.getConfig("group"))) {
			return;
		}
		logMute(event.getOperator(), event.getMember(), event.getGroup(), event.getDurationSeconds());
	}
	
	@EventHandler
	public void onBotMute(BotMuteEvent event) {
		if (event.getGroup().getId() != Long.parseLong(ConfigUtil.getConfig("group"))) {
			return;
		}
		logMute(event.getOperator(), null, event.getGroup(), event.getDurationSeconds());
	}
	
	@EventHandler
	public void onMuteAll(GroupMuteAllEvent event) {
		if (event.getGroup().getId() != Long.parseLong(ConfigUtil.getConfig("group"))) {
			return;
		}
		LogUtil.log(ConfigUtil.getLanguage("mute.all") +
				(event.getNew() ? ConfigUtil.getConfig("on") : ConfigUtil.getConfig("off")));
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
		try {
			for (Iterator<String> it = autoRespondConfig.keys(); it.hasNext(); ) {
				String section = it.next();
				try {
					if (section.startsWith("**")) {
						continue;
					}
					try {
						JSONObject sectionObject = autoRespondConfig.getJSONObject(section);
						String regex = sectionObject.has("Message") ? sectionObject.getString("Message") : null;
						String respond = sectionObject.has("Respond") ? sectionObject.getString("Respond") : null;
						boolean reply = sectionObject.has("Reply") && sectionObject.getBoolean("Reply");
						boolean recall = sectionObject.has("Recall") && sectionObject.getBoolean("Recall");
						int mute = sectionObject.has("Mute") ? sectionObject.getInt("Mute") : 0;
						String runCmd = sectionObject.has("RunCommand") ? sectionObject.getString("RunCommand") : null;
					
						respond = replacePlaceholder(event, respond);
						regex = replacePlaceholder(event, regex);
						if (!Pattern.matches(regex, mCode)) {
							continue;
						}
						if (mute != 0) {
							if (event.getSender().getPermission() != MemberPermission.OWNER &&
									event.getGroup().getBotPermission() != MemberPermission.MEMBER) {
								try {
									event.getSender().mute(mute);
								} catch (Exception e) {
									LogUtil.log(ConfigUtil.getLanguage("no.permission"));
								}
							}
						}
						if (recall) {
							try {
								Mirai.getInstance().recallMessage(event.getBot(), event.getSource());
							} catch (Exception e) {
								LogUtil.log(ConfigUtil.getLanguage("no.permission"));
							}
						}
						try {
							if (reply) {
								event.getGroup().sendMessage(new QuoteReply(event.getSource()).plus(
										MiraiCode.deserializeMiraiCode(respond)));
							} else {
								event.getGroup().sendMessage(MiraiCode.deserializeMiraiCode(respond));
							}
						} catch (BotIsBeingMutedException e) {
							LogUtil.log(ConfigUtil.getLanguage("bot.is.being.muted"));
						}
						if (runCmd != null && !runCmd.isEmpty()) {
							try {
								PluginMain.runCommand(replacePlaceholder(event, runCmd));
							} catch (Exception ignored) { }
						}
					} catch (JSONException ignored) { }
				} catch (Exception e) {
					LogUtil.log(ConfigUtil.getLanguage("unknown.error"));
					e.printStackTrace();
					System.exit(-1);
				}
			}
		} catch (Exception e) {
			LogUtil.log(e.toString());
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
		str = str.replaceAll("%sender_name_card%", event.getSender().getNameCard());
		str = str.replaceAll("%group_name%", event.getGroup().getName());
		str = str.replaceAll("%group_id%", String.valueOf(event.getSender().getId()));
		str = str.replaceAll("%group_owner_nick%", event.getGroup().getOwner().getNick());
		str = str.replaceAll("%group_owner_id%", String.valueOf(event.getGroup().getOwner().getId()));
		str = str.replaceAll("%group_owner_name_card%", event.getGroup().getOwner().getNameCard());
		str = str.replaceAll("%message_mirai_code%", event.getMessage().serializeToMiraiCode());
		str = str.replaceAll("%message_content%", event.getMessage().contentToString());
		str = str.replaceAll("%bot_nick%", event.getBot().getNick());
		str = str.replaceAll("%bot_id%", String.valueOf(event.getBot().getId()));
		String flashId = "";
		String imageId = "";
		String fileId = "";
		if (spl.length > 3) {
			switch (spl[1]) {
				case "flash":
					flashId = spl[2].substring(0, spl[2].indexOf("]"));
					break;
				case "image":
					imageId = spl[2].substring(0, spl[2].indexOf("]"));
					break;
				case "file":
					fileId = spl[2].substring(0, spl[2].indexOf("]"));
					break;
			}
		}
		str = str.replaceAll("%flash_id%", flashId);
		str = str.replaceAll("%image_id%", imageId);
		str = str.replaceAll("%file_id%", fileId);
		if (str.contains("%sender_avatar_id%")) {
			try {
				URL url = new URL(event.getSender().getAvatarUrl());
				InputStream is = url.openStream();
				BufferedInputStream bis = new BufferedInputStream(is);
				byte[] avatar = bis.readAllBytes();
				ExternalResource externalResource = ExternalResource.create(avatar);
				Image img = event.getGroup().uploadImage(externalResource);
				externalResource.close();
				String[] imgSpl = img.serializeToMiraiCode().split(":");
				str = str.replaceAll("%sender_avatar_id%", imgSpl[2].substring(0, imgSpl[2].indexOf("]")));
			} catch (IOException e) {
				LogUtil.log(e.toString());
			}
		}
		return str;
	}
}
