import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogUtil {
	static byte[] all = new byte[0];
	static File file;
	public static void init() {
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			Date date = new Date(System.currentTimeMillis());
			String time = formatter.format(date);
			File dir = new File("logs");
			if (!dir.exists()) {
				if (!dir.mkdirs()) {
					System.out.println(time + ConfigUtil.getConfig("language.properties", "failed.create.config"));
					System.exit(-1);
					return;
				}
			}
			file = new File("logs" + File.separator + time + ".log");
			if (!file.exists()) {
				if (!file.createNewFile()) {
					System.out.println(time + ConfigUtil.getConfig("language.properties", "failed.create.config"));
					System.exit(-1);
					return;
				}
			}
			FileInputStream fis = new FileInputStream(file);
			all = fis.readAllBytes();
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(all);
			fos.flush();
		} catch (IOException e) {
			System.out.println(ConfigUtil.getConfig("language.properties", "unknown.error"));
			System.out.println("(" + e.getCause() + " : " + e.getMessage() + ")");
			System.exit(-1);
		}
	}
	public static void log(String str){
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("[HH:mm:ss] ");
			Date date = new Date(System.currentTimeMillis());
			String time = formatter.format(date);
			FileInputStream fis = new FileInputStream(file);
			all = fis.readAllBytes();
			FileOutputStream fos = new FileOutputStream(file);
			all = byteMerger(all, (time + str + "\n").getBytes());
			fos.write(all);
			fos.flush();
			System.out.println(time + str);
		} catch (IOException e) {
			System.out.println(ConfigUtil.getConfig("language.properties", "unknown.error"));
			System.out.println("(" + e.getCause() + " : " + e.getMessage() + ")");
			System.exit(-1);
		}
	}
	public static void Exit(){
		try {
			FileInputStream fis = new FileInputStream(file);
			all = fis.readAllBytes();
			FileOutputStream fos = new FileOutputStream(file);
			all = byteMerger(all, ("\n\n----=== LogUtil Closed ===----\n\n").getBytes());
			fos.write(all);
			fos.flush();
		} catch (IOException e) {
			System.out.println(ConfigUtil.getConfig("language.properties", "unknown.error"));
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
