import { useCallback, useEffect, useState } from "react";
import { createEnrollment, getAllCourses, getAllCurricularUnits, getAllEnrollments, getAllStudents, setEnrollmentGrade, unenrollStudent } from "./api";
import AdminLayout from "./components/AdminLayout";
import Toast from "./components/Toast";
import CatalogPage from "./pages/CatalogPage";
import CourseFormPage from "./pages/CourseFormPage";
import GroupConfigurationPage from "./pages/GroupConfigurationPage";
import RolePreferencePage from "./pages/RolePreferencePage";
import UnitStudentsPage from "./pages/UnitStudentsPage";
import StudentFormPage from "./pages/StudentFormPage";
import StudentsPage from "./pages/StudentsPage";
import UnitFormPage from "./pages/UnitFormPage";

/**
 * Administrator area. Loads the catalogue (courses, units), the students and their enrollments,
 * shows the overview pages inside the sidebar layout and opens the create / edit forms as full pages.
 */
export default function AdminMenu({ adminEmail, onLogout }) {
    // Open page in the sidebar: "catalog" (Cursos e UCs), "students", "unitStudents", "groups" or "rolePreferences"
    const [section, setSection] = useState("catalog");
    // Open form: null, or { type: "course" | "unit" | "student", id } (id is null when creating)
    const [form, setForm] = useState(null);

    const [data, setData] = useState({ courses: [], units: [], students: [], enrollments: [] });
    const [isLoading, setIsLoading] = useState(true);
    const [loadError, setLoadError] = useState("");
    const [lastUpdated, setLastUpdated] = useState(null);
    // Changes after every enrollment change, so that the open UC panel reloads its students
    const [refreshKey, setRefreshKey] = useState(0);

    const [catalogTab, setCatalogTab] = useState("courses");
    const [selectedCourseId, setSelectedCourseId] = useState(null);
    const [selectedUnitId, setSelectedUnitId] = useState(null);
    const [studentsTab, setStudentsTab] = useState("students");
    const [selectedStudentId, setSelectedStudentId] = useState(null);
    const [selectedGradesUnitId, setSelectedGradesUnitId] = useState(null);

    const [toast, setToast] = useState(null);
    const showToast = useCallback((type, text) => setToast({ type, text, id: Date.now() }), []);
    const closeToast = useCallback(() => setToast(null), []);

    /** Reads everything from the backend again. */
    const reload = useCallback(
        () =>
            Promise.all([getAllCourses(), getAllCurricularUnits(), getAllStudents(), getAllEnrollments()])
                .then(([courses, units, students, enrollments]) => {
                    setData({ courses, units, students, enrollments });
                    setLoadError("");
                    setLastUpdated(new Date());
                })
                .catch((error) => setLoadError(`Não foi possível carregar os dados: ${error.message}`))
                .finally(() => setIsLoading(false)),
        []
    );

    useEffect(() => {
        reload();
    }, [reload]);

    /** Closes the form, shows a success message and reloads the data. */
    async function afterSave(message) {
        setForm(null);
        showToast("success", message);
        await reload();
    }

    // ---------- Enrollments and grades ----------

    /** Runs an enrollment change, then reloads; returns true on success. */
    async function changeEnrollments(action, successMessage) {
        try {
            await action();
            showToast("success", successMessage);
            await reload();
            setRefreshKey((key) => key + 1);
            return true;
        } catch (error) {
            showToast("error", error.message);
            return false;
        }
    }

    const handleEnroll = (studentId, unitId) =>
        changeEnrollments(() => createEnrollment(studentId, unitId), "Inscrição registada.");

    const handleSetGrade = (enrollment, grade) =>
        changeEnrollments(
            () => setEnrollmentGrade(enrollment.id, grade),
            grade === null ? "Nota removida." : "Nota guardada. A média do estudante foi atualizada."
        );

    const handleUnenroll = (enrollment) =>
        changeEnrollments(() => unenrollStudent(enrollment.id), "Inscrição anulada.");

    const handleLoadError = useCallback((error) => showToast("error", error.message), [showToast]);

    // ---------- Forms (full pages, without the sidebar) ----------

    const { courses, units, students, enrollments } = data;
    let formPage = null;

    if (form?.type === "course") {
        formPage = (
            <CourseFormPage
                key={form.id ?? "new"}
                course={courses.find((course) => course.id === form.id)}
                units={units}
                onCancel={() => setForm(null)}
                onSaved={async ({ courseId }) => {
                    setCatalogTab("courses");
                    setSelectedCourseId(courseId);
                    await afterSave(form.id ? "Curso atualizado." : "Curso criado.");
                }}
            />
        );
    } else if (form?.type === "unit") {
        formPage = (
            <UnitFormPage
                key={form.id ?? "new"}
                unit={units.find((unit) => unit.id === form.id)}
                courses={courses}
                onCancel={() => setForm(null)}
                onSaved={async ({ unitId }) => {
                    if (catalogTab === "courses") setCatalogTab("units");
                    setSelectedUnitId(unitId);
                    await afterSave(form.id ? "UC atualizada." : "UC criada.");
                }}
            />
        );
    } else if (form?.type === "student") {
        formPage = (
            <StudentFormPage
                key={form.id ?? "new"}
                student={students.find((student) => student.id === form.id)}
                courses={courses}
                onCancel={() => setForm(null)}
                onSaved={async (saved, isEditing) => {
                    setStudentsTab("students");
                    setSelectedStudentId(saved.id);
                    await afterSave(isEditing ? "Estudante atualizado." : "Estudante criado.");
                }}
            />
        );
    }

    return (
        <>
            {formPage ?? (
                <AdminLayout
                    adminEmail={adminEmail}
                    onLogout={onLogout}
                    section={section}
                    onNavigate={setSection}
                    counts={{ students: students.length }}
                >
                    {section === "catalog" ? (
                        <CatalogPage
                            courses={courses}
                            units={units}
                            isLoading={isLoading}
                            loadError={loadError}
                            lastUpdated={lastUpdated}
                            tab={catalogTab}
                            onTabChange={setCatalogTab}
                            selectedCourseId={selectedCourseId}
                            onSelectCourse={setSelectedCourseId}
                            selectedUnitId={selectedUnitId}
                            onSelectUnit={setSelectedUnitId}
                            onNewCourse={() => setForm({ type: "course", id: null })}
                            onEditCourse={(id) => setForm({ type: "course", id })}
                            onNewUnit={() => setForm({ type: "unit", id: null })}
                            onEditUnit={(id) => setForm({ type: "unit", id })}
                        />
                    ) : section === "unitStudents" ? (
                        <UnitStudentsPage units={units} students={students} onNotify={showToast} />
                    ) : section === "groups" ? (
                        <GroupConfigurationPage units={units} onNotify={showToast} />
                    ) : section === "rolePreferences" ? (
                        <RolePreferencePage onNotify={showToast} />
                    ) : (
                        <StudentsPage
                            students={students}
                            courses={courses}
                            units={units}
                            enrollments={enrollments}
                            isLoading={isLoading}
                            loadError={loadError}
                            lastUpdated={lastUpdated}
                            refreshKey={refreshKey}
                            tab={studentsTab}
                            onTabChange={setStudentsTab}
                            selectedStudentId={selectedStudentId}
                            onSelectStudent={setSelectedStudentId}
                            selectedUnitId={selectedGradesUnitId}
                            onSelectUnit={setSelectedGradesUnitId}
                            onNewStudent={() => setForm({ type: "student", id: null })}
                            onEditStudent={(id) => setForm({ type: "student", id })}
                            onEnroll={handleEnroll}
                            onSetGrade={handleSetGrade}
                            onUnenroll={handleUnenroll}
                            onLoadError={handleLoadError}
                        />
                    )}
                </AdminLayout>
            )}
            <Toast key={toast?.id} toast={toast} onClose={closeToast} />
        </>
    );
}
