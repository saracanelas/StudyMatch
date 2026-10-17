/** Labelled text input with the app styling; extra props go to the <input>. */
export default function TextField({ label, ...inputProps }) {
    return (
        <label className="flex flex-col gap-1 font-medium">
            {label}
            <input
                {...inputProps}
                className="rounded-lg border border-lilac bg-white px-3 py-2 font-normal outline-none focus:border-purple focus:ring-2 focus:ring-blue"
            />
        </label>
    );
}
