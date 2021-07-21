package xiaym.miraibot;
import java.util.*;
import com.windowx.miraibot.plugin.Plugin;
import com.windowx.miraibot.utils.LogUtil;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.*;

public class GrassReply extends Plugin {
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
