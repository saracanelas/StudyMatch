import { CheckIcon, CloseIcon, InfoIcon, LogoIcon, SearchIcon, ChevronRightIcon } from "./Icons";
import { cx } from "./cx";


const BUTTON_VARIANTS = {
    primary: "bg-brand text-white hover:bg-brand-dark disabled:bg-brand/60",
    secondary: "border border-line bg-surface text-ink hover:bg-page",
    danger: "border border-brand bg-surface text-brand hover:bg-brand-soft disabled:border-line disabled:text-subtle disabled:hover:bg-surface",
    ghost: "text-muted hover:bg-page",
};

/** Button in one of the design variants: primary, secondary, danger (outlined) or ghost. */
export function Button({ variant = "secondary", size = "md", className, children, ...props }) {
    const sizes = { sm: "h-9 px-3 text-sm", md: "h-10 px-4 text-sm", lg: "h-12 px-5 text-[15px]" };
    return (
        <button
            type="button"
            {...props}
            className={cx(
                "inline-flex items-center justify-center gap-2 rounded-lg font-semibold transition-colors disabled:cursor-not-allowed",
                sizes[size],
                BUTTON_VARIANTS[variant],
                className
            )}
        >
            {children}
        </button>
    );
}

/** Square brand logo followed by the "StudyMatch" word mark. */
export function Brand({ size = "md" }) {
    const box = size === "lg" ? "size-9" : "size-8";
    return (
        <div className="flex items-center gap-3">
            <span className={cx("grid place-items-center rounded-lg bg-brand text-white", box)}>
                <LogoIcon size={18} />
            </span>
            <span className={cx("font-bold tracking-tight", size === "lg" ? "text-2xl" : "text-xl")}>StudyMatch</span>
        </div>
    );
}

/** Labelled input; `hint` is shown under it. Extra props go to the <input>. */
export function Field({ label, required, hint, ...inputProps }) {
    return (
        <label className="block">
            <span className="mb-2 block text-sm font-medium text-ink">
                {label}
                {required && " *"}
            </span>
            <input
                {...inputProps}
                required={required}
                className="h-12 w-full rounded-lg border border-line bg-field px-3.5 text-[15px] outline-none transition focus:border-brand focus:ring-3 focus:ring-brand/15"
            />
            {hint && <span className="mt-2 block text-xs text-muted">{hint}</span>}
        </label>
    );
}

/** Labelled drop-down; the <option>s are its children. Extra props go to the <select>. */
export function SelectField({ label, hint, children, ...selectProps }) {
    return (
        <label className="block">
            <span className="mb-2 block text-sm font-medium text-ink">{label}</span>
            <select
                {...selectProps}
                className="h-12 w-full rounded-lg border border-line bg-field px-3 text-[15px] outline-none transition focus:border-brand focus:ring-3 focus:ring-brand/15"
            >
                {children}
            </select>
            {hint && <span className="mt-2 block text-xs text-muted">{hint}</span>}
        </label>
    );
}

/** White rounded card used to group the content of a page. */
export function Card({ className, children }) {
    return <section className={cx("rounded-xl border border-line bg-surface p-5", className)}>{children}</section>;
}

/** Page heading with title, description and optional actions on the right. */
export function PageHeader({ title, description, children }) {
    return (
        <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
            <div>
                <h1 className="text-[28px] font-bold tracking-tight">{title}</h1>
                <p className="mt-1 text-sm text-muted">{description}</p>
            </div>
            {children && <div className="flex gap-2.5">{children}</div>}
        </div>
    );
}

/** Text input with a search icon. */
export function SearchInput({ className, ...props }) {
    return (
        <div className={cx("relative", className)}>
            <SearchIcon size={17} className="pointer-events-none absolute top-1/2 left-3.5 -translate-y-1/2 text-muted" />
            <input
                type="search"
                {...props}
                className="h-11 w-full rounded-lg border border-line bg-field pr-3 pl-10 text-sm outline-none transition focus:border-brand focus:ring-3 focus:ring-brand/15"
            />
        </div>
    );
}

/** Rounded checkbox drawn in the brand colour. */
export function Checkbox({ checked, onChange, label }) {
    return (
        <span className="relative inline-grid size-5 shrink-0 place-items-center">
            <input
                type="checkbox"
                checked={checked}
                onChange={onChange}
                aria-label={label}
                className="peer absolute inset-0 cursor-pointer appearance-none rounded-[5px] border border-[#d5d0de] bg-surface checked:border-brand checked:bg-brand focus-visible:ring-3 focus-visible:ring-brand/25"
            />
            <CheckIcon size={13} strokeWidth={2.6} className="pointer-events-none relative text-white opacity-0 peer-checked:opacity-100" />
        </span>
    );
}

