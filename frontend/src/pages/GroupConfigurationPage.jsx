import { useState } from "react";
import { createGroupConfiguration, deleteGroupConfiguration, getGroupConfiguration, getUnitStudents } from "../api";
import { Button, Card, ErrorBanner, Field, Notice, PageHeader, SectionTitle, SelectField } from "../components/ui";
import { CheckIcon, GroupsIcon, InfoIcon } from "../components/Icons";

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
export default function GroupConfigurationPage({ units, onNotify }) {
    const [selectedUnitId, setSelectedUnitId] = useState("");
    const [savedConfiguration, setSavedConfiguration] = useState(null); // null = unit has no configuration
    const [studentsInUnit, setStudentsInUnit] = useState(0);
    const [numberOfStudents, setNumberOfStudents] = useState(MIN_STUDENTS);
    const [roles, setRoles] = useState(resizeRoles([], MIN_STUDENTS));
    const [isLoading, setIsLoading] = useState(false);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [formError, setFormError] = useState("");

    const selectedUnit = units.find((unit) => String(unit.id) === String(selectedUnitId));
    const maxStudents = Math.min(MAX_STUDENTS, Math.max(studentsInUnit, MIN_STUDENTS));

    function resetForm() {
        setNumberOfStudents(MIN_STUDENTS);
        setRoles(resizeRoles([], MIN_STUDENTS));
        setFormError("");
    }

    /** Selects a unit and loads its configuration and how many students it has. */
    async function handleSelectUnit(unitId) {
        setSelectedUnitId(unitId);
        setSavedConfiguration(null);
        resetForm();
        if (!unitId) return;
        setIsLoading(true);
        try {
            const [configuration, students] = await Promise.all([getGroupConfiguration(unitId), getUnitStudents(unitId)]);
            setSavedConfiguration(configuration);
            setStudentsInUnit(students.length);
        } catch (error) {
            onNotify("error", error.message);
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
        setFormError(validationError ?? "");
        if (validationError) return;
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
            onNotify("success", "Configuração de grupos criada. Os alunos da UC foram notificados.");
        } catch (error) {
            setFormError(error.message);
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
            onNotify("success", "Configuração e respostas eliminadas. Já pode criar uma nova.");
        } catch (error) {
            onNotify("error", error.message);
        }
    }

    return (
        <div className="mx-auto max-w-[880px]">
            <PageHeader
                title="Configuração de grupos"
                description="Defina, para cada UC, quantos alunos tem cada grupo e a role de cada membro."
            />

            <Card className="mt-6">
                <SelectField label="Unidade curricular" value={selectedUnitId} onChange={(event) => handleSelectUnit(event.target.value)}>
                    <option value="">Escolha uma unidade curricular</option>
                    {units.map((unit) => (
                        <option key={unit.id} value={unit.id}>
                            {unit.code} · {unit.name}
                        </option>
                    ))}
                </SelectField>
            </Card>

            {isLoading && <p className="py-12 text-center text-sm text-muted">A carregar a configuração…</p>}

            {selectedUnit && !isLoading && savedConfiguration && (
                <Card className="mt-4 space-y-6">
                    <SectionTitle
                        title="Configuração submetida"
                        aside={`${selectedUnit.code} · final`}
                        description="Esta configuração é final. Para a alterar, o administrador tem de a eliminar (juntamente com as respostas dos alunos)."
                    />
                    <div className="flex items-center gap-3 rounded-lg bg-page px-4 py-3">
                        <span className="grid size-9 place-items-center rounded-lg bg-brand-soft text-brand">
                            <GroupsIcon size={18} />
                        </span>
                        <p className="text-sm">
                            <span className="text-[22px] font-semibold">{savedConfiguration.numberOfStudents}</span>
                            <span className="ml-2 text-muted">alunos por grupo</span>
                        </p>
                    </div>
                    <div>
                        <p className="mb-2 text-sm font-medium">Role de cada membro</p>
                        <ol className="overflow-hidden rounded-lg border border-line">
                            {savedConfiguration.roles.map((role, index) => (
                                <li key={index} className="flex items-center gap-3 border-b border-line px-4 py-3 text-sm last:border-b-0">
                                    <span className="grid size-6 place-items-center rounded-md bg-page text-xs font-semibold text-muted">{index + 1}</span>
                                    {role}
                                </li>
                            ))}
                        </ol>
                    </div>
                    <div className="flex flex-col gap-3 border-t border-line pt-5 sm:flex-row sm:items-center sm:justify-between">
                        <p className="text-xs text-muted">Ação reservada ao administrador.</p>
                        <Button variant="danger" onClick={handleDelete}>
                            Eliminar configuração e respostas
                        </Button>
                    </div>
                </Card>
            )}

            {selectedUnit && !isLoading && !savedConfiguration && (
                <form onSubmit={handleSubmit} noValidate>
                    <Card className="mt-4 space-y-6">
                        <SectionTitle
                            title="Nova configuração"
                            aside={`${studentsInUnit} aluno(s) na UC`}
                            description="Esta UC ainda não tem configuração de grupos. Depois de submetida, os alunos são notificados."
                        />
                        {studentsInUnit < MIN_STUDENTS && (
                            <Notice tone="rose" icon={InfoIcon}>
                                A UC precisa de pelo menos {MIN_STUDENTS} alunos associados para formar grupos. Associe alunos em
                                “Alunos das UCs”.
                            </Notice>
                        )}
                        <Field
                            label={`Número de alunos por grupo (${MIN_STUDENTS}–${maxStudents})`}
                            type="number"
                            min={MIN_STUDENTS}
                            max={maxStudents}
                            value={numberOfStudents}
                            onChange={(event) => handleNumberChange(event.target.value)}
                        />
                        <fieldset className="space-y-3">
                            <legend className="mb-3 text-sm font-medium">Role de cada membro</legend>
                            {roles.map((role, index) => (
                                <div key={index} className="flex items-center gap-3">
                                    <span className="grid size-8 shrink-0 place-items-center rounded-md bg-page text-xs font-semibold text-muted">
                                        {index + 1}
                                    </span>
                                    <input
                                        value={role}
                                        maxLength={50}
                                        aria-label={`Role do membro ${index + 1}`}
                                        placeholder="Ex.: Líder, Programador, Tester"
                                        onChange={(event) => handleRoleChange(index, event.target.value)}
                                        className="h-11 w-full rounded-lg border border-line bg-field px-3.5 text-sm outline-none transition focus:border-brand focus:ring-3 focus:ring-brand/15"
                                    />
                                </div>
                            ))}
                        </fieldset>
                        <ErrorBanner message={formError} />
                        <div className="flex flex-col gap-3 border-t border-line pt-5 sm:flex-row sm:items-center sm:justify-between">
                            <p className="text-xs text-muted">Depois de submetida, a configuração não pode ser alterada.</p>
                            <Button type="submit" variant="primary" disabled={isSubmitting}>
                                <CheckIcon size={16} strokeWidth={2.4} /> {isSubmitting ? "A submeter…" : "Submeter configuração"}
                            </Button>
                        </div>
                    </Card>
                </form>
            )}

            {!selectedUnitId && (
                <p className="mt-5 flex items-center gap-2 text-xs text-muted">
                    <InfoIcon size={14} /> Escolha uma UC para ver ou criar a sua configuração de grupos.
                </p>
            )}
        </div>
    );
}
