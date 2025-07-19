import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class JsonUserParser {

    public static String mapUserToJson(User user) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        try {
            Field[] fields = user.getClass().getDeclaredFields();
            if (fields.length > 0) {
                for (Field field : fields) {
                    field.setAccessible(true);
                    String name = field.getName();
                    Object val = field.get(user);

                    if (!name.equals("passwordHash")) {
                        sb.append("\"").append(name).append("\":");
                        if (val == null) {
                            sb.append("null");
                        } else if (val instanceof String) {
                            sb.append("\"").append(escapeJson((String) val)).append("\"");
                        } else {
                            sb.append(val);
                        }
                        sb.append(",");
                    }
                }
                sb.deleteCharAt(sb.length() - 1);
            }
        } catch (IllegalAccessException e) {
            System.err.println(e);
        }
        sb.append("}");

        return sb.toString();
    }

    public static User mapJsonToUser(String json) {
        json = json.trim().replaceAll("^\\{|\\}$", "");
        String[] pairs = json.split(",");
        Map<String, String> fieldMap =  new HashMap<>();

        for (String pair : pairs) {
            String[] keyValue = pair.split(":");
            String key = keyValue[0].trim().replaceAll("^\"|\"$","");
            String value =  keyValue[1].trim().replaceAll("^\"|\"$", "");
            fieldMap.put(key, value);
        }

        return new User(fieldMap.get("username"), fieldMap.get("email"), fieldMap.get("password"));
    }

    public static Map<String, String> mapJsonToUserFields(String json) {
        json = json.trim().replaceAll("^\\{|\\}$", "");
        String[] pairs = json.split(",");
        Map<String, String> fieldMap =  new HashMap<>();

        for (String pair : pairs) {
            String[] keyValue = pair.split(":");
            String key = keyValue[0].trim().replaceAll("^\"|\"$","");
            String value =  keyValue[1].trim().replaceAll("^\"|\"$", "");
            fieldMap.put(key, value);
        }

        return fieldMap;
    }

    private static String escapeJson(String value) {
        return value.replace("\"", "\\\"")
                .replace("\\", "\\\\")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