/** Info box with a leading icon; `tone` is "note" (lavender) or "rose". */
export function Notice({ icon: NoticeIcon = InfoIcon, tone = "note", children }) {
    return (
        <div className={cx("flex gap-3 rounded-lg px-4 py-4 text-sm leading-relaxed text-muted", tone === "rose" ? "bg-rose" : "bg-note")}>
            <NoticeIcon size={18} className={cx("mt-px shrink-0", tone === "rose" ? "text-rose-ink" : "text-brand")} />
            <div>{children}</div>
        </div>
    );
}

/** Red error banner. */
export function ErrorBanner({ message }) {
    if (!message) return null;
    return (
        <p role="alert" className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
            {message}
        </p>
    );
}

/** Two-letter (or code) square badge used before course / UC names. */
export function CodeBadge({ children, tone = "page" }) {
    return (
        <span
            className={cx(
                "grid h-8 min-w-8 shrink-0 place-items-center rounded-md px-1.5 text-[11px] font-semibold",
                tone === "rose" ? "bg-rose text-ink" : "bg-page text-muted"
            )}
        >
            {children}
        </span>
    );
}

/** Small grey/coloured counter pill (e.g. tab counts). */
export function CountPill({ children, tone = "page" }) {
    const tones = { page: "bg-page text-muted", sky: "bg-sky text-sky-ink", rose: "bg-rose text-rose-ink", brand: "bg-brand-soft text-brand" };
    return <span className={cx("rounded-md px-1.5 py-0.5 text-xs font-medium", tones[tone])}>{children}</span>;
}

/**
 * Full-page layout used by the create / edit forms: top bar with the logo, a header
 * with breadcrumb, title and close button, the form body and a sticky footer.
 */
export function FormPage({ breadcrumb, title, description, showRequiredHint, onClose, onSubmit, footerNote, actions, children }) {
    return (
        <div className="min-h-screen bg-page">
            <header className="flex h-[88px] items-center justify-between border-b border-line bg-surface px-6 sm:px-10">
                <Brand />
                <span className="text-[15px] text-muted">Administração</span>
            </header>

            <form onSubmit={onSubmit} className="mx-auto flex max-w-5xl flex-col">
                <div className="border-b border-line px-6 pt-9 pb-8 sm:px-10">
                    <nav className="mb-4 flex items-center gap-2 text-sm text-muted">
                        <button type="button" onClick={onClose} className="hover:text-ink">
                            {breadcrumb[0]}
                        </button>
                        <ChevronRightIcon size={15} strokeWidth={2.2} className="text-ink" />
                        <span>{breadcrumb[1]}</span>
                    </nav>
                    <div className="flex items-start justify-between gap-6">
                        <div>
                            <h1 className="text-[32px] leading-tight font-bold tracking-tight">{title}</h1>
                            <p className="mt-2 text-[15px] text-muted">{description}</p>
                            {showRequiredHint && <p className="mt-5 text-[13px] text-muted">* Campos obrigatórios</p>}
                        </div>
                        <button
                            type="button"
                            onClick={onClose}
                            aria-label="Fechar"
                            className="grid size-10 shrink-0 place-items-center rounded-lg border border-line bg-field text-muted hover:text-ink"
                        >
                            <CloseIcon size={18} />
                        </button>
                    </div>
                </div>

                <div className="space-y-8 px-6 py-9 sm:px-10">{children}</div>

                <footer className="sticky bottom-0 mt-auto flex flex-col gap-4 border-t border-line bg-surface/95 px-6 py-6 backdrop-blur sm:flex-row sm:items-center sm:justify-between sm:px-10">
                    <p className="max-w-sm text-[13px] leading-relaxed text-muted">{footerNote}</p>
                    <div className="flex flex-wrap gap-3">{actions}</div>
                </footer>
            </form>
        </div>
    );
}

/** Section heading inside a form page, with an optional right-aligned note. */
export function SectionTitle({ title, aside, description }) {
    return (
        <div>
            <div className="flex items-baseline justify-between gap-4">
                <h2 className="text-lg font-semibold">{title}</h2>
                {aside && <span className="text-[13px] text-brand">{aside}</span>}
            </div>
            {description && <p className="mt-2 text-sm text-muted">{description}</p>}
        </div>
    );
}
