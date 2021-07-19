package xiaym.miraibot;
import java.util.Random;
import com.windowx.miraibot.plugin.Plugin;
import com.windowx.miraibot.utils.LogUtil;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.*;

public class GrassReply extends Plugin {
				String PluginName = "Grass Reply";
				@Override public void onEnable(){
				loginfo("Grass Reply插件已加载!");
				}

				@Override public void onDisable(){
				loginfo("Grass Reply插件已卸载!");
				}

				@Override public void onGroupMessage(GroupMessageEvent event){
				String msg = event.getMessage().contentToString();
				if(msg.equals("草") || msg.equals("艹")){

																				event.getGroup().sendMessage(new QuoteReply(event.getSource()) .plus("(一种植物)"));
				}
				if(msg.equals("逊")){
												event.getGroup().sendMessage(new QuoteReply(event.getSource()) .plus("那么说，你很勇咯?"));
				}
				}

				public void loginfo(String logmsg){
								LogUtil.log("["+PluginName+"] "+logmsg);
				}
}
