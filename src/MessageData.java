import net.mamoe.mirai.message.data.MessageSource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MessageData implements Serializable {
	private ArrayList<MessageSource> messages = new ArrayList<>();
	
	public MessageData() {
		super();
	}
	public MessageSource get(int key) {
		return this.messages.get(key);
	}
	public void add(MessageSource source) {
		this.messages.add(source);
	}
	public void set(ArrayList<MessageSource> messages) {
		this.messages = messages;
	}
	public List<MessageSource> getMessages() {
		return this.messages;
	}
	public int size() {
		return this.messages.size();
	}
}
