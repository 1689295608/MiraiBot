# MiraiBot 使用文档

## AutoRespond丨自动回复

> 目录
> - [Message](#Message)
> - [Respond](#Respond)
> - [Recall](#Recall)
> - [Reply](#Reply)
> - [Mute](#Mute)
> - [RunCommand](#RunCommand)

--

### Message
用于执行该自动回复项的唯一方法
支持正则语法

### Respond
执行自动回复项后发送的消息，目前支持变量：
- `%sender_nick%` : 发送者昵称
- `%sender_id%` : 发送者 QQ 号
- `%sender_name_card%` : 发送者群昵称
- `%sender_avatar_id%` : 发送者头像图片 ID
- `%group_name%` : 本群名称
- `%group_id%` : 本群群号
- `%group_owner_nick%` : 本群群主昵称
- `%group_owner_id%` : 本群群主 QQ 号
- `%group_owner_name_card%` : 发送者昵称
- `%message_mirai_code%` : 消息的 Mirai 码
- `%message_content%` : 消息的内容
- `%bot_nick%` : 机器人昵称
- `%bot_id%` : 机器人 QQ 号
- `%flash_id%` : 闪照 ID
- `%image_id%` : 图片 ID
- `%file_id%` : 文件 ID

### Recall
执行后是否撤回该消息，布尔值（`true`/`false`）

### Reply
用于修补 MiraiCode 无法回复该消息的问题，为 `true` 时回复该消息 内容为 `Respond` 的内容

### Mute
执行后是否禁言该成员，整数 单位为秒，为 `0` 时不禁言

### RunCommand
执行后是否执行机器人指令，例如 `kick %sender_id%` 即可踢出该成员

--

# 该文档暂未完成 并且 MiraiBot 的功能还未完善。
