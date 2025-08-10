import java.util.HashMap;
import java.util.Map;

public class JsonUtil {

    public static Map<String, String> parseJsonToFieldMap(String json) {
        Map<String, String> fieldMap = new HashMap<>();

        json = json.trim().replaceAll("^\\{|}$", "");
        String[] pairs = json.split(",");

        for (String pair : pairs) {
            String[] keyValue = pair.split(":");

            if (keyValue.length >= 2) {
                String key = keyValue[0].trim().replaceAll("^\"|\"$", "");
                String value = keyValue[1].trim().replaceAll("^\"|\"$", "");
                fieldMap.put(key, value);
            }
        }

        return fieldMap;
    }

    public static String escapeJson(String value) {
        return value.replace("\"", "\\\"")
                .replace("\\", "\\\\")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
