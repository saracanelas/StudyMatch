import { useState } from "react";
import { createCourse, setCourseUnits, updateCourse } from "../api";
import AssociationPicker from "../components/AssociationPicker";
import { Button, ErrorBanner, Field, FormPage, Notice, SectionTitle } from "../components/ui";
import { CheckIcon, PlusIcon } from "../components/Icons";

const UC_NOUN = { singular: "UC", plural: "UCs", gender: "f" };

/** "Adicionar curso" / "Editar curso" page. Pass `course` to edit it. */
export default function CourseFormPage({ course, units, onCancel, onSaved }) {
    const isEditing = Boolean(course);
    const [name, setName] = useState(course?.name ?? "");
    const [code, setCode] = useState(course?.code ?? "");
    const [unitIds, setUnitIds] = useState(course?.unitIds ?? []);
    const [errorMessage, setErrorMessage] = useState("");
    const [isSaving, setIsSaving] = useState(false);

    /** Creates or updates the course (name, code and associated UCs). */
    async function handleSubmit(event) {
        event.preventDefault();
        setIsSaving(true);
        setErrorMessage("");
        try {
            const data = { name: name.trim(), code: code.trim(), unitIds };
            let courseId = course?.id;
            if (isEditing) {
                await updateCourse(courseId, data);
            } else {
                courseId = (await createCourse(data)).id;
                if (unitIds.length > 0) await setCourseUnits(courseId, unitIds);
            }
            await onSaved({ courseId });
        } catch (error) {
            setErrorMessage(error.message);
            setIsSaving(false);
        }
    }

    const selectedCount = unitIds.length;

    return (
        <FormPage
            breadcrumb={["Cursos", isEditing ? "Editar curso" : "Adicionar curso"]}
            title={isEditing ? "Editar curso" : "Adicionar curso"}
            description={
                isEditing
                    ? "Atualize o nome e o código do curso e gerencie as UCs associadas."
                    : "Crie um curso com nome e código. As UCs podem ser associadas ou desassociadas depois."
            }
            showRequiredHint={isEditing}
            onClose={onCancel}
            onSubmit={handleSubmit}
            footerNote={isEditing ? "As alterações só são aplicadas ao guardar." : "Os dados só são guardados após a criação."}
            actions={
                <>
                    <Button size="lg" onClick={onCancel}>Cancelar</Button>
                    {isEditing && (
                        <Button size="lg" variant="danger" disabled title="A eliminação de cursos ainda não é suportada pelo servidor">
                            Eliminar curso
                        </Button>
                    )}
                    <Button size="lg" variant="primary" type="submit" disabled={isSaving}>
                        {isEditing ? <CheckIcon size={18} /> : <PlusIcon size={18} />}
                        {isSaving ? "A guardar…" : isEditing ? "Guardar alterações" : "Criar curso"}
                    </Button>
                </>
            }
        >
            <ErrorBanner message={errorMessage} />

            <section className="space-y-5">
                <SectionTitle title="Informação do curso" aside={isEditing ? `${course.code} · ${course.name}` : undefined} />
                <Field label="Nome do curso" required maxLength={50} placeholder="Ex.: Engenharia Informática" value={name} onChange={(e) => setName(e.target.value)} />
                <Field
                    label="Código do curso"
                    required
                    maxLength={12}
                    placeholder="Ex.: LEI"
                    value={code}
                    onChange={(e) => setCode(e.target.value)}
                    hint={isEditing ? "Código único no catálogo académico." : undefined}
                />
            </section>

            <section className="space-y-5">
                <SectionTitle
                    title={isEditing ? "UCs associadas" : "Unidades curriculares"}
                    aside={isEditing ? `${selectedCount} UCs` : `${selectedCount} selecionada${selectedCount === 1 ? "" : "s"}`}
                    description={
                        isEditing
                            ? "Associe ou desassocie UCs deste curso. Desassociar não elimina a UC do catálogo."
                            : "Selecione as UCs para associar a este curso. As UCs podem ser associadas ou desassociadas depois."
                    }
                />
                <AssociationPicker
                    mode={isEditing ? "edit" : "create"}
                    items={units}
                    initialIds={course?.unitIds}
                    selectedIds={unitIds}
                    onChange={setUnitIds}
                    searchPlaceholder={isEditing ? "Pesquisar UCs para associar…" : "Pesquisar UCs por nome ou código…"}
                    catalogTitle="Sugestões do catálogo"
                    associatedTitle="UCs associadas"
                    noun={UC_NOUN}
                    emptyCatalogText="Ainda não há UCs no catálogo. Pode criar o curso e associar UCs depois."
                />
                {!isEditing && <p className="text-[13px] text-muted">As UCs podem ser associadas ou desassociadas depois de criado o curso.</p>}
            </section>

            <Notice>
                {isEditing
                    ? "Desassociar uma UC remove-a deste curso, mas não a elimina do catálogo. Eliminar o curso remove todas as suas associações, mas não elimina as UCs."
                    : "Cursos e UCs podem ser eliminados depois de criados. As UCs podem pertencer a vários cursos."}
            </Notice>
        </FormPage>
    );
}
