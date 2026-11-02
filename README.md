# Employee & Project Management System

A server-rendered Java web application for managing employee records, projects, task assignments, and employee-to-project allocations. It includes role-aware access control, BCrypt password hashing, Hibernate-based persistence, and Thymeleaf views.

<p align="left">
  <a href="https://employee-management-system-wavl.onrender.com/login"><img alt="Live Demo" src="https://img.shields.io/badge/Live%20Demo-Render-46A758?style=for-the-badge&logo=render&logoColor=white"></a>
  <a href="https://github.com/ayushshekhar018/employee-management-system"><img alt="GitHub" src="https://img.shields.io/badge/GitHub-Repository-181717?style=for-the-badge&logo=github"></a>
</p>

**Live application:** https://employee-management-system-wavl.onrender.com/login  
**Repository:** https://github.com/ayushshekhar018/employee-management-system

> **Deployment status:** The application is deployed and has passed Render's \`/login\` health check. The public login page is reachable. Database-backed workflows should be considered verified only after a successful login and CRUD test against the hosted TiDB database.

## Features

### Authentication and access
- Login and logout using server-side HTTP sessions.
- BCrypt password hashing and verification.
- Automatic rehashing of older BCrypt hashes after successful authentication when the configured work factor is outdated.
- Route-level authentication checks and administrator-only route checks.

### Employee management
- Create, view, update, and delete employee records.
- Store employee details such as department, designation, email, salary, and role.
- View an employee's assigned projects.

### Project management
- Create, view, update, and delete projects.
- Track client, start date, budget, status, and maximum team size.
- View the employees currently allocated to each project.

### Allocation management
- Allocate an employee to an individual project.
- Review a multi-project allocation before confirming it.
- Validate project status, team capacity, and duplicate allocations.
- Display per-project success and failure results for batch allocation.

### Task management
- Create tasks under a project and optionally assign them to an employee.
- Set priority, status, and due date.
- Show an administrator-wide task board or an employee-specific task list.
- Restrict employee task-status changes to tasks assigned to that employee.

## Live deployment

The public demo uses the following architecture:

