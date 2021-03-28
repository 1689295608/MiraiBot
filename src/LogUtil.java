import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogUtil {
	static byte[] all = new byte[0];
	public static void Log(String log){
		try {
			SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd");
			Date date = new Date(System.currentTimeMillis());
			String time = formatter.format(date);
			File dir = new File("logs");
			if (!dir.exists()){
				if (!dir.mkdirs()){
					System.out.println(time + "创建配置文件失败！即将终止进程！");
					System.exit(-1);
					return;
				}
			}
			File file = new File("logs" + File.separator + time + ".log");
			if (!file.exists()){
				if (!file.createNewFile()){
					System.out.println(time + "创建配置文件失败！即将终止进程！");
					System.exit(-1);
					return;
				}
			}
			formatter= new SimpleDateFormat("[HH:mm:ss] ");
			date = new Date(System.currentTimeMillis());
			time = formatter.format(date);
			FileInputStream fis = new FileInputStream(file);
			all = fis.readAllBytes();
			FileOutputStream fos = new FileOutputStream(file);
			all = byteMerger(all, (time + log + "\n").getBytes());
			fos.write(all);
			fos.flush();
			System.out.println(time + log);
		} catch (IOException e) {
			System.out.println("IO 流 抛出错误，即将终止进程！");
			System.out.println("(" + e.getCause() + " : " + e.getMessage() + ")");
			System.exit(-1);
		}
	}
	public static void Exit(){
		try {
			SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd");
			Date date = new Date(System.currentTimeMillis());
			String time = formatter.format(date);
			File dir = new File("logs");
			if (!dir.exists()){
				if (!dir.mkdirs()){
					System.out.println(time + "创建配置文件失败！即将终止进程！");
					System.exit(-1);
					return;
				}
			}
			File file = new File("logs" + File.separator + time + ".log");
			if (!file.exists()){
				if (!file.createNewFile()){
					System.out.println(time + "创建日志文件失败！即将终止进程！");
					System.exit(-1);
					return;
				}
			}
			FileInputStream fis = new FileInputStream(file);
			all = fis.readAllBytes();
			FileOutputStream fos = new FileOutputStream(file);
			all = byteMerger(all, ("\n\n----=== LogUtil Closed ===----\n\n").getBytes());
			fos.write(all);
			fos.flush();
		} catch (IOException e) {
			System.out.println("IO 流 抛出错误，即将终止进程！");
			System.out.println("(" + e.getCause() + " : " + e.getMessage() + ")");
			System.exit(-1);
		}
	}
	public static byte[] byteMerger(byte[] byte1, byte[] byte2){
		byte[] byte3 = new byte[byte1.length + byte2.length];
		System.arraycopy(byte1, 0, byte3, 0, byte1.length);
		System.arraycopy(byte2, 0, byte3, byte1.length, byte2.length);
		return byte3;
	}
}
