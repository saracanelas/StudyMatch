import { Brand } from "./ui";
import { cx } from "./cx";
import {
    BellIcon,
    BuildingIcon,
    CapIcon,
    ChevronRightIcon,
    ChevronUpDownIcon,
    GridIcon,
    GroupsIcon,
    HelpIcon,
    HistoryIcon,
    LogoutIcon,
    ShareIcon,
    SlidersIcon,
    UsersIcon,
} from "./Icons";

// Entries with a `section` open that page; the others are shown as upcoming sections
const NAV_SECTIONS = [
    { items: [{ label: "Visão geral", icon: GridIcon }] },
    {
        title: "Gestão académica",
        items: [
            { label: "Cursos e UCs", icon: CapIcon, section: "catalog" },
            { label: "Estudantes", icon: UsersIcon, section: "students" },
            { label: "Alunos das UCs", icon: ShareIcon, section: "unitStudents" },
            { label: "Grupos", icon: GroupsIcon, section: "groups" },
        ],
    },
    {
        // Temporary: simulates the student area until students can log in
        title: "Área do aluno (teste)",
        items: [{ label: "Preferências de roles", icon: BellIcon, section: "rolePreferences" }],
    },
    {
        title: "Espaço de trabalho",
        items: [
            { label: "Atividade", icon: HistoryIcon },
            { label: "Definições", icon: SlidersIcon },
        ],
    },
];

/** Initials from an e-mail address, e.g. "mariana.silva@x.pt" -> "MS". */
function initialsFromEmail(email = "") {
    const parts = email.split("@")[0].split(/[._-]+/).filter(Boolean);
    const letters = parts.length > 1 ? parts[0][0] + parts[1][0] : (parts[0] ?? "A").slice(0, 2);
    return letters.toUpperCase();
}

/** Name shown in the sidebar, derived from the e-mail ("mariana.silva" -> "Mariana Silva"). */
function nameFromEmail(email = "") {
    const local = email.split("@")[0];
    if (!local) return "Administrador";
    return local
        .split(/[._-]+/)
        .filter(Boolean)
        .map((part) => part[0].toUpperCase() + part.slice(1))
        .join(" ");
}

function NavItem({ label, icon: ItemIcon, active, available, onClick, badge }) {
    return (
        <button
            type="button"
            disabled={!available}
            onClick={onClick}
            title={available ? undefined : "Brevemente disponível"}
            aria-current={active ? "page" : undefined}
            className={cx(
                "flex h-10 w-full items-center gap-3 rounded-lg px-2.5 text-left text-sm transition-colors",
                active ? "bg-rose font-medium text-ink" : "text-muted hover:bg-page hover:text-ink disabled:cursor-default disabled:hover:bg-transparent disabled:hover:text-muted"
            )}
        >
            <ItemIcon size={17} className={active ? "text-brand" : "text-muted"} />
            <span className="flex-1">{label}</span>
            {badge !== undefined && <span className="text-xs text-muted">{badge}</span>}
        </button>
    );
}

const SECTION_LABELS = {
    catalog: "Cursos e UCs",
    students: "Estudantes",
    unitStudents: "Alunos das UCs",
    groups: "Configuração de grupos",
    rolePreferences: "Preferências de roles",
};

/**
 * Sidebar + top bar wrapping the admin overview pages.
 * `section` is the open page ("catalog" or "students"); `counts` holds the numbers shown next to the menu entries.
 */
