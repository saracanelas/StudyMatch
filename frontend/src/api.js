const BASE_URL = "/api"; // Vite forwards /api to the backend (see vite.config.js)

/** Converts a course as sent by the backend into the shape used by the pages. */
const courseFromApi = (course) => ({
    id: course.idCourse,
    name: course.nameCourse,
    code: course.codeCourse,
    unitIds: (course.curricularUnits ?? []).map((unit) => unit.idUnit),
});

/** Converts a curricular unit as sent by the backend into the shape used by the pages. */
const curricularUnitFromApi = (unit) => ({ id: unit.idUnit, name: unit.nameUnit, code: unit.codeUnit });

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

/** Saves the name, code and associated curricular units of a course and returns the updated course. */
export async function updateCourse(id, course) {
    // 1st: update the name and code; 2nd: update the associated curricular units
    await sendRequest(`/courses/${id}`, {
        method: "PUT",
        body: JSON.stringify({ codeCourse: course.code, nameCourse: course.name }),
    });
    const updatedCourse = await sendRequest(`/courses/${id}/units`, {
        method: "PUT",
        body: JSON.stringify(course.unitIds),
    });
    return courseFromApi(updatedCourse);
}

// ---------- Curricular units ----------
/** Returns every curricular unit. */
export async function getAllCurricularUnits() {
    const units = await sendRequest("/units");
    return units.map(curricularUnitFromApi);
}

/** Creates a curricular unit from { name, code } and returns it. */
export async function createCurricularUnit(unit) {
    const createdUnit = await sendRequest("/units", {
        method: "POST",
        body: JSON.stringify({ codeUnit: unit.code, nameUnit: unit.name }),
    });
    return curricularUnitFromApi(createdUnit);
}

/** Saves the name and code of a curricular unit and returns the updated unit. */
export async function updateCurricularUnit(id, unit) {
    const updatedUnit = await sendRequest(`/units/${id}`, {
        method: "PUT",
        body: JSON.stringify({ codeUnit: unit.code, nameUnit: unit.name }),
    });
    return curricularUnitFromApi(updatedUnit);
}
