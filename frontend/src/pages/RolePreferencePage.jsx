import { useState } from "react";
import {
    getAvailableRoles,
    getNotifications,
    getOpenUnitsForStudent,
    getRolePreference,
    markNotificationAsRead,
    submitRolePreference,
} from "../api";
import { Button, Card, CountPill, ErrorBanner, Notice, PageHeader, SectionTitle, SelectField } from "../components/ui";
import { cx } from "../components/cx";
import { BellIcon, CheckIcon, ChevronDownIcon, InfoIcon } from "../components/Icons";

const STUDENT_NUMBER_PATTERN = /^[A-Za-z0-9]{1,20}$/;

/** Returns a copy of `list` with the item at `from` moved to position `to`. */
function moveItem(list, from, to) {
    const copy = [...list];
    const [item] = copy.splice(from, 1);
    copy.splice(to, 0, item);
    return copy;
}

/**
 * Questionnaire where a student orders the roles of an assignment from most to least wanted.
 * The student only sees the units they belong to whose group configuration is open, and their notifications.
 * Temporary: lives in the admin area until students have their own login, so the student number is typed in.
 */
export default function RolePreferencePage({ onNotify }) {
    const [studentNumber, setStudentNumber] = useState("");
    const [activeStudent, setActiveStudent] = useState(null); // number confirmed with "Entrar"
    const [units, setUnits] = useState([]);
    const [notifications, setNotifications] = useState([]);
    const [selectedUnitId, setSelectedUnitId] = useState("");
    const [availableRoles, setAvailableRoles] = useState([]);
    const [ranking, setRanking] = useState(null); // null until the questionnaire is opened
    const [hasSubmitted, setHasSubmitted] = useState(false);
    const [draggedIndex, setDraggedIndex] = useState(null);
    const [error, setError] = useState("");

    const unreadCount = notifications.filter((notification) => !notification.read).length;

    /** Loads the open units and the notifications of the typed student number. */
    async function handleEnter(event) {
        event.preventDefault();
        setError("");
        setRanking(null);
        setSelectedUnitId("");
        const number = studentNumber.trim();
        if (!STUDENT_NUMBER_PATTERN.test(number)) {
            setError("O número de aluno só pode ter letras e números (máximo 20)");
            return;
        }
        try {
            const [openUnits, studentNotifications] = await Promise.all([getOpenUnitsForStudent(number), getNotifications(number)]);
            setUnits(openUnits);
            setNotifications(studentNotifications);
            setActiveStudent(number);
        } catch (err) {
            setActiveStudent(null);
            setError(err.message);
        }
    }

    async function handleMarkAsRead(notificationId) {
        try {
            const updated = await markNotificationAsRead(activeStudent, notificationId);
            setNotifications((current) => current.map((n) => (n.idNotification === notificationId ? updated : n)));
        } catch (err) {
            onNotify("error", err.message);
        }
    }

    /** Loads the roles of the assignment and the ranking the student already submitted, if any. */
    async function handleOpenUnit(unitId) {
        setSelectedUnitId(unitId);
        setError("");
        setRanking(null);
        if (!unitId) return;
        try {
            const roles = await getAvailableRoles(unitId);
            const savedRanking = await getRolePreference(unitId, activeStudent);
            // Use the saved order only if it still matches the current roles of the assignment
            const savedIsCurrent =
                savedRanking && savedRanking.length === roles.length && roles.every((role) => savedRanking.includes(role));
            setAvailableRoles(roles);
            setRanking(savedIsCurrent ? savedRanking : roles);
            setHasSubmitted(Boolean(savedRanking));
            if (savedRanking && !savedIsCurrent) {
                setError("As roles do trabalho mudaram desde a última submissão. Ordene-as de novo.");
            }
        } catch (err) {
            setError(err.message);
        }
    }

    function handleDrop(targetIndex) {
        if (draggedIndex !== null && draggedIndex !== targetIndex) {
            setRanking((current) => moveItem(current, draggedIndex, targetIndex));
        }
        setDraggedIndex(null);
    }

    /** Checks that every role appears exactly once; returns an error message or null. */
    function validate() {
        if (new Set(ranking).size !== ranking.length) {
            return "Não pode haver roles repetidas";
        }
        const missing = availableRoles.filter((role) => !ranking.includes(role));
        if (missing.length > 0 || ranking.length !== availableRoles.length) {
            return `Tem de ordenar todas as roles${missing.length ? `: faltam ${missing.join(", ")}` : ""}`;
        }
        return null;
    }

    async function handleSubmit() {
        const validationError = validate();
        setError(validationError ?? "");
        if (validationError) return;
        try {
            const saved = await submitRolePreference(selectedUnitId, activeStudent, ranking);
            setRanking(saved);
            onNotify("success", hasSubmitted ? "Preferências atualizadas com sucesso." : "Preferências submetidas com sucesso.");
            setHasSubmitted(true);
        } catch (err) {
            setError(err.message);
        }
    }

    const arrowButton = "grid size-8 place-items-center rounded-md text-muted hover:bg-page hover:text-ink disabled:opacity-30 disabled:hover:bg-transparent";

    return (
        <div className="mx-auto max-w-[1180px]">
            <PageHeader
                title="Preferências de roles"
                description="Página temporária: simula o questionário do aluno até os alunos terem conta própria."
            />

            <Card className="mt-6">
                <form onSubmit={handleEnter} className="flex flex-col gap-3 sm:flex-row sm:items-end">
                    <label className="block flex-1">
                        <span className="mb-2 block text-sm font-medium">Número de aluno</span>
                        <input
                            value={studentNumber}
                            maxLength={20}
                            placeholder="Ex.: 2024001"
                            onChange={(event) => {
                                setStudentNumber(event.target.value);
                                setActiveStudent(null);
                                setRanking(null);
                            }}
                            className="h-12 w-full rounded-lg border border-line bg-field px-3.5 text-[15px] outline-none transition focus:border-brand focus:ring-3 focus:ring-brand/15"
                        />
                    </label>
                    <Button type="submit" variant="primary" size="lg">
                        Entrar
                    </Button>
                </form>
            </Card>

            <div className="mt-4">
                <ErrorBanner message={error} />
            </div>

            {activeStudent && (
                <div className="mt-2 grid items-start gap-4 xl:grid-cols-[minmax(0,1fr)_minmax(0,1.3fr)]">
                    <Card className="space-y-4">
                        <h2 className="flex items-center gap-2.5 text-base font-semibold">
                            <BellIcon size={17} className="text-brand" /> Notificações
                            <CountPill tone={unreadCount ? "rose" : "page"}>{unreadCount} por ler</CountPill>
                        </h2>
                        {notifications.length === 0 ? (
                            <p className="text-sm text-muted">Sem notificações.</p>
                        ) : (
                            <ul className="overflow-hidden rounded-lg border border-line">
                                {notifications.map((notification) => (
                                    <li
                                        key={notification.idNotification}
                                        className={cx(
                                            "flex items-start gap-3 border-b border-line px-4 py-3 last:border-b-0",
                                            notification.read ? "text-muted" : "bg-rose/40"
                                        )}
                                    >
                                        <span
                                            className={cx("mt-1.5 size-2 shrink-0 rounded-full", notification.read ? "bg-line" : "bg-brand")}
                                            aria-hidden="true"
                                        />
                                        <span className={cx("flex-1 text-[13px]", !notification.read && "font-medium")}>{notification.message}</span>
                                        {!notification.read && (
                                            <button
                                                type="button"
                                                onClick={() => handleMarkAsRead(notification.idNotification)}
                                                className="shrink-0 text-xs font-medium text-brand hover:underline"
                                            >
                                                Marcar como lida
                                            </button>
                                        )}
                                    </li>
                                ))}
                            </ul>
                        )}
                    </Card>

                    <Card className="space-y-5">
                        {units.length === 0 ? (
                            <Notice icon={InfoIcon}>Não tem nenhuma unidade curricular com a configuração de grupos aberta.</Notice>
                        ) : (
                            <SelectField
                                label="Unidade curricular"
                                hint="Só aparecem as UCs com a configuração de grupos aberta."
                                value={selectedUnitId}
                                onChange={(event) => handleOpenUnit(event.target.value)}
                            >
                                <option value="">Escolha uma unidade curricular</option>
                                {units.map((unit) => (
                                    <option key={unit.id} value={unit.id}>
                                        {unit.code} · {unit.name}
                                    </option>
                                ))}
                            </SelectField>
                        )}

                        {ranking && (
                            <>
                                <SectionTitle
                                    title="Ordene as roles"
                                    aside={hasSubmitted ? "Já submetido" : "Por submeter"}
                                    description="Da que mais quer (1) para a que menos quer. Arraste ou use as setas."
                                />
                                <ol className="space-y-2">
                                    {ranking.map((role, index) => (
                                        <li
                                            key={role}
                                            draggable
                                            onDragStart={() => setDraggedIndex(index)}
                                            onDragOver={(event) => event.preventDefault()}
                                            onDrop={() => handleDrop(index)}
                                            onDragEnd={() => setDraggedIndex(null)}
                                            className={cx(
                                                "flex cursor-grab items-center gap-3 rounded-lg border border-line bg-field px-3 py-2.5",
                                                draggedIndex === index && "opacity-50"
                                            )}
                                        >
                                            <span
                                                className={cx(
                                                    "grid size-7 shrink-0 place-items-center rounded-md text-xs font-semibold",
                                                    index === 0 ? "bg-brand text-white" : "bg-page text-muted"
                                                )}
                                            >
                                                {index + 1}
                                            </span>
                                            <span className="flex-1 text-sm font-medium">{role}</span>
                                            <button
                                                type="button"
                                                aria-label={`Subir ${role}`}
                                                disabled={index === 0}
                                                onClick={() => setRanking((current) => moveItem(current, index, index - 1))}
                                                className={arrowButton}
                                            >
                                                <ChevronDownIcon size={16} className="rotate-180" />
                                            </button>
                                            <button
                                                type="button"
                                                aria-label={`Descer ${role}`}
                                                disabled={index === ranking.length - 1}
                                                onClick={() => setRanking((current) => moveItem(current, index, index + 1))}
                                                className={arrowButton}
                                            >
                                                <ChevronDownIcon size={16} />
                                            </button>
                                        </li>
                                    ))}
                                </ol>
                                <div className="flex justify-end border-t border-line pt-5">
                                    <Button variant="primary" onClick={handleSubmit}>
                                        <CheckIcon size={16} strokeWidth={2.4} /> {hasSubmitted ? "Atualizar preferências" : "Submeter preferências"}
                                    </Button>
                                </div>
                            </>
                        )}
                    </Card>
                </div>
            )}
        </div>
    );
}
