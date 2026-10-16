import { useState } from "react";
import Campo from "./Campo";

/** Form to create a new course. */
export default function CursoForm({ onSubmit, onCancel }) {
    const [nome, setNome] = useState("");
    const [codigo, setCodigo] = useState("");

    /** Sends the trimmed name and code to the parent. */
    function handleSubmit(e) {
        e.preventDefault();
        onSubmit({ nome: nome.trim(), codigo: codigo.trim() });
    }

    return (
        <form onSubmit={handleSubmit} className="max-w-md space-y-4 rounded-2xl bg-azul p-6">
            <h2 className="text-xl font-bold">Novo Curso</h2>
            <Campo label="Nome" value={nome} onChange={(e) => setNome(e.target.value)} required maxLength={50} />
            <Campo label="Código" value={codigo} onChange={(e) => setCodigo(e.target.value)} required maxLength={12} />
            <div className="flex gap-2">
                <button type="submit" className="rounded-lg bg-rosa px-4 py-2 font-semibold hover:bg-lilas">
                    Criar
                </button>
                <button type="button" onClick={onCancel} className="rounded-lg bg-white px-4 py-2 hover:bg-lavanda">
                    Cancelar
                </button>
            </div>
        </form>
    );
}