# Employee & Project Management System

A server-rendered Java web application for managing employee records, projects, task assignments, and employee-to-project allocations. It includes role-aware access control, BCrypt password hashing, Hibernate-based persistence, and Thymeleaf views.

**Repository:** [ayushshekhar018/employee-management-system](https://github.com/ayushshekhar018/employee-management-system)

> **Deployment:** This application is packaged as a Java WAR and requires a Jakarta Servlet-compatible application server. It cannot run as a backend on Netlify's static hosting. Use Apache Tomcat (11.x) or another host that explicitly supports the required Java runtime and WAR deployment.

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

## Technology stack

| Area | Technology |
|---|---|
| Language | Java 17 |
| Build and dependency management | Apache Maven |
| Packaging | WAR |
| Web layer | Jakarta Servlet API 6.1 |
| Application server | Apache Tomcat 11.x (compatible Servlet 6.1 container) |
| ORM / persistence | Hibernate ORM 7 |
| Database | MySQL |
| Server-side templates | Thymeleaf 3.1 |
| Password hashing | jBCrypt |
| Tests | JUnit Jupiter 6 and H2 (test scope) |

Dependency versions are defined in [pom.xml](pom.xml). The Servlet API is declared with Maven's `provided` scope because the servlet container supplies it at runtime.

## Architecture

The codebase follows a layered, servlet-based MVC approach:

1. **Servlets** receive HTTP requests, validate request parameters, coordinate application workflows, and select views.
2. **DAO classes** encapsulate Hibernate queries and transactional data operations.
3. **Model classes** define the `Employee`, `Project`, and `Task` persistence entities and their relationships.
4. **Filters** apply request logging and session/role-based route checks.
5. **Thymeleaf configuration and templates** render server-side HTML responses.
6. **Hibernate** maps entities to MySQL tables and manages persistence through a shared `SessionFactory`.

### Main source layout

```text
employee-management-system/
├── pom.xml
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
```

## Prerequisites

Install the following:

- JDK 17
- Apache Maven 3.8 or later
- MySQL Server 8.0 or later
- Apache Tomcat 11.x (or a compatible Servlet 6.1 container)
- IntelliJ IDEA or another Java IDE (optional)

Verify the tools from a terminal:

```bash
java -version
mvn -version
mysql --version
```

Tomcat 11 is the recommended container because the project uses Jakarta Servlet API 6.1. Do not deploy this WAR to a Tomcat version that only supports the older `javax.servlet` namespace.

## Run locally

### 1. Clone the repository

```bash
git clone https://github.com/ayushshekhar018/employee-management-system.git
cd employee-management-system
```

### 2. Create a MySQL database

Connect to your local MySQL server and create the application database:

```sql
CREATE DATABASE employeedb;
```

### 3. Configure Hibernate

Open:

`src/main/resources/hibernate.cfg.xml`

The checked-in development configuration currently targets:

- Host: `localhost`
- Port: `3306`
- Database: `employeedb`
- Username: `root`
- Password: empty

Update the username and password to match your local MySQL setup before starting the app. For example:

```xml
<property name="hibernate.connection.url">jdbc:mysql://localhost:3306/employeedb?useSSL=false&amp;allowPublicKeyRetrieval=true&amp;serverTimezone=UTC</property>
<property name="hibernate.connection.username">YOUR_MYSQL_USERNAME</property>
<property name="hibernate.connection.password">YOUR_MYSQL_PASSWORD</property>
```

The configuration currently contains literal values; setting environment variables in a hosting dashboard alone will **not** override them. Before a public production deployment, change the configuration/bootstrap code to read credentials from environment variables or a secret manager. Use a dedicated MySQL application user with limited privileges rather than the database root account.

The current `hibernate.hbm2ddl.auto=update` setting is convenient for local development, but it is not a substitute for managed schema migrations and backups. Review it before production use. SQL output is also enabled in the current configuration and should be disabled or appropriately controlled in production.

### 4. Build the application

Run from the project root:

```bash
mvn clean package
```

The WAR is generated at:

```text
target/employee-management-system.war
```

### 5. Deploy to Tomcat

Copy the WAR into the Tomcat `webapps` directory and start the server:

```bash
cp target/employee-management-system.war "$CATALINA_HOME/webapps/"
"$CATALINA_HOME/bin/startup.sh"
```

On Windows, use the corresponding `.bat` scripts in Tomcat's `bin` directory.

Open the application at:

```text
http://localhost:8080/employee-management-system/
```

The context path is derived from the WAR filename. If the WAR is renamed, the context path changes accordingly.

## Run tests

Execute the test suite with:

```bash
mvn test
```

The project includes JUnit Jupiter tests and an H2 test dependency. Check that the test configuration in `src/test/resources/hibernate.cfg.xml` is used for test execution; tests should not require a production database.

## Deployment notes

### Netlify compatibility

This repository is a Java Servlet application, not a static frontend. Netlify static hosting does not run a conventional Tomcat server or deploy a Java WAR. Deploy the backend to a Java-capable service that explicitly supports Java 17 and Tomcat 11/WAR deployment.

A suitable production topology is:

1. Java WAR hosted in a Servlet 6.1-compatible runtime.
2. MySQL hosted on a managed or otherwise secured database service.
3. Database credentials supplied through the deployment platform's secret configuration after the application is updated to read them securely.
4. HTTPS enabled at the application host or trusted reverse proxy.

Netlify may be used for a future, separately built static frontend, but it will not host this WAR backend by itself.

## Security and production checklist

Before exposing the application publicly:

- [ ] Replace the local database credentials and empty password configuration.
- [ ] Move database credentials out of source code and into environment-backed configuration or a secrets manager.
- [ ] Use a dedicated database user with minimum required privileges.
- [ ] Restrict database network access to the application host.
- [ ] Enforce HTTPS.
- [ ] Review authentication, session handling, and authorization rules.
- [ ] Add CSRF protection for state-changing form submissions.
- [ ] Review validation and error handling for all user-submitted fields.
- [ ] Replace development schema auto-update with a controlled migration strategy.
- [ ] Disable or appropriately configure SQL logging.
- [ ] Back up the database and test restore procedures.

## Current project status

The repository contains the application source, Maven build configuration, Thymeleaf templates, Hibernate configuration, and automated tests. A production hosting target and production database configuration are not included. Local setup requires a configured MySQL instance and a compatible Servlet container.

## Author

**Ayush Shekhar**  
[GitHub: @ayushshekhar018](https://github.com/ayushshekhar018)

Issues and constructive suggestions are welcome through the repository's [Issues](https://github.com/ayushshekhar018/employee-management-system/issues) section.
