import { useEffect, useMemo, useState } from "react";
import { getEnrollmentsOfUnit } from "../api";
import { Button, CodeBadge, CountPill, ErrorBanner, SearchInput } from "../components/ui";
import { cx } from "../components/cx";
import { formatGrade, initials, matchesQuery } from "../components/format";
import { EditPill, Pagination, StatCard } from "../components/widgets";
import { BookIcon, CapIcon, CheckIcon, ChevronRightIcon, CloseIcon, InfoIcon, PencilIcon, PlusIcon, UsersIcon } from "../components/Icons";

const PAGE_SIZE = 7;

const formatTime = (date) => date?.toLocaleTimeString("pt-PT", { hour: "2-digit", minute: "2-digit" });
const formatDate = (iso) => (iso ? new Date(`${iso}T00:00:00`).toLocaleDateString("pt-PT") : null);

/** Simple mean of the values that are not null, or null if there are none. */
function mean(values) {
    const present = values.filter((value) => value !== null && value !== undefined);
    return present.length ? present.reduce((sum, value) => sum + value, 0) / present.length : null;
}

// ---------------------------------------------------------------------------

/**
 * Editable grade (0-20). Shows a save button while the value differs from the stored grade;
 * Enter saves, an empty field clears the grade.
 */
function GradeInput({ grade, onSave, label }) {
    const stored = grade === null || grade === undefined ? "" : String(grade);
    const [value, setValue] = useState(stored);
    const [isSaving, setIsSaving] = useState(false);
    const [invalid, setInvalid] = useState(false);

    // Follows the stored grade when it changes elsewhere (e.g. saved in the other tab)
    const [lastStored, setLastStored] = useState(stored);
    if (stored !== lastStored) {
        setLastStored(stored);
        setValue(stored);
        setInvalid(false);
    }

    const isDirty = value.trim() !== stored;

    async function save() {
        const text = value.trim().replace(",", ".");
        const number = text === "" ? null : Number(text);
        if (number !== null && (Number.isNaN(number) || number < 0 || number > 20)) {
            setInvalid(true);
            return;
        }
        setInvalid(false);
        setIsSaving(true);
        try {
            await onSave(number);
        } finally {
            setIsSaving(false);
        }
    }

    return (
        <div className="flex items-center gap-1.5">
            <input
                type="text"
                inputMode="decimal"
                value={value}
                placeholder="—"
                aria-label={label}
                aria-invalid={invalid}
                title={invalid ? "A nota tem de estar entre 0 e 20" : "Nota de 0 a 20 (vazio para apagar)"}
                onChange={(event) => {
                    setValue(event.target.value);
                    setInvalid(false);
                }}
                onKeyDown={(event) => {
                    if (event.key === "Enter") {
                        event.preventDefault();
                        if (isDirty) save();
                    }
                    if (event.key === "Escape") setValue(stored);
                }}
                className={cx(
                    "h-9 w-[4.5rem] rounded-lg border bg-field px-2 text-center text-sm font-semibold outline-none transition focus:ring-3",
                    invalid ? "border-red-400 text-red-700 focus:ring-red-200" : "border-line focus:border-brand focus:ring-brand/15"
                )}
            />
            <button
                type="button"
                onClick={save}
                disabled={!isDirty || isSaving}
                aria-label={`Guardar ${label}`}
                title="Guardar nota"
                className={cx(
                    "grid size-9 place-items-center rounded-lg transition-colors",
                    isDirty ? "bg-brand text-white hover:bg-brand-dark" : "pointer-events-none opacity-0"
                )}
            >
                <CheckIcon size={16} strokeWidth={2.4} />
            </button>
        </div>
    );
}

