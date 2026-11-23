import { useMemo, useState } from "react";
import { Button, CodeBadge, CountPill, ErrorBanner, SearchInput } from "../components/ui";
import { cx } from "../components/cx";
import { EditPill, Menu, Pagination, StatCard } from "../components/widgets";
import {
    ArrowRightIcon,
    BookIcon,
    CapIcon,
    ChevronDownIcon,
    ChevronRightIcon,
    DotsIcon,
    InfoIcon,
    LinkIcon,
    NewFileIcon,
    PencilIcon,
    PlusIcon,
    SlidersIcon,
} from "../components/Icons";

const PAGE_SIZE = 6;
const PREVIEW_SIZE = 4;

const SORT_OPTIONS = [
    { value: "newest", label: "Mais recentes primeiro" },
    { value: "oldest", label: "Mais antigos primeiro" },
    { value: "name", label: "Nome (A–Z)" },
];

/** Case- and accent-insensitive "contains" on name or code. */
function matches(item, query) {
    const normalize = (text) => text.normalize("NFD").replace(/\p{Diacritic}/gu, "").toLowerCase();
    const q = normalize(query.trim());
    return !q || normalize(item.name).includes(q) || normalize(item.code).includes(q);
}

function sortItems(items, sort) {
    const sorted = [...items];
    if (sort === "name") return sorted.sort((a, b) => a.name.localeCompare(b.name, "pt"));
    return sorted.sort((a, b) => (sort === "newest" ? b.id - a.id : a.id - b.id));
}

const formatTime = (date) => date?.toLocaleTimeString("pt-PT", { hour: "2-digit", minute: "2-digit" });

// ---------------------------------------------------------------------------

/**
 * Searchable, sortable, paginated table of courses or curricular units.
 * `rows` are { id, name, code, count }; clicking a row selects it.
 */
