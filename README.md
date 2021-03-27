# MiraiBot
A Mirai-based console robot

***Please note before use***
```
This project is at least compatible with JDK15!
```
**Windows** users please run `Login.bat` and then verify
  After the verification is successful, you can log in to the account with `device.json` in the current directory
  Just copy `device.json` to log in to your account on any device
  Then use `Start.bat` to run

**Linux** users first need to run a terminal in the `MiraiBot` directory
  You need to use the following instructions for the first run:
```
java -classpath MiraiBot.jar: Mirai-Core-2.4.0.jar: Mirai-login-solver-selenium.jar PluginMain
```
  After successfully logging in once, you can delete `Mirai-login-solver-selenium.jar` and use the following command
```
java -classpath MiraiBot.jar: Mirai-Core-2.4.0.jar PluginMain
```

**MacOS** and **CentOS** users, please **test yourself** Shell script
  The specific operating instructions are:
```
java -classpath MiraiBot.jar; Mirai-Core-2.4.0.jar; Mirai-login-solver-selenium.jar PluginMain
```
  Among them, `Mirai-login-solver-selenium.jar` can be deleted after **successful login** once


# MiraiBot
一个基于 Mirai 的控制台机器人

**使用前请注意：**
```
本项目最低兼容到 JDK15！
```
**Windows** 用户请先运行 `Login.bat` 然后进行验证
 验证成功后当前目录的 `device.json` 就可以登录该账户
 只需要复制 `device.json` 就可以在任何设备登录你的账号
 然后接下来使用 `Start.bat` 即可运行

**Linux** 用户首先需要在 `MiraiBot` 目录运行一个终端
 首次运行需要使用以下指令：
```
java -classpath MiraiBot.jar: Mirai-Core-2.4.0.jar: Mirai-login-solver-selenium.jar PluginMain
```
 成功登录一次后即可删除 `Mirai-login-solver-selenium.jar` 并使用以下指令
```
java -classpath MiraiBot.jar: Mirai-Core-2.4.0.jar PluginMain
```

**MacOS** 与 **CentOS** 用户请**自行测试** Shell 脚本
 具体运行指令为：
```
java -classpath MiraiBot.jar;Mirai-Core-2.4.0.jar;Mirai-login-solver-selenium.jar PluginMain
```
 其中 `Mirai-login-solver-selenium.jar` 可以在**成功登录**一次后删除
