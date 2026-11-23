import { useState } from "react";
import { createCurricularUnit, setCourseUnits, updateCurricularUnit } from "../api";
import AssociationPicker from "../components/AssociationPicker";
import { Button, ErrorBanner, Field, FormPage, Notice, SectionTitle } from "../components/ui";
import { CheckIcon, LinkIcon, PlusIcon } from "../components/Icons";

const COURSE_NOUN = { singular: "curso", plural: "cursos", gender: "m" };

/**
 * Updates the course ↔ UC links from the UC side. The backend only exposes
 * PUT /courses/{id}/units, so every course whose membership changed is saved.
 */
async function syncUnitCourses(unitId, courses, selectedCourseIds) {
    const changed = courses.filter((course) => course.unitIds.includes(unitId) !== selectedCourseIds.includes(course.id));
    for (const course of changed) {
        const unitIds = selectedCourseIds.includes(course.id)
            ? [...course.unitIds, unitId]
            : course.unitIds.filter((id) => id !== unitId);
        await setCourseUnits(course.id, unitIds);
    }
}

/** "Adicionar unidade curricular" / "Editar unidade curricular" page. Pass `unit` to edit it. */
export default function UnitFormPage({ unit, courses, onCancel, onSaved }) {
    const isEditing = Boolean(unit);
    const initialCourseIds = isEditing ? courses.filter((course) => course.unitIds.includes(unit.id)).map((course) => course.id) : [];

    const [name, setName] = useState(unit?.name ?? "");
    const [code, setCode] = useState(unit?.code ?? "");
    const [ects, setEcts] = useState(unit?.ects ?? 6);
    const [courseIds, setCourseIds] = useState(initialCourseIds);
    const [errorMessage, setErrorMessage] = useState("");
    const [isSaving, setIsSaving] = useState(false);

    /** Creates or updates the UC, then saves its course associations. */
    async function handleSubmit(event) {
        event.preventDefault();
        setIsSaving(true);
        setErrorMessage("");
        try {
            const data = { name: name.trim(), code: code.trim(), ects: Number(ects) };
            const unitId = isEditing ? (await updateCurricularUnit(unit.id, data)).id : (await createCurricularUnit(data)).id;
            await syncUnitCourses(unitId, courses, courseIds);
            await onSaved({ unitId });
        } catch (error) {
            setErrorMessage(error.message);
            setIsSaving(false);
        }
    }

    const selectedCount = courseIds.length;
    const isShared = initialCourseIds.length > 1;

    return (
        <FormPage
            breadcrumb={["Cursos e unidades curriculares", isEditing ? "Editar UC" : "Adicionar UC"]}
            title={isEditing ? "Editar unidade curricular" : "Adicionar unidade curricular"}
            description={
                isEditing
                    ? "Atualize o nome e o código da UC e gerencie as suas associações com cursos."
                    : "Crie uma UC com nome e código. Pode associá-la a cursos agora ou mais tarde."
            }
            showRequiredHint
            onClose={onCancel}
            onSubmit={handleSubmit}
            footerNote={
                isEditing
                    ? "As alterações só são aplicadas ao guardar. A eliminação da UC remove a UC do catálogo e desassocia-a de todos os cursos, sem eliminar os cursos."
                    : "Os dados só são guardados após a criação."
            }
            actions={
                <>
                    <Button size="lg" onClick={onCancel}>Cancelar</Button>
                    {isEditing && (
                        <Button size="lg" variant="danger" disabled title="A eliminação de UCs ainda não é suportada pelo servidor">
                            Eliminar UC
                        </Button>
                    )}
                    <Button size="lg" variant="primary" type="submit" disabled={isSaving}>
                        {isEditing ? <CheckIcon size={18} /> : <PlusIcon size={18} />}
                        {isSaving ? "A guardar…" : isEditing ? "Guardar alterações" : "Criar UC"}
                    </Button>
                </>
            }
        >
            <ErrorBanner message={errorMessage} />

            <section className="space-y-5">
                <SectionTitle title="Informação da UC" aside={isEditing ? `${unit.code}${isShared ? " · UC partilhada" : ""}` : undefined} />
                <Field label="Nome da unidade curricular" required maxLength={50} placeholder="Ex.: Programação I" value={name} onChange={(e) => setName(e.target.value)} />
                <div className="grid gap-5 sm:grid-cols-[2fr_1fr]">
                    <Field label="Código da UC" required maxLength={12} placeholder="Ex.: LEI101" value={code} onChange={(e) => setCode(e.target.value)} />
                    <Field
                        label="ECTS"
                        type="number"
                        required
                        min={1}
                        max={60}
                        value={ects}
                        onChange={(e) => setEcts(e.target.value)}
                        hint="Peso da UC na média dos estudantes."
                    />
                </div>
            </section>

            <section className="space-y-5">
                <SectionTitle
                    title="Cursos associados"
                    aside={
                        isEditing
                            ? `${selectedCount} curso${selectedCount === 1 ? "" : "s"} selecionado${selectedCount === 1 ? "" : "s"}`
                            : `${selectedCount} selecionado${selectedCount === 1 ? "" : "s"}`
                    }
                    description={
                        isEditing
                            ? "Associe ou desassocie cursos sem eliminar a UC nem os cursos. As alterações só são aplicadas ao guardar."
                            : "Selecione um ou mais cursos para associar à UC. Pode criar a UC sem cursos e associá-los depois."
                    }
                />
                <AssociationPicker
                    mode={isEditing ? "edit" : "create"}
                    variant="cards"
                    items={courses}
                    initialIds={initialCourseIds}
                    selectedIds={courseIds}
                    onChange={setCourseIds}
                    searchPlaceholder={isEditing ? "Pesquisar para associar mais cursos…" : "Pesquisar cursos por nome ou código…"}
                    catalogTitle="Cursos disponíveis"
                    noun={COURSE_NOUN}
                    emptyCatalogText="Ainda não há cursos. Pode criar a UC e associá-la a cursos depois."
                />
                {isEditing && <p className="text-[13px] text-muted">Desmarque um curso para remover apenas essa associação, sem eliminar a UC nem o curso.</p>}
            </section>

            {isEditing && isShared ? (
                <Notice icon={LinkIcon} tone="rose">
                    Esta UC é partilhada por {initialCourseIds.length} cursos. Alterar o nome ou o código atualiza a informação em todos esses cursos.
                </Notice>
            ) : (
                !isEditing && (
                    <Notice>Pode criar a UC sem a associar a um curso. Depois de criada, poderá associá-la, desassociá-la ou eliminá-la.</Notice>
                )
            )}
        </FormPage>
    );
}
