import { useState } from "react";
import Cursos from "./pages/Cursos";
import Ucs from "./pages/Ucs";

const itens = [
    { id: "cursos", label: "Cursos" },
    { id: "ucs", label: "UCs" },
];

/**
 * Admin area with the side menu. The logout button calls `onLogout`,
 * which will be passed in when this is joined with the login part.
 */
export default function App({ onLogout }) {
    const [pagina, setPagina] = useState("cursos");

    return (
        <div className="flex min-h-screen bg-lavanda text-texto">
            <aside className="sticky top-0 flex h-screen w-56 flex-col bg-roxo p-4">
                <h1 className="mb-6 text-xl font-bold">StudyMatch Admin</h1>
                <nav className="flex flex-col gap-2">
                    {itens.map((item) => (
                        <button
                            key={item.id}
                            onClick={() => setPagina(item.id)}
                            className={`rounded-lg px-3 py-2 text-left font-medium ${
                                pagina === item.id ? "bg-rosa" : "hover:bg-azul"
                            }`}
                        >
                            {item.label}
                        </button>
                    ))}
                </nav>

                <button
                    onClick={onLogout}
                    className="mt-auto w-full rounded-lg bg-white px-3 py-2 text-left font-medium hover:bg-rosa"
                >
                    Logout
                </button>
            </aside>

            <main className="flex-1 p-8">
                {pagina === "cursos" ? <Cursos /> : <Ucs />}
            </main>
        </div>
    );
}