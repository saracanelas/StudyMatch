const BASE_URL = "/api"; // o Vite reencaminha /api para o backend (ver vite.config.js)

/** Converts a course as sent by the backend into the shape used by the pages. */
const cursoDoApi = (c) => ({
    id: c.idCourse,
    nome: c.nameCourse,
    codigo: c.codeCourse,
    ucIds: (c.curricularUnits ?? []).map((u) => u.idUnit),
});

/** Converts a curricular unit as sent by the backend into the shape used by the pages. */
const ucDoApi = (u) => ({ id: u.idUnit, nome: u.nameUnit, codigo: u.codeUnit });

/** Sends a request to the backend and returns the JSON body, throwing an Error with the server message on failure. */
async function pedido(caminho, opcoes) {
    const resposta = await fetch(`${BASE_URL}${caminho}`, {
        headers: { "Content-Type": "application/json" },
        ...opcoes,
    });

    if (!resposta.ok) {
        let mensagem = `Erro ${resposta.status} ao contactar o servidor`;
        try {
            const corpo = await resposta.json();
            mensagem = corpo.message || corpo.error || mensagem;
        } catch {
            // a resposta de erro não tinha JSON; fica a mensagem genérica
        }
        throw new Error(mensagem);
    }
    return resposta.status === 204 ? null : resposta.json();
}

// ---------- Cursos ----------
/** Returns every course. */
export async function listarCursos() {
    const dados = await pedido("/courses");
    return dados.map(cursoDoApi);
}

/** Returns the course with the given id. */
export async function obterCurso(id) {
    return cursoDoApi(await pedido(`/courses/${id}`));
}

/** Creates a course from { nome, codigo } and returns it. */
export async function criarCurso(dados) {
    const criado = await pedido("/courses", {
        method: "POST",
        body: JSON.stringify({ codeCourse: dados.codigo, nameCourse: dados.nome }),
    });
    return cursoDoApi(criado);
}

/** Saves the name, code and associated UCs of a course and returns the updated course. */
export async function atualizarCurso(id, dados) {
    // 1.º atualiza o nome e o código; 2.º atualiza as UCs associadas
    await pedido(`/courses/${id}`, {
        method: "PUT",
        body: JSON.stringify({ codeCourse: dados.codigo, nameCourse: dados.nome }),
    });
    const atualizado = await pedido(`/courses/${id}/units`, {
        method: "PUT",
        body: JSON.stringify(dados.ucIds),
    });
    return cursoDoApi(atualizado);
}

// ---------- UCs ----------
/** Returns every curricular unit. */
export async function listarUcs() {
    const dados = await pedido("/units");
    return dados.map(ucDoApi);
}

/** Creates a curricular unit from { nome, codigo } and returns it. */
export async function criarUc(dados) {
    const criada = await pedido("/units", {
        method: "POST",
        body: JSON.stringify({ codeUnit: dados.codigo, nameUnit: dados.nome }),
    });
    return ucDoApi(criada);
}

/** Saves the name and code of a curricular unit and returns the updated unit. */
export async function atualizarUc(id, dados) {
    const atualizada = await pedido(`/units/${id}`, {
        method: "PUT",
        body: JSON.stringify({ codeUnit: dados.codigo, nameUnit: dados.nome }),
    });
    return ucDoApi(atualizada);
}
