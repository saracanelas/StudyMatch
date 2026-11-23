import { useState } from "react";
import { createStudent, updateStudent } from "../api";
import { Button, ErrorBanner, Field, FormPage, Notice, SectionTitle } from "../components/ui";
import { CheckIcon, PlusIcon } from "../components/Icons";

const CURRENT_YEAR = new Date().getFullYear();
const TODAY = new Date().toISOString().slice(0, 10);

/** "Adicionar estudante" / "Editar estudante" page. Pass `student` to edit it. */
export default function StudentFormPage({ student, courses, onCancel, onSaved }) {
    const isEditing = Boolean(student);
    const [form, setForm] = useState({
        name: student?.name ?? "",
        number: student?.number ?? "",
        email: student?.email ?? "",
        birthday: student?.birthday ?? "",
        enrollmentYear: student?.enrollmentYear ?? CURRENT_YEAR,
        courseId: student?.courseId ?? "",
    });
    const [errorMessage, setErrorMessage] = useState("");
    const [isSaving, setIsSaving] = useState(false);

    const update = (field) => (event) => setForm((current) => ({ ...current, [field]: event.target.value }));

    /** Creates or updates the student. */
    async function handleSubmit(event) {
        event.preventDefault();
        setIsSaving(true);
        setErrorMessage("");
        try {
            const data = {
                ...form,
                name: form.name.trim(),
                number: form.number.trim(),
                email: form.email.trim(),
                courseId: form.courseId ? Number(form.courseId) : null,
            };
            const saved = isEditing ? await updateStudent(student.id, data) : await createStudent(data);
            await onSaved(saved, isEditing);
        } catch (error) {
            setErrorMessage(error.message);
            setIsSaving(false);
        }
    }

    return (
        <FormPage
            breadcrumb={["Estudantes", isEditing ? "Editar estudante" : "Adicionar estudante"]}
            title={isEditing ? "Editar estudante" : "Adicionar estudante"}
            description={
                isEditing
                    ? "Atualize os dados do estudante. A média é calculada automaticamente a partir das notas."
                    : "Registe um estudante. Depois de criado, poderá inscrevê-lo em UCs e registar as notas."
            }
            showRequiredHint
            onClose={onCancel}
            onSubmit={handleSubmit}
            footerNote={isEditing ? "As alterações só são aplicadas ao guardar." : "Os dados só são guardados após a criação."}
            actions={
                <>
                    <Button size="lg" onClick={onCancel}>Cancelar</Button>
                    <Button size="lg" variant="primary" type="submit" disabled={isSaving}>
                        {isEditing ? <CheckIcon size={18} /> : <PlusIcon size={18} />}
                        {isSaving ? "A guardar…" : isEditing ? "Guardar alterações" : "Criar estudante"}
                    </Button>
                </>
            }
        >
            <ErrorBanner message={errorMessage} />

            <section className="space-y-5">
                <SectionTitle title="Dados pessoais" aside={isEditing ? `${student.number} · ${student.name}` : undefined} />
                <Field label="Nome completo" required maxLength={100} placeholder="Ex.: Ana Costa" value={form.name} onChange={update("name")} />
                <div className="grid gap-5 sm:grid-cols-2">
                    <Field label="Email" type="email" required maxLength={100} placeholder="Ex.: ana.costa@studymatch.pt" value={form.email} onChange={update("email")} />
                    <Field label="Data de nascimento" type="date" max={TODAY} value={form.birthday} onChange={update("birthday")} />
                </div>
            </section>

            <section className="space-y-5">
                <SectionTitle title="Dados académicos" />
                <div className="grid gap-5 sm:grid-cols-2">
                    <Field
                        label="Número de estudante"
                        required
                        maxLength={20}
                        placeholder="Ex.: 2024001"
                        value={form.number}
                        onChange={update("number")}
                        hint="Único no sistema."
                    />
                    <Field
                        label="Ano de ingresso"
                        type="number"
                        required
                        min={1950}
                        max={2100}
                        value={form.enrollmentYear}
                        onChange={update("enrollmentYear")}
                    />
                </div>
                <div>
                    <label className="block">
                        <span className="mb-2 block text-sm font-medium text-ink">Curso</span>
                        <select
                            value={form.courseId}
                            onChange={update("courseId")}
                            className="h-12 w-full rounded-lg border border-line bg-field px-3 text-[15px] outline-none focus:border-brand focus:ring-3 focus:ring-brand/15"
                        >
                            <option value="">Sem curso</option>
                            {courses.map((course) => (
                                <option key={course.id} value={course.id}>
                                    {course.code} · {course.name}
                                </option>
                            ))}
                        </select>
                    </label>
                </div>
            </section>

            <Notice>
                A média do estudante é ponderada pelos ECTS de cada UC e é atualizada sempre que uma nota é registada.
            </Notice>
        </FormPage>
    );
}
