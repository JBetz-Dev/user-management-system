import java.time.LocalDateTime;

/**
 * Immutable session data record containing user identification and expiry information.
 *
 * @param userId the ID of the authenticated user
 * @param expiry when this session expires
 * @see SessionManager
 */
public record SessionData(int userId, LocalDateTime expiry) {
    boolean isActive() {
        return expiry.isAfter(LocalDateTime.now());
    }

    boolean isExpired() {
        return !isActive();
    }
}