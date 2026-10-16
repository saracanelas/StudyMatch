import { useEffect, useState } from "react";
import { obterCurso, listarUcs, atualizarCurso } from "../api";
import Campo from "../components/Campo";

/** Form to edit a course: its name, code and the UCs associated with it. */
export default function CursoDetalhe({ id, onVoltar, onGuardado }) {
    const [curso, setCurso] = useState(null);
    const [ucs, setUcs] = useState([]);
    const [selecionadas, setSelecionadas] = useState([]);
    const [erro, setErro] = useState("");
    const [aGuardar, setAGuardar] = useState(false);

    // Loads the course and the list of all UCs when the page opens
    useEffect(() => {
        let ativo = true;
        Promise.all([obterCurso(id), listarUcs()])
            .then(([c, u]) => {
                if (!ativo) return;
                setCurso(c);
                setUcs(u);
                setSelecionadas(c.ucIds ?? []);
            })
            .catch((e) => ativo && setErro(e.message));
        return () => { ativo = false; };
    }, [id]);

    /** Ticks or unticks a UC in the list of selected UCs. */
    function alternar(ucId) {
        setSelecionadas((atuais) =>
            atuais.includes(ucId) ? atuais.filter((x) => x !== ucId) : [...atuais, ucId]
        );
    }

    /** Saves the course and its UCs, then goes back to the list. */
    async function guardar(e) {
        e.preventDefault();
        setAGuardar(true);
        setErro("");
        try {
            const atualizado = await atualizarCurso(id, { ...curso, ucIds: selecionadas });
            onGuardado(atualizado);
            onVoltar();
        } catch (err) {
            setErro(err.message);
        } finally {
            setAGuardar(false);
        }
    }

    if (!curso) return <p>{erro || "A carregar..."}</p>;

    return (
        <form onSubmit={guardar} className="max-w-xl space-y-4 rounded-2xl bg-azul p-6">
            <h2 className="text-xl font-bold">Editar Curso</h2>

            <Campo label="Nome" value={curso.nome} onChange={(e) => setCurso({ ...curso, nome: e.target.value })} required maxLength={50} />
            <Campo label="Código" value={curso.codigo} onChange={(e) => setCurso({ ...curso, codigo: e.target.value })} required maxLength={12} />

            <fieldset>
                <legend className="mb-2 font-medium">UCs associadas</legend>
                <div className="max-h-64 space-y-1 overflow-y-auto rounded-lg bg-white p-3">
                    {ucs.map((uc) => (
                        <label key={uc.id} className="flex cursor-pointer items-center gap-2 rounded p-1 hover:bg-lavanda">
                            <input
                                type="checkbox"
                                checked={selecionadas.includes(uc.id)}
                                onChange={() => alternar(uc.id)}
                            />
                            {uc.nome} <span className="text-sm opacity-70">({uc.codigo})</span>
                        </label>
                    ))}
                    {ucs.length === 0 && <p className="text-sm opacity-70">Ainda não há UCs. Cria algumas no separador UCs.</p>}
                </div>
            </fieldset>

            {erro && <p className="text-red-700">{erro}</p>}

            <div className="flex gap-2">
                <button type="submit" disabled={aGuardar} className="rounded-lg bg-rosa px-4 py-2 font-semibold hover:bg-lilas disabled:opacity-50">
                    {aGuardar ? "A guardar..." : "Guardar"}
                </button>
                <button type="button" onClick={onVoltar} className="rounded-lg bg-white px-4 py-2 hover:bg-lavanda">
                    Voltar
                </button>
            </div>
        </form>
    );
}