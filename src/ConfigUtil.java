import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class ConfigUtil {
	/**
	 * Get the value of key in the configuration file
	 * @param key Key
	 * @return Config value
	 */
	public static String getConfig(String key) {
		Properties p = new Properties();
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader("config.properties"));
			try {
				p.load(bufferedReader);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return p.getProperty(key);
	}
	
	/**
	 * Get the value of key in the language file
	 * @param key Key
	 * @return Language value
	 */
	public static String getLanguage(String key) {
		Properties p = new Properties();
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader("language.properties"));
			try {
				p.load(bufferedReader);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return p.getProperty(key);
	}
}
