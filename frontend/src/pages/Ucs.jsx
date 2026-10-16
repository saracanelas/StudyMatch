import { useEffect, useState } from "react";
import { listarUcs, criarUc, atualizarUc } from "../api";
import UcForm from "../components/UcForm";

/** Curricular units page: shows the UCs as square cards and switches to the create or edit form. */
export default function Ucs() {
    const [ucs, setUcs] = useState([]);
    const [aCriar, setACriar] = useState(false);
    const [aEditar, setAEditar] = useState(null); // UC que está a ser editada
    const [erro, setErro] = useState("");

    // Loads the UCs once when the page opens
    useEffect(() => {
        let ativo = true;
        listarUcs()
            .then((dados) => ativo && setUcs(dados))
            .catch((e) => ativo && setErro(e.message));
        return () => { ativo = false; };
    }, []);

    /** Creates a UC and adds it to the list, or shows the error message. */
    async function handleCriar(dados) {
        try {
            const nova = await criarUc(dados);
            setUcs((atuais) => [...atuais, nova]);
            setACriar(false);
            setErro("");
        } catch (e) {
            setErro(e.message);
        }
    }

    /** Saves the edited UC and replaces it in the list, or shows the error message. */
    async function handleEditar(dados) {
        try {
            const atualizada = await atualizarUc(aEditar.id, dados);
            setUcs((atuais) => atuais.map((u) => (u.id === atualizada.id ? atualizada : u)));
            setAEditar(null);
            setErro("");
        } catch (e) {
            setErro(e.message);
        }
    }

    /** Closes the create / edit form and clears the error. */
    function fecharFormulario() {
        setACriar(false);
        setAEditar(null);
        setErro("");
    }

    if (aCriar || aEditar) {
        return (
            <div>
                {erro && <p className="mb-4 max-w-md rounded-lg bg-white p-3 text-red-700">{erro}</p>}
                <UcForm
                    key={aEditar?.id ?? "nova"}
                    inicial={aEditar}
                    onSubmit={aEditar ? handleEditar : handleCriar}
                    onCancel={fecharFormulario}
                />
            </div>
        );
    }

    return (
        <div>
            <div className="mb-4 flex items-center justify-between">
                <h2 className="text-2xl font-bold">Unidades Curriculares</h2>
                <button onClick={() => setACriar(true)} className="rounded-lg bg-rosa px-4 py-2 font-semibold hover:bg-lilas">
                    + Nova UC
                </button>
            </div>

            {erro && <p className="mb-4 rounded-lg bg-white p-3 text-red-700">{erro}</p>}

            <ul className="flex flex-wrap gap-4">
                {ucs.map((uc) => (
                    <li key={uc.id} className="flex size-52 flex-col justify-between rounded-xl bg-azul p-4">
                        <div className="overflow-hidden">
                            <p className="font-semibold break-words">{uc.nome}</p>
                            <p className="text-sm">{uc.codigo}</p>
                        </div>
                        <button onClick={() => setAEditar(uc)} className="self-start rounded-lg bg-white px-3 py-1 hover:bg-lavanda">
                            Editar
                        </button>
                    </li>
                ))}
            </ul>
            {ucs.length === 0 && !erro && <p>Ainda não há UCs.</p>}
        </div>
    );
}
