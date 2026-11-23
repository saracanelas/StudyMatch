import { useEffect } from "react";
import { cx } from "./cx";
import { CheckIcon, CloseIcon, InfoIcon } from "./Icons";

const TOAST_DURATION_MS = 4000;

/**
 * Success / error message shown at the bottom-right corner; closes itself after a few seconds.
 * `toast` is { type: "success" | "error", text, id } or null.
 */
export default function Toast({ toast, onClose }) {
    useEffect(() => {
        if (!toast) return undefined;
        const timer = setTimeout(onClose, TOAST_DURATION_MS);
        return () => clearTimeout(timer);
    }, [toast, onClose]);

    if (!toast) return null;
    const isError = toast.type === "error";

    return (
        <div
            role={isError ? "alert" : "status"}
            className={cx(
                "fixed right-6 bottom-6 z-50 flex max-w-sm items-start gap-3 rounded-xl border bg-surface px-4 py-3.5 text-sm shadow-lg shadow-ink/10",
                isError ? "border-red-200" : "border-mint"
            )}
        >
            <span className={cx("grid size-6 shrink-0 place-items-center rounded-full", isError ? "bg-red-50 text-red-600" : "bg-mint text-mint-ink")}>
                {isError ? <InfoIcon size={15} /> : <CheckIcon size={14} strokeWidth={2.4} />}
            </span>
            <p className="flex-1 pt-0.5 text-ink">{toast.text}</p>
            <button type="button" onClick={onClose} aria-label="Fechar" className="text-muted hover:text-ink">
                <CloseIcon size={16} />
            </button>
        </div>
    );
}
