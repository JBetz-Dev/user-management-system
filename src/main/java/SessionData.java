import java.time.LocalDateTime;

public record SessionData(User user, LocalDateTime expiry) {
    boolean isActive() {
        return expiry.isAfter(LocalDateTime.now());
    }

    boolean isExpired() {
        return !isActive();
    }
}