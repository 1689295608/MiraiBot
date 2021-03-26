import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class ConfigUtil {
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
}