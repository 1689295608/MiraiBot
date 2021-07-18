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
请前往 [Open JDK](https://jdk.java.net/) 下载**高于 JDK 11**的 `Open JDK`

> 本项目基于 JDK 11 开发, 所以请使用 JDK 11 以上的 Java 运行本程序!

基于 JDK 11 开发，也代表着编译环境不能低于 JDK 11。

#### 目前需要注意的就这些，接下来 开始你的插件之旅吧！

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

## 第二部：创建主类

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
