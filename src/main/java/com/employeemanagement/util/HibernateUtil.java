package com.employeemanagement.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Provides the application's shared Hibernate SessionFactory.
 *
 * Local development uses hibernate.cfg.xml. When DB_HOST is supplied,
 * database connection values are read from environment variables so the
 * deployment platform can inject credentials without storing them in Git.
 */
public class HibernateUtil {

    private static SessionFactory sessionFactory;

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            // Build the expensive SessionFactory only when it is first requested.
            Configuration configuration = new Configuration().configure();

            // Prefer deployment-provided database settings; retain XML defaults locally.
            configureDatabaseFromEnvironment(configuration);

            sessionFactory = configuration.buildSessionFactory();
        }
        return sessionFactory;
    }

    /**
     * Overrides the local connection properties when DB_HOST is configured.
     * The password is never logged or written to source control.
     */
    private static void configureDatabaseFromEnvironment(Configuration configuration) {
        String host = System.getenv("DB_HOST");

        // Without DB_HOST, Hibernate uses the local connection in hibernate.cfg.xml.
        if (host == null || host.isBlank()) {
            return;
        }

        String port = environmentOrDefault("DB_PORT", "4000");
        String database = requiredEnvironmentVariable("DB_NAME");
        String username = requiredEnvironmentVariable("DB_USERNAME");
        String password = requiredEnvironmentVariable("DB_PASSWORD");
        String sslMode = environmentOrDefault("DB_SSL_MODE", "VERIFY_IDENTITY");

        String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database
                + "?sslMode=" + sslMode
                + "&allowPublicKeyRetrieval=true"
                + "&serverTimezone=UTC";

        configuration.setProperty("hibernate.connection.url", jdbcUrl);
        configuration.setProperty("hibernate.connection.username", username);
        configuration.setProperty("hibernate.connection.password", password);
    }

    private static String requiredEnvironmentVariable(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Required database environment variable is missing: " + name
            );
        }
        return value;
    }

    private static String environmentOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    /** Closes the shared factory during application shutdown. */
    public static void shutdown() {
        if (sessionFactory == null) return;
        sessionFactory.close();
        sessionFactory = null;
    }
}
