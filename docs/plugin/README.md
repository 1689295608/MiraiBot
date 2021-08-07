# MiraiBot 插件文档 (基础)

本文档使用到 [IntelliJ IDEA](https://www.jetbrains.com/idea/) 来编辑及构建您的插件

## 基础准备
你需要以下软件/环境:
 - Open JDK 11+ 
 - IntelliJ IDEA

## 预先准备
### 你需要一定的编程基础

如果你根本没有编程基础和 Java 基础的话，我们不推荐您直接上手进行编写 MiraiBot 的插件

你需要了解并储备一定的编程基础和 Java 基础，推荐使用 [Practie-It](https://practiceit.cs.washington.edu/) 学习 Java。

### 安装相关编译环境
请前往 [Open JDK](https://jdk.java.net/) 下载**高于或等于 JDK 11**的 `Open JDK`

> 本项目基于 JDK 11 开发, 所以请使用 JDK 11 及以上的 Java 运行本程序!

基于 JDK 11 开发，也代表着编译环境不能低于 JDK 11。

#### 目前需要注意的就这些，接下来 开始你的插件之旅吧！

## 目录

> [第一步：创建项目](#第一步创建项目)
> 
> [第二步：创建主类](#第二步创建主类)
> 
> [第三步：编写插件](#第三步编写插件)
> 
> [第四步：监听事件](#第四步监听事件)
> 
> [第五步：配置文件](#第五步配置文件)
> 
> [第六步：插件配置](#第六步插件配置)
> 
> [第七步：编译插件](#第七步编译插件)

## 第一步：创建项目
### 梦开始的地方

首先打开你的 IDEA，新建一个 Java 项目：

在 IDEA 主界面上点击 `New Project`

在新弹出的 `New Project` 窗口选择 Java,

在右侧的 `Project JDK` 选择你下载的 JDK (再次强调，JDK 版本**必须**大于 11！）

下面的不用管，点击 `Next` 进入下一步。

此时，如果你是一个连 `System.out.println()` 都不会的小白时，请勾选 `Create project form temple` 并选择 `Command Line App`

然后进入下一步，

接下来在 `Project name` 中填入你的项目名称，可以为任何内容 (不推荐使用中文项目名)

在 `Project location` 中填入项目路径，也可以为任何内容，也依然不推荐使用中文路径。

如果没有特殊需要，可以不更改 `More Setting` 的内容

最后，点击 `Finish` 按钮，开始新建项目！


### 引用依赖库

首先你需要下载 MiraiBot 以及 Mirai-Core-All，你需要在 [Release](https://github.com/1689295608/MiraiBot/releases) 下载它们

进入 Release 页面后，找到最新版本，下载 `Assets` 中的 `.zip` 或 `.7z` 文件并解压它。

将其放在一个你喜欢的目录，然后进入 IDEA，按下组合键 `Ctrl + Alt + Shift + S` 或点击 `File` -> `Project Structure`

在新出现的窗口左侧中选择 `Libraries`，点击 `+` -> `Java`。

在新出现的窗口中选择你解压到的目录中的 `Mirai-Core-All.jar`，点击 `OK`，之后会有一个询问窗口，点击 `OK` 即可

然后再添加 `MiraiBot` 的依赖，点击 `+` -> `Java` 后选择 `MiraiBot.jar` 点击 `OK` 后同上，点击 `OK` 即可。

## 第二步：创建主类

一般情况下，新建一个项目后，会有一个 `src` 目录 用于储存源代码

首先，创建一个目录用于区分你的类：
- 右键 `src` 目录
- 选择 `New`
- 选择 `Package`
- 输入你的目录名即可，不推荐包含中文字符 (`.` 就相当于 Windows 的 `/`, 例如要创建 `com/plugin` 文件夹, 则需输入 `com.plugin`

接下来，创建一个 Java 类吧：
- 右键你刚才创建的目录
- 选择 `New`
- 选择 `Java Class`
- 输入的你类名，不推荐包含中文字符

## 第三步：编写插件

**开始这一步，你需要一定的 Java 基础！**

可以参考 [`example.java`](example.java) 进行编写。

一个插件类，必须 `extends` MiraiBot 的 `Plugin` 类，

使用 `import com.windowx.miraibot.plugin.Plugin;` 来导入 MiraiBot 的 `Plugin` 类

你需要将其插入 `package xxx;` 的后面。（`xxx` 表示你的包名）

然后将 `public class xxx {` 改为 `public class xxx extends Plugin {`。（其中 `xxx` 表示你的类名）

到了现在，你的代码应该是如下所示：
```Java
package demo;

import com.windowx.miraibot.plugin.Plugin; /* 导入 Plugin 类 */

public class MyDemo extends Plugin {
  // ......
}
```

## 第四步：监听事件

一般情况下，你无需使用 Mirai 内置的事件监听器，因为 MiraiBot 内置部分事件方法（详见 [`JavaPlugin.java`](/src/com/windowx/miraibot/plugin/JavaPlugin.java)）

接下来你需要简单的监听一个事件：`onEnable` （插件启动事件）

在每一个插件加载的时候就会触发这个事件（注意，是加载时而不是加载完成）

在这个阶段时，机器人已经登录成功了，也就代表你可以对机器人进行操作

我们来简单的输出一个 `info` ，其内容为 `Hello world!`。
```Java
@Override
public void onEnable() {
  this.info("Hello world!");
}
```
让我们来逐行解析一下吧：
- `@Override` 表示重写某个方法
- `public void onEnable() {` 表示重写的方法是 `onEnable()` 方法
- `this.info()` 表示调用 `this` 对象（因为 `extends` 了 `Plugin`，所以可以调用 `Plugin` 的方法）的 `info()` 方法进行输出

另外，MiraiBot 的 `Plugin` 的方法还是蛮多的，可以借助 IDEA 的代码提示功能来进一步了解

也可以翻阅 MiraiBot 的源代码来进行了解：[`Plugin.java`](/src/com/windowx/miraibot/plugin/Plugin.java)

## 第五步：配置文件

一个应用程序，最少不了的就是配置文件了吧，接下来我们将要了解，MiraiBot 插件的配置文件系统

接下来我来演示一段代码，它将简单的表达插件系统的调用方法：
```Java
@Override
public void onEnable() {
  String name = this.getConfig().getProperty("name", "WindowX");
  this.getConfig().setProperty("time", String.valueOf(System.currentTimeMillis()));
  try {
    this.saveConfig();
  } catch (IOException e) {
    e.printStackTrace();
  }
  this.info("Your name:" + name);
}
```
一如既往，我们来分析一下每一行都干了些什么：
- `this.getConfig()` 获取本插件的配置文件 `Properties`，该文件的具体位置在 `/plugins/[插件名]/config.ini`
- `...getConfig().getProperty()` 通过调用 `Properties` 类，获取其 `Property` 项，返回值为其内容
- `...getConfig().setProperty()` 通过调用 `Properties` 类，设置其 `Property` 项
- `this.saveConfig()` 保存本插件配置文件，会抛出 `IOException`，所以需要使用 `try` 来抓取该错误
- `this.info(/* ... */ + name)` 因为在前面获取配置项时，将其内容赋值在字符串变量 `name` 上，所以可以在这里调用

## 第六步：插件配置

一个标准 MiraiBot 插件必须要有一个 `plugin.ini` 才能加载

接下来让我们创建一个 `plugin.ini` 吧！

首先，在项目根目录创建一个 `资源根目录`，也就是先创建一个 `Directory`，然后右键它，选择 `Mark Directory as` 然后选择 `Resource Root` 即可

然后右键这个目录，选择 `New` 然后选择 `File`，再在新出现的窗口里输入 `plugin.ini` 后按下回车键即可

再打开新建的文件 `plugin.ini`，开始配置你的插件：

|配置项     |描述    |示例                   |
|--         |--     |--                     |
|**name**   |插件名称|Demo                   |
|**main**       |插件主类|miraibot.example       |
|owner      |插件作者|WindowX                |
|version    |插件版本|1.0.0                  |
|description|插件描述|一个标准 MiraiBot 插件。|

其中粗体的为必填项，若为空即无法加载该插件！

以下为示例
```Properties
# 插件名
name=Demo
# 插件主类，必须正确 否则无法加载
main=miraibot.example
# 插件作者名，默认为 Unnamed
owner=WindowX
# 插件版本，默认为 1.0.0
version=1.0.0
# 插件描述，默认为 A Plugin For MiraiBot.
description=一个标准 MiraiBot 插件。
```

## 第七步：编译插件

看来你已经编写好了一个完整的插件，接下来让我们来编译它吧！

首先在 IDEA 中按下组合键 `Ctrl + Alt + Shift + S` 或点击 `File` -> `Project Structure`

在新打开的窗口左侧选择 `Artifacts`，在右侧点击 `+` -> `JAR` -> `Empty`。

然后你就新建了一个 `Artifacts` 了，接下来把 `Name` 和 `Output Directory` 改成你想要的内容

再在右侧的 `Avail Elements` 中展开你的项目，双击 `'xxx' compile output` （其中 `xxx` 表示你的项目名） 或者右键 -> `Put into Output Root`

这样这个 `Artifacts` 就配置完成了！点击 `Apply` 或 `OK` 即可保存配置。

接下来，我们开始编译这个项目！

在 IDEA 中选择 `Build` -> `Build Artifacts...`

在新出现的 `Build Artifacts` 窗口中选择你刚才创建好的 `Artifacts`，点击 `Build` 即可！

然后 IDEA 就会开始编译插件，编译完成后 在你输入的 `Output Directory` 中就可以找到刚编译好的插件！
