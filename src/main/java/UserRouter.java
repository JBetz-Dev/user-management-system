/**
 * Routes HTTP requests to appropriate user management operations based on URL path patterns.
 * Implements RESTful routing conventions for user-related endpoints.
 * <p>
 * Responsibilities:
 * - Parse URL paths and HTTP methods to identify user operations
 * - Map request patterns to UserRoute enum values
 * - Handle path normalization (trailing slash removal)
 * - Provide routing decisions without business logic concerns
 * <p>
 * Supported route patterns:
 * - GET /users -> Get all users (requires session)
 * - POST /users -> Register new user
 * - POST /users/login -> Authenticate user
 * - POST /users/logout -> Logout current user
 * - PATCH /users/{id}/password -> Change user password (requires session)
 * - PATCH /users/{id}/email -> Change user email (requires session)
 *
 * @see UserRoute
 * @see UserRequestHandler
 */
public class UserRouter {

    public UserRoute getRoute(String method, String pathString) {
        if (pathString.endsWith("/")) {
            pathString = pathString.substring(0, pathString.length() - 1);
        }

        String[] segments = pathString.split("/");
        int segmentsLength = segments.length;

        if (segmentsLength == 2 && method.equals("GET")) {
            return UserRoute.GET_ALL_USERS;
        } else if (segmentsLength == 2 && method.equals("POST")) {
            return UserRoute.REGISTER;
        } else if (segmentsLength == 3 && segments[2].equals("login") && method.equals("POST")) {
            return UserRoute.LOGIN;
        } else if (segmentsLength == 3 && segments[2].equals("logout") && method.equals("POST")) {
            return UserRoute.LOGOUT;
        } else if (segmentsLength == 4 && segments[3].equals("password") && method.equals("PATCH")) {
            return UserRoute.CHANGE_PASSWORD;
        } else if (segmentsLength == 4 && segments[3].equals("email") && method.equals("PATCH")) {
            return UserRoute.CHANGE_EMAIL;
        } else {
            return UserRoute.NOT_FOUND;
        }
    }
}
