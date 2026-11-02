# Employee Management System

A web-based Employee Management System built with Java, Jakarta Servlets, Hibernate ORM, Thymeleaf, and MySQL. The application provides a central interface for managing employees, projects, tasks, and project allocations.

> **Deployment note:** This is a Java Servlet application packaged as a WAR file. Netlify is designed primarily for static sites and serverless functions; it does not run a traditional Java Servlet container such as Apache Tomcat. Deploy the Java application to a Java-compatible host (for example, a VPS or a platform that supports Docker/Tomcat). Netlify can optionally host a separate static frontend, but it cannot host this WAR application by itself.

## Features

- **Authentication:** Login and logout flow with servlet-based authentication filtering.
- **Employee management:** Create, view, update, and delete employee records.
- **Project management:** Create and manage projects and view project details.
- **Task management:** Create tasks, assign them to projects, update task status, and delete tasks.
- **Employee/project allocation:** Allocate employees to projects, including multi-employee allocation and review/confirmation flows.
- **Employee and project views:** View an employee's projects and the employees assigned to a project.
- **Server-rendered pages:** Thymeleaf templates are rendered on the server.
- **Persistence:** Hibernate ORM manages application entities stored in MySQL.
- **Password utilities:** BCrypt-based password hashing utility is included.

## Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Build | Apache Maven |
| Packaging | WAR |
| Web layer | Jakarta Servlet API 6.1 |
| Application server | Apache Tomcat (Servlet 6.1-compatible version) |
| ORM | Hibernate ORM 7 |
| Database | MySQL |
| Template engine | Thymeleaf 3 |
| Password hashing | jBCrypt |
| Testing | JUnit 6 and H2 (test scope) |

## Architecture

The project follows a layered structure:

- **Servlets** handle HTTP requests, application flow, and form submissions.
- **DAO classes** encapsulate database operations.
- **Model classes** represent Employee, Project, and Task entities.
- **Hibernate** maps entities to relational database tables.
- **Thymeleaf templates** render dynamic HTML pages on the server.
- **Filters** provide request authentication checks and request logging.

## Project Structure

```text
employee-management-system/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/employeemanagement/
│   │   │   ├── config/       # Thymeleaf configuration
│   │   │   ├── dao/          # Employee, Project, Task and User DAOs
│   │   │   ├── filter/       # Authentication and request logging filters
│   │   │   ├── model/        # Employee, Project and Task entities
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
│       ├── java/              # DAO, model and utility tests
│       └── resources/         # Test Hibernate configuration
└── README.md
```

## Prerequisites

Install the following before running the project locally:

- JDK 17
- Apache Maven 3.8+
- MySQL Server 8.0+
- Apache Tomcat with support for Jakarta Servlet 6.1
- IntelliJ IDEA (recommended, but any Java IDE can be used)

Check your installations:

```bash
java -version
mvn -version
mysql --version
```

## Local Setup

### 1. Clone the repository

```bash
git clone https://github.com/ayushshekhar018/employee-management-system.git
cd employee-management-system
```

### 2. Create the MySQL database

Log in to MySQL and create the database:

```sql
CREATE DATABASE employeedb;
```

The current Hibernate configuration points to a local MySQL database named `employeedb`.

### 3. Configure database credentials

Open:

`src/main/resources/hibernate.cfg.xml`

Update the connection properties to match your MySQL installation:

```xml
<property name="hibernate.connection.url">
    jdbc:mysql://localhost:3306/employeedb?useSSL=false&amp;allowPublicKeyRetrieval=true&amp;serverTimezone=UTC
</property>
<property name="hibernate.connection.username">YOUR_MYSQL_USERNAME</property>
<property name="hibernate.connection.password">YOUR_MYSQL_PASSWORD</property>
```

**Important:** The checked-in configuration currently has an empty password and uses the MySQL `root` user. Set credentials appropriate for your local environment. Do not commit real production credentials to GitHub. For production, load secrets from environment variables or a secrets manager and use a dedicated database user with limited privileges.

The current setting `hibernate.hbm2ddl.auto=update` is intended for development. Review the schema-management strategy before production deployment; do not assume it is a substitute for database migrations or backups.

### 4. Build the WAR

From the project root, run:

```bash
mvn clean package
```

The deployable WAR should be generated at:

```text
target/employee-management-system.war
```

### 5. Deploy locally to Tomcat

Copy the generated WAR into Tomcat's `webapps` directory and start Tomcat.

For example:

```bash
cp target/employee-management-system.war "$CATALINA_HOME/webapps/"
"$CATALINA_HOME/bin/startup.sh"
```

On Windows, use the corresponding `.bat` scripts in Tomcat's `bin` directory.

Open:

```text
http://localhost:8080/employee-management-system/
```

The context path is based on the WAR file name. If you rename the WAR, the URL context path will change accordingly.

## Running Tests

Run the automated test suite with:

```bash
mvn test
```

The project includes JUnit tests and an H2 test dependency. Tests should use the test configuration under `src/test/resources`, rather than connecting to a production database.

## Deployment

### Can this project be deployed directly to Netlify?

**No.** Netlify does not provide a traditional Java Servlet container for running a WAR file. Publishing this repository to Netlify as a static site will not start the servlets, Hibernate, or Thymeleaf rendering.

### Recommended deployment architecture

1. Deploy the **Java WAR** to a host that supports Java 17 and a compatible Tomcat/Servlet runtime.
2. Provision a **managed MySQL database** (or another reachable MySQL server).
3. Configure the Java application's database URL, username, and password using environment variables or the hosting provider's secret configuration.
4. Allow the application host to connect to the database securely. Do not use `localhost` for a remote database.
5. Use Netlify only if you later separate a static frontend from the Java backend; configure that frontend to call the deployed backend API.

Before choosing a hosting provider, verify its supported Java version, Tomcat/Servlet compatibility, WAR deployment method, persistent storage requirements, and database connectivity.

## Security and Production Checklist

- [ ] Replace the empty/local database password with a secure secret.
- [ ] Do not expose database credentials in source control.
- [ ] Use a dedicated MySQL database user instead of the root account.
- [ ] Restrict database network access to the application host.
- [ ] Use HTTPS for public deployments.
- [ ] Review authentication, session, and authorization behaviour before exposing the app publicly.
- [ ] Review Hibernate schema generation settings and establish a backup/migration strategy.
- [ ] Disable verbose SQL logging in production.
- [ ] Test all employee, project, task, and allocation flows against the production-like environment.

## Current Status

This repository contains the Java web application source, Maven build configuration, Thymeleaf templates, Hibernate configuration, and tests. A public production deployment is not configured by this README; a Java-compatible application host and a reachable MySQL database are required.

## Author

**Ayush Shekhar**

GitHub: [@ayushshekhar018](https://github.com/ayushshekhar018)

---

If you find a bug or have a suggestion, feel free to open an issue in the repository.
