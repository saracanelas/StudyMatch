import { useRef, useState } from "react";
import { Button, Checkbox, SearchInput } from "./ui";
import { cx } from "./cx";
import { ArrowRightIcon, PlusIcon } from "./Icons";

const PREVIEW_SIZE = 4;

/** Case- and accent-insensitive "contains" on name or code. */
function matches(item, query) {
    const normalize = (text) => text.normalize("NFD").replace(/\p{Diacritic}/gu, "").toLowerCase();
    const q = normalize(query.trim());
    return !q || normalize(item.name).includes(q) || normalize(item.code).includes(q);
}

function ItemText({ item }) {
    return (
        <div className="min-w-0 flex-1">
            <p className="truncate text-[15px] font-medium">{item.name}</p>
            <p className="mt-0.5 text-xs text-muted">{item.code}</p>
        </div>
    );
}

/**
 * Picks the items (UCs or courses) associated with the record being created / edited.
 *
 * - "create": a searchable checklist of the whole catalogue.
 * - "edit": a search box to associate more items, plus the list of associated items
 *   (checkbox + "Desassociar"); `variant` "list" draws one card, "cards" one card per item.
 */
export default function AssociationPicker({
    mode,
    variant = "list",
    items,
    initialIds = [],
    selectedIds,
    onChange,
    searchPlaceholder,
    catalogTitle,
    associatedTitle,
    noun,
    emptyCatalogText,
}) {
    const [query, setQuery] = useState("");
    const [expanded, setExpanded] = useState(false);
    // Items shown in the "associated" list: the original ones plus any added in this session
    const [trackedIds, setTrackedIds] = useState(initialIds);
    const searchRef = useRef(null);

    const isSelected = (id) => selectedIds.includes(id);
    const toggle = (id) => onChange(isSelected(id) ? selectedIds.filter((x) => x !== id) : [...selectedIds, id]);
    const associate = (id) => {
        if (!trackedIds.includes(id)) setTrackedIds((ids) => [...ids, id]);
        if (!isSelected(id)) onChange([...selectedIds, id]);
        setQuery("");
    };

    if (items.length === 0) {
        return <p className="rounded-xl border border-dashed border-line bg-field px-4 py-8 text-center text-sm text-muted">{emptyCatalogText}</p>;
    }

    if (mode === "create") {
        const visible = items.filter((item) => matches(item, query));
        return (
            <div className="space-y-4">
                <SearchInput placeholder={searchPlaceholder} value={query} onChange={(e) => setQuery(e.target.value)} aria-label={searchPlaceholder} />
                <div className="overflow-hidden rounded-xl border border-line bg-field">
                    <p className="px-4 pt-4 pb-2 text-[11px] font-medium tracking-wide text-muted uppercase">{catalogTitle}</p>
                    <ul className="max-h-[340px] overflow-y-auto">
                        {visible.map((item) => (
                            <li key={item.id} className="border-b border-line last:border-b-0">
                                <label className="flex cursor-pointer items-center gap-4 px-4 py-4 hover:bg-page/60">
                                    <Checkbox checked={isSelected(item.id)} onChange={() => toggle(item.id)} label={item.name} />
                                    <ItemText item={item} />
                                </label>
                            </li>
                        ))}
                    </ul>
                    {visible.length === 0 && <p className="px-4 pb-5 text-sm text-muted">Nenhum resultado para “{query}”.</p>}
                </div>
            </div>
        );
    }

    // ---- edit mode ----
    const candidates = query.trim() ? items.filter((item) => !trackedIds.includes(item.id) && matches(item, query)).slice(0, 6) : [];
    const tracked = trackedIds.map((id) => items.find((item) => item.id === id)).filter(Boolean);
    const shown = expanded ? tracked : tracked.slice(0, PREVIEW_SIZE);

    const unlinkButton = (item) =>
        isSelected(item.id) ? (
            variant === "cards" ? (
                <Button size="sm" onClick={() => toggle(item.id)}>Desassociar</Button>
            ) : (
                <button type="button" onClick={() => toggle(item.id)} className="text-[13px] font-semibold text-brand hover:text-brand-dark">
                    Desassociar
                </button>
            )
        ) : (
            <button type="button" onClick={() => toggle(item.id)} className="text-[13px] font-semibold text-muted hover:text-ink">
                Voltar a associar
            </button>
        );

    return (
        <div className="space-y-5">
            <div className="relative">
                <SearchInput
                    ref={searchRef}
                    placeholder={searchPlaceholder}
                    value={query}
                    onChange={(e) => setQuery(e.target.value)}
                    aria-label={searchPlaceholder}
                />
                {query.trim() && (
                    <ul className="absolute inset-x-0 top-full z-10 mt-1.5 overflow-hidden rounded-xl border border-line bg-surface shadow-lg shadow-ink/5">
                        {candidates.map((item) => (
                            <li key={item.id} className="flex items-center gap-3 border-b border-line px-4 py-3 last:border-b-0">
                                <ItemText item={item} />
                                <Button size="sm" onClick={() => associate(item.id)}>
                                    <PlusIcon size={14} /> Associar
                                </Button>
                            </li>
                        ))}
                        {candidates.length === 0 && <li className="px-4 py-4 text-sm text-muted">Nenhum{noun.gender === "f" ? "a" : ""} {noun.singular} por associar corresponde à pesquisa.</li>}
                    </ul>
                )}
            </div>

            {tracked.length === 0 ? (
                <p className="rounded-xl border border-dashed border-line bg-field px-4 py-8 text-center text-sm text-muted">
                    Sem {noun.plural} associad{noun.gender === "f" ? "a" : "o"}s. Pesquise acima para associar.
                </p>
            ) : variant === "cards" ? (
                <ul className="space-y-4">
                    {tracked.map((item) => (
                        <li key={item.id} className="flex items-center gap-4 rounded-xl border border-line bg-field px-4 py-4">
                            <Checkbox checked={isSelected(item.id)} onChange={() => toggle(item.id)} label={item.name} />
                            <ItemText item={item} />
                            {unlinkButton(item)}
                        </li>
                    ))}
                </ul>
            ) : (
                <div className="overflow-hidden rounded-xl border border-line bg-field">
                    <div className="flex items-center justify-between px-4 pt-4 pb-1">
                        <p className="text-[13px] font-medium">{associatedTitle}</p>
                        <button
                            type="button"
                            onClick={() => {
                                setExpanded(true);
                                searchRef.current?.focus();
                            }}
                            className="text-[13px] font-semibold text-brand hover:text-brand-dark"
                        >
                            Gerir associações
                        </button>
                    </div>
                    <ul>
                        {shown.map((item) => (
                            <li key={item.id} className={cx("flex items-center gap-4 border-b border-line px-4 py-4", !isSelected(item.id) && "opacity-70")}>
                                <Checkbox checked={isSelected(item.id)} onChange={() => toggle(item.id)} label={item.name} />
                                <ItemText item={item} />
                                {unlinkButton(item)}
                            </li>
                        ))}
                    </ul>
                    <div className="flex items-center justify-between px-4 py-3.5 text-[13px]">
                        <span className="text-muted">
                            A mostrar {shown.length} de {tracked.length} {noun.plural}
                        </span>
                        {tracked.length > PREVIEW_SIZE && (
                            <button type="button" onClick={() => setExpanded((v) => !v)} className="flex items-center gap-1 font-semibold text-brand hover:text-brand-dark">
                                {expanded ? "Mostrar menos" : noun.gender === "f" ? `Ver todas as ${tracked.length} ${noun.plural}` : `Ver todos os ${tracked.length} ${noun.plural}`}
                                <ArrowRightIcon size={14} className={cx(expanded && "-rotate-90")} />
                            </button>
                        )}
                    </div>
                </div>
            )}
        </div>
    );
}
