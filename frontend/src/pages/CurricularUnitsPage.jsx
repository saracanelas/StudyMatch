import { useEffect, useState } from "react";
import { getAllCurricularUnits, createCurricularUnit, updateCurricularUnit } from "../api";
import CurricularUnitForm from "../components/CurricularUnitForm";

/** Curricular units page: shows the units as square cards and switches to the create or edit form. */
export default function CurricularUnitsPage() {
    const [units, setUnits] = useState([]);
    const [isCreating, setIsCreating] = useState(false);
    const [unitBeingEdited, setUnitBeingEdited] = useState(null);
    const [errorMessage, setErrorMessage] = useState("");

    // Loads the curricular units once when the page opens
    useEffect(() => {
        let isMounted = true;
        getAllCurricularUnits()
            .then((loadedUnits) => isMounted && setUnits(loadedUnits))
            .catch((error) => isMounted && setErrorMessage(error.message));
        return () => { isMounted = false; };
    }, []);

    /** Creates a curricular unit and adds it to the list, or shows the error message. */
    async function handleCreateUnit(unitData) {
        try {
            const createdUnit = await createCurricularUnit(unitData);
            setUnits((currentUnits) => [...currentUnits, createdUnit]);
            setIsCreating(false);
            setErrorMessage("");
        } catch (error) {
            setErrorMessage(error.message);
        }
    }

    /** Saves the edited curricular unit and replaces it in the list, or shows the error message. */
    async function handleUpdateUnit(unitData) {
        try {
            const updatedUnit = await updateCurricularUnit(unitBeingEdited.id, unitData);
            setUnits((currentUnits) =>
                currentUnits.map((unit) => (unit.id === updatedUnit.id ? updatedUnit : unit))
            );
            setUnitBeingEdited(null);
            setErrorMessage("");
        } catch (error) {
            setErrorMessage(error.message);
        }
    }

    /** Closes the create / edit form and clears the error. */
    function closeForm() {
        setIsCreating(false);
        setUnitBeingEdited(null);
        setErrorMessage("");
    }

    if (isCreating || unitBeingEdited) {
        return (
            <div>
                {errorMessage && <p className="mb-4 max-w-md rounded-lg bg-white p-3 text-red-700">{errorMessage}</p>}
                <CurricularUnitForm
                    key={unitBeingEdited?.id ?? "new"}
                    unitToEdit={unitBeingEdited}
                    onSubmit={unitBeingEdited ? handleUpdateUnit : handleCreateUnit}
                    onCancel={closeForm}
                />
            </div>
        );
    }

    return (
        <div>
            <div className="mb-4 flex items-center justify-between">
                <h2 className="text-2xl font-bold">Unidades Curriculares</h2>
                <button onClick={() => setIsCreating(true)} className="rounded-lg bg-pink px-4 py-2 font-semibold hover:bg-lilac">
                    + Nova unidade curricular
                </button>
            </div>

            {errorMessage && <p className="mb-4 rounded-lg bg-white p-3 text-red-700">{errorMessage}</p>}

            <ul className="flex flex-wrap gap-4">
                {units.map((unit) => (
                    <li key={unit.id} className="flex size-52 flex-col justify-between rounded-xl bg-blue p-4">
                        <div className="overflow-hidden">
                            <p className="font-semibold break-words">{unit.name}</p>
                            <p className="text-sm">Código: {unit.code}</p>
                        </div>
                        <button onClick={() => setUnitBeingEdited(unit)} className="self-start rounded-lg bg-white px-3 py-1 hover:bg-lavender">
                            Editar
                        </button>
                    </li>
                ))}
            </ul>
            {units.length === 0 && !errorMessage && <p>Ainda não há unidades curriculares.</p>}
        </div>
    );
}
