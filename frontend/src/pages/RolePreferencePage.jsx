import { useEffect, useState } from "react";
import { getAllCurricularUnits, getAvailableRoles, getRolePreference, submitRolePreference } from "../api";
import TextField from "../components/TextField";

/** Returns a copy of `list` with the item at `from` moved to position `to`. */
function moveItem(list, from, to) {
    const copy = [...list];
    const [item] = copy.splice(from, 1);
    copy.splice(to, 0, item);
    return copy;
}

/**
 * Questionnaire where a student orders the roles of an assignment from most to least wanted.
 * Temporary: lives in the admin menu until students have their own login, so the student number is typed in.
 */
export default function RolePreferencePage() {
    const [units, setUnits] = useState([]);
    const [selectedUnitId, setSelectedUnitId] = useState("");
    const [studentNumber, setStudentNumber] = useState("");
    const [availableRoles, setAvailableRoles] = useState([]);
    const [ranking, setRanking] = useState(null); // null until the questionnaire is opened
    const [hasSubmitted, setHasSubmitted] = useState(false);
    const [draggedIndex, setDraggedIndex] = useState(null);
    const [feedback, setFeedback] = useState(null); // { type: "success" | "error", message }

    // Loads the curricular units once when the page opens
    useEffect(() => {
        let isMounted = true;
        getAllCurricularUnits()
            .then((loadedUnits) => isMounted && setUnits(loadedUnits))
            .catch((error) => isMounted && setFeedback({ type: "error", message: error.message }));
        return () => { isMounted = false; };
    }, []);

    /** Loads the roles of the assignment and the ranking the student already submitted, if any. */
    async function handleOpen(event) {
        event.preventDefault();
        setFeedback(null);
        setRanking(null);
        const number = studentNumber.trim();
        if (!selectedUnitId || !number) {
            setFeedback({ type: "error", message: "Escolha a unidade curricular e indique o número de aluno" });
            return;
        }
        try {
            const roles = await getAvailableRoles(selectedUnitId);
            const savedRanking = await getRolePreference(selectedUnitId, number);
            // Use the saved order only if it still matches the current roles of the assignment
            const savedIsCurrent = savedRanking
                && savedRanking.length === roles.length
                && roles.every((role) => savedRanking.includes(role));
            setAvailableRoles(roles);
            setRanking(savedIsCurrent ? savedRanking : roles);
            setHasSubmitted(Boolean(savedRanking));
            if (savedRanking && !savedIsCurrent) {
                setFeedback({ type: "error", message: "As roles do trabalho mudaram desde a última submissão. Ordene-as de novo." });
            }
        } catch (error) {
            setFeedback({ type: "error", message: error.message });
        }
    }

    function handleDrop(targetIndex) {
        if (draggedIndex !== null && draggedIndex !== targetIndex) {
            setRanking((current) => moveItem(current, draggedIndex, targetIndex));
        }
        setDraggedIndex(null);
    }

    /** Checks that every role appears exactly once; returns an error message or null. */
    function validate() {
        if (new Set(ranking).size !== ranking.length) {
            return "Não pode haver roles repetidas";
        }
        const missing = availableRoles.filter((role) => !ranking.includes(role));
        if (missing.length > 0 || ranking.length !== availableRoles.length) {
            return `Tem de ordenar todas as roles${missing.length ? `: faltam ${missing.join(", ")}` : ""}`;
        }
        return null;
    }

    async function handleSubmit() {
        const validationError = validate();
        if (validationError) {
            setFeedback({ type: "error", message: validationError });
            return;
        }
        try {
            const saved = await submitRolePreference(selectedUnitId, studentNumber.trim(), ranking);
            setRanking(saved);
            setFeedback({
                type: "success",
                message: hasSubmitted ? "Preferências atualizadas com sucesso." : "Preferências submetidas com sucesso.",
            });
            setHasSubmitted(true);
        } catch (error) {
            setFeedback({ type: "error", message: error.message });
        }
    }

    return (
        <div className="max-w-xl">
            <h2 className="mb-1 text-2xl font-bold">Preferências de Roles</h2>
            <p className="mb-4 text-sm opacity-70">Página temporária: simula o questionário do aluno.</p>

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

            <form onSubmit={handleOpen} className="mb-6 space-y-4 rounded-2xl bg-blue p-6">
                <label className="flex flex-col gap-1 font-medium">
                    Unidade curricular
                    <select
                        value={selectedUnitId}
                        onChange={(event) => { setSelectedUnitId(event.target.value); setRanking(null); }}
                        className="rounded-lg border border-lilac bg-white px-3 py-2 font-normal outline-none focus:border-purple focus:ring-2 focus:ring-blue"
                    >
                        <option value="">Escolha uma unidade curricular</option>
                        {units.map((unit) => (
                            <option key={unit.id} value={unit.id}>{unit.code} – {unit.name}</option>
                        ))}
                    </select>
                </label>
                <TextField
                    label="Número de aluno"
                    value={studentNumber}
                    maxLength={20}
                    onChange={(event) => { setStudentNumber(event.target.value); setRanking(null); }}
                />
                <button type="submit" className="rounded-lg bg-pink px-4 py-2 font-semibold hover:bg-lilac">
                    Abrir questionário
                </button>
            </form>

            {ranking && (
                <div className="space-y-4 rounded-2xl bg-blue p-6">
                    <p className="font-medium">
                        Ordene as roles da que mais quer (1) para a que menos quer. Arraste ou use as setas.
                    </p>
                    <ol className="space-y-2">
                        {ranking.map((role, index) => (
                            <li
                                key={role}
                                draggable
                                onDragStart={() => setDraggedIndex(index)}
                                onDragOver={(event) => event.preventDefault()}
                                onDrop={() => handleDrop(index)}
                                onDragEnd={() => setDraggedIndex(null)}
                                className={`flex cursor-grab items-center gap-3 rounded-lg bg-white px-3 py-2 ${
                                    draggedIndex === index ? "opacity-50" : ""
                                }`}
                            >
                                <span className="w-6 font-bold">{index + 1}.</span>
                                <span className="flex-1">{role}</span>
                                <button
                                    type="button"
                                    aria-label={`Subir ${role}`}
                                    disabled={index === 0}
                                    onClick={() => setRanking((current) => moveItem(current, index, index - 1))}
                                    className="rounded px-2 hover:bg-lavender disabled:opacity-30"
                                >
                                    ↑
                                </button>
                                <button
                                    type="button"
                                    aria-label={`Descer ${role}`}
                                    disabled={index === ranking.length - 1}
                                    onClick={() => setRanking((current) => moveItem(current, index, index + 1))}
                                    className="rounded px-2 hover:bg-lavender disabled:opacity-30"
                                >
                                    ↓
                                </button>
                            </li>
                        ))}
                    </ol>
                    <button onClick={handleSubmit} className="rounded-lg bg-pink px-4 py-2 font-semibold hover:bg-lilac">
                        {hasSubmitted ? "Atualizar preferências" : "Submeter preferências"}
                    </button>
                </div>
            )}
        </div>
    );
}