function CatalogTable({ title, count, countLabel, searchPlaceholder, emptyText, rows, selectedId, onSelect, onEdit, filterOptions, itemNoun, pluralNoun }) {
    const [query, setQuery] = useState("");
    const [sort, setSort] = useState("newest");
    const [filter, setFilter] = useState(filterOptions[0].value);
    const [page, setPage] = useState(1);

    const filterFn = filterOptions.find((option) => option.value === filter).test;
    const visibleRows = sortItems(rows.filter((row) => matches(row, query) && filterFn(row)), sort);
    const pageCount = Math.max(1, Math.ceil(visibleRows.length / PAGE_SIZE));
    const currentPage = Math.min(page, pageCount);
    const pageRows = visibleRows.slice((currentPage - 1) * PAGE_SIZE, currentPage * PAGE_SIZE);
    const firstShown = visibleRows.length === 0 ? 0 : (currentPage - 1) * PAGE_SIZE + 1;
    const lastShown = (currentPage - 1) * PAGE_SIZE + pageRows.length;

    const resetPage = (setter) => (value) => {
        setter(value);
        setPage(1);
    };

    return (
        <section className="flex flex-col overflow-hidden rounded-xl border border-line bg-surface">
            <div className="px-4 pt-4 pb-4">
                <div className="flex items-center justify-between gap-3">
                    <h2 className="flex items-center gap-2.5 text-base font-semibold">
                        {title} <CountPill tone="sky">{count}</CountPill>
                    </h2>
                    <Menu
                        value={sort}
                        onChange={resetPage(setSort)}
                        options={SORT_OPTIONS}
                        trigger={({ toggle }) => (
                            <button type="button" onClick={toggle} className="text-xs text-muted hover:text-ink">
                                {SORT_OPTIONS.find((option) => option.value === sort).label}
                            </button>
                        )}
                    />
                </div>
                <div className="mt-4 flex gap-2">
                    <SearchInput
                        className="flex-1"
                        placeholder={searchPlaceholder}
                        value={query}
                        onChange={(event) => resetPage(setQuery)(event.target.value)}
                        aria-label={searchPlaceholder}
                    />
                    <Menu
                        value={filter}
                        onChange={resetPage(setFilter)}
                        options={filterOptions}
                        trigger={({ toggle }) => (
                            <button
                                type="button"
                                onClick={toggle}
                                className={cx(
                                    "flex h-11 items-center gap-2 rounded-lg border px-3 text-[13px] hover:bg-page",
                                    filter === filterOptions[0].value ? "border-line text-ink" : "border-brand text-brand"
                                )}
                            >
                                <SlidersIcon size={16} />
                                Filtros
                            </button>
                        )}
                    />
                </div>
            </div>

            <div className="overflow-x-auto">
                <table className="w-full min-w-[520px] text-left">
                    <thead>
                        <tr className="border-y border-line text-[10px] font-medium tracking-wide text-muted uppercase">
                            <th className="py-3 pl-4 font-medium">{itemNoun}</th>
                            <th className="w-[18%] font-medium">Código</th>
                            <th className="w-[10%] text-center font-medium">{countLabel}</th>
                            <th className="w-[18%] pr-4 font-medium">Ações</th>
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
                                    <td className="py-3.5 pl-4">
                                        <div className="flex items-center gap-3">
                                            <CodeBadge tone={isSelected ? "rose" : "page"}>{row.code.slice(0, 4)}</CodeBadge>
                                            <div className="min-w-0">
                                                <p className="truncate font-semibold">{row.name}</p>
                                                <p className="mt-0.5 text-[11px] text-muted">{row.code}</p>
                                            </div>
                                        </div>
                                    </td>
                                    <td className="text-xs text-muted">{row.code}</td>
                                    <td className="text-center font-medium">{row.count}</td>
                                    <td className="pr-4">
                                        <div className="flex items-center justify-between gap-2">
                                            <EditPill onClick={() => onEdit(row.id)} />
                                            {isSelected ? (
                                                <ChevronRightIcon size={15} className="text-muted" />
                                            ) : (
                                                <DotsIcon size={15} className="text-muted" />
                                            )}
                                        </div>
                                    </td>
                                </tr>
                            );
                        })}
                    </tbody>
                </table>
                {pageRows.length === 0 && <p className="px-4 py-10 text-center text-sm text-muted">{emptyText}</p>}
            </div>

            <div className="mt-auto flex items-center justify-between border-t border-line px-4 py-3.5 text-xs text-muted">
                <span>
                    A mostrar {firstShown}–{lastShown} de {visibleRows.length} {pluralNoun}
                </span>
                <Pagination page={currentPage} pageCount={pageCount} onChange={setPage} />
            </div>
        </section>
    );
}

/** One line of a related-items list in the detail panel. */
function RelatedRow({ name, code, shared, onEdit }) {
    return (
        <li className="flex items-center gap-3 border-b border-line py-3.5 last:border-b-0">
            <span className="grid size-8 shrink-0 place-items-center rounded-md bg-page text-muted">
                <BookIcon size={16} />
            </span>
            <div className="min-w-0 flex-1">
                <p className="flex items-center gap-1.5 truncate text-[13px] font-medium">
                    {name}
                    {shared && <LinkIcon size={13} className="text-brand" aria-label="Partilhada" />}
                </p>
                <p className="mt-0.5 text-[11px] text-muted">{code}</p>
            </div>
            <button type="button" onClick={onEdit} className="text-xs text-muted hover:text-ink">
                Editar
            </button>
            <DotsIcon size={15} className="text-muted" />
        </li>
    );
}

