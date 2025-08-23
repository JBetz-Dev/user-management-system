import java.time.LocalDateTime;

/**
 * Immutable session data record containing user identification and expiry information.
 * <p>
 * Stores minimal session state to avoid data synchronization issues between
 * session cache and database. Services fetch fresh user data as needed.
 *
 * @param userId the ID of the authenticated user
 * @param expiry when this session expires
 */
public record SessionData(int userId, LocalDateTime expiry) {
    boolean isActive() {
        return expiry.isAfter(LocalDateTime.now());
    }

    boolean isExpired() {
        return !isActive();
    }
}