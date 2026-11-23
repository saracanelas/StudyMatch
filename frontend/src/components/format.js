/** Normalises text for case- and accent-insensitive search. */
const normalize = (text) => String(text ?? "").normalize("NFD").replace(/\p{Diacritic}/gu, "").toLowerCase();

/** True if any of the given texts contains the query (ignoring case and accents). */
export function matchesQuery(query, ...texts) {
    const q = normalize(query.trim());
    return !q || texts.some((text) => normalize(text).includes(q));
}

/** Formats a grade or average with up to two decimals ("15,5"), or "—" when there is none. */
export function formatGrade(value) {
    if (value === null || value === undefined) return "—";
    return value.toLocaleString("pt-PT", { maximumFractionDigits: 2 });
}

/** Initials of a person's name: "Ana Costa" -> "AC". */
export function initials(name = "") {
    const parts = name.trim().split(/\s+/).filter(Boolean);
    if (parts.length === 0) return "?";
    return ((parts[0][0] ?? "") + (parts.length > 1 ? parts[parts.length - 1][0] : "")).toUpperCase();
}
