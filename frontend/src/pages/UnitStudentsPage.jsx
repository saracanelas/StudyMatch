import { useEffect, useState } from "react";
import { enrollStudent, getAllCurricularUnits, getUnitStudents, removeStudent } from "../api";
import TextField from "../components/TextField";

/**
 * Administrator page to associate students with a curricular unit.
 * Temporary: students are identified only by their number until they have their own accounts.
 */
export default function UnitStudentsPage() {
    const [units, setUnits] = useState([]);
    const [selectedUnitId, setSelectedUnitId] = useState("");
    const [students, setStudents] = useState([]);
    const [newStudentNumber, setNewStudentNumber] = useState("");
    const [feedback, setFeedback] = useState(null); // { type: "success" | "error", message }

    // Loads the curricular units once when the page opens
    useEffect(() => {
        let isMounted = true;
        getAllCurricularUnits()
            .then((loadedUnits) => isMounted && setUnits(loadedUnits))
            .catch((error) => isMounted && setFeedback({ type: "error", message: error.message }));
        return () => { isMounted = false; };
    }, []);

    async function handleSelectUnit(unitId) {
        setSelectedUnitId(unitId);
        setStudents([]);
        setFeedback(null);
        if (!unitId) return;
        try {
            setStudents(await getUnitStudents(unitId));
        } catch (error) {
            setFeedback({ type: "error", message: error.message });
        }
    }

    async function handleAdd(event) {
        event.preventDefault();
        const number = newStudentNumber.trim();
        if (!/^[A-Za-z0-9]{1,20}$/.test(number)) {
            setFeedback({ type: "error", message: "O número de aluno só pode ter letras e números (máximo 20)" });
            return;
        }
        try {
            const saved = await enrollStudent(selectedUnitId, number);
            setStudents((current) => [...current, saved].sort());
            setNewStudentNumber("");
            setFeedback({ type: "success", message: `Aluno ${saved} associado à UC.` });
        } catch (error) {
            setFeedback({ type: "error", message: error.message });
        }
    }

    async function handleRemove(studentNumber) {
        const confirmed = window.confirm(
            `Remover o aluno ${studentNumber} desta UC? A resposta que tenha submetido também será eliminada.`
        );
        if (!confirmed) return;
        try {
            await removeStudent(selectedUnitId, studentNumber);
            setStudents((current) => current.filter((number) => number !== studentNumber));
            setFeedback({ type: "success", message: `Aluno ${studentNumber} removido da UC.` });
        } catch (error) {
            setFeedback({ type: "error", message: error.message });
        }
    }

    return (
        <div className="max-w-xl">
            <h2 className="mb-1 text-2xl font-bold">Alunos das UCs</h2>
            <p className="mb-4 text-sm opacity-70">
                Temporário: enquanto os alunos não têm conta, são associados às UCs pelo número de aluno.
            </p>

            {feedback && (
                <p
                    role={feedback.type === "error" ? "alert" : "status"}
                    className={`mb-4 rounded-lg p-3 ${
                        feedback.type === "error" ? "bg-white text-red-700" : "bg-green-100 text-green-800"
                    }`}
                >
                    {feedback.message}
                </p>
            )}

            <label className="mb-4 flex flex-col gap-1 font-medium">
                Unidade curricular
                <select
                    value={selectedUnitId}
                    onChange={(event) => handleSelectUnit(event.target.value)}
                    className="rounded-lg border border-lilac bg-white px-3 py-2 font-normal outline-none focus:border-purple focus:ring-2 focus:ring-blue"
                >
                    <option value="">Escolha uma unidade curricular</option>
                    {units.map((unit) => (
                        <option key={unit.id} value={unit.id}>{unit.code} – {unit.name}</option>
                    ))}
                </select>
            </label>

            {selectedUnitId && (
                <div className="space-y-4 rounded-2xl bg-blue p-6">
                    <form onSubmit={handleAdd} className="flex items-end gap-2">
                        <div className="flex-1">
                            <TextField
                                label="Número de aluno"
                                value={newStudentNumber}
                                maxLength={20}
                                onChange={(event) => setNewStudentNumber(event.target.value)}
                            />
                        </div>
                        <button type="submit" className="rounded-lg bg-pink px-4 py-2 font-semibold hover:bg-lilac">
                            Associar
                        </button>
                    </form>

                    <p className="font-medium">{students.length} aluno(s) associado(s)</p>
                    <ul className="space-y-2">
                        {students.map((number) => (
                            <li key={number} className="flex items-center justify-between rounded-lg bg-white px-3 py-2">
                                <span>{number}</span>
                                <button
                                    type="button"
                                    onClick={() => handleRemove(number)}
                                    className="rounded px-2 text-sm text-red-700 hover:bg-lavender"
                                >
                                    Remover
                                </button>
                            </li>
                        ))}
                    </ul>
                </div>
            )}
        </div>
    );
}
