import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class IniUtil {
	private static final Map<String, HashMap<String, String>> sectionsMap = new HashMap<>();
	private static HashMap<String, String> itemsMap = new HashMap<>();
	private static String currentSection = "";
	
	public static void loadData(File file) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) {
					continue;
				}
				if (line.startsWith("[") && line.endsWith("]")) {
					if (itemsMap.size() > 0 && !currentSection.trim().isEmpty()) {
						sectionsMap.put(currentSection, itemsMap);
					}
					currentSection = line.substring(1, line.length() - 1);
					itemsMap = new HashMap<>();
				} else {
					int index = line.indexOf("=");
					if (index != -1){
						String key = line.substring(0, index);
						String value = line.substring(index + 1);
						itemsMap.put(key, value);
					}
				}
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static String getValue(String section, String item) {
		HashMap<String, String> map = sectionsMap.get(section);
		if (map == null) {
			return null;
		}
		return map.get(item);
	}
	
	public static ArrayList<String> getSectionNames() {
		return new ArrayList<>(sectionsMap.keySet());
	}
	/*
	 暂时不用
	 public static Map<String, String> getItemsBySectionName(String section) {
	 	return sectionsMap.get(section);
	 }
	*/
}