export default function AdminLayout({ adminEmail, onLogout, section, onNavigate, counts = {}, children }) {
    const name = nameFromEmail(adminEmail);

    return (
        <div className="flex min-h-screen bg-page">
            <aside className="sticky top-0 hidden h-screen w-56 shrink-0 flex-col border-r border-line bg-surface px-4 pt-5 pb-4 lg:flex">
                <div className="px-1.5">
                    <Brand size="lg" />
                </div>

                <div className="mt-6 flex items-center gap-2.5 rounded-xl border border-line bg-surface p-2.5 shadow-[0_1px_2px_rgba(37,34,47,0.04)]">
                    <span className="grid size-8 shrink-0 place-items-center rounded-md bg-rose text-rose-ink">
                        <BuildingIcon size={16} />
                    </span>
                    <span className="min-w-0 flex-1">
                        <span className="block text-[13px] leading-tight font-semibold">Administração académica</span>
                        <span className="block truncate text-[11px] text-muted">Cursos, UCs e estudantes</span>
                    </span>
                    <ChevronUpDownIcon size={14} className="shrink-0 text-muted" />
                </div>

                <nav className="mt-6 flex flex-col gap-1">
                    {NAV_SECTIONS.map((group, index) => (
                        <div key={index} className={cx(index > 0 && "mt-4")}>
                            {group.title && (
                                <p className="mb-1.5 px-0.5 text-[10px] font-medium tracking-wide text-muted uppercase">
                                    {group.title}
                                </p>
                            )}
                            <div className="flex flex-col gap-1">
                                {group.items.map((item) => (
                                    <NavItem
                                        key={item.label}
                                        label={item.label}
                                        icon={item.icon}
                                        available={Boolean(item.section)}
                                        active={item.section === section}
                                        badge={counts[item.section]}
                                        onClick={() => item.section && onNavigate(item.section)}
                                    />
                                ))}
                            </div>
                        </div>
                    ))}
                </nav>

                <div className="mt-auto">
                    <div className="rounded-xl bg-rose p-3.5">
                        <ShareIcon size={18} className="text-brand" />
                        <p className="mt-3 text-[13px] font-semibold">Boas ligações começam aqui.</p>
                        <p className="mt-1.5 text-[11px] leading-relaxed text-muted">
                            Organize a oferta académica para formar melhores grupos.
                        </p>
                    </div>

                    <div className="mt-5 border-b border-line pb-4">
                        <NavItem label="Centro de ajuda" icon={HelpIcon} />
                    </div>

                    <div className="mt-4 flex items-center gap-2.5 px-1">
                        <span className="grid size-8 shrink-0 place-items-center rounded-full bg-rose text-[11px] font-semibold text-rose-ink">
                            {initialsFromEmail(adminEmail)}
                        </span>
                        <span className="min-w-0 flex-1">
                            <span className="block truncate text-[13px] font-semibold">{name}</span>
                            <span className="block text-[11px] text-muted">Administrador(a)</span>
                        </span>
                        <button
                            type="button"
                            onClick={onLogout}
                            aria-label="Terminar sessão"
                            title="Terminar sessão"
                            className="grid size-8 place-items-center rounded-md text-muted hover:bg-page hover:text-ink"
                        >
                            <LogoutIcon size={17} />
                        </button>
                    </div>
                </div>
            </aside>

            <div className="flex min-w-0 flex-1 flex-col">
                <header className="flex h-16 items-center justify-between border-b border-line bg-surface px-6 lg:px-8">
                    <div className="lg:hidden">
                        <Brand />
                    </div>
                    <nav className="hidden items-center gap-2 text-[13px] text-muted lg:flex">
                        <span>Administração académica</span>
                        <ChevronRightIcon size={14} strokeWidth={2} />
                        <span className="font-medium text-ink">{SECTION_LABELS[section]}</span>
                    </nav>
                    <div className="flex items-center gap-4">
                        <span className="h-6 w-px bg-line" />
                        <button type="button" aria-label="Notificações" className="text-muted hover:text-ink">
                            <BellIcon size={18} />
                        </button>
                        <button
                            type="button"
                            onClick={onLogout}
                            aria-label="Terminar sessão"
                            className="text-muted hover:text-ink lg:hidden"
                        >
                            <LogoutIcon size={18} />
                        </button>
                    </div>
                </header>

                <main className="flex-1 px-6 py-7 lg:px-8">{children}</main>
            </div>
        </div>
    );
}
