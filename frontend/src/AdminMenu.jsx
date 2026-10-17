import { useState } from "react";
import CoursesPage from "./pages/CoursesPage";
import CurricularUnitsPage from "./pages/CurricularUnitsPage";

const ADMIN_MENU_ITEMS = [
    { page: "courses", label: "Cursos" },
    { page: "curricularUnits", label: "Unidades Curriculares" },
];

/** Admin area with the side menu; the logout button calls `onLogout`. */
export default function AdminMenu({ onLogout }) {
    const [currentPage, setCurrentPage] = useState("courses");

    return (
        <div className="flex min-h-screen bg-lavender text-ink">
            <aside className="sticky top-0 flex h-screen w-60 flex-col bg-purple p-4">
                <h1 className="text-xl font-bold">StudyMatch</h1>
                <p className="mb-6 text-sm opacity-70">Painel de administração</p>
                <nav className="flex flex-col gap-2">
                    {ADMIN_MENU_ITEMS.map((item) => (
                        <button
                            key={item.page}
                            onClick={() => setCurrentPage(item.page)}
                            className={`rounded-lg px-3 py-2 text-left font-medium ${
                                currentPage === item.page ? "bg-pink" : "hover:bg-blue"
                            }`}
                        >
                            {item.label}
                        </button>
                    ))}
                </nav>

                <button
                    onClick={onLogout}
                    className="mt-auto w-full rounded-lg bg-white px-3 py-2 text-left font-medium hover:bg-pink"
                >
                    Terminar sessão
                </button>
            </aside>

            <main className="flex-1 p-8">
                {currentPage === "courses" ? <CoursesPage /> : <CurricularUnitsPage />}
            </main>
        </div>
    );
}