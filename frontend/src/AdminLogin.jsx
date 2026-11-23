import { useState } from "react";
import { loginAdmin } from "./Admin.js";
import { Brand, Button, ErrorBanner, Field } from "./components/ui";

/** Administrator login page; calls `onLogin(email)` after a successful login. */
export default function AdminLogin({ onLogin }) {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");

    // Show authentication errors and prevent duplicate submissions
    const [error, setError] = useState("");
    const [isSubmitting, setIsSubmitting] = useState(false);

    async function handleSubmit(event) {
        event.preventDefault();
        setError("");
        setIsSubmitting(true);
        try {
            await loginAdmin(email, password);
            onLogin(email.trim());
        } catch (err) {
            // Keep the form visible and show the error to the user
            setError(err.message);
            setIsSubmitting(false);
        }
    }

    return (
        <div className="grid min-h-screen place-items-center bg-page px-4 py-10">
            <div className="w-full max-w-md">
                <div className="mb-8 flex justify-center">
                    <Brand size="lg" />
                </div>

                <form onSubmit={handleSubmit} className="rounded-2xl border border-line bg-surface p-8 shadow-[0_1px_2px_rgba(37,34,47,0.04)]">
                    <h1 className="text-2xl font-bold tracking-tight">Bem-vindo(a) de volta</h1>
                    <p className="mt-1.5 text-sm text-muted">Inicie sessão na administração académica.</p>

                    <div className="mt-7 space-y-5">
                        <Field
                            label="Email"
                            type="email"
                            value={email}
                            onChange={(event) => setEmail(event.target.value)}
                            autoComplete="email"
                            placeholder="admin@studymatch.pt"
                            required
                        />
                        <Field
                            label="Palavra-passe"
                            type="password"
                            value={password}
                            onChange={(event) => setPassword(event.target.value)}
                            autoComplete="current-password"
                            required
                        />
                        <ErrorBanner message={error} />
                    </div>

                    <Button type="submit" variant="primary" size="lg" disabled={isSubmitting} className="mt-7 w-full">
                        {isSubmitting ? "A entrar…" : "Entrar"}
                    </Button>
                </form>

                <p className="mt-6 text-center text-xs text-muted">Boas ligações começam aqui.</p>
            </div>
        </div>
    );
}