/** Select + button to add an enrollment (a unit for a student, or a student for a unit). */
function EnrollPicker({ options, placeholder, emptyText, onEnroll, buttonLabel }) {
    const [selected, setSelected] = useState("");
    const [isSaving, setIsSaving] = useState(false);

    async function handleEnroll() {
        setIsSaving(true);
        try {
            const enrolled = await onEnroll(Number(selected));
            if (enrolled) setSelected("");
        } finally {
            setIsSaving(false);
        }
    }

    if (options.length === 0) {
        return <p className="text-xs text-muted">{emptyText}</p>;
    }
    return (
        <div className="flex flex-col gap-2 sm:flex-row">
            <select
                value={selected}
                onChange={(event) => setSelected(event.target.value)}
                aria-label={placeholder}
                className="h-9 min-w-0 flex-1 rounded-lg border border-line bg-field px-2.5 text-[13px] outline-none focus:border-brand focus:ring-3 focus:ring-brand/15"
            >
                <option value="">{placeholder}</option>
                {options.map((option) => (
                    <option key={option.value} value={option.value}>
                        {option.label}
                    </option>
                ))}
            </select>
            <Button variant="primary" size="sm" disabled={!selected || isSaving} onClick={handleEnroll}>
                <PlusIcon size={15} /> {isSaving ? "A inscrever…" : buttonLabel}
            </Button>
        </div>
    );
}

/** Small "remove" icon button. */
function RemoveButton({ label, onClick }) {
    return (
        <button
            type="button"
            onClick={onClick}
            aria-label={label}
            title={label}
            className="grid size-8 place-items-center rounded-md text-subtle hover:bg-rose hover:text-rose-ink"
        >
            <CloseIcon size={15} />
        </button>
    );
}

/** Generic searchable, paginated table; `columns` are { header, className, render(row) }. */
function DataTable({ title, count, searchPlaceholder, emptyText, rows, columns, searchFields, selectedId, onSelect, pluralNoun }) {
    const [query, setQuery] = useState("");
    const [page, setPage] = useState(1);

    const visibleRows = rows.filter((row) => matchesQuery(query, ...searchFields(row)));
    const pageCount = Math.max(1, Math.ceil(visibleRows.length / PAGE_SIZE));
    const currentPage = Math.min(page, pageCount);
    const pageRows = visibleRows.slice((currentPage - 1) * PAGE_SIZE, currentPage * PAGE_SIZE);
    const firstShown = visibleRows.length === 0 ? 0 : (currentPage - 1) * PAGE_SIZE + 1;

    return (
        <section className="flex flex-col overflow-hidden rounded-xl border border-line bg-surface">
            <div className="p-4">
                <h2 className="flex items-center gap-2.5 text-base font-semibold">
                    {title} <CountPill tone="sky">{count}</CountPill>
                </h2>
                <SearchInput
                    className="mt-4"
                    placeholder={searchPlaceholder}
                    aria-label={searchPlaceholder}
                    value={query}
                    onChange={(event) => {
                        setQuery(event.target.value);
                        setPage(1);
                    }}
                />
            </div>
            <div className="overflow-x-auto">
                <table className="w-full min-w-[560px] text-left">
                    <thead>
                        <tr className="border-y border-line text-[10px] font-medium tracking-wide text-muted uppercase">
                            {columns.map((column, index) => (
                                <th key={column.header} className={cx("py-3 font-medium", index === 0 && "pl-4", index === columns.length - 1 && "pr-4", column.className)}>
                                    {column.header}
                                </th>
                            ))}
                        </tr>
                    </thead>
                    <tbody>
                        {pageRows.map((row) => {
                            const isSelected = row.id === selectedId;
                            return (
                                <tr
                                    key={row.id}
                                    onClick={() => onSelect(row.id)}
                                    aria-selected={isSelected}
                                    className={cx(
                                        "cursor-pointer border-b border-line text-[13px] transition-colors last:border-b-0",
                                        isSelected ? "bg-rose shadow-[inset_2px_0_0_var(--color-brand)]" : "hover:bg-page/60"
                                    )}
                                >
                                    {columns.map((column, index) => (
                                        <td key={column.header} className={cx("py-3.5", index === 0 && "pl-4", index === columns.length - 1 && "pr-4", column.className)}>
                                            {column.render(row, isSelected)}
                                        </td>
                                    ))}
                                </tr>
                            );
                        })}
                    </tbody>
                </table>
                {pageRows.length === 0 && <p className="px-4 py-10 text-center text-sm text-muted">{query ? "Nenhum resultado para a pesquisa." : emptyText}</p>}
            </div>
            <div className="mt-auto flex items-center justify-between border-t border-line px-4 py-3.5 text-xs text-muted">
                <span>
                    A mostrar {firstShown}–{(currentPage - 1) * PAGE_SIZE + pageRows.length} de {visibleRows.length} {pluralNoun}
                </span>
                <Pagination page={currentPage} pageCount={pageCount} onChange={setPage} />
            </div>
        </section>
    );
}

