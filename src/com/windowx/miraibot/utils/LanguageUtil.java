package com.windowx.miraibot.utils;

public class LanguageUtil {
	/**
	 * Get the language file by the value obtained by "Locale.getDefault().getLanguage()"
	 *
	 * @param language The result obtained by "Locale.getDefault().getLanguage()"
	 * @return The byte[] of the language file
	 */
	public static byte[] languageFile(String language) {
		if (language.equals("zh")) {
			return ("# 中文（简体）\n" +
					"config.error=配置文件出现错误，请检查配置文件后再试！\n" +
					"qq.password.not.exits=请填写配置文件的 QQ号 与 密码！\n" +
					"failed.create.config=创建配置文件失败！\n" +
					"failed.check.update=检测更新失败！（%s）\n" +
					"found.new.update=发现新版本：%s\n" +
					"checking.update=正在检测更新...\n" +
					"already.latest.version=已是最新版本！（%s）\n" +
					"too.new.version=当前版本高于最新版本，你修改了 Jar 包？ =)\n" +
					"trying.login=正在尝试使用%s登录, 稍后可能会出现验证码弹窗...\n" +
					"registering.event=正在注册事件...\n" +
					"enabling.plugin=正在加载插件 [%s]...\n" +
					"failed.load.plugin=在加载插件 [%s] 时发生了错误：%s\n" +
					"login.success=登录成功，您的昵称是：%s\n" +
					"not.group.set=配置文件中的 聊群 项为空！您将无法发送和接收到聊群的消息！\n" +
					"not.entered.group=机器人并未加入聊群 %s , 但机器人目前可以继续使用！\n" +
					"now.group=当前进入的聊群为：%s (%s)\n" +
					"stopping.bot=正在关闭机器人：%s (%s)\n" +
					"please.restart=请重新启动 MiraiBot\n" +
					"console.cleared=已清除控制台！\n" +
					"bot=机器人\n" +
					"not.user=无法获取该用户！\n" +
					"not.friend=你没有这个好友！\n" +
					"not.qq=%s 不是一个 QQ！\n" +
					"time.too.long=禁言时间 %s 太长了\n" +
					"usage=语法\n" +
					"language=语言\n" +
					"file.error=该文件不存在！\n" +
					"group.id.not.found=群 ID %s 未找到, 请确保已填入配置文件？\n" +
					"failed.clipboard=无法获取当前剪切板的图片！\n" +
					"creating.word.image=正在生成文字图片...\n" +
					"width.height.error=宽度、高度和字体大小必须为整数！\n" +
					"deleted.friend=已删除 %s (%s)\n" +
					"message.not.found=未找到该消息！\n" +
					"message.id.error=消息位置必须是整数！\n" +
					"request.not.found=未找到该入群请求！\n" +
					"request.id.error=入群请求位置必须是整数！\n" +
					"request.accept=已同意该入群请求！\n" +
					"failed.accept.request=无法同意该入群请求！\n" +
					"recalled=已撤回该消息！\n" +
					"kicked=已踢出该成员！\n" +
					"nudged=成功戳一戳该成员！\n" +
					"time=时长\n" +
					"failed.recall=无法撤回该消息！\n" +
					"no.permission=你没有足够的权限进行该操作！\n" +
					"bot.is.being.muted=机器人已被禁言！无法发送消息！\n" +
					"member.mute=%s (%s) 被 %s (%s) 禁言 %s 秒！\n" +
					"member.unmute=%s (%s) 已被 %s (%s) 解除禁言！\n" +
					"open=已开启\n" +
					"off=已关闭\n" +
					"mute.all=全员禁言\n" +
					"qq.password.error=请检查配置文件中的 QQ号 是否正确！\n" +
					"success.change.language=成功修改语言为 简体中文\n" +
					"up.loading.img=正在上传图片...\n" +
					"up.loading.voice=正在上传语音...\n" +
					"join.request.group=[%s] %s (%s) 请求加入聊群 %s (%s)\n" +
					"joined.group=%s (%s) 加入了聊群 %s (%s)\n" +
					"left.group=%s (%s) 离开了聊群 %s (%s)\n" +
					"kick.group=%s (%s) 被 %s (%s) 请出了聊群 %s (%s)\n" +
					"recall.message=%s 撤回了一条 [%s] 消息\n" +
					"recall.unknown.message=%s 撤回了一条消息\n" +
					"recall.others.message=%s 撤回了一条 %s 的 [%s] 消息\n" +
					"recall.others.unknown.message=%s 撤回了一条 [%s] 的消息\n" +
					"image.id=图片ID\n" +
					"format.group.message=[%s] %s(%s): %s\n" +
					"format.group.recallable.message=[%s] %s(%s): %s\n" +
					"format.user.message=%s(%s) -> %s(%s): %s\n" +
					"format.user.recallable.message=[%s] %s(%s) -> %s(%s): %s\n" +
					"command.stop=关闭机器人\n" +
					"command.check.update=手动检查 MiraiBot 是否有更新\n" +
					"command.friend.list=获取当前机器人好友列表\n" +
					"command.language=更改 MiraiBot 的语言\n" +
					"command.group.list=获取当前聊群成员列表\n" +
					"command.help=显示 MiraiBot 所有指令\n" +
					"command.send=向好友发送消息（支持 Mirai码）\n" +
					"command.reply=回复一条消息\n" +
					"command.recall=撤回一个消息\n" +
					"command.image=向当前聊群发送图片\n" +
					"command.image.info=查看一个图片的信息\n" +
					"command.up.img=上传一个图片到服务器并获取到图片信息\n" +
					"command.up.clip.img=上传当前剪切板的图片\n" +
					"command.new.img=创建并上传一个图片\n" +
					"command.del=删除一个好友\n" +
					"command.avatar=获取一个用户的头像\n" +
					"command.accept.request=同意一个入群请求\n" +
					"command.kick=踢出一个用户\n" +
					"command.mute=禁言一个用户指定时间（单位：秒）\n" +
					"command.name.card=修改一个用户的群昵称\n" +
					"command.group=切换到已设置的某个群\n" +
					"command.unload=卸载某个插件\n" +
					"command.load=加载某个插件\n" +
					"command.plugins=列出目前已加载的所有插件\n" +
					"command.reload=重载某个插件\n" +
					"command.music=通过网易云音乐 ID 分享音乐\n" +
					"command.nudge=戳一戳某人\n" +
					"command.voice=发送一个语音\n" +
					"command.accept.invite=接受一个邀请入群请求\n" +
					"name.card.set=已将 %s 的群昵称设置为 %s\n" +
					"reloaded=已重新加载配置文件！\n" +
					"please.input=请输入\n" +
					"true.or.false=是否\n" +
					"check.update=检查更新\n" +
					"group=群\n" +
					"id=ID\n" +
					"name.card=群昵称\n" +
					"password=密码\n" +
					"qq=QQ\n" +
					"reason=原因\n" +
					"contents=内容\n" +
					"request.id=请求ID\n" +
					"message.id=消息ID\n" +
					"file.path=文件路径\n" +
					"width=宽度\n" +
					"height=高度\n" +
					"plugin.name=插件名\n" +
					"font.size=字体大小\n" +
					"plugin.not.exits=插件 %s 不存在或已被卸载!\n" +
					"unloading.plugin=正在卸载插件 %s...\n" +
					"unloaded.plugin=插件 %s 已成功卸载！\n" +
					"file.name=文件名\n" +
					"plugin.file.not.exits=未找到插件文件 %s!\n" +
					"loading.plugin=正在加载插件 %s...\n" +
					"loaded.plugin=插件 %s 已成功加载！\n" +
					"plugin.already.loaded=插件 %s 已经被载入！\n" +
					"music.id=网易云音乐 ID\n" +
					"music.id.error=网易云音乐 ID 错误, 请检查后再试！\n" +
					"music.code.error=获取音乐信息失败, 请检查后再试！\n" +
					"you=你\n" +
					"itself=TA自己\n" +
					"yourself=你自己\n" +
					"nudge.message=%s%s%s%s\n" +
					"bot.leave.group=机器人 %s(%s) 离开了群 %s(%s)\n" +
					"bot.leave.group=机器人 %s(%s) 被 %s(%s) 踢出了群 %s(%s)\n" +
					"bot.permission.change=机器人 %s(%s) 的群权限由 %s 更改为了 %s\n" +
					"member.name.card.change=群成员 %s(%s) 的群昵称由 %s 被修改为了 %s\n" +
					"member.permission.change=群成员 %s(%s) 的权限由 %s 被修改为了 %s\n" +
					"invite.request.group=[%s] 好友 %s(%s) 邀请你进入群 %s(%s)\n" +
					"invite.id=邀请入群 ID\n" +
					"invite.id.error=邀请入群请求 ID 错误！\n" +
					"invite.not.found=未找到该入群请求 ID！\n" +
					"failed.accept.invite=同意邀请入群请求失败！\n" +
					"invite.accepted=成功接受该邀请入群请求！\n" +
					"unknown.error=出现错误！进程即将终止！\n").getBytes();
		} else if (language.equals("tw")) {
			return ("# 中文（繁體）\n" +
					"config.error=配置文件出現錯誤，請檢查配置文件後再試！\n" +
					"qq.password.not.exits=請填寫配置文件的 QQ號 與 密碼！\n" +
					"failed.create.config=創建配置文件失敗！\n" +
					"failed.check.update=檢測更新失敗！（%s）\n" +
					"checking.update=正在檢測更新...\n" +
					"already.latest.version=已是最新版本！（%s）\n" +
					"too.new.version=當前版本高於最新版本，你修改了 Jar 包？ =)\n" +
					"found.new.update=發現新版本：%s\n" +
					"trying.login=正在嘗試使用%s登錄, 稍後可能會出現驗證碼彈窗...\n" +
					"registering.event=正在註冊事件...\n" +
					"enabling.plugin=正在加載插件 [%s]...\n" +
					"failed.load.plugin=在加載插件 [%s] 時發生了錯誤：%s\n" +
					"login.success=登錄成功，您的昵稱是：%s\n" +
					"not.group.set=配置文件中的 聊群 項為空！您將無法發送和接收到聊群的消息！\n" +
					"not.entered.group=機器人並未加入聊群 %s , 但機器人目前可以繼續使用！\n" +
					"now.group=當前進入的聊群為：%s (%s)\n" +
					"stopping.bot=正在關閉機器人：%s (%s)\n" +
					"please.restart=請重新啓動 MiraiBot\n" +
					"console.cleared=已清除控制臺！\n" +
					"bot=機器人\n" +
					"not.user=無法獲取該用戶！\n" +
					"not.friend=你沒有這個好友！\n" +
					"not.qq=%s 不是一個 QQ！\n" +
					"time.too.long=禁言時間 %s 太長了\n" +
					"usage=語法\n" +
					"language=語言\n" +
					"file.error=該文件不存在！\n" +
					"group.id.not.found=群 ID %s 未找到, 請確保已填入配置文件？\n" +
					"failed.clipboard=無法獲取當前剪切板的圖片！\n" +
					"creating.word.image=正在生成文字圖片...\n" +
					"width.height.error=寬度、高度和字體大小必須為整數！\n" +
					"deleted.friend=已刪除 %s (%s)\n" +
					"message.not.found=未找到該消息！\n" +
					"message.id.error=消息位置必須是整數！\n" +
					"request.not.found=未找到該入群請求！\n" +
					"request.id.error=入群請求位置必須是整數！\n" +
					"request.accept=已同意該入群請求！\n" +
					"failed.accept.request=無法同意該入群請求！\n" +
					"recalled=已撤回該消息！\n" +
					"kicked=已踢出該成員！\n" +
					"nudged=成功戳一戳該成員！\n" +
					"time=時長\n" +
					"failed.recall=無法撤回該消息！\n" +
					"no.permission=你沒有足夠的權限進行該操作！\n" +
					"bot.is.being.muted=機器人已被禁言！無發發送消息！\n" +
					"member.mute=%s (%s) 被 %s (%s) 禁言 %s 秒！\n" +
					"member.unmute=%s (%s) 已被 %s (%s) 解除禁言！\n" +
					"open=已開啟！\n" +
					"off=已關閉\n" +
					"mute.all=全員禁言\n" +
					"qq.password.error=請檢查配置文件中的 QQ號 是否正確！\n" +
					"success.change.language=成功修改語言為 繁體中文\n" +
					"up.loading.img=正在上傳圖片...\n" +
					"up.loading.voice=正在上傳語音...\n" +
					"join.request.group=[%s] %s (%s) 請求加入聊群 %s (%s)\n" +
					"joined.group=%s (%s) 加入了聊群 %s (%s)\n" +
					"left.group=%s (%s) 離開了聊群 %s (%s)\n" +
					"kick.group=%s (%s) 被 %s (%s) 請出了聊群 %s (%s)\n" +
					"recall.message=%s 撤回了一條 [%s] 消息\n" +
					"recall.unknown.message=%s 撤回了一條消息\n" +
					"recall.others.message=%s 撤回了一條 %s 的 [%s] 消息\n" +
					"recall.others.unknown.message=%s 撤回了一條 [%s] 的消息\n" +
					"image.id=圖片ID\n" +
					"format.group.message=[%s] %s(%s): %s\n" +
					"format.group.recallable.message=[%s] %s(%s): %s\n" +
					"format.user.message=%s(%s) -> %s(%s): %s\n" +
					"format.user.recallable.message=[%s] %s(%s) -> %s(%s): %s\n" +
					"command.stop=關閉機器人\n" +
					"command.check.update=手動檢查 MiraiBot 是否有更新\n" +
					"command.friend.list=獲取當前機器人好友列表\n" +
					"command.language=更改 MiraiBot 的語言\n" +
					"command.group.list=獲取當前聊群成員列表\n" +
					"command.help=顯示 MiraiBot 所有指令\n" +
					"command.send=向好友發送消息（支持 Mirai碼）\n" +
					"command.reply=回復一條消息\n" +
					"command.recall=撤回一個消息\n" +
					"command.image=向當前聊群發送圖片\n" +
					"command.image.info=查看一個圖片的信息\n" +
					"command.up.img=上傳一個圖片到服務器並獲取到圖片信息\n" +
					"command.up.clip.img=上傳當前剪切板的圖片\n" +
					"command.new.img=創建並上傳一個圖片\n" +
					"command.del=刪除一個好友\n" +
					"command.accept.request=同意一個入群請求\n" +
					"command.avatar=獲取一個用戶的頭像\n" +
					"command.kick=踢出一個用戶\n" +
					"command.mute=禁言一個用戶指定時間（單位：秒）\n" +
					"command.name.card=修改一個用戶的群昵稱\n" +
					"command.group=切換到已設置的某個群\n" +
					"command.unload=卸載某個插件\n" +
					"command.load=加載某個插件\n" +
					"command.plugins=列出目前已加載的所有插件\n" +
					"command.reload=重載某個插件\n" +
					"command.music=通過網易云音樂 ID 分享音樂\n" +
					"command.nudge=戳一戳某人\n" +
					"command.voice=發送一個語音\n" +
					"command.accept.invite=接受一個邀請入群請求\n" +
					"name.card.set=已将 %s 的群昵称设置为 %s\n" +
					"reloaded=已重新加載配置文件！\n" +
					"please.input=請輸入\n" +
					"true.or.false=是否\n" +
					"check.update=檢查更新\n" +
					"group=群\n" +
					"id=ID\n" +
					"name.card=群昵稱\n" +
					"password=密碼\n" +
					"qq=QQ\n" +
					"reason=原因\n" +
					"contents=內容\n" +
					"request.id=請求ID\n" +
					"message.id=消息ID\n" +
					"file.path=文件路徑\n" +
					"width=寬度\n" +
					"height=高度\n" +
					"plugin.name=插件名\n" +
					"font.size=字體大小\n" +
					"plugin.not.exits=插件 %s 不存在或已被卸載!\n" +
					"unloading.plugin=正在卸載插件 %s...\n" +
					"unloaded.plugin=插件 %s 已成功卸載！\n" +
					"file.name=文件名\n" +
					"plugin.file.not.exits=未找到插件文件 %s!\n" +
					"loading.plugin=正在加載插件 %s...\n" +
					"loaded.plugin=插件 %s 已成功加載！\n" +
					"plugin.already.loaded=插件 %s 已經被載入！\n" +
					"music.id=網易雲音樂 ID\n" +
					"music.id.error=網易雲音樂 ID 錯誤, 請檢查后再試！\n" +
					"music.code.error=獲取音樂信息失敗, 請檢查后再試！\n" +
					"you=你\n" +
					"itself=TA自己\n" +
					"yourself=你自己\n" +
					"nudge.message=%s%s%s%s\n" +
					"bot.leave.group=機器人 %s(%s) 離開了群 %s(%s)\n" +
					"bot.leave.group=機器人 %s(%s) 被 %s(%s) 踢出了群 %s(%s)\n" +
					"bot.permission.change=機器人 %s(%s) 的群權限由 %s 更改為了 %s\n" +
					"member.name.card.change=群成員 %s(%s) 的群昵稱由 %s 被修改為了 %s\n" +
					"member.permission.change=群成員 %s(%s) 的權限由 %s 被修改為了 %s\n" +
					"invite.request.group=[%s] 好友 %s(%s) 邀請你進入群 %s(%s)\n" +
					"invite.id=邀請入群 ID\n" +
					"invite.id.error=邀請入群請求 ID 錯誤！\n" +
					"invite.not.found=未找到該邀請入群請求 ID！\n" +
					"failed.accept.invite=同意邀請入群請求失敗！\n" +
					"invite.accepted=成功接受該邀請入群請求！\n" +
					"unknown.error=出現錯誤！進程即將終止！\n").getBytes();
		} else {
			return ("# English (" + language + ")\n" +
					"config.error=An error occurred in the configuration file, please check the configuration file and try again!\n" +
					"qq.password.not.exits=Please fill in your QQ number and password of the configuration file!\n" +
					"failed.create.config=Failed to create configuration file!\n" +
					"failed.check.update=Failed to check update! (%s)\n" +
					"checking.update=Checking update...\n" +
					"already.latest.version=This version is the latest version. (%s)\n" +
					"too.new.version=The current version is higher than the latest version. Have you modified the Jar package? =)\n" +
					"found.new.update=New version found: %s\n" +
					"trying.login=Trying to log in with %s, a verification code pop-up window may appear later...\n" +
					"registering.event=Registering event...\n" +
					"enabling.plugin=Loading plugin [%s]...\n" +
					"failed.load.plugin=An error occurred while loading plugin [%s]: %s\n" +
					"login.success=Login successful, your nickname is: %s\n" +
					"not.group.set=The chat group number in the configuration file is empty! You will not be able to send and receive chat group messages!\n" +
					"not.entered.group=The robot has not joined the chat group for %s, but the robot can continue to be used for now!\n" +
					"now.group=The currently entered chat group is: %s (%s)\n" +
					"stopping.bot=Closing robot: %s (%s)\n" +
					"please.restart=Please restart the MiraiBot\n" +
					"console.cleared=Console cleared!\n" +
					"bot=Robot\n" +
					"not.user=Unable to get the user!\n" +
					"not.friend=You don't have this friend!\n" +
					"not.qq=%s is not a QQ number!\n" +
					"time.too.long=Mute time %s is too long\n" +
					"usage=Usage\n" +
					"language=Language\n" +
					"file.error=The file does not exist!\n" +
					"group.id.not.found=Group ID %s is not found, please make sure to fill in the configuration file?\n" +
					"failed.clipboard=Unable to get the image file of the current clipboard!\n" +
					"creating.word.image=Generating text image...\n" +
					"width.height.error=The width, height, and font size must be integers!\n" +
					"deleted.friend=%s (%s) deleted.\n" +
					"message.not.found=The message was not found!\n" +
					"message.id.error=The message position must be an integer!\n" +
					"request.not.found=The request to join the group was not found!\n" +
					"request.id.error=The group entry request position must be an integer!\n" +
					"request.accept=Agreed group request!\n" +
					"failed.accept.request=Unable to agree to the join group request!\n" +
					"recalled=The message has been recalled!\n" +
					"kicked=This member has been kicked out!\n" +
					"nudged=Nudge the member successfully!\n" +
					"time=Time\n" +
					"failed.recall=Recall Failed!\n" +
					"no.permission=You do not have permissions to perform this operation!\n" +
					"bot.is.being.muted=The bot has been muted! Unable to send message!\n" +
					"member.mute=%s (%s) was muted by %s (%s) for %s seconds!\n" +
					"member.unmute=%s (%s) has been unbanned by %s (%s)!\n" +
					"open=turned on\n" +
					"off=turned off\n" +
					"mute.all=Mute all \n" +
					"qq.password.error=Please check whether the QQ number in the configuration file is correct!\n" +
					"success.change.language=Successfully changed the language to English\n" +
					"up.loading.img=Uploading Image File...\n" +
					"up.loading.voice=Uploading Voice File...\n" +
					"join.request.group=[%s] %s (%s) request to join the chat group %s (%s)\n" +
					"joined.group=%s (%s) joined the chat group %s (%s)\n" +
					"left.group=%s (%s) left the chat group %s (%s)\n" +
					"kick.group=%s (%s) was invited out of chat group by %s (%s) %s (%s)\n" +
					"recall.message=%s recalled a [%s] message\n" +
					"recall.unknown.message=%s recalled a message\n" +
					"recall.others.message=%s recalled a %s message for [%s]\n" +
					"recall.others.unknown.message=%s recalled a message for [%s]\n" +
					"image.id=Image ID\n" +
					"format.group.message=[%s] %s(%s): %s\n" +
					"format.group.recallable.message=[%s] %s(%s): %s\n" +
					"format.user.message=%s(%s) -> %s(%s): %s\n" +
					"format.user.recallable.message=[%s] %s(%s) -> %s(%s): %s\n" +
					"command.stop=Close the robot\n" +
					"command.check.update=Manually check if MiraiBot is updated\n" +
					"command.friend.list=Get the current robot's friend list\n" +
					"command.language=Change the language of MiraiBot\n" +
					"command.group.list=Get the list of current chat group members\n" +
					"command.help=Show all MiraiBot's commands\n" +
					"command.send=Send messages to friends (support Mirai code)\n" +
					"command.reply=Reply to a message\n" +
					"command.recall=Recall a message\n" +
					"command.image=Send a picture to the current chat group\n" +
					"command.image.info=View the information of a picture\n" +
					"command.up.img=Upload a picture to the server and get the picture information\n" +
					"command.up.clip.img=Upload a picture of the current clipboard\n" +
					"command.new.img=Create and upload an image\n" +
					"command.del=Delete a friend\n" +
					"command.accept.request=Agree to a request to join the group\n" +
					"command.avatar=Get a user's avatar\n" +
					"command.kick=Kick an user\n" +
					"command.mute=Mute a user-specified time (unit: second)\n" +
					"command.name.card=Modify a user’s group nickname\n" +
					"command.group=Switch to a group that has been set\n" +
					"command.unload=Uninstall a plugin\n" +
					"command.load=Load a plugin\n" +
					"command.plugins=List all plugins currently loaded\n" +
					"command.reload=Reload a plugin\n" +
					"command.music=Share music through NetEase Cloud Music ID\n" +
					"command.nudge=Nudge a member\n" +
					"command.voice=Send a voice\n" +
					"command.accept.invite=Accept an invitation to join the group request\n" +
					"name.card.set=The group nickname of %s has been set to %s\n" +
					"reloaded=Reloaded the configuration file!\n" +
					"please.input=Please input \n" +
					"true.or.false= whether \n" +
					"check.update= check update\n" +
					"group=Group\n" +
					"id=ID\n" +
					"name.card=Group Name Card\n" +
					"password=Password\n" +
					"qq=QQ\n" +
					"reason=Reason\n" +
					"contents=Content\n" +
					"request.id=Request ID\n" +
					"message.id=Message ID\n" +
					"file.path=File path\n" +
					"width=Width\n" +
					"height=Height\n" +
					"plugin.name=Plugin name\n" +
					"font.size=Font size\n" +
					"plugin.not.exits=Plugin %s does not exist or has been uninstalled!\n" +
					"unloading.plugin=Uninstalling plugin %s...\n" +
					"unloaded.plugin=Plugin %s has been successfully uninstalled!\n" +
					"file.name=File name\n" +
					"plugin.file.not.exits=Plugin file not found %s!\n" +
					"loading.plugin=Loading plugin %s...\n" +
					"loaded.plugin=Plugin %s has been successfully loaded!\n" +
					"plugin.already.loaded=Plugin %s has been already loaded!\n" +
					"music.id=NetEase Cloud Music ID\n" +
					"music.id.error=NetEase Cloud Music ID error, please check and try again！\n" +
					"music.code.error=Failed to get music information, please check and try again！\n" +
					"you=You\n" +
					"itself=itself\n" +
					"yourself=yourself\n" +
					"nudge.message=%s %s %s %s\n" +
					"bot.leave.group=Robot %s(%s) left the group %s(%s)\n" +
					"bot.leave.group=Robot %s(%s) was kicked out of group %s(%s) by %s(%s)\n" +
					"bot.permission.change=The group permissions of the robot %s(%s) have been changed from %s to %s\n" +
					"member.name.card.change=The group nickname of group member %s(%s) was changed from %s to %s\n" +
					"member.permission.change=The permissions of group member %s(%s) have been changed from %s to %s\n" +
					"invite.request.group=[%s] Friends %s(%s) invited you to join group %s(%s)\n" +
					"invite.id=Invite group ID\n" +
					"invite.id.error=Invite group request ID is wrong!\n" +
					"invite.not.found=The request ID of the invitation to join the group was not found!\n" +
					"failed.accept.invite=The request to agree to join the group failed!\n" +
					"invite.accepted=Successfully accepted the invitation to join the group!\n" +
					"unknown.error=An error occurred! The process is about to terminate!\n").getBytes();
		}
	}
}
