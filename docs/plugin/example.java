package miraibot.example;

import java.util.*;
import com.windowx.miraibot.plugin.Plugin;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.QuoteReply;

public class example extends Plugin {
        //编译后会存在于 /miraibot/example/example.class 主类为miraibot.example.example

	@Override
	public void onEnable(){
		this.info(this.getName() + " 已加载!");
        }

        @Override
	public void onDisable(){
		this.info(this.getName() + " 插件已卸载!");
	}

	@Override
	public void onGroupMessage(GroupMessageEvent event){
		String msg = event.getMessage().contentToString();
		if(msg.equals("草") || msg.equals("艹")){
        	         event.getGroup().sendMessage(
				 new QuoteReply(event.getSource())
				 .plus("(一种植物)")
			 );
		}
        }
}
