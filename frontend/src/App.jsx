import { useState } from "react";
import AdminLogin from "./AdminLogin.jsx";
import AdminMenu from "./AdminMenu.jsx";

// The session survives a page refresh but ends when the browser tab is closed
const SESSION_KEY = "studymatch.adminEmail";

export default function App() {
    const [adminEmail, setAdminEmail] = useState(() => sessionStorage.getItem(SESSION_KEY));

    // If the administrator is not authenticated, display the login page
    if (!adminEmail) {
        return (
            <AdminLogin
                onLogin={(email) => {
                    sessionStorage.setItem(SESSION_KEY, email);
                    setAdminEmail(email);
                }}
            />
        );
    }

    // Authenticated: display the administration area
    return (
        <AdminMenu
            adminEmail={adminEmail}
            onLogout={() => {
                sessionStorage.removeItem(SESSION_KEY);
                setAdminEmail(null);
            }}
        />
    );
}
