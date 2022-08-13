# MiraiBot 插件文档 - ListenerHost 篇
当你在开发 MiraiBot 插件的时候，切记不要使用 Mirai 提供的 Channel 进行事件的监听

这将会导致一些问题（插件卸载后事件依然被监听）

注册事件监听器的方法和 Bukkit 或是 Spigot 十分类似，你也可以了解相关的教程，在此不多讲

## 目录
> [第一步：创建监听类](#第一步：创建监听类)
> 
> [第二步：注册监听器](#第二步：注册监听器)
> 
> [第三步：编写代码](#第三步：编写代码)
> 
> [示例效果](#示例效果)


## 第一步：创建监听类
你需要一个类用于注册 MiraiBot 的事件监听器

示例代码如下：
```Java
import com.windowx.miraibot.event.ListenerHost;

public class DemoListener implements ListenerHost {

}
```

正如代码所示，你需要 引入 `com.windowx.miraibot.event.ListenerHost` 类

并使你的类 实现 `ListenerHost` 类

这一步的作用是为了方便 `loader.registerListener(ListenerHost listener)` 的调用

## 第二步：注册监听器
在创建好监听器的类后，你需要让它在 MiraiBot 中注册 才能使它生效，否则其中的代码并不会执行

此时你就需要调用 `this.getPluginLoader()` 的 `registerListener(Plugin plugin, ListenerHost listener)` 方法

假如你的类名是 `DemoListener` 并且与插件主类在同一包下，则代码将是：
```Java
this.getPluginLoader().registerListener(this, new DemoListener());
```

~~如果你有强迫症 觉得这行代码看着很难受 你也可以分为两行：~~
```Java
PluginLoader loader = this.getPluginLoader();
loader.registerListener(this, new DemoListener());
```

不仅如此，假如你需要一次性注册多个监听器，那么只需要调用其 `registerListeners(Plugin plugin, ListenerHost[] listeners)` 方法即可

**小提示：** 注意，这行代码应该写在你的插件的主类的 `onEnable()` 方法里，否则不会被执行

## 第三步：编写代码
注册好监听器后你就可以开始开发了，这里有一个需要注意的地方是

MiraiBot 只会用反射调用带有 `@EventHandler` 的方法

此处的 `EventHandler` 的包名应该是 `com.windowx.miraibot.event.EventHandler` 而不是其他的

MiraiBot 会通过反射传递一个参数，你需要做的就是在你的方法里的第一且唯一一个参数里写上你想要监听的事件

例如监听 `GroupMessageEvent` 的代码应该是：
```Java
@EventHandler
public void onGroupMessage(GroupMessageEvent event) {
  // do something...
}
```

值得一提的是这里的 `onGroupMessage` 是可以随意改变的

这样在 MiraiBot 触发 `GroupMessageEvent` 时便会调用这个方法 且将事件传递到这个方法的第一个参数

## 示例效果
**Demo.java** 主类
```Java
package com.demo;

import com.windowx.miraibot.plugin.Plugin;

public class Demo extends Plugin {
  public static Logger logger;
  
  @Override
  public void onEnable() {
    // 注册事件
    PluginLoader loader = this.getPluginLoader();
    loader.registerListener(this, new DemoListener());
    // 设置变量，在 DemoListener 中直接调用
    logger = this.getLogger();
  }
}
```

**DemoListener.java**
```Java
package com.demo;

import com.demo.Demo;
import com.windowx.miraibot.plugin.Plugin;
import com.windowx.miraibot.event.ListenerHost;
import com.windowx.mirainot.event.EventHandler;
import net.mamoe.mirai.event.events.GroupMessageEvent;

public class DemoListener implements ListenerHost {
  @EventHandler
  public void onGroupMessage(GroupMessageEvent event) {
    logger.info(event.getNick() + ": " + event.getMessage());
  }
}
```

该实例的效果就是在群发送消息后在控制台输出 info 消息

你可以查看以下源代码文件来得知更多的用法：
- [PluginLoader.java](https://github.com/1689295608/MiraiBot/blob/main/src/main/java/com/windowx/miraibot/plugin/PluginLoader.java)
- [EventHandler.java](https://github.com/1689295608/MiraiBot/blob/main/src/main/java/com/windowx/miraibot/event/EventHandler.java)
- [ListenerHost.java](https://github.com/1689295608/MiraiBot/blob/main/src/main/java/com/windowx/miraibot/event/ListenerHost.java)
- [EventListener.java](https://github.com/1689295608/MiraiBot/blob/main/src/main/java/com/windowx/miraibot/EventListener.java)
- [Plugin.java](https://github.com/1689295608/MiraiBot/blob/main/src/main/java/com/windowx/miraibot/plugin/Plugin.java)
