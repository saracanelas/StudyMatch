const BASE_URL = "/api"; // Vite forwards /api to the backend (see vite.config.js)

// ---------- Conversions between the backend JSON and the shapes used by the pages ----------

/** Course: { id, name, code, unitIds }. */
const courseFromApi = (course) => ({
    id: course.idCourse,
    name: course.nameCourse,
    code: course.codeCourse,
    unitIds: (course.curricularUnits ?? []).map((unit) => unit.idUnit),
});

/** Curricular unit: { id, name, code, ects }. */
const curricularUnitFromApi = (unit) => ({
    id: unit.idUnit,
    name: unit.nameUnit,
    code: unit.codeUnit,
    ects: unit.ects,
});

/** Student: { id, number, name, email, birthday, enrollmentYear, className, courseId, gradeAverage }. */
const studentFromApi = (student) => ({
    id: student.idStudent,
    number: student.numberStudent,
    name: student.nameStudent,
    email: student.emailStudent,
    birthday: student.birthday ?? "",
    enrollmentYear: student.enrollmentYear,
    className: student.classStudent ?? "",
    courseId: student.course?.idCourse ?? null,
    gradeAverage: student.gradeAverage ?? null,
});

/** Student as expected by the backend, from the form data of StudentFormPage. */
const studentToApi = (student) => ({
    numberStudent: student.number,
    nameStudent: student.name,
    emailStudent: student.email,
    birthday: student.birthday || null,
    enrollmentYear: Number(student.enrollmentYear),
    classStudent: student.className || null,
    course: student.courseId ? { idCourse: student.courseId } : null,
});

/** Enrollment of a student in a unit: { id, studentId, unitId, grade, student }. */
const enrollmentFromApi = (enrollment) => ({
    id: enrollment.idEnrollment,
    studentId: enrollment.student?.idStudent,
    unitId: enrollment.curricularUnit?.idUnit,
    grade: enrollment.cuGrade ?? null,
    student: enrollment.student ? studentFromApi(enrollment.student) : null,
});

/** Sends a request to the backend and returns the JSON body, throwing an Error with the server message on failure. */
async function sendRequest(path, options) {
    const response = await fetch(`${BASE_URL}${path}`, {
        headers: { "Content-Type": "application/json" },
        ...options,
    });

    if (!response.ok) {
        let errorMessage = `Erro ${response.status} ao contactar o servidor`;
        try {
            const body = await response.json();
            errorMessage = body.message || body.error || errorMessage;
        } catch {
            // the error response had no JSON body; keep the generic message
        }
        throw new Error(errorMessage);
    }
    return response.status === 204 ? null : response.json();
}

// ---------- Courses ----------

/** Returns every course. */
export async function getAllCourses() {
    const courses = await sendRequest("/courses");
    return courses.map(courseFromApi);
}

/** Returns the course with the given id. */
export async function getCourseById(id) {
    return courseFromApi(await sendRequest(`/courses/${id}`));
}

/** Creates a course from { name, code } and returns it. */
export async function createCourse(course) {
    const createdCourse = await sendRequest("/courses", {
        method: "POST",
        body: JSON.stringify({ codeCourse: course.code, nameCourse: course.name }),
    });
    return courseFromApi(createdCourse);
}

/** Saves the name and code of a course (and its units, when `unitIds` is given) and returns the updated course. */
export async function updateCourse(id, course) {
    const updatedCourse = await sendRequest(`/courses/${id}`, {
        method: "PUT",
        body: JSON.stringify({ codeCourse: course.code, nameCourse: course.name }),
    });
    if (course.unitIds) return setCourseUnits(id, course.unitIds);
    return courseFromApi(updatedCourse);
}

/** Replaces the curricular units associated with a course and returns the updated course. */
export async function setCourseUnits(id, unitIds) {
    const updatedCourse = await sendRequest(`/courses/${id}/units`, {
        method: "PUT",
        body: JSON.stringify(unitIds),
    });
    return courseFromApi(updatedCourse);
}

// ---------- Curricular units ----------

/** Returns every curricular unit. */
export async function getAllCurricularUnits() {
    const units = await sendRequest("/units");
    return units.map(curricularUnitFromApi);
}

/** Creates a curricular unit from { name, code, ects } and returns it. */
export async function createCurricularUnit(unit) {
    const createdUnit = await sendRequest("/units", {
        method: "POST",
        body: JSON.stringify({ codeUnit: unit.code, nameUnit: unit.name, ects: unit.ects }),
    });
    return curricularUnitFromApi(createdUnit);
}

/** Saves the name, code and ECTS of a curricular unit and returns the updated unit. */
export async function updateCurricularUnit(id, unit) {
    const updatedUnit = await sendRequest(`/units/${id}`, {
        method: "PUT",
        body: JSON.stringify({ codeUnit: unit.code, nameUnit: unit.name, ects: unit.ects }),
    });
    return curricularUnitFromApi(updatedUnit);
}

// ---------- Students ----------

/** Returns every student. */
export async function getAllStudents() {
    const students = await sendRequest("/students");
    return students.map(studentFromApi);
}

/** Creates a student from the form data and returns it. */
export async function createStudent(student) {
    const createdStudent = await sendRequest("/students", {
        method: "POST",
        body: JSON.stringify(studentToApi(student)),
    });
    return studentFromApi(createdStudent);
}

/** Saves the data of a student and returns the updated student. */
export async function updateStudent(id, student) {
    const updatedStudent = await sendRequest(`/students/${id}`, {
        method: "PUT",
        body: JSON.stringify(studentToApi(student)),
    });
    return studentFromApi(updatedStudent);
}

// ---------- Enrollments and grades ----------

/** Returns every enrollment of every student. */
export async function getAllEnrollments() {
    const enrollments = await sendRequest("/enrollments");
    return enrollments.map(enrollmentFromApi);
}

/** Returns the enrollments (with the students) of a curricular unit. */
export async function getEnrollmentsOfUnit(unitId) {
    const enrollments = await sendRequest(`/units/${unitId}/enrollments`);
    return enrollments.map(enrollmentFromApi);
}

/** Enrolls a student in a curricular unit and returns the new enrollment. */
export async function enrollStudent(studentId, unitId) {
    const enrollment = await sendRequest("/enrollments", {
        method: "POST",
        body: JSON.stringify({ studentId, unitId }),
    });
    return enrollmentFromApi(enrollment);
}

/** Registers or changes the grade (0-20) of an enrollment; null clears it. */
export async function setEnrollmentGrade(enrollmentId, grade) {
    const enrollment = await sendRequest(`/enrollments/${enrollmentId}/grade`, {
        method: "PUT",
        body: JSON.stringify({ grade }),
    });
    return enrollmentFromApi(enrollment);
}

/** Removes an enrollment (and its grade). */
export async function unenrollStudent(enrollmentId) {
    await sendRequest(`/enrollments/${enrollmentId}`, { method: "DELETE" });
}
