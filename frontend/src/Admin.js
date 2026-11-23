/** Checks the administrator credentials with the backend; throws an Error with the reason on failure. */
export async function loginAdmin(email, password) {
    const response = await fetch("/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email: email.trim(), password }),
    });

    let data = {};
    try {
        data = await response.json();
    } catch {
        // no JSON body (e.g. the backend is not running); handled below
    }

    if (response.status === 401) throw new Error("Email ou palavra-passe incorretos.");
    if (!response.ok) throw new Error(data.error || "Não foi possível iniciar sessão. Verifique se o servidor está ativo.");
    return data;
}
