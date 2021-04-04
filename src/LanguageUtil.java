public class LanguageUtil {
	/**
	 * Get the language file by the value obtained by "Locale.getDefault().getLanguage()"
	 * @param language The result obtained by "Locale.getDefault().getLanguage()"
	 * @return The byte[] of the language file
	 */
	public static byte[] languageFile(String language) {
		if (language.equals("zh")) {
			return ("# 中文（简体）\n" +
					"config.error=配置文件出现错误，请检查配置文件后再试！\n" +
					"qq.password.not.exits=请填写配置文件的 QQ号 与 密码！\n" +
					"failed.create.config=创建配置文件失败！\n" +
					"trying.login=正在尝试使用$1登录, 稍后可能会出现验证码弹窗...\n" +
					"registering.event=正在注册事件...\n" +
					"login.success=登录成功，您的昵称是：$1\n" +
					"not.group.set=配置文件中的 聊群 项为空！您将无法发送和接收到聊群的消息！\n" +
					"not.entered.group=机器人并未加入聊群 $1 , 但机器人目前可以继续使用！\n" +
					"now.group=当前进入的聊群为：$1 ($2)\n" +
					"stopping.bot=正在关闭机器人：$1 ($2)\n" +
					"bot=机器人\n" +
					"not.friend=你没有这个好友！\n" +
					"not.qq=$1 不是一个 QQ！\n" +
					"usage=语法\n" +
					"failed.clipboard=无法获取当前剪切板的图片！\n" +
					"creating.word.image=正在生成文字图片...\n" +
					"width.height.error=宽度、高度和字体大小必须为整数！\n" +
					"deleted.friend=已删除 $1 ($2)\n" +
					"message.not.found=未找到该消息！\n" +
					"message.id.error=消息位置必须是整数！\n" +
					"recalled=已撤回该消息！\n" +
					"failed.recall=无法撤回该消息！\n" +
					"no.permission=该消息不是你发出的且你不是管理员！\n" +
					"qq.password.error=请检查配置文件中的 QQ号 是否正确！\n" +
					"up.loading.img=正在上传图片...\n" +
					"recall.message=$1 撤回了一条 [$2] 消息\n" +
					"recall.unknown.message=$1 撤回了一条消息\n" +
					"recall.others.message=$1 撤回了一条 $2 的 [$3] 消息\n" +
					"recall.others.unknown.message=$1 撤回了一条 [$2] 的消息\n" +
					"command.stop=关闭机器人\n" +
					"command.friend.list=获取当前机器人好友列表\n" +
					"command.group.list=获取当前聊群成员列表\n" +
					"command.help=显示 MiraiBot 所有指令\n" +
					"command.send=向好友发送消息（支持 Mirai码）\n" +
					"command.reply=回复一条消息\n" +
					"command.recall=撤回一个消息\n" +
					"command.image=向当前聊群发送图片\n" +
					"command.up.img=上传一个图片到服务器并获取到ID\n" +
					"command.up.clip.img=上传当前剪切板的图片\n" +
					"command.new.img=创建并上传一个图片\n" +
					"command.del=删除一个好友\n" +
					"qq=qq\n" +
					"contents=内容\n" +
					"message.id=消息ID\n" +
					"file.path=文件路径\n" +
					"width=宽度\n" +
					"height=高度\n" +
					"font.size=字体大小\n" +
					"unknown.error=出现错误！进程即将终止！\n").getBytes();
		} else if (language.equals("tw")) {
			return ("# 中文（繁體）\n" +
					"config.error=配置文件出現錯誤，請檢查配置文件後再試！\n" +
					"qq.password.not.exits=請填寫配置文件的 QQ號 與 密碼！\n" +
					"failed.create.config=創建配置文件失敗！\n" +
					"trying.login=正在嘗試使用$1登錄, 稍後可能會出現驗證碼彈窗...\n" +
					"registering.event=正在註冊事件...\n" +
					"login.success=登錄成功，您的昵稱是：$1\n" +
					"not.group.set=配置文件中的 聊群 項為空！您將無法發送和接收到聊群的消息！\n" +
					"not.entered.group=機器人並未加入聊群 $1 , 但機器人目前可以繼續使用！\n" +
					"now.group=當前進入的聊群為：$1 ($2)\n" +
					"stopping.bot=正在關閉機器人：$1 ($2)\n" +
					"bot=機器人\n" +
					"not.friend=你沒有這個好友！\n" +
					"not.qq=$1 不是一個 QQ！\n" +
					"usage=語法\n" +
					"failed.clipboard=無法獲取當前剪切板的圖片！\n" +
					"creating.word.image=正在生成文字圖片...\n" +
					"width.height.error=寬度、高度和字體大小必須為整數！\n" +
					"deleted.friend=已刪除 $1 ($2)\n" +
					"message.not.found=未找到該消息！\n" +
					"message.id.error=消息位置必須是整數！\n" +
					"recalled=已撤回該消息！\n" +
					"failed.recall=無法撤回該消息！\n" +
					"no.permission=該消息不是你發出的且你不是管理員！\n" +
					"qq.password.error=請檢查配置文件中的 QQ號 是否正確！\n" +
					"up.loading.img=正在上傳圖片...\n" +
					"recall.message=$1 撤回了一條 [$2] 消息\n" +
					"recall.unknown.message=$1 撤回了一條消息\n" +
					"recall.others.message=$1 撤回了一條 $2 的 [$3] 消息\n" +
					"recall.others.unknown.message=$1 撤回了一條 [$2] 的消息\n" +
					"command.stop=關閉機器人\n" +
					"command.friend.list=獲取當前機器人好友列表\n" +
					"command.group.list=獲取當前聊群成員列表\n" +
					"command.help=顯示 MiraiBot 所有指令\n" +
					"command.send=向好友發送消息（支持 Mirai碼）\n" +
					"command.reply=回復一條消息\n" +
					"command.recall=撤回一個消息\n" +
					"command.image=向當前聊群發送圖片\n" +
					"command.up.img=上傳一個圖片到服務器並獲取到ID\n" +
					"command.up.clip.img=上傳當前剪切板的圖片\n" +
					"command.new.img=創建並上傳一個圖片\n" +
					"command.del=刪除一個好友\n" +
					"qq=qq\n" +
					"contents=內容\n" +
					"message.id=消息ID\n" +
					"file.path=文件路徑\n" +
					"width=寬度\n" +
					"height=高度\n" +
					"font.size=字體大小\n" +
					"unknown.error=出現錯誤！進程即將終止！\n").getBytes();
		} else {
			return ("# English (" + language + ")\n" +
					"config.error=An error occurred in the configuration file, please check the configuration file and try again!\n" +
					"qq.password.not.exits=Please fill in the QQ number and password of the configuration file!\n" +
					"failed.create.config=Failed to create configuration file!\n" +
					"trying.login=Trying to log in with $1, a verification code pop-up window may appear later...\n" +
					"registering.event=Registering event...\n" +
					"login.success=Login is successful, your nickname is: $1\n" +
					"not.group.set=The chat group item in the configuration file is empty! You will not be able to send and receive chat group messages!\n" +
					"not.entered.group=The robot has not joined the chat group for $1, but the robot can continue to be used for now!\n" +
					"now.group=The currently entered chat group is: $1 ($2)\n" +
					"stopping.bot=Closing robot: $1 ($2)\n" +
					"bot=Robot\n" +
					"not.friend=You don't have this friend!\n" +
					"not.qq=$1 is not a QQ!\n" +
					"usage=Usage\n" +
					"failed.clipboard=Unable to get the picture of the current clipboard!\n" +
					"creating.word.image=Generating text image...\n" +
					"width.height.error=The width, height, and font size must be integers!\n" +
					"deleted.friend=$1 ($2) deleted\n" +
					"message.not.found=The message was not found!\n" +
					"message.id.error=The message position must be an integer!\n" +
					"recalled=The message has been withdrawn!\n" +
					"failed.recall=The message cannot be retracted!\n" +
					"no.permission=The message was not sent by you and you are not an administrator!\n" +
					"qq.password.error=Please check whether the QQ number in the configuration file is correct!\n" +
					"up.loading.img=Uploading Image...\n" +
					"recall.message=$1 withdrew a [$2] message\n" +
					"recall.unknown.message=$1 withdrew a message\n" +
					"recall.others.message=$1 withdrew a $3 message for [$2]\n" +
					"recall.others.unknown.message=$1 withdrew a message for [$2]\n" +
					"command.stop=Turn off the robot\n" +
					"command.friend.list=Get the current robot friend list\n" +
					"command.group.list=Get the list of current chat group members\n" +
					"command.help=Show all MiraiBot commands\n" +
					"command.send=Send messages to friends (support Mirai code)\n" +
					"command.reply=Reply to a message\n" +
					"command.recall=Recall a message\n" +
					"command.image=Send a picture to the current chat group\n" +
					"command.up.img=Upload a picture to the server and get the ID\n" +
					"command.up.clip.img=Upload a picture of the current clipboard\n" +
					"command.new.img=Create and upload an image\n" +
					"command.del=Delete a friend\n" +
					"qq=qq\n" +
					"contents=Content\n" +
					"message.id=Message ID\n" +
					"file.path=File path\n" +
					"width=Width\n" +
					"height=Height\n" +
					"font.size=Font size\n" +
					"unknown.error=An error occurred! The process is about to terminate!\n").getBytes();
		}
	}
}
