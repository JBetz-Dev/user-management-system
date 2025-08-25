import {navigation} from "./components/navigation.js";

/**
 * Main application entry point handling page-specific script loading and navigation.
 * Coordinates application initialization and page-specific functionality.
 *
 * Responsibilities:
 * - Initialize navigation component for all pages
 * - Dynamically load page-specific scripts based on current URL
 * - Determine current page context from URL path
 * - Coordinate application-wide component initialization
 *
 * @see {navigation}
 */
navigation.initialize();

const currentPage = getCurrentPageName();
switch (currentPage) {
    case "login":
        import("./pages/login.js");
        break;
    case "register":
        import("./pages/register.js");
        break;
    case "user-area":
        import("./pages/userArea.js");
        break;
    case "profile":
        import("./pages/profile.js");
        break;
    default: break;
}

function getCurrentPageName() {
    const path = window.location.pathname;

    if (path.includes('/login.html')) {
        return "login";
    } else if (path.includes('/register.html')) {
        return "register";
    } else if (path.includes('/user-area.html')) {
        return "user-area";
    } else if (path.includes('/profile.html')) {
        return "profile";
    } else {
        const filename = path.split('/').pop() || "index.html";
        return filename.replace('.html', '');
    }
}