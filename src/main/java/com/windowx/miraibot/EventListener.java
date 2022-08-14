package com.windowx.miraibot;

import com.windowx.miraibot.utils.ConfigUtil;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.EventPriority;
import net.mamoe.mirai.event.ListenerHost;
import net.mamoe.mirai.event.events.*;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageSource;

import java.util.ArrayList;

import static com.windowx.miraibot.MiraiBot.*;
import static com.windowx.miraibot.utils.LanguageUtil.l;

public class EventListener implements ListenerHost {
	public static final ArrayList<MemberJoinRequestEvent> joinRequest = new ArrayList<>();
	public static final ArrayList<BotInvitedJoinGroupRequestEvent> inviteRequest = new ArrayList<>();
	public static boolean showQQ;
	public static ArrayList<MessageSource> messages = new ArrayList<>();

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEvent(Event event) {
		loader.broadcastEvent(event);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onGroupJoin(MemberJoinEvent event) {
		if (MiraiBot.isNotAllowedGroup(event.getGroupId())) {
			event.cancel();
			return;
		}
		if (MiraiBot.group != event.getGroup()) {
			return;
		}
		logger.info(l("joined.group")
				, event.getMember().getNameCard()
				, String.valueOf(event.getMember().getId())
				, event.getGroup().getName()
				, String.valueOf(event.getGroupId())
		);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onGroupLeave(MemberLeaveEvent.Quit event) {
		if (MiraiBot.isNotAllowedGroup(event.getGroupId())) {
			event.cancel();
			return;
		}
		if (MiraiBot.group != event.getGroup()) {
			return;
		}
		logger.info(l("left.group")
				, event.getMember().getNick()
				, String.valueOf(event.getMember().getId())
				, event.getGroup().getName()
				, String.valueOf(event.getGroupId())
		);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onGroupKick(MemberLeaveEvent.Kick event) {
		if (MiraiBot.isNotAllowedGroup(event.getGroupId())) {
			event.cancel();
			return;
		}
		if (MiraiBot.group != event.getGroup()) {
			return;
		}
		Member operator = event.getOperator();
		logger.info(l("kick.group")
				, event.getMember().getNick()
				, String.valueOf(event.getMember().getId())
				, operator != null ? operator.getNameCard() : event.getBot().getNick()
				, String.valueOf(operator != null ? operator.getId() : event.getBot().getId())
				, event.getGroup().getName(), String.valueOf(event.getGroupId())
		);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onJoinRequest(MemberJoinRequestEvent event) {
		if (MiraiBot.isNotAllowedGroup(event.getGroupId()) || event.getGroup() == null) {
			event.cancel();
			return;
		}
		if (MiraiBot.group != event.getGroup()) {
			return;
		}
		if (event.getGroup() != null) {
			joinRequest.add(event);
			logger.info(l("join.request.group")
					, String.valueOf(joinRequest.size())
					, event.getFromNick()
					, String.valueOf(event.getFromId())
					, event.getGroup().getName()
					, String.valueOf(event.getGroupId())
			);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBotInvitedJoinGroupRequest(BotInvitedJoinGroupRequestEvent event) {
		if (MiraiBot.isNotAllowedGroup(event.getGroupId())) {
			event.cancel();
			return;
		}
		inviteRequest.add(event);
		logger.info(l("invite.request.group")
			, String.valueOf(inviteRequest.size())
			, event.getInvitorNick()
			, String.valueOf(event.getInvitorId())
			, event.getGroupName()
			, String.valueOf(event.getGroupId())
		);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onGroupRecall(MessageRecallEvent.GroupRecall event) {
		if (MiraiBot.isNotAllowedGroup(event.getGroup().getId())) {
			event.cancel();
			return;
		}
		if (MiraiBot.group != event.getGroup()) {
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
					logger.info(l("recall.message"), operator.getNameCard() + showQQ(operator.getId()), String.valueOf(id));
				} else {
					logger.info(l("recall.unknown.message"), operator.getNameCard() + showQQ(operator.getId()));
				}
			} else {
				if (id != -1) {
					logger.info(l("recall.others.message")
							, operator.getNameCard() + showQQ(operator.getId())
							, sender.getNameCard() + showQQ(sender.getId())
							, String.valueOf(id)
					);
				} else {
					logger.info(l("recall.others.unknown.message")
							, operator.getNameCard() + showQQ(operator.getId())
							, String.valueOf(id)
					);
				}
			}
		} else {
			if (id != -1) {
				logger.info(l("recall.message")
						, event.getBot().getNick() + showQQ(event.getBot().getId())
						, String.valueOf(id)
				);
			} else {
				logger.info(l("recall.unknown.message"), event.getBot().getNick() + showQQ(event.getBot().getId()));
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
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
			logger.info(l("recall.message"), operator.getNick() + showQQ(operator.getId()), String.valueOf(id));
		} else {
			logger.info(l("recall.unknown.message"), operator.getNick() + showQQ(operator.getId()));
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBotLeave(BotLeaveEvent.Active event) {
		if (MiraiBot.isNotAllowedGroup(event.getGroupId())) {
			event.cancel();
			return;
		}
		logger.info(l("bot.leave.group")
				, event.getBot().getNick()
				, String.valueOf(event.getBot().getId())
				, event.getGroup().getName()
				, String.valueOf(event.getGroupId())
		);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBotKick(BotLeaveEvent.Kick event) {
		if (MiraiBot.isNotAllowedGroup(event.getGroupId())) {
			event.cancel();
			return;
		}
		logger.info(l("bot.leave.group")
				, event.getBot().getNick()
				, String.valueOf(event.getBot().getId())
				, event.getOperator().getNameCard()
				, String.valueOf(event.getOperator().getId())
				, event.getGroup().getName()
				, String.valueOf(event.getGroupId())
		);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBotPermissionChange(BotGroupPermissionChangeEvent event) {
		if (MiraiBot.isNotAllowedGroup(event.getGroupId())) {
			event.cancel();
			return;
		}
		logger.info(l("bot.permission.change")
				, event.getBot().getNick()
				, String.valueOf(event.getBot().getId())
				, event.getOrigin().name()
				, event.getNew().name()
		);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onGroupNameChange(GroupNameChangeEvent event) {
		if (MiraiBot.isNotAllowedGroup(event.getGroupId())) {
			event.cancel();
			return;
		}
		logger.info(l("group.name.change")
				, String.valueOf(event.getGroupId())
				, event.getOrigin()
				, event.getNew()
		);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onMemberNameCardChange(MemberCardChangeEvent event) {
		if (MiraiBot.isNotAllowedGroup(event.getGroupId())) {
			event.cancel();
			return;
		}
		logger.info(l("member.name.card.change")
				, event.getMember().getNick()
				, String.valueOf(event.getMember().getId())
				, event.getOrigin()
				, event.getNew()
		);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onMemberPermissionChange(MemberPermissionChangeEvent event) {
		if (MiraiBot.isNotAllowedGroup(event.getGroupId())) {
			event.cancel();
			return;
		}
		logger.info(l("member.permission.change")
				, event.getMember().getNick()
				, String.valueOf(event.getMember().getId())
				, event.getOrigin().name()
				, event.getNew().name()
		);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onGroupPostSend(GroupMessagePostSendEvent event) {
		if (MiraiBot.isNotAllowedGroup(event.getTarget().getId())) {
			event.cancel();
			return;
		}
		if (MiraiBot.group != event.getTarget()) {
			return;
		}
		MessageReceipt<Group> receipt = event.getReceipt();
		String msg = msg2str(event.getMessage());
		if (receipt != null) {
			messages.add(receipt.getSource());
			logger.info(l("format.group.recallable.message")
					, String.valueOf(messages.size())
					, event.getBot().getNick()
					, String.valueOf(event.getBot().getId())
					, msg
			);
		} else {
			logger.info(l("format.group.message")
					, event.getBot().getNick()
					, String.valueOf(event.getBot().getId())
					, msg
			);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onFriendPostSend(FriendMessagePostSendEvent event) {
		MessageReceipt<Friend> receipt = event.getReceipt();
		String msg = msg2str(event.getMessage());
		if (receipt != null) {
			messages.add(receipt.getSource());
			logger.info(l("format.user.recallable.message")
					, String.valueOf(messages.size())
					, event.getBot().getNick()
					, String.valueOf(event.getBot().getId())
					, event.getTarget().getNick()
					, String.valueOf(event.getTarget().getId())
					, msg
			);
		} else {
			logger.info(l("format.user.recallable.message")
					, event.getBot().getNick()
					, String.valueOf(event.getBot().getId())
					, event.getTarget().getNick()
					, String.valueOf(event.getTarget().getId())
					, msg
			);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onNudge(NudgeEvent event) {
		if (event.getSubject() == MiraiBot.group) {
			Member from = MiraiBot.group.get(event.getFrom().getId());
			Member target = MiraiBot.group.get(event.getTarget().getId());
			if (from == null || target == null) return;
			String fnick = from.getNameCard();
			if (fnick.isEmpty()) {
				fnick = from.getNick();
			}
			String tnick = target.getNameCard();
			if (tnick.isEmpty()) {
				tnick = target.getNick();
			}
			logger.info(l("nudge.message")
					, from != MiraiBot.group.getBotAsMember() ? fnick + showQQ(from.getId()) : l("you")
					, event.getAction()
					, target != from ? tnick + showQQ(target.getId()) :
							target != MiraiBot.group.getBotAsMember() ? l("itself") : l("yourself")
					, event.getSuffix()
			);
		} else {
			if (!(ConfigUtil.getConfig("friend").equals("*") || event.getFrom().getId() == Long.parseLong(ConfigUtil.getConfig("friend")))) {
				event.cancel();
				return;
			}
			Friend from = event.getBot().getFriend(event.getFrom().getId());
			Friend target = event.getBot().getFriend(event.getTarget().getId());
			if (from != null && target != null) {
				logger.info(l("nudge.message")
						, from != event.getBot().getAsFriend() ? from.getNick() + showQQ(from.getId()) : l("you")
						, event.getAction()
						, target != from ? target.getNick() + showQQ(target.getId()) :
								target != event.getBot().getAsFriend() ? l("itself") : l("yourself")
						, event.getSuffix()
				);
			}
		}
	}
	
	private void logMute(Member op, Member member, Group group, int time) {
		if (op == null) {
			op = group.getBotAsMember();
		}
		if (member == null) {
			member = group.getBotAsMember();
		}
		logger.info(l("member.mute")
				, member.getNameCard()
				, String.valueOf(member.getId())
				, op.getNameCard()
				, String.valueOf(op.getId())
				, String.valueOf(time)
		);
	}
	
	private void logUnmute(Member op, Member member, Group group) {
		if (op == null) {
			op = group.getBotAsMember();
		}
		if (member == null) {
			member = group.getBotAsMember();
		}
		logger.info(l("member.unmute")
				, member.getNameCard()
				, String.valueOf(member.getId())
				, op.getNameCard()
				, String.valueOf(op.getId())
		);
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onMemberUnmute(MemberUnmuteEvent event) {
		if (MiraiBot.isNotAllowedGroup(event.getGroupId())) {
			event.cancel();
			return;
		}
		if (MiraiBot.group != event.getGroup()) {
			return;
		}
		logUnmute(event.getOperator(), event.getMember(), event.getGroup());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBotUnmute(BotUnmuteEvent event) {
		if (MiraiBot.isNotAllowedGroup(event.getGroupId())) {
			event.cancel();
			return;
		}
		if (MiraiBot.group != event.getGroup()) {
			return;
		}
		logUnmute(event.getOperator(), null, event.getGroup());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onMemberMute(MemberMuteEvent event) {
		if (MiraiBot.isNotAllowedGroup(event.getGroupId())) {
			event.cancel();
			return;
		}
		if (MiraiBot.group != event.getGroup()) {
			return;
		}
		logMute(event.getOperator(), event.getMember(), event.getGroup(), event.getDurationSeconds());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBotMute(BotMuteEvent event) {
		if (MiraiBot.isNotAllowedGroup(event.getGroupId())) {
			event.cancel();
			return;
		}
		if (MiraiBot.group != event.getGroup()) {
			return;
		}
		logMute(event.getOperator(), null, event.getGroup(), event.getDurationSeconds());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onMuteAll(GroupMuteAllEvent event) {
		if (MiraiBot.isNotAllowedGroup(event.getGroupId())) {
			event.cancel();
			return;
		}
		if (MiraiBot.group != event.getGroup()) {
			return;
		}
		logger.info(l("mute.all") +
				(event.getNew() ? l("open") : l("off")));
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onGroupMessage(GroupMessageEvent event) {
		if (MiraiBot.isNotAllowedGroup(event.getGroup().getId())) {
			event.cancel();
			return;
		}
		String msg = msg2str(event.getMessage());
		if (MiraiBot.group == event.getGroup()) {
			messages.add(event.getSource());
			logger.info(l("format.group.message")
					, String.valueOf(messages.size())
					, event.getSenderName()
					, String.valueOf(event.getSender().getId())
					, msg
			);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onFriendMessage(FriendMessageEvent event) {
		if (!(ConfigUtil.getConfig("friend").equals("*") || event.getSender().getId() == Long.parseLong(ConfigUtil.getConfig("friend")))) {
			event.cancel();
			return;
		}
		String msg = msg2str(event.getMessage());
		logger.info(event.getSender().getNick() + showQQ(event.getSender().getId()) + "-> " + event.getBot().getNick() + " " + msg);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onTempMessage(GroupTempMessageEvent event) {
		if (!(ConfigUtil.getConfig("friend").equals("*") || event.getSender().getId() == Long.parseLong(ConfigUtil.getConfig("friend")))) {
			event.cancel();
			return;
		}
		String msg = msg2str(event.getMessage());
		logger.info(event.getSender().getNick() + showQQ(event.getSender().getId()) + "-> " + event.getBot().getNick() + " " + msg);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onStrangerMessage(StrangerMessageEvent event) {
		if (!(ConfigUtil.getConfig("friend").equals("*") || event.getSender().getId() == Long.parseLong(ConfigUtil.getConfig("friend")))) {
			event.cancel();
			return;
		}
		String msg = msg2str(event.getMessage());
		logger.info(event.getSender().getNick() + showQQ(event.getSender().getId()) + "-> " + event.getBot().getNick() + " " + msg);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onForceOffline(BotOfflineEvent.Force event) {
		logger.warn(l("force.offline"), event.getMessage());
		try {
			bot.login();
		} catch (Exception e) {
			logger.error(l("cannot.re.login"), e.getMessage());
			logger.trace(e);
			try {
				MiraiCommand.cmds.get("stop").start();
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onOnline(BotOnlineEvent event) {
		logger.info(l("bot.online"), bot.getNick());
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

	public String msg2str(MessageChain chain) {
		if (Boolean.parseBoolean(ConfigUtil.getConfig("debug"))) {
			return chain.serializeToMiraiCode();
		}
		return chain.contentToString();
	}
}
