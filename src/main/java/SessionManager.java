import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private static final Map<String, SessionData> activeSessions = new ConcurrentHashMap<>();
    private static long sessionCleanupCounter = 0;

    public static String extractSessionIdFromCookie(String cookie) {
        String sessionId = "";
        String[] segments = cookie.split(";");

        for (String segment : segments) {
            String[] pairs = segment.split("=");

            if (pairs.length == 2) {
                String key = pairs[0].trim().replaceAll("^\"|\"$","");
                String value = pairs[1].trim().replaceAll("^\"|\"$","");

                if (key.equals("sessionId")) {
                    sessionId = value;
                }
            }
        }

        return sessionId;
    }

    protected static boolean isActiveSession(String sessionId) {
        SessionData sessionData = activeSessions.get(sessionId);

        if (sessionData == null) {
            return false;
        } else {
            LocalDateTime sessionExpiry =  sessionData.expiry();
            LocalDateTime currentDateTime = LocalDateTime.now();

            return sessionExpiry.isAfter(currentDateTime);
        }
    }

    protected static void setActiveSession(String sessionId, User user) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalDateTime expiryDateTime = currentDateTime.plusMinutes(60);

        activeSessions.put(sessionId, new SessionData(user, expiryDateTime));

        if (sessionCleanupCounter++ == 1000) {
            removeInactiveSessions();
        }
    }

    protected static User getUserIfActiveSession(String sessionId) {
        SessionData sessionData = activeSessions.get(sessionId);
        if (sessionData.isActive()) {
            return sessionData.user();
        } else {
            return null;
        }
    }

    private static void removeInactiveSessions() {
        List<String> expiredSessionIds = new ArrayList<>();

        for (Map.Entry<String, SessionData> entry : activeSessions.entrySet()) {
            if (entry.getValue().isExpired()) {
                expiredSessionIds.add(entry.getKey());
            }
        }

        for (String sessionId : expiredSessionIds) {
            activeSessions.remove(sessionId);
        }

        sessionCleanupCounter = 0;
    }

    record SessionData(User user, LocalDateTime expiry) {
        boolean isExpired() {
            return expiry.isBefore(LocalDateTime.now());
        }

        boolean isActive() {
            return expiry.isAfter(LocalDateTime.now());
        }
    }
}
