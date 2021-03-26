# MiraiBot
A Mirai-based console robot

***Please note before use***
```
Since the minimum requirement for Mirai is Java 8, please use a JDK above Java 8
For details, please see: https://github.com/mamoe/mirai/blob/dev/docs/Preparations.md#jvm-%E7%8E%AF%E5%A2%83%E8%A6%81%E6%B1%82
```
**Windows** users please run `Login.bat` and then verify
  After the verification is successful, you can log in to the account with `device.json` in the current directory
  Just copy `device.json` to log in to your account on any device
  Then use `Start.bat` to run

**MacOS** and **CentOS** users, please **test yourself** Shell script
  The specific operating instructions are:
```
java -classpath MiraiBot.jar; Mirai-Core-2.4.0.jar; Mirai-login-solver-selenium.jar PluginMain
```
  Among them, `Mirai-login-solver-selenium.jar` can be deleted after **successful login** once

**Linux** and other users need a `device.json` that **can log in** before **can use**


# MiraiBot
一个基于 Mirai 的控制台机器人

**使用前请注意：***
```
由于 Mirai 最低需求是 Java8，所以请使用 Java8 以上的 JDK
详情请看：https://github.com/mamoe/mirai/blob/dev/docs/Preparations.md#jvm-%E7%8E%AF%E5%A2%83%E8%A6%81%E6%B1%82
```
**Windows** 用户请先运行 `Login.bat` 然后进行验证
 验证成功后当前目录的 `device.json` 就可以登录该账户
 只需要复制 `device.json` 就可以在任何设备登录你的账号
 然后接下来使用 `Start.bat` 即可运行

**MacOS** 与 **CentOS** 用户请**自行测试** Shell 脚本
 具体运行指令为：
```
java -classpath MiraiBot.jar;Mirai-Core-2.4.0.jar;Mirai-login-solver-selenium.jar PluginMain
```
 其中 `Mirai-login-solver-selenium.jar` 可以在**成功登录**一次后删除

**Linux** 等其他用户需要一个**可以登录**的 `device.json` 才**可以使用**
