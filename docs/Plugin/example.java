package miraibot;

import com.windowx.miraibot.plugin.Plugin;

public class example extends Plugin {
    @Override
    public void onEnable() {
        this.info(this.getName() + " 已加载!");
    }

    @Override
    public void onDisable() {
        this.info(this.getName() + " 插件已卸载!");
    }
}
