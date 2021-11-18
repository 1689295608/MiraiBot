# MiraiBot 使用文档

## AutoRespond丨自动回复

> 目录
> - [Message](#Message)
> - [Respond](#Respond)
> - [Recall](#Recall)
> - [Reply](#Reply)
> - [Mute](#Mute)
> - [RunCommand](#RunCommand)

---

### Message
用于执行该自动回复项的唯一方法
支持正则语法

### Respond
执行自动回复项后发送的消息，详见 [AutoRespond.json](//github.com/1689295608/MiraiBot/blob/main/AutoRespond.json)

### Recall
执行后是否撤回该消息，布尔值（`true`/`false`）

### Reply
用于修补 MiraiCode 无法回复该消息的问题，为 `true` 时回复该消息 内容为 `Respond` 的内容

### Mute
执行后是否禁言该成员，整数 单位为秒，为 `0` 时不禁言

### RunCommand
执行后是否执行机器人指令，例如 `kick %sender_id%` 即可踢出该成员

---

## 该文档暂未完成
