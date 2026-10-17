import { useEffect, useState } from "react";
import { getAllCourses, createCourse } from "../api";
import CourseForm from "../components/CourseForm";
import EditCoursePage from "./EditCoursePage";

/** Courses page: shows the courses as square cards and switches to the create or edit form. */
export default function CoursesPage() {
    const [courses, setCourses] = useState([]);
    const [isCreating, setIsCreating] = useState(false);
    const [editingCourseId, setEditingCourseId] = useState(null);
    const [errorMessage, setErrorMessage] = useState("");

    // Loads the courses once when the page opens
    useEffect(() => {
        let isMounted = true;
        getAllCourses()
            .then((loadedCourses) => isMounted && setCourses(loadedCourses))
            .catch((error) => isMounted && setErrorMessage(error.message));
        return () => { isMounted = false; };
    }, []);

    /** Creates a course and adds it to the list, or shows the error message. */
    async function handleCreateCourse(courseData) {
        try {
            const createdCourse = await createCourse(courseData);
            setCourses((currentCourses) => [...currentCourses, createdCourse]);
            setIsCreating(false);
            setErrorMessage("");
        } catch (error) {
            setErrorMessage(error.message);
        }
    }

    /** Replaces a course in the list after it was edited. */
    function handleCourseSaved(savedCourse) {
        setCourses((currentCourses) =>
            currentCourses.map((course) => (course.id === savedCourse.id ? savedCourse : course))
        );
    }

    /** Closes the create form and clears the error. */
    function closeCreateForm() {
        setIsCreating(false);
        setErrorMessage("");
    }

    if (isCreating) {
        return (
            <div>
                {errorMessage && <p className="mb-4 max-w-md rounded-lg bg-white p-3 text-red-700">{errorMessage}</p>}
                <CourseForm onSubmit={handleCreateCourse} onCancel={closeCreateForm} />
            </div>
        );
    }
    if (editingCourseId !== null) {
        return (
            <EditCoursePage
                courseId={editingCourseId}
                onBack={() => setEditingCourseId(null)}
                onSaved={handleCourseSaved}
            />
        );
    }

    return (
        <div>
            <div className="mb-4 flex items-center justify-between">
                <h2 className="text-2xl font-bold">Cursos</h2>
                <button onClick={() => setIsCreating(true)} className="rounded-lg bg-pink px-4 py-2 font-semibold hover:bg-lilac">
                    + Novo curso
                </button>
            </div>

            {errorMessage && <p className="mb-4 rounded-lg bg-white p-3 text-red-700">{errorMessage}</p>}

            <ul className="flex flex-wrap gap-4">
                {courses.map((course) => (
                    <li key={course.id} className="flex size-52 flex-col justify-between rounded-xl bg-blue p-4">
                        <div className="overflow-hidden">
                            <p className="font-semibold break-words">{course.name}</p>
                            <p className="text-sm">Código: {course.code}</p>
                            <p className="text-sm">{course.unitIds?.length ?? 0} unidades curriculares</p>
                        </div>
                        <button onClick={() => setEditingCourseId(course.id)} className="self-start rounded-lg bg-white px-3 py-1 hover:bg-lavender">
                            Editar
                        </button>
                    </li>
                ))}
            </ul>
            {courses.length === 0 && !errorMessage && <p>Ainda não há cursos.</p>}
        </div>
    );
}