function EmptyPanel({ icon: PanelIcon, title, text }) {
    return (
        <section className="grid min-h-80 place-items-center rounded-xl border border-dashed border-line bg-surface p-8 text-center">
            <div>
                <span className="mx-auto grid size-11 place-items-center rounded-xl bg-brand-soft text-brand">
                    <PanelIcon size={20} />
                </span>
                <p className="mt-4 font-semibold">{title}</p>
                <p className="mt-1 text-sm text-muted">{text}</p>
            </div>
        </section>
    );
}

/** Big number with a caption (grade average, enrolled count...). */
function Metric({ label, value, hint }) {
    return (
        <div className="rounded-lg bg-note px-3.5 py-3">
            <p className="text-[11px] text-muted">{label}</p>
            <p className="mt-0.5 text-xl font-semibold">{value}</p>
            {hint && <p className="text-[11px] text-muted">{hint}</p>}
        </div>
    );
}

// ---------------------------------------------------------------------------

/** Right-hand panel for the "Estudantes" tab: the student's data, units and grades. */
function StudentPanel({ student, course, enrollments, units, onEdit, onEnroll, onSetGrade, onUnenroll }) {
    if (!student) {
        return <EmptyPanel icon={UsersIcon} title="Nenhum estudante selecionado" text="Selecione um estudante para ver as UCs e registar notas." />;
    }

    const unitsById = new Map(units.map((unit) => [unit.id, unit]));
    const enrolledUnitIds = new Set(enrollments.map((enrollment) => enrollment.unitId));
    const graded = enrollments.filter((enrollment) => enrollment.grade !== null);
    const totalEcts = graded.reduce((sum, enrollment) => sum + (unitsById.get(enrollment.unitId)?.ects ?? 0), 0);
    // UCs of the student's course first, then the rest of the catalogue
    const available = units
        .filter((unit) => !enrolledUnitIds.has(unit.id))
        .sort((a, b) => Number(Boolean(course?.unitIds.includes(b.id))) - Number(Boolean(course?.unitIds.includes(a.id))) || a.name.localeCompare(b.name, "pt"));

    const details = [
        course ? `${course.code} · ${course.name}` : "Sem curso",
        `Ingresso em ${student.enrollmentYear}`,
        student.className && `Turma ${student.className}`,
        student.birthday && `Nascido(a) a ${formatDate(student.birthday)}`,
    ].filter(Boolean);

    return (
        <section className="flex flex-col rounded-xl border border-line bg-surface">
            <div className="border-b border-line p-4 pb-5">
                <div className="flex items-start justify-between">
                    <div className="flex items-center gap-2.5 pt-1.5 text-xs">
                        <span className="font-medium text-muted">Nº {student.number}</span>
                        <span className="rounded-md bg-mint px-2 py-0.5 font-medium text-mint-ink">Estudante</span>
                    </div>
                    <Button size="sm" onClick={onEdit}>
                        <PencilIcon size={15} /> Editar
                    </Button>
                </div>
                <h2 className="mt-4 text-xl font-semibold break-words">{student.name}</h2>
                <p className="mt-1 text-xs text-muted">{student.email}</p>
                <p className="mt-3 text-xs leading-relaxed text-muted">{details.join(" · ")}</p>
                <div className="mt-4 grid grid-cols-2 gap-2.5">
                    <Metric label="Média (ponderada por ECTS)" value={formatGrade(student.gradeAverage)} hint={graded.length ? `${graded.length} UC${graded.length === 1 ? "" : "s"} · ${totalEcts} ECTS` : "Sem notas registadas"} />
                    <Metric label="UCs inscritas" value={enrollments.length} hint={`${enrollments.length - graded.length} por avaliar`} />
                </div>
            </div>

            <div className="px-4 pt-4">
                <div className="flex items-center justify-between">
                    <h3 className="text-sm font-semibold">UCs e notas</h3>
                    <span className="text-[11px] text-muted">Nota de 0 a 20 · Enter para guardar</span>
                </div>
                {enrollments.length > 0 ? (
                    <ul className="mt-2">
                        {enrollments.map((enrollment) => {
                            const unit = unitsById.get(enrollment.unitId);
                            return (
                                <li key={enrollment.id} className="flex items-center gap-3 border-b border-line py-3 last:border-b-0">
                                    <span className="grid size-8 shrink-0 place-items-center rounded-md bg-page text-muted">
                                        <BookIcon size={16} />
                                    </span>
                                    <div className="min-w-0 flex-1">
                                        <p className="truncate text-[13px] font-medium">{unit?.name ?? "UC removida"}</p>
                                        <p className="mt-0.5 text-[11px] text-muted">
                                            {unit?.code} · {unit?.ects} ECTS
                                        </p>
                                    </div>
                                    <GradeInput grade={enrollment.grade} label={`nota de ${unit?.name}`} onSave={(grade) => onSetGrade(enrollment, grade)} />
                                    <RemoveButton label={`Anular inscrição em ${unit?.name}`} onClick={() => onUnenroll(enrollment)} />
                                </li>
                            );
                        })}
                    </ul>
                ) : (
                    <p className="py-8 text-center text-sm text-muted">Este estudante ainda não está inscrito em nenhuma UC.</p>
                )}
            </div>

            <div className="mt-auto border-t border-line p-4">
                <p className="text-[13px] font-semibold">Inscrever numa UC</p>
                <p className="mt-0.5 mb-3 text-[11px] text-muted">As UCs do curso do estudante aparecem primeiro.</p>
                <EnrollPicker
                    options={available.map((unit) => ({
                        value: unit.id,
                        label: `${unit.code} · ${unit.name}${course?.unitIds.includes(unit.id) ? "" : " (fora do curso)"}`,
                    }))}
                    placeholder="Escolher UC…"
                    emptyText="O estudante já está inscrito em todas as UCs do catálogo."
                    buttonLabel="Inscrever"
                    onEnroll={(unitId) => onEnroll(student.id, unitId)}
                />
            </div>
        </section>
    );
}

