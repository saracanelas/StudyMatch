import { useEffect, useRef, useState } from "react";
import { cx } from "./cx";
import { ChevronLeftIcon, ChevronRightIcon } from "./Icons";

export function StatCard({ label, value, caption, icon: StatIcon, tone }) {
    const tones = { sky: "bg-sky text-sky-ink", brand: "bg-brand-soft text-brand", rose: "bg-rose text-rose-ink" };
    return (
        <div className="flex items-center justify-between rounded-xl border border-line bg-surface px-4 py-5">
            <div>
                <p className="text-[13px] text-muted">{label}</p>
                <p className="mt-1.5 flex items-baseline gap-3">
                    <span className="text-[28px] leading-none font-semibold">{value}</span>
                    <span className="text-xs text-muted">{caption}</span>
                </p>
            </div>
            <span className={cx("grid size-9 place-items-center rounded-lg", tones[tone])}>
                <StatIcon size={18} />
            </span>
        </div>
    );
}

/** Small drop-down menu anchored to a trigger button. */
export function Menu({ trigger, options, value, onChange, align = "right" }) {
    const [open, setOpen] = useState(false);
    const ref = useRef(null);

    useEffect(() => {
        if (!open) return;
        const close = (event) => !ref.current?.contains(event.target) && setOpen(false);
        document.addEventListener("mousedown", close);
        return () => document.removeEventListener("mousedown", close);
    }, [open]);

    return (
        <div ref={ref} className="relative">
            {trigger({ open, toggle: () => setOpen((isOpen) => !isOpen) })}
            {open && (
                <ul
                    role="menu"
                    className={cx(
                        "absolute z-20 mt-1.5 min-w-48 rounded-lg border border-line bg-surface p-1 shadow-lg shadow-ink/5",
                        align === "right" ? "right-0" : "left-0"
                    )}
                >
                    {options.map((option) => (
                        <li key={option.value}>
                            <button
                                type="button"
                                role="menuitemradio"
                                aria-checked={option.value === value}
                                onClick={() => {
                                    onChange(option.value);
                                    setOpen(false);
                                }}
                                className={cx(
                                    "w-full rounded-md px-3 py-2 text-left text-[13px] hover:bg-page",
                                    option.value === value ? "font-medium text-brand" : "text-ink"
                                )}
                            >
                                {option.label}
                            </button>
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
}

export function Pagination({ page, pageCount, onChange }) {
    if (pageCount <= 1) return null;
    return (
        <div className="flex items-center gap-1 text-[13px]">
            <button type="button" disabled={page === 1} onClick={() => onChange(page - 1)} aria-label="Página anterior" className="grid size-7 place-items-center rounded-md text-muted disabled:opacity-40">
                <ChevronLeftIcon size={15} />
            </button>
            {Array.from({ length: pageCount }, (_, i) => i + 1).map((number) => (
                <button
                    key={number}
                    type="button"
                    onClick={() => onChange(number)}
                    aria-current={number === page ? "page" : undefined}
                    className={cx("grid size-7 place-items-center rounded-md", number === page ? "bg-sky font-medium text-ink" : "text-muted hover:bg-page")}
                >
                    {number}
                </button>
            ))}
            <button type="button" disabled={page === pageCount} onClick={() => onChange(page + 1)} aria-label="Página seguinte" className="grid size-7 place-items-center rounded-md text-muted disabled:opacity-40">
                <ChevronRightIcon size={15} />
            </button>
        </div>
    );
}

/** "Editar" status pill used in the table and in the detail panel. */
export function EditPill({ onClick }) {
    return (
        <button
            type="button"
            onClick={(event) => {
                event.stopPropagation();
                onClick();
            }}
            className="rounded-md bg-mint px-2 py-1 text-xs font-medium text-mint-ink hover:brightness-95"
        >
            Editar
        </button>
    );
}
