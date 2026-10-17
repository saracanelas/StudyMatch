import { useState } from "react";
import TextField from "./TextField";

/** Form to create a new curricular unit, or to edit one when `unitToEdit` is given. */
export default function CurricularUnitForm({ unitToEdit, onSubmit, onCancel }) {
    const [name, setName] = useState(unitToEdit?.name ?? "");
    const [code, setCode] = useState(unitToEdit?.code ?? "");
    const isEditing = Boolean(unitToEdit);

    /** Sends the trimmed name and code to the parent. */
    function handleSubmit(event) {
        event.preventDefault();
        onSubmit({ name: name.trim(), code: code.trim() });
    }

    return (
        <form onSubmit={handleSubmit} className="max-w-md space-y-4 rounded-2xl bg-blue p-6">
            <h2 className="text-xl font-bold">{isEditing ? "Editar unidade curricular" : "Nova unidade curricular"}</h2>
            <TextField label="Nome" value={name} onChange={(event) => setName(event.target.value)} required maxLength={50} />
            <TextField label="Código" value={code} onChange={(event) => setCode(event.target.value)} required maxLength={12} />
            <div className="flex gap-2">
                <button type="submit" className="rounded-lg bg-pink px-4 py-2 font-semibold hover:bg-lilac">
                    {isEditing ? "Guardar alterações" : "Criar unidade curricular"}
                </button>
                <button type="button" onClick={onCancel} className="rounded-lg bg-white px-4 py-2 hover:bg-lavender">
                    Cancelar
                </button>
            </div>
        </form>
    );
}
