import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class ConfigUtil {
	public static Properties config = new Properties();
	public static Properties language = new Properties();
	
	/**
	 * Initialize the config system
	 */
	public static void init() {
		try {
			config.load(new BufferedReader(new FileReader("config.properties")));
			language.load(new BufferedReader(new FileReader("language.properties")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Get the value of key in the configuration file
	 * @param key Key
	 * @return Config value
	 */
	public static String getConfig(String key) {
		return config.getProperty(key);
	}
	
	/**
	 * Get the value of key in the language file
	 * @param key Key
	 * @return Language value
	 */
	public static String getLanguage(String key) {
		return language.getProperty(key);
	}
}
