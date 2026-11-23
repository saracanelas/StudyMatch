import { useState } from "react";
import CoursesPage from "./pages/CoursesPage";
import CurricularUnitsPage from "./pages/CurricularUnitsPage";
import GroupConfigurationPage from "./pages/GroupConfigurationPage";
import RolePreferencePage from "./pages/RolePreferencePage";
import UnitStudentsPage from "./pages/UnitStudentsPage";

const ADMIN_MENU_ITEMS = [
    { page: "courses", label: "Cursos" },
    { page: "curricularUnits", label: "Unidades Curriculares" },
    { page: "unitStudents", label: "Alunos das UCs" }, // temporary: until students have accounts
    { page: "groupConfiguration", label: "Configuração de Grupos" },
    { page: "rolePreferences", label: "Preferências de Roles" }, // temporary: will move to the student area
];

/** Admin area with the side menu; the logout button calls `onLogout`. */
export default function AdminMenu({ onLogout }) {
    const [currentPage, setCurrentPage] = useState("courses");

    return (
        <div className="flex min-h-screen bg-lavender text-ink">
            <aside className="sticky top-0 flex h-screen w-60 flex-col bg-purple p-4">
                <h1 className="text-xl font-bold" style={{color:"black"}}>StudyMatch</h1>
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
                    className="mt-auto w-full rounded-lg bg-pink px-3 py-2 text-left font-medium hover:bg-lilac"
                >
                    Terminar sessão
                </button>
            </aside>

            <main className="flex-1 p-8">
                {currentPage === "courses" && <CoursesPage />}
                {currentPage === "curricularUnits" && <CurricularUnitsPage />}
                {currentPage === "unitStudents" && <UnitStudentsPage />}
                {currentPage === "groupConfiguration" && <GroupConfigurationPage />}
                {currentPage === "rolePreferences" && <RolePreferencePage />}
            </main>
        </div>
    );
}