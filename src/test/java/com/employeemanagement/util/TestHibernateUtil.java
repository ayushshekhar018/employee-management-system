package com.employeemanagement.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class TestHibernateUtil {
    private static SessionFactory sessionFactory;

    public static synchronized SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            sessionFactory = new Configuration().configure().buildSessionFactory();
        }
        return sessionFactory;
    }

    public static synchronized void shutdown() {
        if (sessionFactory == null) return;
        sessionFactory.close();
        sessionFactory = null;
    }
}
