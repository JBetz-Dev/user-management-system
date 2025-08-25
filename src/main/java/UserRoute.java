/**
 * Enumeration of all supported user management routes with associated metadata.
 * Encapsulates routing decisions and authentication requirements for user operations.
 * <p>
 * Responsibilities:
 * - Define all available user management endpoints
 * - Specify session authentication requirements per route
 * - Provide metadata for consistent authorization handling
 *
 * @see UserRouter
 * @see UserRequestHandler
 */
public enum UserRoute {
    LOGIN(false),
    LOGOUT(false),
    REGISTER(false),
    GET_ALL_USERS(true),
    CHANGE_PASSWORD(true),
    CHANGE_EMAIL(true),
    NOT_FOUND(false);

    private final boolean requiresSession;

    UserRoute(boolean requiresSession) {
        this.requiresSession = requiresSession;
    }

    public boolean requiresSession() {
        return requiresSession;
    }
}