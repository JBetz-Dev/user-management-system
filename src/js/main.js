import {navigation} from "./components/navigation.js";

navigation.initialize();
importPageSpecificFiles();

function importPageSpecificFiles() {
    const path = window.location.pathname;

    switch (path) {
        case path.includes('/login.html'):
            import("./pages/login.js");
            break;
        case path.includes('/register.html'):
            import("./pages/register.js");
            break;
        case path.includes('/user-area.html'):
            import("./pages/userArea.js");
            break;
        case path.includes('/profile.html'):
            import("./pages/profile.js");
            break;
        default:
            break;
    }
}