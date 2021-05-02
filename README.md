# MiraiBot
A Mirai-based console robot.

**Please note before use:**

This project is developed based on JDK11, so please use Java above JDK11 to run this program!

## Statement

<h3>All development is for learning, please do not use it for illegal purposes</h3>

- MiraiBot is a free and open source software for learning and entertainment purposes only.
- MiraiBot will not compulsorily charge fees or impose material conditions on users in any way.
- MiraiBot is maintained by the entire open source community and is not a work belonging to an individual. All contributors enjoy the copyright of their work.

## License

See https://github.com/1689295608/MiraiBot/blob/main/LICENSE for details

MiraiBot inherits [Mirai](https://github.com/mamoe/mirai) Open source using AGPLv3 protocol. For the healthy development of the entire community, we strongly recommend that you do the following:

- Software indirectly exposed to MiraiBot uses AGPLv3 open source
- Does not encourage and does not support all commercial use

### Derivative software needs to declare and quote

- If you quote the package released by MiraiBot without modifying MiraiBot, the derivative project needs to mention MiraiBot in any part of the description.
- If the MiraiBot source code is modified and then released, or another project is released by referring to MiraiBot's internal implementation, the derivative project must be clearly stated in the first part of the article or at the location where'MiraiBot'-related content first appears from this repository (https://github.com/1689295608/MiraiBot). The fact that it is free and open source must not be distorted or hidden.

---

### For Windows
**Windows** users, please run `Login.bat` first and then verify

After the verification is successful, the `device.json` in the current directory can be used to log in to the account on any other device

Then use `Start.bat` to log in to the account


### For Linux
**Linux** users first need to run a terminal in the `MiraiBot` directory

You need to use the following instructions for the first run:

	java -classpath MiraiBot.jar:Mirai-Core-All.jar:Mirai-login-solver-selenium.jar com.windowx.miraibot.PluginMain

After successfully logging in once, you can delete `Mirai-login-solver-selenium.jar` and use the following command

	java -classpath MiraiBot.jar:Mirai-Core-All.jar com.windowx.miraibot.PluginMain

---

# MiraiBot
一个基于 [Mirai](https://github.com/mamoe/mirai) 的控制台机器人。

**使用前请注意：**

	本项目基于 JDK11 开发，所以请使用 JDK11 以上的 Java 运行本程序！

## 声明

<h3>一切开发旨在学习，请勿用于非法用途</h3>

- MiraiBot 是一款免费且开放源代码的软件，仅供学习和娱乐用途使用。
- MiraiBot 不会通过任何方式强制收取费用，或对使用者提出物质条件。
- MiraiBot 由整个开源社区维护，并不是属于某个个体的作品，所有贡献者都享有其作品的著作权。

## 许可证

详见 https://github.com/1689295608/MiraiBot/blob/main/LICENSE

MiraiBot 继承 [Mirai](https://github.com/mamoe/mirai) 使用 AGPLv3 协议开源。为了整个社区的良性发展，我们强烈建议您做到以下几点：

- 间接接触到 MiraiBot 的软件使用 AGPLv3 开源
- 不鼓励，不支持一切商业使用

### 衍生软件需声明引用

- 若引用 MiraiBot 发布的软件包而不修改 MiraiBot，则衍生项目需在描述的任意部位提及使用 MiraiBot。
- 若修改 MiraiBot 源代码再发布，或参考 MiraiBot 内部实现发布另一个项目，则衍生项目必须在文章首部或 'MiraiBot' 相关内容首次出现的位置明确声明来源于本仓库 ( [MiraiBot](https://github.com/1689295608/MiraiBot))。不得扭曲或隐藏免费且开源的事实。

---

### Windows 用户
**Windows** 用户请先运行 `Login.bat` 然后进行验证

验证成功后当前目录的 `device.json` 就可以用于在任何其他设备登录该账户

接下来使用 `Start.bat` 即可登录到该账户


### Linux 用户
**Linux** 用户首先需要在 `MiraiBot` 目录运行一个终端

首次运行需要使用以下指令：

	java -classpath MiraiBot.jar:Mirai-Core-All.jar:Mirai-login-solver-selenium.jar com.windowx.miraibot.PluginMain

成功登录一次后即可删除 `Mirai-login-solver-selenium.jar` 并使用以下指令

	java -classpath MiraiBot.jar:Mirai-Core-All.jar com.windowx.miraibot.PluginMain
