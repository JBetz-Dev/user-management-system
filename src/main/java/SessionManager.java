import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages user session data and lifecycle for HTTP-based authentication.
 * Provides in-memory session storage with automatic cleanup and expiry management.
 * <p>
 * Sessions store minimal data (userId + expiry) to avoid cache coherence issues.
 * All session operations are thread-safe using ConcurrentHashMap for concurrent access.
 * Uses counter-based cleanup (every 1000 operations) and 60-minute session timeout.
 *
 * @see SessionData
 * @see UserRequestHandler
 * @see FileRequestHandler
 */
public class SessionManager {
    private static final Map<String, SessionData> activeSessions = new ConcurrentHashMap<>();
    private static long sessionCleanupCounter = 0;

    private static String extractSessionIdFromCookie(String cookie) {
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

    protected static SessionData getActiveSession(String cookie) {
        String sessionId = extractSessionIdFromCookie(cookie);
        SessionData sessionData = activeSessions.get(sessionId);

        if (sessionData == null || sessionData.isExpired()) {
            return null;
        }

        return sessionData;
    }

    protected static String setActiveSession(int userId) {
        String sessionId = UUID.randomUUID().toString();
        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalDateTime expiryDateTime = currentDateTime.plusMinutes(60);

        activeSessions.put(sessionId, new SessionData(userId, expiryDateTime));

        if (sessionCleanupCounter++ == 1000) {
            removeInactiveSessions();
        }

        return sessionId;
    }

    protected static void invalidateUserSessions(int userId) {
        activeSessions.entrySet().removeIf(
                entry -> entry.getValue().userId() == userId
        );
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
}