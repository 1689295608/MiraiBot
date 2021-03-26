# MiraiBot
一个基于 Mirai 的控制台机器人

**Windows** 用户请先运行`Login.bat`然后进行验证
 验证成功后当前目录的 `device.json` 就可以登录该账户
 只需要复制 `device.json` 就可以在任何设备登录你的账号
 然后接下来使用 `Start.bat` 即可运行

**MacOS** 与 **CentOS** 用户请**自行测试** Shell 脚本
 具体运行指令为：
 `java -classpath MiraiBot.jar;Mirai-Core-2.4.0.jar;Mirai-login-solver-selenium.jar PluginMain`
 其中 `Mirai-login-solver-selenium.jar` 可以在**成功登录**一次后删除

**Linux** 等其他用户需要一个**可以登录**的 `device.json` 才**可以使用**
