import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class IniUtil {
	private static final Map<String, HashMap<String, String>> sectionsMap = new HashMap<>();
	private static HashMap<String, String> itemsMap = new HashMap<>();
	private static String currentSection = "";
	private static void loadData(File file) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if ("".equals(line))
					continue;
				if (line.startsWith("[") && line.endsWith("]")) {
					if (itemsMap.size() > 0 && !"".equals(currentSection.trim())) {
						sectionsMap.put(currentSection, itemsMap);
					}
					currentSection = "";
					itemsMap = null;
					currentSection = line.substring(1, line.length() - 1);
					itemsMap = new HashMap<>();
				}
				else {
					int index = line.indexOf("=");
					if (index != -1)
					{
						String key = line.substring(0, index);
						String value = line.substring(index + 1);
						itemsMap.put(key, value);
					}
				}
			}
			reader.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (reader != null) {
				try {
					reader.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static String getValue(String section, String item, File file) {
		loadData(file);
		HashMap<String, String> map = sectionsMap.get(section);
		if (map == null) {
			return null;
		}
		return map.get(item);
	}
	
	public static List<String> getSectionNames(File file) {
		loadData(file);
		Set<String> key = sectionsMap.keySet();
		return new ArrayList<>(key);
	}
	
	public static Map<String, String> getItemsBySectionName(String section, File file) {
		loadData(file);
		return sectionsMap.get(section);
	}
}

