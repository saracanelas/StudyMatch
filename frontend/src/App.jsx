import { useState } from "react";
import AdminLogin from "./AdminLogin.jsx";
import AdminMenu from "./AdminMenu.jsx";


export default function App() {
    const [autenticado, setAutenticado] = useState(false);

  // Se ainda não estiver autenticado, mostra o login
  if (!autenticado) {
    return (
      <AdminLogin
        onLogin={() => setAutenticado(true)}
      />
    );
  }

  // Se o login for válido, mostra o perfil de administração
  return (
    <AdminMenu
      onLogout={() => {
        setAutenticado(false);
      }}
    />
  );
}