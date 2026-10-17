import { useState } from "react";
import AdminLogin from "./AdminLogin.jsx";
import AdminMenu from "./AdminMenu.jsx";


export default function App() {
    const [autenticado, setAutenticado] = useState(false);

  // If the administrator is not authenticatedndisplay the login page
  if (!autenticado) {
    return (
      <AdminLogin
      // Update the authentication state after a successful login
        onLogin={() => setAutenticado(true)}
      />
    );
  }

  // If the administrator is authenticated, display the admin profile
  return (
    <AdminMenu
      onLogout={() => {
        // Log out the administrator and return to the login page
        setAutenticado(false);
      }}
    />
  );
}