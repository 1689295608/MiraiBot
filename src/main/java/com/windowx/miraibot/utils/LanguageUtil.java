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
			return ("""
					# 中文（简体）
					config.error=配置文件出现错误，请检查配置文件后再试！
					qq.password.not.exits=请填写配置文件的 QQ号 与 密码！
					failed.create.config=创建配置文件失败！
					failed.check.update=检测更新失败！（%s）
					found.new.update=发现新版本：%s
					checking.update=正在检测更新...
					already.latest.version=已是最新版本！（%s）
					trying.login=正在尝试使用%s登录, 稍后可能会出现验证码弹窗...
					registering.event=正在注册事件...
					enabling.plugin=正在加载插件 [%s]...
					failed.load.plugin=在加载插件 [%s] 时发生了错误：%s
					login.success=登录成功，您的昵称是：%s
					not.group.set=配置文件中的 聊群 项为空！您将无法发送和接收到聊群的消息！
					not.entered.group=机器人并未加入聊群 %s , 但机器人目前可以继续使用！
					now.group=当前进入的聊群为：%s (%s)
					stopping.bot=正在关闭机器人：%s (%s)
					please.restart=请重新启动 MiraiBot
					console.cleared=已清除控制台！
					bot=机器人
					not.user=无法获取该用户！
					not.friend=你没有这个好友！
					not.qq=%s 不是一个 QQ！
					time.too.long=禁言时间 %s 太长了
					usage=语法
					language=语言
					file.error=该文件不存在！
					group.id.not.found=群 ID %s 未找到, 请确保已填入配置文件？
					failed.clipboard=无法获取当前剪切板的图片！
					creating.word.image=正在生成文字图片...
					width.height.error=宽度、高度和字体大小必须为整数！
					deleted.friend=已删除 %s (%s)
					message.not.found=未找到该消息！
					message.id.error=消息位置必须是整数！
					request.not.found=未找到该入群请求！
					request.id.error=入群请求位置必须是整数！
					request.accepted=已同意该入群请求！
					failed.accept.request=无法同意该入群请求！
					recalled=已撤回该消息！
					kicked=已踢出该成员！
					nudged=成功戳一戳该成员！
					time=时长
					failed.recall=无法撤回该消息！
					no.permission=你没有足够的权限进行该操作！
					bot.is.being.muted=机器人已被禁言！无法发送消息！
					member.mute=%s (%s) 被 %s (%s) 禁言 %s 秒！
					member.unmute=%s (%s) 已被 %s (%s) 解除禁言！
					open=已开启
					off=已关闭
					mute.all=全员禁言
					qq.password.error=请检查配置文件中的 QQ号 是否正确！
					success.change.language=成功修改语言为 简体中文
					up.loading.img=正在上传图片...
					up.loading.voice=正在上传语音...
					join.request.group=[%s] %s (%s) 请求加入聊群 %s (%s)
					joined.group=%s (%s) 加入了聊群 %s (%s)
					left.group=%s (%s) 离开了聊群 %s (%s)
					kick.group=%s (%s) 被 %s (%s) 请出了聊群 %s (%s)
					recall.message=%s 撤回了一条 [%s] 消息
					recall.unknown.message=%s 撤回了一条消息
					recall.others.message=%s 撤回了一条 %s 的 [%s] 消息
					recall.others.unknown.message=%s 撤回了一条 [%s] 的消息
					image.id=图片ID
					format.group.message=[%s] %s(%s): %s
					format.group.recallable.message=[%s] %s(%s): %s
					format.user.message=%s(%s) -> %s(%s): %s
					format.user.recallable.message=[%s] %s(%s) -> %s(%s): %s
					command.stop=关闭机器人
					command.check.update=手动检查 MiraiBot 是否有更新
					command.friend.list=获取当前机器人好友列表
					command.language=更改 MiraiBot 的语言
					command.member.list=获取当前聊群成员列表
					command.help=显示 MiraiBot 所有指令
					command.send=向好友发送消息（支持 Mirai码）
					command.reply=回复一条消息
					command.recall=撤回一个消息
					command.image=向当前聊群发送图片
					command.image.info=查看一个图片的信息
					command.up.img=上传一个图片到服务器并获取到图片信息
					command.up.clip.img=上传当前剪切板的图片
					command.new.img=创建并上传一个图片
					command.del=删除一个好友
					command.avatar=获取一个用户的头像
					command.accept.request=同意一个入群请求
					command.kick=踢出一个用户
					command.mute=禁言一个用户指定时间（单位：秒）
					command.name.card=修改一个用户的群昵称
					command.group=切换到已设置的某个群
					command.unload=卸载某个插件
					command.load=加载某个插件
					command.plugins=列出目前已加载的所有插件
					command.reload=重载某个插件
					command.music=通过网易云音乐 ID 分享音乐
					command.nudge=戳一戳某人
					command.voice=发送一个语音
					command.accept.invite=接受一个邀请入群请求
					name.card.set=已将 %s 的群昵称设置为 %s
					reloaded=已重新加载配置文件！
					before.settings=在启动前，您需要完成一些最基础的设置
					please.input.qq=请输入您的机器人的 QQ 号
					please.input.password=请输入您的机器人的密码
					please.input.group.id=请输入启用机器人的群（用","分隔）
					please.input.check.update.on.setup=是否在每次启动都执行检查更新程序？（是(true) 否(false)）
					group=群
					id=ID
					name.card=群昵称
					password=密码
					qq=QQ
					reason=原因
					contents=内容
					request.id=请求ID
					message.id=消息ID
					file.path=文件路径
					width=宽度
					height=高度
					plugin.name=插件名
					font.size=字体大小
					plugin.not.exits=插件 %s 不存在或已被卸载!
					unloading.plugin=正在卸载插件 %s...
					unloaded.plugin=插件 %s 已成功卸载！
					file.name=文件名
					plugin.file.not.exits=未找到插件文件 %s!
					loading.plugin=正在加载插件 %s...
					loaded.plugin=插件 %s 已成功加载！
					plugin.already.loaded=插件 %s 已经被载入！
					music.id=网易云音乐 ID
					music.id.error=网易云音乐 ID 错误, 请检查后再试！
					music.code.error=获取音乐信息失败, 请检查后再试！
					you=你
					itself=TA自己
					yourself=你自己
					nudge.message=%s%s%s%s
					bot.leave.group=机器人 %s(%s) 离开了群 %s(%s)
					bot.leave.group=机器人 %s(%s) 被 %s(%s) 踢出了群 %s(%s)
					bot.permission.change=机器人 %s(%s) 的群权限由 %s 更改为了 %s
					member.name.card.change=群成员 %s(%s) 的群昵称由 %s 被修改为了 %s
					member.permission.change=群成员 %s(%s) 的权限由 %s 被修改为了 %s
					invite.request.group=[%s] 好友 %s(%s) 邀请你进入群 %s(%s)
					invite.id=邀请入群 ID
					invite.id.error=邀请入群请求 ID 错误！
					invite.not.found=未找到该入群请求 ID！
					failed.accept.invite=同意邀请入群请求失败！
					invite.accepted=成功接受该邀请入群请求！
					format.time=[HH:mm:ss]\s
					dice.not.number=骰子值必须是整数
					dice.value.error=骰子的值不能大于六和小于一！
					dice.value=骰子值
					contact.id.error=联系人 ID 错误！
					contact=联系人
					cannot.create.plugin.dir=创建插件目录失败
					depend.not.exits=缺少插件依赖 "%s"
					event.error=执行 %s 插件的 %s 事件时出现错误：%s
					exception.string=问题 %s 描述: %s
					exception.details=\\t 在 %s.%s（%s 的第 %d 行）
					unknown.error=出现错误！进程即将终止！
					""").getBytes();
		} else if (language.equals("tw")) {
			return ("""
					# 中文（繁體）
					config.error=配置文件出現錯誤，請檢查配置文件後再試！
					qq.password.not.exits=請填寫配置文件的 QQ號 與 密碼！
					failed.create.config=創建配置文件失敗！
					failed.check.update=檢測更新失敗！（%s）
					checking.update=正在檢測更新...
					already.latest.version=已是最新版本！（%s）
					found.new.update=發現新版本：%s
					trying.login=正在嘗試使用%s登錄, 稍後可能會出現驗證碼彈窗...
					registering.event=正在註冊事件...
					enabling.plugin=正在加載插件 [%s]...
					failed.load.plugin=在加載插件 [%s] 時發生了錯誤：%s
					login.success=登錄成功，您的昵稱是：%s
					not.group.set=配置文件中的 聊群 項為空！您將無法發送和接收到聊群的消息！
					not.entered.group=機器人並未加入聊群 %s , 但機器人目前可以繼續使用！
					now.group=當前進入的聊群為：%s (%s)
					stopping.bot=正在關閉機器人：%s (%s)
					please.restart=請重新啓動 MiraiBot
					console.cleared=已清除控制臺！
					bot=機器人
					not.user=無法獲取該用戶！
					not.friend=你沒有這個好友！
					not.qq=%s 不是一個 QQ！
					time.too.long=禁言時間 %s 太長了
					usage=語法
					language=語言
					file.error=該文件不存在！
					group.id.not.found=群 ID %s 未找到, 請確保已填入配置文件？
					failed.clipboard=無法獲取當前剪切板的圖片！
					creating.word.image=正在生成文字圖片...
					width.height.error=寬度、高度和字體大小必須為整數！
					deleted.friend=已刪除 %s (%s)
					message.not.found=未找到該消息！
					message.id.error=消息位置必須是整數！
					request.not.found=未找到該入群請求！
					request.id.error=入群請求位置必須是整數！
					request.accepted=已同意該入群請求！
					failed.accept.request=無法同意該入群請求！
					recalled=已撤回該消息！
					kicked=已踢出該成員！
					nudged=成功戳一戳該成員！
					time=時長
					failed.recall=無法撤回該消息！
					no.permission=你沒有足夠的權限進行該操作！
					bot.is.being.muted=機器人已被禁言！無發發送消息！
					member.mute=%s (%s) 被 %s (%s) 禁言 %s 秒！
					member.unmute=%s (%s) 已被 %s (%s) 解除禁言！
					open=已開啟！
					off=已關閉
					mute.all=全員禁言
					qq.password.error=請檢查配置文件中的 QQ號 是否正確！
					success.change.language=成功修改語言為 繁體中文
					up.loading.img=正在上傳圖片...
					up.loading.voice=正在上傳語音...
					join.request.group=[%s] %s (%s) 請求加入聊群 %s (%s)
					joined.group=%s (%s) 加入了聊群 %s (%s)
					left.group=%s (%s) 離開了聊群 %s (%s)
					kick.group=%s (%s) 被 %s (%s) 請出了聊群 %s (%s)
					recall.message=%s 撤回了一條 [%s] 消息
					recall.unknown.message=%s 撤回了一條消息
					recall.others.message=%s 撤回了一條 %s 的 [%s] 消息
					recall.others.unknown.message=%s 撤回了一條 [%s] 的消息
					image.id=圖片ID
					format.group.message=[%s] %s(%s): %s
					format.group.recallable.message=[%s] %s(%s): %s
					format.user.message=%s(%s) -> %s(%s): %s
					format.user.recallable.message=[%s] %s(%s) -> %s(%s): %s
					command.stop=關閉機器人
					command.check.update=手動檢查 MiraiBot 是否有更新
					command.friend.list=獲取當前機器人好友列表
					command.language=更改 MiraiBot 的語言
					command.member.list=獲取當前聊群成員列表
					command.help=顯示 MiraiBot 所有指令
					command.send=向好友發送消息（支持 Mirai碼）
					command.reply=回復一條消息
					command.recall=撤回一個消息
					command.image=向當前聊群發送圖片
					command.image.info=查看一個圖片的信息
					command.up.img=上傳一個圖片到服務器並獲取到圖片信息
					command.up.clip.img=上傳當前剪切板的圖片
					command.new.img=創建並上傳一個圖片
					command.del=刪除一個好友
					command.accept.request=同意一個入群請求
					command.avatar=獲取一個用戶的頭像
					command.kick=踢出一個用戶
					command.mute=禁言一個用戶指定時間（單位：秒）
					command.name.card=修改一個用戶的群昵稱
					command.group=切換到已設置的某個群
					command.unload=卸載某個插件
					command.load=加載某個插件
					command.plugins=列出目前已加載的所有插件
					command.reload=重載某個插件
					command.music=通過網易云音樂 ID 分享音樂
					command.nudge=戳一戳某人
					command.voice=發送一個語音
					command.accept.invite=接受一個邀請入群請求
					name.card.set=已将 %s 的群昵称设置为 %s
					reloaded=已重新加載配置文件！
					before.settings=在啟動前，您需要完成一些最基礎的設置
					please.input.qq=請輸入您的機器人的 QQ 號
					please.input.password=請輸入您的機器人的密碼
					please.input.group.id=請輸入啟用機器人的群（用","分隔）
					please.input.check.update.on.setup=是否在每次啟動都執行檢查更新程序？ （是(true) 否(false)）
					group=群
					id=ID
					name.card=群昵稱
					password=密碼
					qq=QQ
					reason=原因
					contents=內容
					request.id=請求ID
					message.id=消息ID
					file.path=文件路徑
					width=寬度
					height=高度
					plugin.name=插件名
					font.size=字體大小
					plugin.not.exits=插件 %s 不存在或已被卸載!
					unloading.plugin=正在卸載插件 %s...
					unloaded.plugin=插件 %s 已成功卸載！
					file.name=文件名
					plugin.file.not.exits=未找到插件文件 %s!
					loading.plugin=正在加載插件 %s...
					loaded.plugin=插件 %s 已成功加載！
					plugin.already.loaded=插件 %s 已經被載入！
					music.id=網易雲音樂 ID
					music.id.error=網易雲音樂 ID 錯誤, 請檢查后再試！
					music.code.error=獲取音樂信息失敗, 請檢查后再試！
					you=你
					itself=TA自己
					yourself=你自己
					nudge.message=%s%s%s%s
					bot.leave.group=機器人 %s(%s) 離開了群 %s(%s)
					bot.leave.group=機器人 %s(%s) 被 %s(%s) 踢出了群 %s(%s)
					bot.permission.change=機器人 %s(%s) 的群權限由 %s 更改為了 %s
					member.name.card.change=群成員 %s(%s) 的群昵稱由 %s 被修改為了 %s
					member.permission.change=群成員 %s(%s) 的權限由 %s 被修改為了 %s
					invite.request.group=[%s] 好友 %s(%s) 邀請你進入群 %s(%s)
					invite.id=邀請入群 ID
					invite.id.error=邀請入群請求 ID 錯誤！
					invite.not.found=未找到該邀請入群請求 ID！
					failed.accept.invite=同意邀請入群請求失敗！
					invite.accepted=成功接受該邀請入群請求！
					format.time=[HH:mm:ss]\s
					dice.not.number=骰子值必須是整數
					dice.value.error=骰子的值不能大於六和小於一！
					dice.value=骰子值
					contact.id.error=聯係人 ID 錯誤！
					contact=聯係人
					cannot.create.plugin.dir=創建插件目錄失敗
					depend.not.exits=缺少插件依賴 "%s"
					event.error=執行 %s 插件的 %s 事件時出現錯誤：%s
					exception.string=問題 %s 描述: %s
					exception.details=\\t 在 %s.%s（%s 的第 %d 行）
					unknown.error=出現錯誤！進程即將終止！
					""").getBytes();
		} else {
			return ("# English (" + language + ")\n" +
					"config.error=An error occurred in the configuration file, please check the configuration file and try again!\n" +
					"qq.password.not.exits=Please fill in your QQ number and password of the configuration file!\n" +
					"failed.create.config=Failed to create configuration file!\n" +
					"failed.check.update=Failed to check update! (%s)\n" +
					"checking.update=Checking update...\n" +
					"already.latest.version=This version is the latest version. (%s)\n" +
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
					"request.accepted=Agreed group request!\n" +
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
					"command.member.list=Get the list of current chat group members\n" +
					"command.help=Show all commands of MiraiBot\n" +
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
					"before.settings=Before using the bot, you should set something basically up\n" +
					"please.input.qq=Please input the qq number of the bot\n" +
					"please.input.password=Please input the password of the bot\n" +
					"please.input.group.id=Please input the enabled groups number (use \",\" split it)\n" +
					"please.input.check.update.on.setup=Whether check update on setup? (true/false)\n" +
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
					"format.time=[HH:mm:ss] \n" +
					"dice.not.number=The dice value must be an integer\n" +
					"dice.value.error=The value of the dice cannot be greater than six and less than one！\n" +
					"dice.value=Dice value\n" +
					"contact.id.error=Wrong contact ID!\n" +
					"contact=Contact\n" +
					"cannot.create.plugin.dir=Failed to create plugin directory\n" +
					"depend.not.exits=Missing plugin dependency \"%s\"\n" +
					"event.error=Error executing %s event for %s plugin: %s\n" +
					"exception.string=%s: %s\n" +
					"exception.details=\\t at %s.%s(%s:%d）\n" +
					"unknown.error=An error occurred! The process is about to terminate!\n").getBytes();
		}
	}
}
