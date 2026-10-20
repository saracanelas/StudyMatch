# StudyMatch

StudyMatch is a full-stack platform being developed to support the formation of
student groups using academic information, competencies and other relevant
profile data. The long-term objective is to create meaningful student profiles
and use them to support fairer and more effective group formation.


## Requirements

Before running the project, install:

- Java 21 or later;
- Node.js and npm;
- MySQL 8 or a compatible MySQL server.

## Configuration

The backend uses environment variables for database and administrator
configuration.

From the `backend` directory, configure:

```text
DB_USERNAME=your_mysql_username
DB_PASSWORD=your_mysql_password
```

The application uses the `studymatch` MySQL database. The database is created
automatically if it does not already exist, provided that the MySQL user has the
required permissions.

Passwords and other credentials must not be committed to the repository.

## Note

During the development phase we have it configured so that if there is no
Admin currently at the database, a user Admin is created with the following 
credentials.

```text
ADMIN_NAME=Administrator
ADMIN_EMAIL=admin@example.com
ADMIN_PASSWORD=change-this-password
```


## Running the Application

### Start the Backend

On Windows:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

On macOS or Linux:

```bash
cd backend
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`.

### Start the Frontend

In a second terminal:

```bash
cd frontend
npm install
npm run dev
```

The frontend will normally be available at `http://localhost:5173`.

## Build and Test

The Maven Wrapper allows the backend to be built without requiring a global
Maven installation.

On Windows:

```powershell
cd backend
.\mvnw.cmd clean test
.\mvnw.cmd clean package
```

On macOS or Linux:

```bash
cd backend
./mvnw clean test
./mvnw clean package
```

The backend tests use JUnit 5 and Spring Boot Test. JaCoCo is configured to
generate a coverage report in `backend/target/site/jacoco`.


## Project Documentation

- [`docs/architecture.md`](docs/architecture.md) - system architecture;
- [`docs/usecases.md`](docs/usecases.md) - use cases;
- [`docs/workflow.md`](docs/workflow.md) - GitHub development workflow.

## Academic Context

StudyMatch is being developed as part of the Software Quality project. The
product requirements evolve across different sprints. The implementation
should therefore remain modular, testable and flexible enough to support future
student-profile, academic-history and group-formation requirements.
