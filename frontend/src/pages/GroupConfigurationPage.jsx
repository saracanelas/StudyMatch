import { useEffect, useState } from "react";
import {
    createGroupConfiguration,
    deleteGroupConfiguration,
    getAllCurricularUnits,
    getGroupConfiguration,
    getUnitStudents,
} from "../api";
import TextField from "../components/TextField";

const MIN_STUDENTS = 2;
const MAX_STUDENTS = 10;

/** Returns `roles` with exactly `count` entries, keeping the ones already written. */
function resizeRoles(roles, count) {
    return Array.from({ length: count }, (_, index) => roles[index] ?? "");
}

/**
 * Page where the teacher picks a curricular unit and sets the number of students and the role of each member.
 * A configuration is final once submitted: only the administrator can delete it (with every student answer)
 * so a new one can be created.
 */
export default function GroupConfigurationPage() {
    const [units, setUnits] = useState([]);
    const [selectedUnitId, setSelectedUnitId] = useState("");
    const [savedConfiguration, setSavedConfiguration] = useState(null); // null = unit has no configuration
    const [studentsInUnit, setStudentsInUnit] = useState(0);
    const [numberOfStudents, setNumberOfStudents] = useState(MIN_STUDENTS);
    const [roles, setRoles] = useState(resizeRoles([], MIN_STUDENTS));
    const [isLoading, setIsLoading] = useState(false);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [feedback, setFeedback] = useState(null); // { type: "success" | "error", message }

    // Loads the curricular units once when the page opens
    useEffect(() => {
        let isMounted = true;
        getAllCurricularUnits()
            .then((loadedUnits) => isMounted && setUnits(loadedUnits))
            .catch((error) => isMounted && setFeedback({ type: "error", message: error.message }));
        return () => { isMounted = false; };
    }, []);

    function resetForm() {
        setNumberOfStudents(MIN_STUDENTS);
        setRoles(resizeRoles([], MIN_STUDENTS));
    }

    /** Selects a unit and loads its configuration and how many students it has. */
    async function handleSelectUnit(unitId) {
        setSelectedUnitId(unitId);
        setFeedback(null);
        setSavedConfiguration(null);
        resetForm();
        if (!unitId) return;
        setIsLoading(true);
        try {
            const [configuration, students] = await Promise.all([
                getGroupConfiguration(unitId),
                getUnitStudents(unitId),
            ]);
            setSavedConfiguration(configuration);
            setStudentsInUnit(students.length);
        } catch (error) {
            setFeedback({ type: "error", message: error.message });
        } finally {
            setIsLoading(false);
        }
    }

    /** Changes the number of students and adds or removes role fields to match. */
    function handleNumberChange(value) {
        setNumberOfStudents(value);
        const count = Number(value);
        if (Number.isInteger(count) && count >= MIN_STUDENTS && count <= MAX_STUDENTS) {
            setRoles((currentRoles) => resizeRoles(currentRoles, count));
        }
    }

    function handleRoleChange(index, value) {
        setRoles((currentRoles) => currentRoles.map((role, i) => (i === index ? value : role)));
    }

    /** Checks the form in the browser and returns an error message, or null if it is valid. */
    function validate() {
        const count = Number(numberOfStudents);
        if (!Number.isInteger(count) || count < MIN_STUDENTS || count > MAX_STUDENTS) {
            return `O número de alunos tem de ser um número inteiro entre ${MIN_STUDENTS} e ${MAX_STUDENTS}`;
        }
        if (studentsInUnit < count) {
            return `A UC tem ${studentsInUnit} aluno(s) associado(s); não é possível formar grupos de ${count} alunos`;
        }
        if (roles.some((role) => role.trim() === "")) {
            return "Todas as roles têm de ser preenchidas";
        }
        return null;
    }

    /** Validates, asks for confirmation (the configuration is final) and creates it. */
    async function handleSubmit(event) {
        event.preventDefault();
        const validationError = validate();
        if (validationError) {
            setFeedback({ type: "error", message: validationError });
            return;
        }
        const confirmed = window.confirm(
            "Depois de submetida, a configuração não pode ser alterada. Para a mudar terá de pedir ao " +
            "administrador para a eliminar. Deseja submeter?"
        );
        if (!confirmed) return;
        setIsSubmitting(true);
        try {
            const saved = await createGroupConfiguration(selectedUnitId, {
                numberOfStudents: Number(numberOfStudents),
                roles: roles.map((role) => role.trim()),
            });
            setSavedConfiguration(saved);
            setFeedback({
                type: "success",
                message: "Configuração de grupos criada com sucesso. Os alunos da UC foram notificados.",
            });
        } catch (error) {
            setFeedback({ type: "error", message: error.message });
        } finally {
            setIsSubmitting(false);
        }
    }

    /** Administrator: deletes the configuration and every student answer, so a new one can be made. */
    async function handleDelete() {
        const confirmed = window.confirm(
            "Eliminar esta configuração de grupos? Todas as respostas dos alunos associadas serão também eliminadas."
        );
        if (!confirmed) return;
        try {
            await deleteGroupConfiguration(selectedUnitId);
            setSavedConfiguration(null);
            resetForm();
            setFeedback({ type: "success", message: "Configuração e respostas eliminadas. Já pode criar uma nova." });
        } catch (error) {
            setFeedback({ type: "error", message: error.message });
        }
    }

    return (
        <div className="max-w-xl">
            <h2 className="mb-4 text-2xl font-bold">Configuração de Grupos</h2>

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
                        <option key={unit.id} value={unit.id}>
                            {unit.code} – {unit.name}
                        </option>
                    ))}
                </select>
            </label>

            {isLoading && <p>A carregar...</p>}

            {selectedUnitId && !isLoading && savedConfiguration && (
                <div className="space-y-4 rounded-2xl bg-blue p-6">
                    <p className="text-sm">
                        Esta configuração já foi submetida e é final. Para a alterar, é necessário pedir ao
                        administrador para a eliminar (juntamente com as respostas dos alunos).
                    </p>
                    <p><span className="font-medium">Alunos por grupo:</span> {savedConfiguration.numberOfStudents}</p>
                    <div>
                        <p className="mb-1 font-medium">Role de cada membro</p>
                        <ol className="list-decimal space-y-1 pl-6">
                            {savedConfiguration.roles.map((role, index) => <li key={index}>{role}</li>)}
                        </ol>
                    </div>
                    <div className="border-t border-lilac pt-4">
                        <p className="mb-2 text-sm opacity-70">Ação reservada ao administrador</p>
                        <button
                            type="button"
                            onClick={handleDelete}
                            className="rounded-lg bg-white px-4 py-2 font-semibold text-red-700 hover:bg-lavender"
                        >
                            Eliminar configuração e respostas
                        </button>
                    </div>
                </div>
            )}

            {selectedUnitId && !isLoading && !savedConfiguration && (
                <form onSubmit={handleSubmit} noValidate className="space-y-4 rounded-2xl bg-blue p-6">
                    <p className="text-sm">
                        Esta unidade ainda não tem configuração. Alunos associados à UC: <strong>{studentsInUnit}</strong>.
                    </p>
                    {studentsInUnit < MIN_STUDENTS && (
                        <p className="rounded-lg bg-white p-3 text-sm text-red-700">
                            A UC precisa de pelo menos {MIN_STUDENTS} alunos associados para formar grupos.
                            Associe alunos na página "Alunos das UCs".
                        </p>
                    )}
                    <TextField
                        label={`Número de alunos por grupo (${MIN_STUDENTS}–${Math.min(MAX_STUDENTS, Math.max(studentsInUnit, MIN_STUDENTS))})`}
                        type="number"
                        min={MIN_STUDENTS}
                        max={Math.min(MAX_STUDENTS, Math.max(studentsInUnit, MIN_STUDENTS))}
                        value={numberOfStudents}
                        onChange={(event) => handleNumberChange(event.target.value)}
                    />
                    <fieldset className="space-y-2">
                        <legend className="mb-1 font-medium">Role de cada membro</legend>
                        {roles.map((role, index) => (
                            <TextField
                                key={index}
                                label={`Membro ${index + 1}`}
                                value={role}
                                maxLength={50}
                                placeholder="ex.: Líder, Programador, Tester"
                                onChange={(event) => handleRoleChange(index, event.target.value)}
                            />
                        ))}
                    </fieldset>
                    <button
                        type="submit"
                        disabled={isSubmitting}
                        className="rounded-lg bg-pink px-4 py-2 font-semibold hover:bg-lilac disabled:opacity-50"
                    >
                        Submeter configuração
                    </button>
                </form>
            )}
        </div>
    );
}