/** Right-hand panel: details of the selected course or curricular unit. */
function DetailPanel({ item, kind, related, sharedIds, onEdit, onEditRelated, onPrimary, onSecondary }) {
    const [expanded, setExpanded] = useState(false);
    const [relatedFilter, setRelatedFilter] = useState("all");
    const isCourse = kind === "course";

    if (!item) {
        return (
            <section className="grid min-h-80 place-items-center rounded-xl border border-dashed border-line bg-surface p-8 text-center">
                <div>
                    <span className="mx-auto grid size-11 place-items-center rounded-xl bg-brand-soft text-brand">
                        {isCourse ? <CapIcon size={20} /> : <BookIcon size={20} />}
                    </span>
                    <p className="mt-4 font-semibold">{isCourse ? "Nenhum curso selecionado" : "Nenhuma UC selecionada"}</p>
                    <p className="mt-1 text-sm text-muted">Selecione um item da lista para ver os detalhes.</p>
                </div>
            </section>
        );
    }

    const filtered = relatedFilter === "shared" ? related.filter((entry) => sharedIds.has(entry.id)) : related;
    const shown = expanded ? filtered : filtered.slice(0, PREVIEW_SIZE);
    const sharedCount = related.filter((entry) => sharedIds.has(entry.id)).length;
    const relatedLabel = isCourse ? "UCs" : "cursos";

    return (
        <section className="flex flex-col rounded-xl border border-line bg-surface">
            <div className="border-b border-line p-4 pb-5">
                <div className="flex items-start justify-between">
                    <div className="flex items-center gap-2.5 pt-1.5 text-xs">
                        <span className="font-medium text-muted">{item.code}</span>
                        <span className="rounded-md bg-mint px-2 py-0.5 font-medium text-mint-ink">{isCourse ? "Curso" : "UC"}</span>
                    </div>
                    <Button size="sm" onClick={onEdit}>
                        <PencilIcon size={15} /> Editar
                    </Button>
                </div>
                <h2 className="mt-4 text-xl font-semibold break-words">{item.name}</h2>
                <p className="mt-1 text-xs text-muted">Código {item.code}{!isCourse && ` · ${item.ects} ECTS`}</p>
                <p className="mt-4 flex items-center gap-2 text-xs text-muted">
                    <LinkIcon size={14} className="text-brand" />
                    {related.length} {isCourse ? "UCs associadas" : related.length === 1 ? "curso associado" : "cursos associados"}
                    {isCourse && (
                        <>
                            <span>·</span> {sharedCount} partilhadas com outros cursos
                        </>
                    )}
                </p>
            </div>

            <div className="px-4 pt-4">
                <div className="flex items-center justify-between">
                    <h3 className="text-sm font-semibold">{isCourse ? "UCs associadas" : "Cursos associados"}</h3>
                    {isCourse && (
                        <Menu
                            value={relatedFilter}
                            onChange={setRelatedFilter}
                            options={[
                                { value: "all", label: "Todas" },
                                { value: "shared", label: "Partilhadas" },
                            ]}
                            trigger={({ toggle }) => (
                                <button type="button" onClick={toggle} className="flex items-center gap-1 text-xs text-muted hover:text-ink">
                                    {relatedFilter === "all" ? "Todas" : "Partilhadas"} <ChevronDownIcon size={13} />
                                </button>
                            )}
                        />
                    )}
                </div>
                {shown.length > 0 ? (
                    <ul className="mt-2">
                        {shown.map((entry) => (
                            <RelatedRow
                                key={entry.id}
                                name={entry.name}
                                code={entry.code}
                                shared={sharedIds.has(entry.id)}
                                onEdit={() => onEditRelated(entry.id)}
                            />
                        ))}
                    </ul>
                ) : (
                    <p className="py-8 text-center text-sm text-muted">
                        {isCourse ? "Este curso ainda não tem UCs associadas." : "Esta UC ainda não está associada a nenhum curso."}
                    </p>
                )}
                {filtered.length > PREVIEW_SIZE && (
                    <button
                        type="button"
                        onClick={() => setExpanded((value) => !value)}
                        className="mx-auto flex items-center gap-1.5 border-t border-line py-3.5 text-xs font-medium text-brand w-full justify-center"
                    >
                        {expanded ? "Mostrar menos" : `Ver todas as ${filtered.length} ${relatedLabel}`}
                        <ArrowRightIcon size={13} className={cx(expanded && "-rotate-90")} />
                    </button>
                )}
            </div>

            <div className="mt-auto border-t border-line p-4">
                <div className="flex items-start gap-3">
                    <span className="grid size-8 shrink-0 place-items-center rounded-md bg-rose text-rose-ink">
                        <LinkIcon size={16} />
                    </span>
                    <div>
                        <p className="text-[13px] font-semibold">{isCourse ? "Associe ou desassocie UCs" : "Associe ou desassocie cursos"}</p>
                        <p className="mt-0.5 text-[11px] text-muted">
                            {isCourse
                                ? "Associe UCs a este curso ou desassocie-as sem as eliminar do catálogo."
                                : "Associe esta UC a cursos ou desassocie-a sem a eliminar do catálogo."}
                        </p>
                    </div>
                </div>
                <div className="mt-3 flex flex-wrap gap-2">
                    <Button variant="primary" size="sm" onClick={onPrimary}>
                        <PlusIcon size={15} /> {isCourse ? "Associar UCs" : "Associar cursos"}
                    </Button>
                    <Button size="sm" onClick={onSecondary}>
                        <NewFileIcon size={15} /> {isCourse ? "Criar nova UC" : "Criar novo curso"}
                    </Button>
                </div>
            </div>
        </section>
    );
}

