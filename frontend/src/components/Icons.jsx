/** Small line icons (stroke follows the current text colour). */
function Icon({ children, size = 18, className = "", strokeWidth = 1.7 }) {
    return (
        <svg
            width={size}
            height={size}
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth={strokeWidth}
            strokeLinecap="round"
            strokeLinejoin="round"
            className={className}
            aria-hidden="true"
        >
            {children}
        </svg>
    );
}

export const LogoIcon = (p) => (
    <Icon {...p}>
        <path d="M12 3 3 7.5l9 4.5 9-4.5L12 3Z" />
        <path d="m3 12 9 4.5 9-4.5" />
        <path d="m3 16.5 9 4.5 9-4.5" />
    </Icon>
);
export const BuildingIcon = (p) => (
    <Icon {...p}>
        <path d="M4 21V9l8-5 8 5v12" />
        <path d="M9 21v-5h6v5" />
        <path d="M10 10h4" />
    </Icon>
);
export const GridIcon = (p) => (
    <Icon {...p}>
        <rect x="4" y="4" width="6" height="6" rx="1.5" />
        <rect x="14" y="4" width="6" height="6" rx="1.5" />
        <rect x="4" y="14" width="6" height="6" rx="1.5" />
        <rect x="14" y="14" width="6" height="6" rx="1.5" />
    </Icon>
);
export const CapIcon = (p) => (
    <Icon {...p}>
        <path d="M22 9 12 4 2 9l10 5 10-5Z" />
        <path d="M6 11v5c0 1.5 2.7 3 6 3s6-1.5 6-3v-5" />
    </Icon>
);
export const UsersIcon = (p) => (
    <Icon {...p}>
        <circle cx="9" cy="8" r="3.5" />
        <path d="M2.5 20c.8-3.5 3.4-5.5 6.5-5.5s5.7 2 6.5 5.5" />
        <path d="M16 4.6a3.5 3.5 0 0 1 0 6.8M18 14.8c1.8.7 3 2.5 3.5 5.2" />
    </Icon>
);
export const GroupsIcon = (p) => (
    <Icon {...p}>
        <rect x="9" y="3" width="6" height="5" rx="1" />
        <rect x="3" y="16" width="6" height="5" rx="1" />
        <rect x="15" y="16" width="6" height="5" rx="1" />
        <path d="M12 8v4M6 16v-4h12v4" />
    </Icon>
);
export const HistoryIcon = (p) => (
    <Icon {...p}>
        <path d="M3 12a9 9 0 1 0 3-6.7L3 8" />
        <path d="M3 3v5h5M12 7v5l3 2" />
    </Icon>
);
export const SlidersIcon = (p) => (
    <Icon {...p}>
        <path d="M4 7h10M18 7h2M4 17h4M12 17h8" />
        <circle cx="16" cy="7" r="2" />
        <circle cx="10" cy="17" r="2" />
    </Icon>
);
export const HelpIcon = (p) => (
    <Icon {...p}>
        <circle cx="12" cy="12" r="9" />
        <path d="M9.5 9.5a2.5 2.5 0 1 1 3.5 2.3c-.6.3-1 .9-1 1.6v.3M12 17h.01" />
    </Icon>
);
export const LogoutIcon = (p) => (
    <Icon {...p}>
        <path d="M14 4h4a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2h-4" />
        <path d="M10 16 6 12l4-4M6 12h10" />
    </Icon>
);
export const BellIcon = (p) => (
    <Icon {...p}>
        <path d="M6 9a6 6 0 1 1 12 0c0 6 2 7.5 2 7.5H4S6 15 6 9Z" />
        <path d="M10 20a2 2 0 0 0 4 0" />
    </Icon>
);
export const BookIcon = (p) => (
    <Icon {...p}>
        <path d="M12 6.5C10.5 5 8 4.5 3 4.5v14c5 0 7.5.5 9 2 1.5-1.5 4-2 9-2v-14c-5 0-7.5.5-9 2Z" />
        <path d="M12 6.5v14" />
    </Icon>
);
export const LinkIcon = (p) => (
    <Icon {...p}>
        <path d="M9 17H7a5 5 0 0 1 0-10h2M15 7h2a5 5 0 0 1 0 10h-2M8 12h8" />
    </Icon>
);
export const PlusIcon = (p) => (
    <Icon {...p}>
        <path d="M12 5v14M5 12h14" />
    </Icon>
);
export const SearchIcon = (p) => (
    <Icon {...p}>
        <circle cx="11" cy="11" r="7" />
        <path d="m20 20-3.5-3.5" />
    </Icon>
);
export const ChevronRightIcon = (p) => (
    <Icon {...p}>
        <path d="m9 6 6 6-6 6" />
    </Icon>
);
export const ChevronLeftIcon = (p) => (
    <Icon {...p}>
        <path d="m15 6-6 6 6 6" />
    </Icon>
);
export const ChevronDownIcon = (p) => (
    <Icon {...p}>
        <path d="m6 9 6 6 6-6" />
    </Icon>
);
export const ChevronUpDownIcon = (p) => (
    <Icon {...p}>
        <path d="m8 9 4-4 4 4M8 15l4 4 4-4" />
    </Icon>
);
export const DotsIcon = (p) => (
    <Icon {...p}>
        <circle cx="5" cy="12" r="1" fill="currentColor" />
        <circle cx="12" cy="12" r="1" fill="currentColor" />
        <circle cx="19" cy="12" r="1" fill="currentColor" />
    </Icon>
);
export const PencilIcon = (p) => (
    <Icon {...p}>
        <path d="M16.5 3.5a2.1 2.1 0 0 1 3 3L8 18l-4 1 1-4 11.5-11.5Z" />
    </Icon>
);
export const ArrowRightIcon = (p) => (
    <Icon {...p}>
        <path d="M5 12h14M13 6l6 6-6 6" />
    </Icon>
);
export const InfoIcon = (p) => (
    <Icon {...p}>
        <circle cx="12" cy="12" r="9" />
        <path d="M12 11v5M12 8h.01" />
    </Icon>
);
export const CloseIcon = (p) => (
    <Icon {...p}>
        <path d="M6 6l12 12M18 6 6 18" />
    </Icon>
);
export const CheckIcon = (p) => (
    <Icon {...p}>
        <path d="m5 12.5 4.5 4.5L19 7.5" />
    </Icon>
);
export const NewFileIcon = (p) => (
    <Icon {...p}>
        <rect x="5" y="3" width="14" height="18" rx="2" />
        <path d="M12 9v6M9 12h6" />
    </Icon>
);
export const ShareIcon = (p) => (
    <Icon {...p}>
        <circle cx="6" cy="6" r="2" />
        <circle cx="18" cy="10" r="2" />
        <circle cx="9" cy="18" r="2" />
        <path d="M7.5 7.5 16.3 9.3M17 12l-6.6 4.6" />
    </Icon>
);