| Component | Platform / configuration |
|---|---|
| Application hosting | [Render](https://render.com/) |
| Application runtime | Docker container running Apache Tomcat 11 |
| Build | Maven; packages the project as a WAR |
| Database hosting | [TiDB Cloud Starter](https://tidbcloud.com/) |
| Database compatibility | MySQL-compatible |
| Database name | \`employeedb\` |
| Database region | AWS Singapore (\`ap-southeast-1\`) |
| Public application URL | [employee-management-system-wavl.onrender.com/login](https://employee-management-system-wavl.onrender.com/login) |
| Deployment branch | \`deploy/render-tidb\` |

### Deployment configuration in this repository

The \`deploy/render-tidb\` branch contains the hosting configuration:

- \`Dockerfile\` — builds the WAR with Maven and runs it on Tomcat 11.
- \`docker-entrypoint.sh\` — configures Tomcat to listen on Render's \`PORT\`.
- \`.dockerignore\` — excludes IDE and generated build files from the Docker build context.
- \`render.yaml\` — Render Blueprint configuration.
- \`HibernateUtil.java\` — reads hosted database connection settings from environment variables when \`DB_HOST\` is configured.

The deployment branch is kept separate from \`master\` so that the local-development baseline and hosted deployment configuration remain distinct.

### Environment variables

The Render service uses these variables. **Actual credentials are intentionally not stored in this repository.**

| Variable | Purpose | Hosted value / setting |
|---|---|---|
| \`DB_HOST\` | TiDB endpoint hostname | Stored in Render environment settings |
| \`DB_PORT\` | TiDB MySQL-compatible port | \`4000\` |
| \`DB_NAME\` | Database/schema name | \`employeedb\` |
| \`DB_USERNAME\` | TiDB database username | Stored in Render environment settings |
| \`DB_PASSWORD\` | TiDB database password | Stored as a secret in Render |
| \`DB_SSL_MODE\` | JDBC TLS verification mode | \`VERIFY_IDENTITY\` |

When \`DB_HOST\` is absent, local development uses the values in \`src/main/resources/hibernate.cfg.xml\`. When \`DB_HOST\` is present, the application requires \`DB_NAME\`, \`DB_USERNAME\`, and \`DB_PASSWORD\`; \`DB_PORT\` defaults to \`4000\` and \`DB_SSL_MODE\` defaults to \`VERIFY_IDENTITY\`.

### Free-tier behaviour

This deployment is intended as a portfolio/demo environment, not production hosting.

- Render's free web service may spin down after inactivity. The first request after an idle period can be noticeably slower.
- Render free services do not provide a persistent local filesystem. Application data belongs in the external database, not in container-local files.
- TiDB Cloud Starter has usage and storage quotas. It is not an unlimited database plan; check the provider's current limits and usage dashboard.
- Availability, quotas, and free-tier terms can change. Review the providers' current documentation before relying on the demo for important data.

Provider documentation: [Render Free](https://render.com/docs/free) · [Render Docker web services](https://render.com/docs/web-services) · [TiDB Cloud Starter](https://docs.pingcap.com/tidbcloud/select-cluster-tier/).

## Technology stack

| Area | Technology |
|---|---|
| Language | Java 17 |
| Build and dependency management | Apache Maven |
| Packaging | WAR |
| Web layer | Jakarta Servlet API 6.1 |
| Application server | Apache Tomcat 11.x |
| Container | Docker |
| ORM / persistence | Hibernate ORM 7 |
| Database | MySQL / TiDB Cloud (MySQL-compatible) |
| Server-side templates | Thymeleaf 3.1 |
| Password hashing | jBCrypt |
| Tests | JUnit Jupiter 6 and H2 (test scope) |

Dependency versions are defined in [pom.xml](pom.xml). The Servlet API is declared with Maven's \`provided\` scope because the servlet container supplies it at runtime.

## Architecture

The codebase follows a layered, servlet-based MVC approach:

1. **Servlets** receive HTTP requests, validate request parameters, coordinate application workflows, and select views.
2. **DAO classes** encapsulate Hibernate queries and transactional data operations.
3. **Model classes** define the \`Employee\`, \`Project\`, and \`Task\` persistence entities and their relationships.
4. **Filters** apply request logging and session/role-based route checks.
5. **Thymeleaf configuration and templates** render server-side HTML responses.
6. **Hibernate** maps entities to database tables and manages persistence through a shared \`SessionFactory\`.

### Main source layout

\`\`\`text
employee-management-system/
├── pom.xml
├── Dockerfile                  # Render deployment image
├── docker-entrypoint.sh        # Render PORT configuration
├── render.yaml                 # Render Blueprint
├── .dockerignore
├── src/
│   ├── main/
│   │   ├── java/com/employeemanagement/
│   │   │   ├── config/       # Thymeleaf lifecycle and rendering configuration
│   │   │   ├── dao/          # Employee, project, task, and user data access
│   │   │   ├── filter/       # Authentication and request logging filters
│   │   │   ├── model/        # Hibernate entities
│   │   │   ├── servlet/      # HTTP request handlers
│   │   │   └── util/         # Hibernate and password utilities
│   │   ├── resources/
│   │   │   └── hibernate.cfg.xml
│   │   └── webapp/
│   │       ├── WEB-INF/
│   │       │   ├── templates/  # Thymeleaf HTML templates
│   │       │   └── web.xml
│   │       ├── css/
│   │       └── index.jsp
│   └── test/
│       ├── java/              # DAO, model, and utility tests
│       └── resources/         # Test Hibernate configuration
└── README.md
\`\`\`

## Run locally

### Prerequisites

- JDK 17
- Apache Maven 3.8 or later
- MySQL Server 8.0 or later
- Apache Tomcat 11.x (or a compatible Servlet 6.1 container)
- IntelliJ IDEA or another Java IDE (optional)

Verify the tools:

\`\`\`bash
java -version
mvn -version
mysql --version
\`\`\`

### 1. Clone the repository

\`\`\`bash
git clone https://github.com/ayushshekhar018/employee-management-system.git
cd employee-management-system
\`\`\`

### 2. Create a MySQL database

\`\`\`sql
CREATE DATABASE employeedb;
\`\`\`

### 3. Configure Hibernate

For local development, open \`src/main/resources/hibernate.cfg.xml\` and update the connection properties to match your local MySQL installation:

\`\`\`xml
<property name="hibernate.connection.url">jdbc:mysql://localhost:3306/employeedb?useSSL=false&amp;allowPublicKeyRetrieval=true&amp;serverTimezone=UTC</property>
<property name="hibernate.connection.username">YOUR_MYSQL_USERNAME</property>
<property name="hibernate.connection.password">YOUR_MYSQL_PASSWORD</property>
\`\`\`

Use a dedicated local database user where possible. Do not commit real credentials.

The current \`hibernate.hbm2ddl.auto=update\` setting is convenient for development, but it is not a substitute for managed schema migrations and backups. SQL output is enabled in the current Hibernate configuration; review this before production use.

### 4. Build the application

\`\`\`bash
mvn clean package
\`\`\`

The WAR is generated at:

\`\`\`text
target/employee-management-system.war
\`\`\`

### 5. Deploy locally to Tomcat

Copy the WAR into Tomcat's \`webapps\` directory and start the server:

\`\`\`bash
cp target/employee-management-system.war "$CATALINA_HOME/webapps/"
"$CATALINA_HOME/bin/startup.sh"
\`\`\`

Open:

\`\`\`text
http://localhost:8080/employee-management-system/
\`\`\`

Tomcat 11 is recommended because the project uses Jakarta Servlet API 6.1. Do not deploy this WAR to a container that only supports the older \`javax.servlet\` namespace.

## Run tests

\`\`\`bash
mvn test
\`\`\`

The project includes JUnit Jupiter tests and an H2 test dependency. Tests should use the configuration in \`src/test/resources/hibernate.cfg.xml\` and should not require the hosted production database.

## Initial administrator account

The repository does not currently seed a default administrator account automatically. After the database tables have been created, create an administrator using the schema's actual column definitions and a BCrypt password hash generated with the project's \`PasswordUtil.hash()\` method. Never store a plain-text password in the password column.

## Security and production considerations

Before using the application for real organizational data:

- Store database credentials only in environment-backed secrets or a secrets manager.
- Use a dedicated database account with minimum required privileges.
- Restrict database network access and enforce TLS.
- Enforce HTTPS.
- Review authentication, session handling, and authorization rules.
- Add CSRF protection for state-changing form submissions.
- Review validation and error handling for all user-submitted fields.
- Replace development schema auto-update with a controlled migration strategy.
- Disable or appropriately configure SQL logging.
- Back up the database and test restore procedures.
- Do not rely on free-tier hosting for production availability or critical data.

## Author

**Ayush Shekhar**  
[GitHub: @ayushshekhar018](https://github.com/ayushshekhar018)

Issues and constructive suggestions are welcome through the repository's [Issues](https://github.com/ayushshekhar018/employee-management-system/issues) section.