/** Right-hand panel for the "Notas por UC" tab: students enrolled in the unit (GET /units/{id}/enrollments). */
function UnitPanel({ unit, students, refreshKey, onEnroll, onSetGrade, onUnenroll, onLoadError }) {
    const [loaded, setLoaded] = useState({ key: null, enrollments: [] });
    const requestKey = unit ? `${unit.id}:${refreshKey}` : null;

    useEffect(() => {
        if (!unit) return undefined;
        let isMounted = true;
        getEnrollmentsOfUnit(unit.id)
            .then((enrollments) => isMounted && setLoaded({ key: `${unit.id}:${refreshKey}`, enrollments }))
            .catch((error) => isMounted && onLoadError(error));
        return () => {
            isMounted = false;
        };
    }, [unit, refreshKey, onLoadError]);

    if (!unit) {
        return <EmptyPanel icon={BookIcon} title="Nenhuma UC selecionada" text="Selecione uma UC para ver os estudantes inscritos e lançar as notas." />;
    }

    const isLoading = loaded.key !== requestKey;
    const enrollments = [...loaded.enrollments].sort((a, b) => a.student.name.localeCompare(b.student.name, "pt"));
    const enrolledIds = new Set(enrollments.map((enrollment) => enrollment.studentId));
    const unitAverage = mean(enrollments.map((enrollment) => enrollment.grade));
    const passed = enrollments.filter((enrollment) => enrollment.grade !== null && enrollment.grade >= 9.5).length;

    return (
        <section className="flex flex-col rounded-xl border border-line bg-surface">
            <div className="border-b border-line p-4 pb-5">
                <div className="flex items-center gap-2.5 text-xs">
                    <span className="font-medium text-muted">{unit.code}</span>
                    <span className="rounded-md bg-mint px-2 py-0.5 font-medium text-mint-ink">{unit.ects} ECTS</span>
                </div>
                <h2 className="mt-4 text-xl font-semibold break-words">{unit.name}</h2>
                <div className="mt-4 grid grid-cols-3 gap-2.5">
                    <Metric label="Inscritos" value={isLoading ? "…" : enrollments.length} />
                    <Metric label="Média da UC" value={isLoading ? "…" : formatGrade(unitAverage === null ? null : Math.round(unitAverage * 100) / 100)} />
                    <Metric label="Aprovados (≥ 9,5)" value={isLoading ? "…" : passed} />
                </div>
            </div>

            <div className="px-4 pt-4">
                <div className="flex items-center justify-between">
                    <h3 className="text-sm font-semibold">Estudantes inscritos</h3>
                    <span className="text-[11px] text-muted">Nota de 0 a 20 · Enter para guardar</span>
                </div>
                {isLoading ? (
                    <p className="py-8 text-center text-sm text-muted">A carregar inscritos…</p>
                ) : enrollments.length > 0 ? (
                    <ul className="mt-2">
                        {enrollments.map((enrollment) => (
                            <li key={enrollment.id} className="flex items-center gap-3 border-b border-line py-3 last:border-b-0">
                                <span className="grid size-8 shrink-0 place-items-center rounded-full bg-rose text-[11px] font-semibold text-rose-ink">
                                    {initials(enrollment.student.name)}
                                </span>
                                <div className="min-w-0 flex-1">
                                    <p className="truncate text-[13px] font-medium">{enrollment.student.name}</p>
                                    <p className="mt-0.5 text-[11px] text-muted">Nº {enrollment.student.number}</p>
                                </div>
                                <GradeInput grade={enrollment.grade} label={`nota de ${enrollment.student.name}`} onSave={(grade) => onSetGrade(enrollment, grade)} />
                                <RemoveButton label={`Anular inscrição de ${enrollment.student.name}`} onClick={() => onUnenroll(enrollment)} />
                            </li>
                        ))}
                    </ul>
                ) : (
                    <p className="py-8 text-center text-sm text-muted">Ainda não há estudantes inscritos nesta UC.</p>
                )}
            </div>

            <div className="mt-auto border-t border-line p-4">
                <p className="mb-3 text-[13px] font-semibold">Inscrever estudante</p>
                <EnrollPicker
                    options={students
                        .filter((student) => !enrolledIds.has(student.id))
                        .sort((a, b) => a.name.localeCompare(b.name, "pt"))
                        .map((student) => ({ value: student.id, label: `${student.number} · ${student.name}` }))}
                    placeholder="Escolher estudante…"
                    emptyText={students.length ? "Todos os estudantes já estão inscritos nesta UC." : "Ainda não há estudantes registados."}
                    buttonLabel="Inscrever"
                    onEnroll={(studentId) => onEnroll(studentId, unit.id)}
                />
            </div>
        </section>
    );
}

