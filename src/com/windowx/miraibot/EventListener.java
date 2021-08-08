package com.windowx.miraibot;

import com.windowx.miraibot.plugin.Plugin;
import com.windowx.miraibot.utils.ConfigUtil;
import com.windowx.miraibot.utils.LogUtil;
import net.mamoe.mirai.Mirai;
import net.mamoe.mirai.contact.*;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.EventPriority;
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

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EventListener implements ListenerHost {
	public static final ArrayList<MemberJoinRequestEvent> requests = new ArrayList<>();
	public static boolean showQQ;
	public static File autoRespond;
	public static ArrayList<MessageSource> messages = new ArrayList<>();
	public static JSONObject autoRespondConfig;
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onGroupJoin(MemberJoinEvent event) {
		if (PluginMain.isNotAllowedGroup(event.getGroupId())) {
			event.cancel();
			return;
		}
		if (PluginMain.group != event.getGroup()) {
			return;
		}
		LogUtil.log(ConfigUtil.getLanguage("joined.group")
				, event.getMember().getNameCard()
				, String.valueOf(event.getMember().getId())
				, event.getGroup().getName()
				, String.valueOf(event.getGroupId())
		);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onGroupLeave(MemberLeaveEvent.Quit event) {
		if (PluginMain.isNotAllowedGroup(event.getGroupId())) {
			event.cancel();
			return;
		}
		if (PluginMain.group != event.getGroup()) {
			return;
		}
		LogUtil.log(ConfigUtil.getLanguage("left.group")
				, event.getMember().getNick()
				, String.valueOf(event.getMember().getId())
				, event.getGroup().getName()
				, String.valueOf(event.getGroupId())
		);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onGroupKick(MemberLeaveEvent.Kick event) {
		if (PluginMain.isNotAllowedGroup(event.getGroupId())) {
			event.cancel();
			return;
		}
		if (PluginMain.group != event.getGroup()) {
			return;
		}
		Member operator = event.getOperator();
		LogUtil.log(ConfigUtil.getLanguage("kick.group")
				, event.getMember().getNick()
				, String.valueOf(event.getMember().getId())
				, operator != null ? operator.getNameCard() : event.getBot().getNick()
				, String.valueOf(operator != null ? operator.getId() : event.getBot().getId())
				, event.getGroup().getName(), String.valueOf(event.getGroupId())
		);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onJoinRequest(MemberJoinRequestEvent event) {
		if (PluginMain.isNotAllowedGroup(event.getGroupId()) || event.getGroup() == null) {
			event.cancel();
			return;
		}
		if (PluginMain.group != event.getGroup()) {
			return;
		}
		if (event.getGroup() != null) {
			requests.add(event);
			LogUtil.log(ConfigUtil.getLanguage("join.request.group")
					, String.valueOf(requests.size())
					, event.getFromNick()
					, String.valueOf(event.getFromId())
					, event.getGroup().getName()
					, String.valueOf(event.getGroupId())
			);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onGroupRecall(MessageRecallEvent.GroupRecall event) {
		if (PluginMain.isNotAllowedGroup(event.getGroup().getId())) {
			event.cancel();
			return;
		}
		if (PluginMain.group != event.getGroup()) {
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
					LogUtil.log(ConfigUtil.getLanguage("recall.message"), operator.getNameCard() + showQQ(operator.getId()), String.valueOf(id));
				} else {
					LogUtil.log(ConfigUtil.getLanguage("recall.unknown.message"), operator.getNameCard() + showQQ(operator.getId()));
				}
			} else {
				if (id != -1) {
					LogUtil.log(ConfigUtil.getLanguage("recall.others.message")
							, operator.getNameCard() + showQQ(operator.getId())
							, sender.getNameCard() + showQQ(sender.getId())
							, String.valueOf(id)
					);
				} else {
					LogUtil.log(ConfigUtil.getLanguage("recall.others.unknown.message")
							, operator.getNameCard() + showQQ(operator.getId())
							, String.valueOf(id)
					);
				}
			}
		} else {
			if (id != -1) {
				LogUtil.log(ConfigUtil.getLanguage("recall.message")
						, event.getBot().getNick() + showQQ(event.getBot().getId())
						, String.valueOf(id)
				);
			} else {
				LogUtil.log(ConfigUtil.getLanguage("recall.unknown.message"), event.getBot().getNick() + showQQ(event.getBot().getId()));
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onFriendRecall(MessageRecallEvent.FriendRecall event) {
		if (!(ConfigUtil.getConfig("friend").equals("*") || event.getOperator().getId() == Long.parseLong(ConfigUtil.getConfig("friend")))) {
			event.cancel();
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
			LogUtil.log(ConfigUtil.getLanguage("recall.message"), operator.getNick() + showQQ(operator.getId()), String.valueOf(id));
		} else {
			LogUtil.log(ConfigUtil.getLanguage("recall.unknown.message"), operator.getNick() + showQQ(operator.getId()));
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onGroupPostSend(GroupMessagePostSendEvent event) {
		if (PluginMain.isNotAllowedGroup(event.getTarget().getId())) {
			event.cancel();
			return;
		}
		if (PluginMain.group != event.getTarget()) {
			return;
		}
		MessageReceipt<Group> receipt = event.getReceipt();
		String msg = (ConfigUtil.getConfig("debug").equals("true") ? event.getMessage().serializeToMiraiCode() : event.getMessage().contentToString());
		if (receipt != null) {
			messages.add(receipt.getSource());
			LogUtil.log(ConfigUtil.getLanguage("format.group.recallable.message")
					, String.valueOf(messages.size())
					, event.getBot().getNick()
					, String.valueOf(event.getBot().getId())
					, msg
			);
		} else {
			LogUtil.log(ConfigUtil.getLanguage("format.group.message")
					, event.getBot().getNick()
					, String.valueOf(event.getBot().getId())
					, msg
			);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onFriendPostSend(FriendMessagePostSendEvent event) {
		MessageReceipt<Friend> receipt = event.getReceipt();
		String msg = (ConfigUtil.getConfig("debug").equals("true") ? event.getMessage().serializeToMiraiCode() : event.getMessage().contentToString());
		if (receipt != null) {
			messages.add(receipt.getSource());
			LogUtil.log(ConfigUtil.getLanguage("format.user.recallable.message")
					, String.valueOf(messages.size())
					, event.getBot().getNick()
					, String.valueOf(event.getBot().getId())
					, event.getTarget().getNick()
					, String.valueOf(event.getTarget().getId())
					, msg
			);
		} else {
			LogUtil.log(ConfigUtil.getLanguage("format.user.recallable.message")
					, event.getBot().getNick()
					, String.valueOf(event.getBot().getId())
					, event.getTarget().getNick()
					, String.valueOf(event.getTarget().getId())
					, msg
			);
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
				, member.getNameCard()
				, String.valueOf(member.getId())
				, op.getNameCard()
				, String.valueOf(op.getId())
				, String.valueOf(time)
		);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onMemberMute(MemberMuteEvent event) {
		if (PluginMain.isNotAllowedGroup(event.getGroupId())) {
			event.cancel();
			return;
		}
		if (PluginMain.group != event.getGroup()) {
			return;
		}
		logMute(event.getOperator(), event.getMember(), event.getGroup(), event.getDurationSeconds());
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onBotMute(BotMuteEvent event) {
		if (PluginMain.isNotAllowedGroup(event.getGroupId())) {
			event.cancel();
			return;
		}
		if (PluginMain.group != event.getGroup()) {
			return;
		}
		logMute(event.getOperator(), null, event.getGroup(), event.getDurationSeconds());
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onMuteAll(GroupMuteAllEvent event) {
		if (PluginMain.isNotAllowedGroup(event.getGroupId())) {
			event.cancel();
			return;
		}
		if (PluginMain.group != event.getGroup()) {
			return;
		}
		LogUtil.log(ConfigUtil.getLanguage("mute.all") +
				(event.getNew() ? ConfigUtil.getConfig("on") : ConfigUtil.getConfig("off")));
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onGroupMessage(GroupMessageEvent event) {
		if (PluginMain.isNotAllowedGroup(event.getGroup().getId())) {
			event.cancel();
			return;
		}
		String mCode = event.getMessage().serializeToMiraiCode();
		String msg = ConfigUtil.getConfig("debug").equals("true") ?
				event.getMessage().serializeToMiraiCode() : event.getMessage().contentToString();
		if (PluginMain.group == event.getGroup()) {
			messages.add(event.getSource());
			LogUtil.log(ConfigUtil.getLanguage("format.group.message")
					, String.valueOf(messages.size())
					, event.getSenderName()
					, String.valueOf(event.getSender().getId())
					, msg
			);
		}
		
		for (Plugin p : PluginMain.plugins) {
			if (!p.isEnabled()) continue;
			try {
				p.onGroupMessage(event);
			} catch (Exception e) {
				System.out.println();
				e.printStackTrace();
			}
		}
		
		try {
			for (Iterator<String> it = autoRespondConfig.keys(); it.hasNext(); ) {
				String section = it.next();
				try {
					if (section.startsWith("**")) continue;
					try {
						JSONObject sectionObject = autoRespondConfig.getJSONObject(section);
						String regex = sectionObject.has("Message") ? sectionObject.getString("Message") : "";
						String respond = sectionObject.has("Respond") ? sectionObject.getString("Respond") : "";
						boolean reply = sectionObject.has("Reply") && sectionObject.getBoolean("Reply");
						boolean recall = sectionObject.has("Recall") && sectionObject.getBoolean("Recall");
						int mute = sectionObject.has("Mute") ? sectionObject.getInt("Mute") : 0;
						String runCmd = sectionObject.has("RunCommand") ? sectionObject.getString("RunCommand") : "";
						String changeNameCard = sectionObject.has("ChangeNameCard") ? sectionObject.getString("ChangeNameCard") : null;
						String permission = sectionObject.has("Permission") ? sectionObject.getString("Permission") : "*";
						String noPermission = (sectionObject.has("NoPermission") ? sectionObject.getString("NoPermission") : "");
						String noPermissionMsg = sectionObject.has("NoPermissionMsg") ? sectionObject.getString("NoPermissionMsg") : "";
						String[] owners = ConfigUtil.getConfig("owner").split(",");
						runCmd = mCode.replaceAll(regex, runCmd);
						
						regex = replaceGroupMsgPlaceholder(event, regex);
						respond = replaceGroupMsgPlaceholder(event, mCode.replaceAll(regex, respond));
						if (!mCode.matches(regex)) {
							continue;
						}
						boolean breakRespond = true;
						if (!permission.equals("*")) {
							for (String s : permission.split(",")) {
								if (String.valueOf(event.getSender().getId()).equals(s)) {
									breakRespond = false;
									break;
								}
							}
							if (breakRespond) {
								for (String s : owners) {
									if (String.valueOf(event.getSender().getId()).equals(s)) {
										breakRespond = false;
									}
								}
							}
						} else {
							breakRespond = false;
						}
						for (String s : noPermission.split(",")) {
							if (String.valueOf(event.getSender().getId()).equals(s)) {
								breakRespond = true;
							}
						}
						if (breakRespond) {
							boolean noPermissionReply = sectionObject.has("NoPermissionReply") && sectionObject.getBoolean("NoPermissionReply");
							boolean noPermissionRecall = sectionObject.has("NoPermissionRecall") && sectionObject.getBoolean("NoPermissionRecall");
							int noPermissionMute = sectionObject.has("NoPermissionMute") ? sectionObject.getInt("NoPermissionMute") : 0;
							String noPermissionRunCmd = sectionObject.has("NoPermissionRunCmd") ? sectionObject.getString("NoPermissionRunCmd") : "";
							noPermissionMsg = replaceGroupMsgPlaceholder(event, mCode.replaceAll(regex, noPermissionMsg));
							if (!noPermissionRunCmd.isEmpty()) {
								if (runCmd.startsWith("!SCRIPT")) {
									try {
										ScriptEngineManager manager = new ScriptEngineManager();
										ScriptEngine engine = manager.getEngineByName("JavaScript");
										noPermissionRunCmd = engine.eval(noPermissionRunCmd.substring(7)).toString();
									} catch (Exception e) {
										noPermissionRunCmd = e.toString();
									}
								}
								try {
									PluginMain.runCommand(replaceGroupMsgPlaceholder(event, noPermissionRunCmd));
								} catch (Exception e) {
									System.out.println();
									e.printStackTrace();
								}
								noPermissionMsg = noPermissionMsg.replaceAll("%last_console_output%", LogUtil.lastOpt);
							}
							if (noPermissionRecall) {
								try {
									Mirai.getInstance().recallMessage(event.getBot(), event.getSource());
								} catch (Exception e) {
									LogUtil.log(ConfigUtil.getLanguage("no.permission"));
								}
							}
							if (noPermissionMute != 0) {
								try {
									event.getSender().mute(mute);
								} catch (Exception e) {
									LogUtil.log(ConfigUtil.getLanguage("no.permission"));
								}
							}
							if (!noPermissionMsg.isEmpty()) {
								if (noPermissionMsg.startsWith("!SCRIPT")) {
									try {
										ScriptEngineManager manager = new ScriptEngineManager();
										ScriptEngine engine = manager.getEngineByName("JavaScript");
										noPermissionMsg = engine.eval(noPermissionMsg.substring(7)).toString();
									} catch (Exception e) {
										noPermissionMsg = e.toString();
									}
								}
								if (!noPermissionReply) {
									event.getGroup().sendMessage(MiraiCode.deserializeMiraiCode(noPermissionMsg));
								} else {
									event.getGroup().sendMessage(new QuoteReply(event.getSource()).plus(MiraiCode.deserializeMiraiCode(noPermissionMsg)));
								}
							}
							continue;
						}
						
						if (!runCmd.isEmpty()) {
							if (runCmd.startsWith("!SCRIPT")) {
								try {
									ScriptEngineManager manager = new ScriptEngineManager();
									ScriptEngine engine = manager.getEngineByName("JavaScript");
									runCmd = engine.eval(runCmd.substring(7)).toString();
								} catch (Exception e) {
									runCmd = e.toString();
								}
							}
							try {
								PluginMain.runCommand(replaceGroupMsgPlaceholder(event, runCmd));
							} catch (Exception e) {
								System.out.println();
								e.printStackTrace();
							}
							respond = respond.replaceAll("%last_console_output%", LogUtil.lastOpt);
						}
						if (mute != 0) {
							try {
								event.getSender().mute(mute);
							} catch (Exception e) {
								LogUtil.log(ConfigUtil.getLanguage("no.permission"));
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
							if (!respond.isEmpty()) {
								if (respond.startsWith("!SCRIPT")) {
									try {
										ScriptEngineManager manager = new ScriptEngineManager();
										ScriptEngine engine = manager.getEngineByName("JavaScript");
										respond = engine.eval(respond.substring(7)).toString();
									} catch (Exception e) {
										respond = e.toString();
									}
								}
								if (reply) {
									event.getGroup().sendMessage(new QuoteReply(event.getSource()).plus(
											MiraiCode.deserializeMiraiCode(respond)));
								} else {
									event.getGroup().sendMessage(MiraiCode.deserializeMiraiCode(respond));
								}
							}
						} catch (BotIsBeingMutedException e) {
							LogUtil.log(ConfigUtil.getLanguage("bot.is.being.muted"));
						}
						if (changeNameCard != null) {
							try {
								((NormalMember) event.getSender()).setNameCard(replaceGroupMsgPlaceholder(event, mCode.replaceAll(regex, changeNameCard)));
							} catch (Exception e) {
								LogUtil.log(ConfigUtil.getLanguage("no.permission"));
							}
						}
					} catch (JSONException e) {
						LogUtil.log(ConfigUtil.getLanguage("unknown.error"));
						System.out.println();
						e.printStackTrace();
					}
				} catch (Exception e) {
					LogUtil.log(ConfigUtil.getLanguage("unknown.error"));
					System.out.println();
					e.printStackTrace();
					System.exit(-1);
				}
			}
		} catch (Exception e) {
			LogUtil.log(e.toString());
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onFriendMessage(FriendMessageEvent event) {
		if (!(ConfigUtil.getConfig("friend").equals("*") || event.getSender().getId() == Long.parseLong(ConfigUtil.getConfig("friend")))) {
			event.cancel();
			return;
		}
		String msg = ConfigUtil.getConfig("debug").equals("true") ?
				event.getMessage().plus("").serializeToMiraiCode() : event.getMessage().contentToString();
		LogUtil.log(event.getSender().getNick() + showQQ(event.getSender().getId()) + "-> " + event.getBot().getNick() + " " + msg);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onTempMessage(GroupTempMessageEvent event) {
		if (!(ConfigUtil.getConfig("friend").equals("*") || event.getSender().getId() == Long.parseLong(ConfigUtil.getConfig("friend")))) {
			event.cancel();
			return;
		}
		String msg = ConfigUtil.getConfig("debug").equals("true") ?
				event.getMessage().plus("").serializeToMiraiCode() : event.getMessage().contentToString();
		LogUtil.log(event.getSender().getNick() + showQQ(event.getSender().getId()) + "-> " + event.getBot().getNick() + " " + msg);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onStrangerMessage(StrangerMessageEvent event) {
		if (!(ConfigUtil.getConfig("friend").equals("*") || event.getSender().getId() == Long.parseLong(ConfigUtil.getConfig("friend")))) {
			event.cancel();
			return;
		}
		String msg = ConfigUtil.getConfig("debug").equals("true") ?
				event.getMessage().plus("").serializeToMiraiCode() : event.getMessage().contentToString();
		LogUtil.log(event.getSender().getNick() + showQQ(event.getSender().getId()) + "-> " + event.getBot().getNick() + " " + msg);
	}
	
	/**
	 * A useless function.
	 *
	 * @param qq qq
	 * @return show qq
	 */
	public String showQQ(Long qq) {
		return showQQ ? "(" + qq + ")" : "";
	}
	
	/**
	 * Replace all placeholders with corresponding values
	 *
	 * @param event GroupMessageEvent
	 * @param str   Text to replace
	 * @return Replaced text
	 */
	public String replaceGroupMsgPlaceholder(GroupMessageEvent event, String str) {
		if (str == null) return "";
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
		str = str.replaceAll("%message_id%", String.valueOf(messages.size()));
		str = str.replaceAll("%bot_nick%", event.getBot().getNick());
		str = str.replaceAll("%bot_id%", String.valueOf(event.getBot().getId()));
		String flashId = "", imageId = "", fileId = "";
		if (spl.length >= 3) {
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
				str = str.replaceAll("%sender_avatar_id%", img.getImageId());
			} catch (IOException e) {
				LogUtil.log(e.toString());
			}
		}
		Pattern pattern = Pattern.compile("%url_([^%]+)%");
		Matcher matcher = pattern.matcher(str);
		while (matcher.find()) {
			String con;
			URL url;
			try {
				String u = matcher.group(1);
				boolean hasP = u.toLowerCase().startsWith("https://") || u.toLowerCase().startsWith("http://");
				url = new URL(hasP ? u : "https://" + u);
				InputStream is = url.openStream();
				BufferedInputStream bis = new BufferedInputStream(is);
				con = new String(bis.readAllBytes(), StandardCharsets.UTF_8);
			} catch (IOException e) {
				con = e.toString();
			}
			str = str.replace("%url_" + matcher.group(1) + "%", con);
		}
		return str;
	}
}
