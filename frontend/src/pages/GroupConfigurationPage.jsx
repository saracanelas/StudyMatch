import { useEffect, useState } from "react";
import { getAllCurricularUnits, getGroupConfiguration, saveGroupConfiguration } from "../api";
import TextField from "../components/TextField";

const MIN_STUDENTS = 2;
const MAX_STUDENTS = 10;

/** Returns `roles` with exactly `count` entries, keeping the ones already written. */
function resizeRoles(roles, count) {
    return Array.from({ length: count }, (_, index) => roles[index] ?? "");
}

/** Page where the teacher picks a curricular unit and sets the number of students and the role of each member. */
export default function GroupConfigurationPage() {
    const [units, setUnits] = useState([]);
    const [selectedUnitId, setSelectedUnitId] = useState("");
    const [numberOfStudents, setNumberOfStudents] = useState(MIN_STUDENTS);
    const [roles, setRoles] = useState(resizeRoles([], MIN_STUDENTS));
    const [isNew, setIsNew] = useState(true);
    const [isLoading, setIsLoading] = useState(false);
    const [feedback, setFeedback] = useState(null); // { type: "success" | "error", message }

    // Loads the curricular units once when the page opens
    useEffect(() => {
        let isMounted = true;
        getAllCurricularUnits()
            .then((loadedUnits) => isMounted && setUnits(loadedUnits))
            .catch((error) => isMounted && setFeedback({ type: "error", message: error.message }));
        return () => { isMounted = false; };
    }, []);

    /** Selects a unit and loads its configuration, or starts an empty one if it has none. */
    async function handleSelectUnit(unitId) {
        setSelectedUnitId(unitId);
        setFeedback(null);
        if (!unitId) return;
        setIsLoading(true);
        try {
            const configuration = await getGroupConfiguration(unitId);
            setIsNew(configuration === null);
            const count = configuration?.numberOfStudents ?? MIN_STUDENTS;
            setNumberOfStudents(count);
            setRoles(resizeRoles(configuration?.roles ?? [], count));
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
        if (roles.some((role) => role.trim() === "")) {
            return "Todas as roles têm de ser preenchidas";
        }
        return null;
    }

    /** Validates and saves the configuration, showing a success or error message. */
    async function handleSubmit(event) {
        event.preventDefault();
        const validationError = validate();
        if (validationError) {
            setFeedback({ type: "error", message: validationError });
            return;
        }
        try {
            const saved = await saveGroupConfiguration(
                selectedUnitId,
                { numberOfStudents: Number(numberOfStudents), roles: roles.map((role) => role.trim()) },
                isNew
            );
            setNumberOfStudents(saved.numberOfStudents);
            setRoles(saved.roles);
            setFeedback({
                type: "success",
                message: isNew ? "Configuração de grupos criada com sucesso." : "Configuração de grupos atualizada com sucesso.",
            });
            setIsNew(false);
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

            {selectedUnitId && !isLoading && (
                <form onSubmit={handleSubmit} noValidate className="space-y-4 rounded-2xl bg-blue p-6">
                    <p className="text-sm">
                        {isNew ? "Esta unidade ainda não tem configuração." : "A editar a configuração existente."}
                    </p>
                    <TextField
                        label={`Número de alunos por grupo (${MIN_STUDENTS}–${MAX_STUDENTS})`}
                        type="number"
                        min={MIN_STUDENTS}
                        max={MAX_STUDENTS}
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
                    <button type="submit" className="rounded-lg bg-pink px-4 py-2 font-semibold hover:bg-lilac">
                        {isNew ? "Criar configuração" : "Guardar alterações"}
                    </button>
                </form>
            )}
        </div>
    );
}
