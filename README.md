# MiraiBot
一个基于 [Mirai](https://github.com/mamoe/mirai) 的控制台机器人。

**使用前请注意：**
```
本项目基于 JDK11 开发, 所以请使用 JDK11 以上的 Java 运行本程序!
```

## 声明

<h3>一切开发旨在学习, 请勿用于非法用途</h3>

- MiraiBot 是一款免费且开放源代码的软件, 仅供学习和娱乐用途使用.
- MiraiBot 不会通过任何方式强制收取费用, 或对使用者提出物质条件.
- MiraiBot 由整个开源社区维护, 并不是属于某个个体的作品, 所有贡献者都享有其作品的著作权.

## 许可证

详见 https://github.com/1689295608/MiraiBot/blob/main/LICENSE

MiraiBot 继承 [Mirai](https://github.com/mamoe/mirai) 使用 AGPLv3 协议开源. 为了整个社区的良性发展, 我们强烈建议您做到以下几点:

- 间接接触到 MiraiBot 的软件使用 AGPLv3 开源
- **不鼓励, 不支持一切商业使用**

<h3>若因您对 MiraiBot 的不当使用而导致您的任何损失, 该软件 及 该软件开发者不负任何责任</h3>

### 衍生软件需声明引用

- 若引用 MiraiBot 发布的软件包而不修改 MiraiBot, 则衍生项目需相关内容首次出现的部位明确申明来源于本仓库 ([MiraiBot](https://github.com/1689295608/MiraiBot)).
- 若修改 MiraiBot 源代码再发布, 或参考 MiraiBot 内部实现发布另一个项目, 则衍生项目必须在文章首部或 'MiraiBot' 相关内容首次出现的位置明确声明来源于本仓库 ([MiraiBot](https://github.com/1689295608/MiraiBot)). 不得扭曲或隐藏免费且开源的事实.

---

## 初次使用
**初次使用** 的用户, 请前往 [Release](https://github.com/1689295608/MiraiBot/releases/latest) 下载最新的 .zip/.7z 包

然后将其解压到任意文件夹内 ( *不推荐包含特殊字符的文件夹及路径* ), 即可进行后续操作.


### Windows 用户
**Windows** 用户直接运行 `Start.bat` 即可。

登录时如果这个 `device.json` 没有登录过这个 QQ 那么一般情况下会进行设备锁验证

那将会弹出一个窗口，一般是一个链接，如果你有手机 QQ 那么推荐使用 QQ 内置浏览器打开

因为那样就可以进行人脸验证（如果没开启就没有）

在电脑浏览器打开也可以，可以进行 QQ 扫码验证 需要登录该 QQ 的手机 QQ 扫码授权

如果近几天没有使用账户信息辅助验证，那么也可以进行账户信息辅助验证来登录该 QQ

### Linux 用户
**Linux** 用户首先需要在 `MiraiBot` 目录运行一个终端

然后运行 `Start.sh` （即在终端输入 `bash Start.sh`）即可。

如果您的 Linux 有可视化环境，那么可以按照 Windows 用户方法进行设备锁验证

反之，你可以尝试通过 [MiraiAndroid](https://github.com/mzdluo123/MiraiAndroid) 来进行设备锁验证

你可以使用两种方法来生产 `device.json`：
1. [将您当前的 `device.json` 导入到 `MiraiAndroid`](#将您当前的 device.json 导入到 MiraiAndroid)
2. [将 `MiraiAndroid` 的 `device.json` 应用于当前](#将 MiraiAndroid 的 device.json 应用于当前)

这两个的区别在于，如果你当前的 `device.json` 需要用于其他用处不想被覆盖，则可以导入当前 `device.json` 到 `MiraiAndroid`

#### 将您当前的 `device.json` 导入到 `MiraiAndroid`

首先将当前的 `device.json` 通过任何方式传输到你的手机中

然后在你的手机打开 `MiraiAndroid`，点击左上角菜单按钮，在弹出的菜单中选择 `工具` 选项卡

然后点击 `导入 DEVICE.JSON`，然后选择你刚才传输到你手机中的 `device.json`

最后使用 `MiraiAndroid` 登录一次你的 QQ 即可

#### 将 `MiraiAndroid` 的 `device.json` 应用于当前

首先在你的手机上打开 `MiraiAndroid`

然后登录并进行相应验证后

点击左上角菜单按钮，在弹出的菜单中选择 `工具` 选项卡

点击 `导出 DEVICE.JSON` 然后选择保存位置后

将这个 `device.json` 传输到你的 Linux 中，替换你当前的 `device.json`

再次登录即可成功登录
