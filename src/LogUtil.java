import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class LogUtil {
	static byte[] all = new byte[0];
	static File file;
	private static final ArrayList<String> messages = new ArrayList<>();
	/**
	 * Initialize the log system
	 */
	public static void init() {
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			Date date = new Date(System.currentTimeMillis());
			String time = formatter.format(date);
			File dir = new File("logs");
			if (!dir.exists()) {
				if (!dir.mkdirs()) {
					System.out.println(time + ConfigUtil.getLanguage("failed.create.config"));
					System.exit(-1);
					return;
				}
			}
			file = new File("logs" + File.separator + time + ".log");
			if (!file.exists()) {
				if (!file.createNewFile()) {
					System.out.println(time + ConfigUtil.getLanguage("failed.create.config"));
					System.exit(-1);
					return;
				}
			}
			FileInputStream fis = new FileInputStream(file);
			all = fis.readAllBytes();
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(all);
			fos.flush();
			fos.close();
		} catch (IOException e) {
			System.out.println(ConfigUtil.getLanguage("unknown.error"));
			System.out.println("(" + e.getCause() + " : " + e.getMessage() + ")");
			System.exit(-1);
		}
	}
	
	/**
	 * Output a message and record it in the log file
	 * @param str What to output
	 */
	public static void log(String str) {
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("[HH:mm:ss] ");
			Date date = new Date(System.currentTimeMillis());
			String time = formatter.format(date);
			FileInputStream fis = new FileInputStream(file);
			all = fis.readAllBytes();
			FileOutputStream fos = new FileOutputStream(file);
			String[] allLine = str.split("\n");
			byte[] add = new byte[0];
			for (String s : allLine) {
				try { clear(); } catch (InterruptedException ignored) { }
				messages.add(
						(str.startsWith(">") ? "" : time)
								+ s
				);
				for (String message : messages) {
					System.out.println(message);
				}
				System.out.print("> ");
				add = byteMerger(add, ((str.startsWith(">") ? "" : time) + s + "\n").getBytes());
				try {
					Thread.sleep(3);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			fos.write(byteMerger(all, add));
			fos.flush();
			fos.close();
		} catch (IOException e) {
			System.out.println(ConfigUtil.getLanguage("unknown.error"));
			System.out.println("(" + e.getCause() + " : " + e.getMessage() + ")");
			System.exit(-1);
		}
	}
	
	/**
	 * Shut down the log system and record a shutdown log
	 */
	public static void Exit() {
		try {
			FileInputStream fis = new FileInputStream(file);
			all = fis.readAllBytes();
			FileOutputStream fos = new FileOutputStream(file);
			all = byteMerger(all, ("\n----=== MiraiBot Closed ===----\n\n").getBytes());
			fos.write(all);
			fos.flush();
			fos.close();
		} catch (IOException e) {
			System.out.println(ConfigUtil.getLanguage("unknown.error"));
			System.out.println("(" + e.getCause() + " : " + e.getMessage() + ")");
			System.exit(-1);
		}
	}
	
	/**
	 * Combine two byte[] into one byte[]
	 * @param byte1 Byte[] to be merged at the beginning
	 * @param byte2 Byte[] to be merged to the end
	 * @return Merged result
	 */
	public static byte[] byteMerger(byte[] byte1, byte[] byte2) {
		byte[] byte3 = new byte[byte1.length + byte2.length];
		System.arraycopy(byte1, 0, byte3, 0, byte1.length);
		System.arraycopy(byte2, 0, byte3, byte1.length, byte2.length);
		return byte3;
	}
	
	/**
	 * Clear the console text
	 * @throws IOException IOException
	 * @throws InterruptedException InterruptedException
	 */
	public static void clear() throws IOException, InterruptedException {
		String os = System.getProperty("os.name").toLowerCase();
		if (os.contains("windows")) {
			new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
		} else if (os.contains("linux")) {
			new ProcessBuilder("clear").inheritIO().start().waitFor();
		}
	}
}
