import { useEffect, useState } from "react";
import { getCourseById, getAllCurricularUnits, updateCourse } from "../api";
import TextField from "../components/TextField";

/** Form to edit a course: its name, code and the curricular units associated with it. */
export default function EditCoursePage({ courseId, onBack, onSaved }) {
    const [course, setCourse] = useState(null);
    const [allUnits, setAllUnits] = useState([]);
    const [selectedUnitIds, setSelectedUnitIds] = useState([]);
    const [errorMessage, setErrorMessage] = useState("");
    const [isSaving, setIsSaving] = useState(false);

    // Loads the course and the list of all curricular units when the page opens
    useEffect(() => {
        let isMounted = true;
        Promise.all([getCourseById(courseId), getAllCurricularUnits()])
            .then(([loadedCourse, loadedUnits]) => {
                if (!isMounted) return;
                setCourse(loadedCourse);
                setAllUnits(loadedUnits);
                setSelectedUnitIds(loadedCourse.unitIds ?? []);
            })
            .catch((error) => isMounted && setErrorMessage(error.message));
        return () => { isMounted = false; };
    }, [courseId]);

    /** Ticks or unticks a curricular unit in the list of selected units. */
    function toggleUnit(unitId) {
        setSelectedUnitIds((currentIds) =>
            currentIds.includes(unitId) ? currentIds.filter((id) => id !== unitId) : [...currentIds, unitId]
        );
    }

    /** Saves the course and its curricular units, then goes back to the list. */
    async function handleSave(event) {
        event.preventDefault();
        setIsSaving(true);
        setErrorMessage("");
        try {
            const savedCourse = await updateCourse(courseId, { ...course, unitIds: selectedUnitIds });
            onSaved(savedCourse);
            onBack();
        } catch (error) {
            setErrorMessage(error.message);
        } finally {
            setIsSaving(false);
        }
    }

    if (!course) return <p>{errorMessage || "A carregar..."}</p>;

    return (
        <form onSubmit={handleSave} className="max-w-xl space-y-4 rounded-2xl bg-blue p-6">
            <h2 className="text-xl font-bold">Editar curso</h2>

            <TextField label="Nome" value={course.name} onChange={(event) => setCourse({ ...course, name: event.target.value })} required maxLength={50} />
            <TextField label="Código" value={course.code} onChange={(event) => setCourse({ ...course, code: event.target.value })} required maxLength={12} />

            <fieldset>
                <legend className="mb-2 font-medium">Unidades curriculares deste curso</legend>
                <div className="max-h-64 space-y-1 overflow-y-auto rounded-lg bg-white p-3">
                    {allUnits.map((unit) => (
                        <label key={unit.id} className="flex cursor-pointer items-center gap-2 rounded p-1 hover:bg-lavender">
                            <input
                                type="checkbox"
                                checked={selectedUnitIds.includes(unit.id)}
                                onChange={() => toggleUnit(unit.id)}
                            />
                            {unit.name} <span className="text-sm opacity-70">({unit.code})</span>
                        </label>
                    ))}
                    {allUnits.length === 0 && (
                        <p className="text-sm opacity-70">Ainda não há unidades curriculares. Cria-as no menu "Unidades Curriculares".</p>
                    )}
                </div>
            </fieldset>

            {errorMessage && <p className="text-red-700">{errorMessage}</p>}

            <div className="flex gap-2">
                <button type="submit" disabled={isSaving} className="rounded-lg bg-pink px-4 py-2 font-semibold hover:bg-lilac disabled:opacity-50">
                    {isSaving ? "A guardar..." : "Guardar alterações"}
                </button>
                <button type="button" onClick={onBack} className="rounded-lg bg-white px-4 py-2 hover:bg-lavender">
                    Cancelar
                </button>
            </div>
        </form>
    );
}
