import { useState } from "react";
import Campo from "./Campo";

/** Form to create a new UC, or to edit one when `inicial` is given. */
export default function UcForm({ inicial, onSubmit, onCancel }) {
    const [nome, setNome] = useState(inicial?.nome ?? "");
    const [codigo, setCodigo] = useState(inicial?.codigo ?? "");

    /** Sends the trimmed name and code to the parent. */
    function handleSubmit(e) {
        e.preventDefault();
        onSubmit({ nome: nome.trim(), codigo: codigo.trim() });
    }

    return (
        <form onSubmit={handleSubmit} className="max-w-md space-y-4 rounded-2xl bg-azul p-6">
            <h2 className="text-xl font-bold">{inicial ? "Editar UC" : "Nova UC"}</h2>
            <Campo label="Nome" value={nome} onChange={(e) => setNome(e.target.value)} required maxLength={50} />
            <Campo label="Código" value={codigo} onChange={(e) => setCodigo(e.target.value)} required maxLength={12} />
            <div className="flex gap-2">
                <button type="submit" className="rounded-lg bg-rosa px-4 py-2 font-semibold hover:bg-lilas">
                    {inicial ? "Guardar" : "Criar"}
                </button>
                <button type="button" onClick={onCancel} className="rounded-lg bg-white px-4 py-2 hover:bg-lavanda">
                    Cancelar
                </button>
            </div>
        </form>
    );
}
