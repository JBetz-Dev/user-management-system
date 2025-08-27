import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for basic JSON parsing and validation operations.
 * Provides simple JSON string parsing to field maps and validates required field presence.
 * <p>
 * This is a minimal JSON implementation focused on parsing simple key-value objects
 * from HTTP request bodies. Built for learning JSON structure and format.
 * Not intended for complex nested JSON structures.
 * <p>
 * Required fields validation throws MissingRequiredFieldException to enforce expected
 * input and avoid null checking in consuming services.
 *
 * @see UserRequestHandler
 */
public class JsonUtil {

    public static Map<String, String> parseJsonWithRequiredFields(String requestBody, List<String> requiredFields)
            throws MissingRequiredFieldException {
        Map<String, String> providedFields = parseJsonToFieldMap(requestBody);

        for (String requiredField : requiredFields) {
            String providedField = providedFields.get(requiredField);
            if (providedField == null || providedField.trim().isEmpty()) {
                throw new MissingRequiredFieldException("Required field not found: " + requiredField);
            }
        }

        return providedFields;
    }

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

    public static class MissingRequiredFieldException extends Exception {
        public MissingRequiredFieldException(String message) {
            super(message);
        }
    }
}
