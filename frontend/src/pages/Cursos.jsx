import { useEffect, useState } from "react";
import { listarCursos, criarCurso } from "../api";
import CursoForm from "../components/CursoForm";
import CursoDetalhe from "./CursoDetalhe";

/** Courses page: shows the courses as square cards and switches to the create or edit form. */
export default function Cursos() {
    const [cursos, setCursos] = useState([]);
    const [vista, setVista] = useState("lista");
    const [erro, setErro] = useState("");

    // Loads the courses once when the page opens
    useEffect(() => {
        let ativo = true;
        listarCursos()
            .then((dados) => ativo && setCursos(dados))
            .catch((e) => ativo && setErro(e.message));
        return () => { ativo = false; };
    }, []);

    /** Creates a course and adds it to the list, or shows the error message. */
    async function handleCriar(dados) {
        try {
            const novo = await criarCurso(dados);
            setCursos((atuais) => [...atuais, novo]);
            setVista("lista");
            setErro("");
        } catch (e) {
            setErro(e.message);
        }
    }

    /** Replaces a course in the list after it was edited. */
    function handleGuardado(atualizado) {
        setCursos((atuais) => atuais.map((c) => (c.id === atualizado.id ? atualizado : c)));
    }

    if (vista === "novo") {
        return (
            <div>
                {erro && <p className="mb-4 max-w-md rounded-lg bg-white p-3 text-red-700">{erro}</p>}
                <CursoForm onSubmit={handleCriar} onCancel={() => { setVista("lista"); setErro(""); }} />
            </div>
        );
    }
    if (typeof vista === "number") {
        return (
            <CursoDetalhe id={vista} onVoltar={() => setVista("lista")} onGuardado={handleGuardado} />
        );
    }

    return (
        <div>
            <div className="mb-4 flex items-center justify-between">
                <h2 className="text-2xl font-bold">Cursos</h2>
                <button onClick={() => setVista("novo")} className="rounded-lg bg-rosa px-4 py-2 font-semibold hover:bg-lilas">
                    + Novo Curso
                </button>
            </div>

            {erro && <p className="mb-4 rounded-lg bg-white p-3 text-red-700">{erro}</p>}

            <ul className="flex flex-wrap gap-4">
                {cursos.map((curso) => (
                    <li key={curso.id} className="flex size-52 flex-col justify-between rounded-xl bg-azul p-4">
                        <div className="overflow-hidden">
                            <p className="font-semibold break-words">{curso.nome}</p>
                            <p className="text-sm">{curso.codigo}</p>
                            <p className="text-sm">{curso.ucIds?.length ?? 0} UCs associadas</p>
                        </div>
                        <button onClick={() => setVista(curso.id)} className="self-start rounded-lg bg-white px-3 py-1 hover:bg-lavanda">
                            Editar
                        </button>
                    </li>
                ))}
            </ul>
            {cursos.length === 0 && !erro && <p>Ainda não há cursos.</p>}
        </div>
    );
}
