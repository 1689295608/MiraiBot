package com.windowx.miraibot.utils;

import org.fusesource.jansi.AnsiConsole;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.fusesource.jansi.Ansi.ansi;

public class LogUtil {
	public static StringBuilder messages = new StringBuilder();
	static byte[] all = new byte[0];
	static File file;
	public static boolean ansiColor;
	static String os = System.getProperty("os.name").toLowerCase();
	public static String lastOpt;
	
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
			int i = 1;
			while (new File("logs" + File.separator + time + "-" + i + ".log").exists()) {
				i++;
			}
			file = new File("logs" + File.separator + time + "-" + i + ".log");
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
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public static String write(String str, String... args) {
		if (str == null) return null;
		lastOpt = String.format(str, (Object[]) args);
		String time = "";
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("[HH:mm:ss] ");
			Date date = new Date(System.currentTimeMillis());
			time = formatter.format(date);
			FileInputStream fis = new FileInputStream(file);
			all = fis.readAllBytes();
			FileOutputStream fos = new FileOutputStream(file);
			String[] allLine = str.split("\n");
			byte[] add = new byte[0];
			for (String s : allLine) {
				add = byteMerger(add, (time + s + "\n").getBytes());
			}
			fos.write(byteMerger(all, add));
			fos.flush();
			fos.close();
		} catch (IOException e) {
			System.out.println(ConfigUtil.getLanguage("unknown.error"));
			System.out.println("(" + e.getCause() + " : " + e.getMessage() + ")");
			System.exit(-1);
		}
		return time;
	}
	
	/**
	 * Output a message and record it in the log file
	 *
	 * @param str What to output
	 */
	public static void log(String str, String... args) {
		if (str == null) return;
		AnsiConsole.out().printf("\r" + write(str, args) + str + "\n> ", (Object[]) args);
	}
	
	public static void log(String str) {
		if (str == null) return;
		AnsiConsole.out().print("\r" + write(str) + str + "\n> ");
	}
	
	/**
	 * Output an error message and record it in the log file
	 *
	 * @param str What to output
	 */
	public static void error(String str, String... args) {
		if (str == null) return;
		if (os.contains("linux") || ansiColor) {
			AnsiConsole.out().printf("\r" + ansi().eraseScreen().render("@|red "+ write(str, args) + str + "|@") + "\n> ", (Object[]) args);
		} else if (os.contains("windows")) {
			AnsiConsole.err().printf("\r" + write(str, args) + str + "\n> ", (Object[]) args);
		}
	}
	public static void error(String str) {
		if (str == null) return;
		if (os.contains("linux") || ansiColor) {
			AnsiConsole.out().print("\r" + ansi().eraseScreen().render("@|red "+ write(str) + str + "|@") + "\n> ");
		} else if (os.contains("windows")) {
			AnsiConsole.err().print("\r" + write(str) + str + "\n> ");
		}
	}
	
	/**
	 * Output an error message and record it in the log file
	 *
	 * @param str What to output
	 */
	public static void warn(String str, String... args) {
		if (str == null) return;
		if (os.contains("linux") || ansiColor) {
			AnsiConsole.out().printf("\r" + ansi().eraseScreen().render("@|yellow "+ write(str, args) + str + "|@") + "\n> ", (Object[]) args);
		} else if (os.contains("windows")) {
			AnsiConsole.out().printf("\r" + write(str, args) + str + "\n> ", (Object[]) args);
		}
	}
	public static void warn(String str) {
		if (str == null) return;
		if (os.contains("linux") || ansiColor) {
			AnsiConsole.out().print("\r" + ansi().eraseScreen().render("@|yellow "+ write(str) + str + "|@") + "\n> ");
		} else if (os.contains("windows")) {
			AnsiConsole.out().print("\r" + write(str) + str + "\n> ");
		}
	}
	
	/**
	 * Combine two byte[] into one byte[]
	 *
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
	 */
	public static void clear() {
		try {
			System.console().flush();
			String os = System.getProperty("os.name").toLowerCase();
			if (os.contains("windows")) {
				new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
			} else if (os.contains("linux")) {
				new ProcessBuilder("clear").inheritIO().start().waitFor();
			}
		} catch (Exception ignored) {
		}
	}
}
