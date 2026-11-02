package com.employeemanagement.dao;

import com.employeemanagement.util.HibernateUtil;
import com.employeemanagement.util.TestHibernateUtil;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

/**
 * Base class for DAO tests that sets up an H2 in-memory database.
 * Replaces the static HibernateUtil sessionFactory with the test one.
 */
public abstract class BaseDAOTest {

    private static SessionFactory originalSessionFactory;

    @BeforeAll
    static void setUpHibernate() {
        // The test hibernate.cfg.xml is on the test classpath, so TestHibernateUtil
        // builds a SessionFactory against H2. We inject it into HibernateUtil
        // via reflection so the production DAOs use H2 during tests.
        SessionFactory testSf = TestHibernateUtil.getSessionFactory();
        try {
            java.lang.reflect.Field field = HibernateUtil.class.getDeclaredField("sessionFactory");
            field.setAccessible(true);
            originalSessionFactory = (SessionFactory) field.get(null);
            field.set(null, testSf);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject test SessionFactory", e);
        }
    }

    @AfterAll
    static void tearDownHibernate() {
        // Restore original and shut down test factory
        try {
            java.lang.reflect.Field field = HibernateUtil.class.getDeclaredField("sessionFactory");
            field.setAccessible(true);
            field.set(null, originalSessionFactory);
        } catch (Exception ignored) {
        }
        TestHibernateUtil.shutdown();
    }
}
