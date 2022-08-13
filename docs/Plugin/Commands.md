# MiraiBot 插件文档 - Commands 篇

本章会详细说明，如何创建一个自己的命令

在 MiraiBot 中创建命令的方法与 Bukkit 或 Spigot 等较为相似，如果有相关经验的人 那么上手应该不难

## 创建命令执行器 (CommandExecutor)
你需要自己创建一个类用作命令的监听，而命令监听器则必须 `implements CommandExecutor`

示例代码如下：
```Java
import com.windowx.miraibot.command.CommandExecutor;

public class DemoExecutor implements CommandExecutor {
    @Override
    public void onCommand(String label, String[] args) {
        // ...
    }
}
```

其中传递的参数：
- label: 执行的命令
- args: 执行命令附带的参数数组

## 注册命令执行器
在创建完命令执行器后，你需要让它生效

首先你需要获取你的插件的 `Commands`：
```Java
Commands commands = this.getCommands();
```

然后就是创建你的 `Command`

一个 Command 应当包含 `name` 和 `description`

假设你的命令是 `demo`，简介是 `A demo command.`，那么代码应该是如下所示：
```Java
Command command = new Command("demo", "A demo command.");
```

创建完了 `Command` 后，就是设置 `CommandExecutor` 了

假设你的命令执行器类是 `DemoExecutor`，那么：
```Java
command.setExecutor(new DemoExecutor());
```

最后就是在你的插件 `Commands` 里添加上它了：

```Java
commands.set("demo", command);
```

**值得注意的是**：这些代码应当在 `onEnable` 的时候执行，而不是在某些事件之类的时候再执行

因为命令系统只会在所有插件 `onEnable` 之后设置，在那之后的改变都不会生效

## 大功告成

在完成上述操作之后你的命令就已经被添加到 MiraiBot 命令列表中了

*Enjoy it! <3*