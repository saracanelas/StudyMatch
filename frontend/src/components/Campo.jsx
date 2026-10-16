/** Labelled text input with the app styling; extra props go to the <input>. */
export default function Campo({ label, ...props }) {
    return (
        <label className="flex flex-col gap-1 font-medium">
            {label}
            <input
                {...props}
                className="rounded-lg border border-lilas bg-white px-3 py-2 font-normal outline-none focus:border-roxo focus:ring-2 focus:ring-azul"
            />
        </label>
    );
}