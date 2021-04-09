# MiraiBot
A Mirai-based console robot.

**Please note before use:**

This project is developed based on JDK11, so please use Java above JDK11 to run this program!


### For Windows
**Windows** users, please run `Login.bat` first and then verify

After the verification is successful, the `device.json` in the current directory can be used to log in to the account on any other device

Then use `Start.bat` to log in to the account


### For Linux
**Linux** users first need to run a terminal in the `MiraiBot` directory

You need to use the following instructions for the first run:

	java -classpath MiraiBot.jar:Mirai-Core-All.jar:Mirai-login-solver-selenium.jar PluginMain

After successfully logging in once, you can delete `Mirai-login-solver-selenium.jar` and use the following command

	java -classpath MiraiBot.jar:Mirai-Core-All.jar PluginMain

---

# MiraiBot
一个基于 Mirai 的控制台机器人。

**使用前请注意：**

	本项目基于 JDK11 开发，所以请使用 JDK11 以上的 Java 运行本程序！


### Windows 用户
**Windows** 用户请先运行 `Login.bat` 然后进行验证

验证成功后当前目录的 `device.json` 就可以用于在任何其他设备登录该账户

接下来使用 `Start.bat` 即可登录到该账户


### Linux 用户
**Linux** 用户首先需要在 `MiraiBot` 目录运行一个终端

首次运行需要使用以下指令：

	java -classpath MiraiBot.jar:Mirai-Core-All.jar:Mirai-login-solver-selenium.jar PluginMain

成功登录一次后即可删除 `Mirai-login-solver-selenium.jar` 并使用以下指令

	java -classpath MiraiBot.jar:Mirai-Core-All.jar PluginMain