// ---------------------------------------------------------------------------

/** "Cursos e UCs" overview: stats, tabs, catalogue table and detail panel. */
export default function CatalogPage({
    courses,
    units,
    isLoading,
    loadError,
    lastUpdated,
    tab,
    onTabChange,
    selectedCourseId,
    onSelectCourse,
    selectedUnitId,
    onSelectUnit,
    onNewCourse,
    onEditCourse,
    onNewUnit,
    onEditUnit,
}) {
    // unit id -> courses that contain it (the units endpoint does not return their courses)
    const coursesByUnit = useMemo(() => {
        const map = new Map(units.map((unit) => [unit.id, []]));
        courses.forEach((course) => course.unitIds.forEach((unitId) => map.get(unitId)?.push(course)));
        return map;
    }, [courses, units]);

    const sharedUnitIds = useMemo(
        () => new Set([...coursesByUnit].filter(([, list]) => list.length > 1).map(([id]) => id)),
        [coursesByUnit]
    );
    const orphanUnits = units.filter((unit) => (coursesByUnit.get(unit.id) ?? []).length === 0);

    const courseRows = courses.map((course) => ({ ...course, count: course.unitIds.length }));
    const unitRows = units.map((unit) => ({ ...unit, count: coursesByUnit.get(unit.id)?.length ?? 0 }));

    const isCoursesTab = tab === "courses";
    const tableRows = isCoursesTab ? courseRows : tab === "units" ? unitRows : unitRows.filter((row) => row.count === 0);

    // Default selection: the first course / unit in the list
    const selectedCourse = courses.find((course) => course.id === selectedCourseId) ?? courses[0];
    const unitPool = tab === "orphans" ? orphanUnits : units;
    const selectedUnit = unitPool.find((unit) => unit.id === selectedUnitId) ?? unitPool[0];

    const unitsById = new Map(units.map((unit) => [unit.id, unit]));
    const courseUnits = selectedCourse ? selectedCourse.unitIds.map((id) => unitsById.get(id)).filter(Boolean) : [];
    const unitCourses = selectedUnit ? coursesByUnit.get(selectedUnit.id) ?? [] : [];

    const tabs = [
        { value: "courses", label: "Cursos", count: courses.length, tone: "brand" },
        { value: "units", label: "UCs", count: units.length, tone: "page" },
        { value: "orphans", label: "Sem curso", count: orphanUnits.length, tone: "rose" },
    ];

    return (
        <div className="mx-auto max-w-[1180px]">
            <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
                <div>
                    <h1 className="text-[28px] font-bold tracking-tight">Cursos e UCs</h1>
                    <p className="mt-1 text-sm text-muted">Crie, edite e elimine cursos e UCs. Associe UCs a cursos e gere o catálogo.</p>
                </div>
                <div className="flex gap-2.5">
                    <Button onClick={onNewUnit} className="shadow-[0_1px_2px_rgba(37,34,47,0.05)]">
                        <PlusIcon size={16} /> Nova UC
                    </Button>
                    <Button variant="primary" onClick={onNewCourse}>
                        <PlusIcon size={16} /> Novo curso
                    </Button>
                </div>
            </div>

            <div className="mt-6 grid gap-4 md:grid-cols-3">
                <StatCard label="Cursos" value={courses.length} caption={`${courses.length} cursos no catálogo`} icon={CapIcon} tone="sky" />
                <StatCard label="UCs" value={units.length} caption="no catálogo" icon={BookIcon} tone="brand" />
                <StatCard label="UCs sem curso" value={orphanUnits.length} caption="podem ser associadas" icon={LinkIcon} tone="rose" />
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
                <p className="py-16 text-center text-sm text-muted">A carregar o catálogo…</p>
            ) : (
                <div className="mt-2 grid items-stretch gap-4 xl:grid-cols-[minmax(0,1.4fr)_minmax(0,1fr)]">
                    {isCoursesTab ? (
                        <CatalogTable
                            key="courses"
                            title="Cursos"
                            itemNoun="Curso"
                            pluralNoun="cursos"
                            count={courses.length}
                            countLabel="UCs"
                            searchPlaceholder="Pesquisar curso por nome ou código..."
                            emptyText={courses.length ? "Nenhum curso corresponde à pesquisa." : "Ainda não há cursos. Crie o primeiro com “Novo curso”."}
                            rows={tableRows}
                            selectedId={selectedCourse?.id}
                            onSelect={onSelectCourse}
                            onEdit={onEditCourse}
                            filterOptions={[
                                { value: "all", label: "Todos os cursos", test: () => true },
                                { value: "with", label: "Com UCs", test: (row) => row.count > 0 },
                                { value: "without", label: "Sem UCs", test: (row) => row.count === 0 },
                            ]}
                        />
                    ) : (
                        <CatalogTable
                            key={tab}
                            title={tab === "units" ? "UCs" : "UCs sem curso"}
                            itemNoun="UC"
                            pluralNoun="UCs"
                            count={tableRows.length}
                            countLabel="Cursos"
                            searchPlaceholder="Pesquisar UC por nome ou código..."
                            emptyText={
                                tab === "orphans"
                                    ? "Todas as UCs estão associadas a pelo menos um curso."
                                    : units.length
                                      ? "Nenhuma UC corresponde à pesquisa."
                                      : "Ainda não há UCs. Crie a primeira com “Nova UC”."
                            }
                            rows={tableRows}
                            selectedId={selectedUnit?.id}
                            onSelect={onSelectUnit}
                            onEdit={onEditUnit}
                            filterOptions={[
                                { value: "all", label: "Todas as UCs", test: () => true },
                                { value: "shared", label: "Partilhadas (2+ cursos)", test: (row) => row.count > 1 },
                                { value: "single", label: "Num só curso", test: (row) => row.count === 1 },
                            ]}
                        />
                    )}

                    {isCoursesTab ? (
                        <DetailPanel
                            key={`course-${selectedCourse?.id}`}
                            kind="course"
                            item={selectedCourse}
                            related={courseUnits}
                            sharedIds={sharedUnitIds}
                            onEdit={() => onEditCourse(selectedCourse.id)}
                            onEditRelated={onEditUnit}
                            onPrimary={() => onEditCourse(selectedCourse.id)}
                            onSecondary={onNewUnit}
                        />
                    ) : (
                        <DetailPanel
                            key={`unit-${selectedUnit?.id}`}
                            kind="unit"
                            item={selectedUnit}
                            related={unitCourses}
                            sharedIds={new Set()}
                            onEdit={() => onEditUnit(selectedUnit.id)}
                            onEditRelated={onEditCourse}
                            onPrimary={() => onEditUnit(selectedUnit.id)}
                            onSecondary={onNewCourse}
                        />
                    )}
                </div>
            )}

            <div className="mt-5 flex flex-col gap-2 text-xs text-muted sm:flex-row sm:items-center sm:justify-between">
                <p className="flex items-center gap-2">
                    <InfoIcon size={14} />
                    {isCoursesTab
                        ? "Selecione um curso para consultar, editar, associar, desassociar ou eliminar UCs."
                        : "Selecione uma UC para consultar, editar ou gerir os cursos a que pertence."}
                </p>
                {lastUpdated && <p className="text-subtle">Atualizado hoje, às {formatTime(lastUpdated)}</p>}
            </div>
        </div>
    );
}