// ---------------------------------------------------------------------------

/** "Estudantes" page: students, their enrollments and grades. */
export default function StudentsPage({
    students,
    courses,
    units,
    enrollments,
    isLoading,
    loadError,
    lastUpdated,
    refreshKey,
    tab,
    onTabChange,
    selectedStudentId,
    onSelectStudent,
    selectedUnitId,
    onSelectUnit,
    onNewStudent,
    onEditStudent,
    onEnroll,
    onSetGrade,
    onUnenroll,
    onLoadError,
}) {
    const coursesById = useMemo(() => new Map(courses.map((course) => [course.id, course])), [courses]);
    const enrollmentsByStudent = useMemo(() => {
        const map = new Map();
        enrollments.forEach((enrollment) => map.set(enrollment.studentId, [...(map.get(enrollment.studentId) ?? []), enrollment]));
        return map;
    }, [enrollments]);
    const enrolledCountByUnit = useMemo(() => {
        const map = new Map();
        enrollments.forEach((enrollment) => map.set(enrollment.unitId, (map.get(enrollment.unitId) ?? 0) + 1));
        return map;
    }, [enrollments]);

    const isStudentsTab = tab === "students";
    const selectedStudent = students.find((student) => student.id === selectedStudentId) ?? students[0];
    const selectedUnit = units.find((unit) => unit.id === selectedUnitId) ?? units[0];
    const overallAverage = mean(students.map((student) => student.gradeAverage));
    const gradedCount = enrollments.filter((enrollment) => enrollment.grade !== null).length;

    const studentColumns = [
        {
            header: "Estudante",
            render: (student, isSelected) => (
                <div className="flex items-center gap-3">
                    <span className={cx("grid size-8 shrink-0 place-items-center rounded-full text-[11px] font-semibold", isSelected ? "bg-surface text-rose-ink" : "bg-rose text-rose-ink")}>
                        {initials(student.name)}
                    </span>
                    <div className="min-w-0">
                        <p className="truncate font-semibold">{student.name}</p>
                        <p className="mt-0.5 truncate text-[11px] text-muted">{student.email}</p>
                    </div>
                </div>
            ),
        },
        { header: "Número", className: "w-[15%] text-xs text-muted", render: (student) => student.number },
        { header: "Curso", className: "w-[12%] text-xs text-muted", render: (student) => coursesById.get(student.courseId)?.code ?? "—" },
        { header: "UCs", className: "w-[8%] text-center font-medium", render: (student) => enrollmentsByStudent.get(student.id)?.length ?? 0 },
        { header: "Média", className: "w-[10%] text-center font-semibold", render: (student) => formatGrade(student.gradeAverage) },
        {
            header: "Ações",
            className: "w-[13%]",
            render: (student, isSelected) => (
                <div className="flex items-center justify-between gap-2">
                    <EditPill onClick={() => onEditStudent(student.id)} />
                    {isSelected && <ChevronRightIcon size={15} className="text-muted" />}
                </div>
            ),
        },
    ];

    const unitColumns = [
        {
            header: "UC",
            render: (unit, isSelected) => (
                <div className="flex items-center gap-3">
                    <CodeBadge tone={isSelected ? "rose" : "page"}>{unit.code.slice(0, 4)}</CodeBadge>
                    <div className="min-w-0">
                        <p className="truncate font-semibold">{unit.name}</p>
                        <p className="mt-0.5 text-[11px] text-muted">{unit.code}</p>
                    </div>
                </div>
            ),
        },
        { header: "ECTS", className: "w-[12%] text-center text-xs text-muted", render: (unit) => unit.ects },
        { header: "Inscritos", className: "w-[14%] text-center font-medium", render: (unit) => enrolledCountByUnit.get(unit.id) ?? 0 },
        { header: "", className: "w-[8%]", render: (unit, isSelected) => isSelected && <ChevronRightIcon size={15} className="text-muted" /> },
    ];

    const tabs = [
        { value: "students", label: "Estudantes", count: students.length, tone: "brand" },
        { value: "units", label: "Notas por UC", count: units.length, tone: "page" },
    ];

    return (
        <div className="mx-auto max-w-[1180px]">
            <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
                <div>
                    <h1 className="text-[28px] font-bold tracking-tight">Estudantes</h1>
                    <p className="mt-1 text-sm text-muted">Registe estudantes, inscreva-os nas UCs e lance as notas. A média é ponderada pelos ECTS.</p>
                </div>
                <Button variant="primary" onClick={onNewStudent}>
                    <PlusIcon size={16} /> Novo estudante
                </Button>
            </div>

            <div className="mt-6 grid gap-4 md:grid-cols-3">
                <StatCard label="Estudantes" value={students.length} caption="registados" icon={UsersIcon} tone="sky" />
                <StatCard label="Inscrições em UCs" value={enrollments.length} caption={`${gradedCount} com nota`} icon={BookIcon} tone="brand" />
                <StatCard
                    label="Média geral"
                    value={formatGrade(overallAverage === null ? null : Math.round(overallAverage * 100) / 100)}
                    caption="média das médias"
                    icon={CapIcon}
                    tone="rose"
                />
            </div>

            <div role="tablist" className="mt-8 flex gap-6 border-b border-line">
                {tabs.map((item) => {
                    const active = item.value === tab;
                    return (
                        <button
                            key={item.value}
                            type="button"
                            role="tab"
                            aria-selected={active}
                            onClick={() => onTabChange(item.value)}
                            className={cx(
                                "-mb-px flex items-center gap-2 border-b-2 pb-3 text-sm",
                                active ? "border-brand font-medium text-brand" : "border-transparent text-muted hover:text-ink"
                            )}
                        >
                            {item.label} <CountPill tone={item.tone}>{item.count}</CountPill>
                        </button>
                    );
                })}
            </div>

            <div className="mt-4">
                <ErrorBanner message={loadError} />
            </div>

            {isLoading ? (
                <p className="py-16 text-center text-sm text-muted">A carregar estudantes…</p>
            ) : isStudentsTab ? (
                <div className="mt-2 grid items-stretch gap-4 xl:grid-cols-[minmax(0,1.4fr)_minmax(0,1fr)]">
                    <DataTable
                        title="Estudantes"
                        count={students.length}
                        pluralNoun="estudantes"
                        searchPlaceholder="Pesquisar por nome, número ou email..."
                        emptyText="Ainda não há estudantes. Crie o primeiro com “Novo estudante”."
                        rows={students}
                        columns={studentColumns}
                        searchFields={(student) => [student.name, student.number, student.email]}
                        selectedId={selectedStudent?.id}
                        onSelect={onSelectStudent}
                    />
                    <StudentPanel
                        student={selectedStudent}
                        course={coursesById.get(selectedStudent?.courseId)}
                        enrollments={enrollmentsByStudent.get(selectedStudent?.id) ?? []}
                        units={units}
                        onEdit={() => onEditStudent(selectedStudent.id)}
                        onEnroll={onEnroll}
                        onSetGrade={onSetGrade}
                        onUnenroll={onUnenroll}
                    />
                </div>
            ) : (
                <div className="mt-2 grid items-stretch gap-4 xl:grid-cols-[minmax(0,1.2fr)_minmax(0,1fr)]">
                    <DataTable
                        title="UCs"
                        count={units.length}
                        pluralNoun="UCs"
                        searchPlaceholder="Pesquisar UC por nome ou código..."
                        emptyText="Ainda não há UCs. Crie-as em “Cursos e UCs”."
                        rows={units}
                        columns={unitColumns}
                        searchFields={(unit) => [unit.name, unit.code]}
                        selectedId={selectedUnit?.id}
                        onSelect={onSelectUnit}
                    />
                    <UnitPanel
                        unit={selectedUnit}
                        students={students}
                        refreshKey={refreshKey}
                        onEnroll={onEnroll}
                        onSetGrade={onSetGrade}
                        onUnenroll={onUnenroll}
                        onLoadError={onLoadError}
                    />
                </div>
            )}

            <div className="mt-5 flex flex-col gap-2 text-xs text-muted sm:flex-row sm:items-center sm:justify-between">
                <p className="flex items-center gap-2">
                    <InfoIcon size={14} />
                    {isStudentsTab
                        ? "Selecione um estudante para o inscrever em UCs e registar as notas."
                        : "Selecione uma UC para lançar as notas de todos os inscritos."}
                </p>
                {lastUpdated && <p className="text-subtle">Atualizado hoje, às {formatTime(lastUpdated)}</p>}
            </div>
        </div>
    );
}
