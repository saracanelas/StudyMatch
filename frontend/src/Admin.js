
export async function loginAdmin(email, password) { //funçao exportada para adminLogin 
  const response = await fetch("/api/admin", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      email: email.trim(),
      password,
    }),
  });

  const dados = await response.json();

  if (!response.ok) {
    throw new Error(dados.erro || "Erro ao fazer login");
  }

  if (dados.utilizador) {
    localStorage.setItem("studymatch_user",JSON.stringify(dados.utilizador));
  }

  if (dados.redirecionar) {
    window.location.href = dados.redirecionar;
  }

  return dados;
}