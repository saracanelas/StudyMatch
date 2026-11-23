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

/** Student: { id, number, name, email, birthday, enrollmentYear, courseId, gradeAverage }. */
const studentFromApi = (student) => ({
    id: student.idStudent,
    number: student.numberStudent,
    name: student.nameStudent,
    email: student.emailStudent,
    birthday: student.birthday ?? "",
    enrollmentYear: student.enrollmentYear,
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
export async function createEnrollment(studentId, unitId) {
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


// ---------- Group configuration ----------
/** Returns { numberOfStudents, roles } of a curricular unit, or null if it has no configuration yet. */
export async function getGroupConfiguration(unitId) {
    const response = await fetch(`${BASE_URL}/units/${unitId}/group-configuration`);
    if (response.status === 404) {
        return null;
    }
    if (!response.ok) {
        throw new Error(`Erro ${response.status} ao carregar a configuração de grupos`);
    }
    const configuration = await response.json();
    return { numberOfStudents: configuration.numberOfStudents, roles: configuration.roles };
}

/** Creates the group configuration of a curricular unit and returns it. Once created it is final. */
export async function createGroupConfiguration(unitId, configuration) {
    const savedConfiguration = await sendRequest(`/units/${unitId}/group-configuration`, {
        method: "POST",
        body: JSON.stringify({ numberOfStudents: configuration.numberOfStudents, roles: configuration.roles }),
    });
    return { numberOfStudents: savedConfiguration.numberOfStudents, roles: savedConfiguration.roles };
}

/** Administrator: deletes the group configuration of a unit and every student answer associated with it. */
export async function deleteGroupConfiguration(unitId) {
    await sendRequest(`/units/${unitId}/group-configuration`, { method: "DELETE" });
}

// ---------- Students of a curricular unit (temporary: identified by number) ----------
/** Returns the student numbers associated with a curricular unit. */
export async function getUnitStudents(unitId) {
    return sendRequest(`/units/${unitId}/students`);
}

/** Associates a student with a curricular unit and returns the saved student number. */
export async function enrollStudent(unitId, studentNumber) {
    const saved = await sendRequest(`/units/${unitId}/students`, {
        method: "POST",
        body: JSON.stringify({ studentNumber }),
    });
    return saved.studentNumber;
}

/** Removes a student (and their role preference) from a curricular unit. */
export async function removeStudent(unitId, studentNumber) {
    await sendRequest(`/units/${unitId}/students/${encodeURIComponent(studentNumber)}`, { method: "DELETE" });
}

// ---------- Student area ----------
/** Returns the units of the student whose group configuration is open. */
export async function getOpenUnitsForStudent(studentNumber) {
    const units = await sendRequest(`/students/${encodeURIComponent(studentNumber)}/units`);
    return units.map(curricularUnitFromApi);
}

/** Returns the notifications of the student, newest first. */
export async function getNotifications(studentNumber) {
    return sendRequest(`/students/${encodeURIComponent(studentNumber)}/notifications`);
}

/** Marks a notification of the student as read and returns it. */
export async function markNotificationAsRead(studentNumber, notificationId) {
    return sendRequest(`/students/${encodeURIComponent(studentNumber)}/notifications/${notificationId}/read`, {
        method: "PATCH",
    });
}

// ---------- Role preferences ----------
/** Returns the roles a student can rank in the assignment of a curricular unit. */
export async function getAvailableRoles(unitId) {
    return sendRequest(`/units/${unitId}/roles`);
}

/** Returns the roles a student already ranked for the unit, or null if they have not submitted yet. */
export async function getRolePreference(unitId, studentNumber) {
    const response = await fetch(`${BASE_URL}/units/${unitId}/role-preferences/${encodeURIComponent(studentNumber)}`);
    if (response.status === 404) {
        return null;
    }
    if (!response.ok) {
        let errorMessage = `Erro ${response.status} ao carregar as preferências`;
        try {
            errorMessage = (await response.json()).error || errorMessage;
        } catch {
            // no JSON body; keep the generic message
        }
        throw new Error(errorMessage);
    }
    return (await response.json()).rankedRoles;
}

/** Submits or updates the ranking of roles of a student and returns the saved ranking. */
export async function submitRolePreference(unitId, studentNumber, rankedRoles) {
    const saved = await sendRequest(`/units/${unitId}/role-preferences/${encodeURIComponent(studentNumber)}`, {
        method: "PUT",
        body: JSON.stringify({ rankedRoles }),
    });
    return saved.rankedRoles;
}
