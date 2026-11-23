import { useState } from "react";
import { enrollStudent, getUnitStudents, removeStudent } from "../api";
import { Button, Card, CountPill, ErrorBanner, PageHeader, SectionTitle, SelectField } from "../components/ui";
import { initials } from "../components/format";
import { CloseIcon, InfoIcon, PlusIcon } from "../components/Icons";

const STUDENT_NUMBER_PATTERN = /^[A-Za-z0-9]{1,20}$/;

/**
 * Administrator page to associate students with a curricular unit for the group work.
 * Temporary: students are identified only by their number until they have their own accounts.
 * `students` are the registered students, offered as suggestions for the number.
 */
export default function UnitStudentsPage({ units, students, onNotify }) {
    const [selectedUnitId, setSelectedUnitId] = useState("");
    const [unitStudents, setUnitStudents] = useState([]);
    const [newStudentNumber, setNewStudentNumber] = useState("");
    const [isLoading, setIsLoading] = useState(false);
    const [formError, setFormError] = useState("");

    const selectedUnit = units.find((unit) => String(unit.id) === String(selectedUnitId));
    const namesByNumber = new Map(students.map((student) => [student.number, student.name]));

    async function handleSelectUnit(unitId) {
        setSelectedUnitId(unitId);
        setUnitStudents([]);
        setFormError("");
        if (!unitId) return;
        setIsLoading(true);
        try {
            setUnitStudents(await getUnitStudents(unitId));
        } catch (error) {
            onNotify("error", error.message);
        } finally {
            setIsLoading(false);
        }
    }

    async function handleAdd(event) {
        event.preventDefault();
        const number = newStudentNumber.trim();
        if (!STUDENT_NUMBER_PATTERN.test(number)) {
            setFormError("O número de aluno só pode ter letras e números (máximo 20)");
            return;
        }
        setFormError("");
        try {
            const saved = await enrollStudent(selectedUnitId, number);
            setUnitStudents((current) => [...current, saved].sort());
            setNewStudentNumber("");
            onNotify("success", `Aluno ${saved} associado à UC.`);
        } catch (error) {
            setFormError(error.message);
        }
    }

    async function handleRemove(studentNumber) {
        const confirmed = window.confirm(
            `Remover o aluno ${studentNumber} desta UC? A resposta que tenha submetido também será eliminada.`
        );
        if (!confirmed) return;
        try {
            await removeStudent(selectedUnitId, studentNumber);
            setUnitStudents((current) => current.filter((number) => number !== studentNumber));
            onNotify("success", `Aluno ${studentNumber} removido da UC.`);
        } catch (error) {
            onNotify("error", error.message);
        }
    }

    return (
        <div className="mx-auto max-w-[880px]">
            <PageHeader
                title="Alunos das UCs"
                description="Associe alunos às UCs para a formação de grupos. Temporário: enquanto os alunos não têm conta, são identificados pelo número."
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

            {selectedUnit && (
                <Card className="mt-4 space-y-5">
                    <SectionTitle
                        title={
                            <span className="flex items-center gap-2.5">
                                Alunos associados <CountPill tone="sky">{unitStudents.length}</CountPill>
                            </span>
                        }
                        aside={`${selectedUnit.code} · ${selectedUnit.name}`}
                    />

                    <form onSubmit={handleAdd} className="flex flex-col gap-2 sm:flex-row">
                        <input
                            value={newStudentNumber}
                            maxLength={20}
                            list="registered-student-numbers"
                            aria-label="Número de aluno"
                            placeholder="Número de aluno, ex.: 2024001"
                            onChange={(event) => setNewStudentNumber(event.target.value)}
                            className="h-11 min-w-0 flex-1 rounded-lg border border-line bg-field px-3.5 text-sm outline-none transition focus:border-brand focus:ring-3 focus:ring-brand/15"
                        />
                        <datalist id="registered-student-numbers">
                            {students.map((student) => (
                                <option key={student.id} value={student.number}>
                                    {student.name}
                                </option>
                            ))}
                        </datalist>
                        <Button type="submit" variant="primary" className="h-11">
                            <PlusIcon size={16} /> Associar
                        </Button>
                    </form>
                    <ErrorBanner message={formError} />

                    {isLoading ? (
                        <p className="py-8 text-center text-sm text-muted">A carregar alunos…</p>
                    ) : unitStudents.length === 0 ? (
                        <p className="rounded-lg border border-dashed border-line py-8 text-center text-sm text-muted">
                            Ainda não há alunos associados a esta UC.
                        </p>
                    ) : (
                        <ul className="overflow-hidden rounded-lg border border-line">
                            {unitStudents.map((number) => {
                                const name = namesByNumber.get(number);
                                return (
                                    <li key={number} className="flex items-center gap-3 border-b border-line px-4 py-3 last:border-b-0">
                                        <span className="grid size-8 shrink-0 place-items-center rounded-full bg-rose text-[11px] font-semibold text-rose-ink">
                                            {name ? initials(name) : "#"}
                                        </span>
                                        <div className="min-w-0 flex-1">
                                            <p className="truncate text-[13px] font-medium">{name ?? `Aluno ${number}`}</p>
                                            <p className="mt-0.5 text-[11px] text-muted">Nº {number}</p>
                                        </div>
                                        <Button variant="ghost" size="sm" onClick={() => handleRemove(number)} aria-label={`Remover o aluno ${number}`}>
                                            <CloseIcon size={15} /> Remover
                                        </Button>
                                    </li>
                                );
                            })}
                        </ul>
                    )}
                </Card>
            )}

            {!selectedUnitId && (
                <p className="mt-5 flex items-center gap-2 text-xs text-muted">
                    <InfoIcon size={14} /> Escolha uma UC para ver e gerir os alunos associados.
                </p>
            )}
        </div>
    );
}
