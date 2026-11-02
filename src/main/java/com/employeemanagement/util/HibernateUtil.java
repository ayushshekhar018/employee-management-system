package com.employeemanagement.util;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;


/**
 * Provides the application's shared Hibernate SessionFactory.
 */
public class HibernateUtil {
    private static SessionFactory sessionFactory;
    public static synchronized SessionFactory getSessionFactory() {
        // Build the expensive SessionFactory only when it is first requested.
        if (sessionFactory == null) {
            sessionFactory = new Configuration().configure().buildSessionFactory();

        }
        return sessionFactory;
    }
    /** Closes the shared factory during application shutdown. */
    public static void shutdown(){
            if(sessionFactory==null)return;
            sessionFactory.close();
            sessionFactory=null;
        }

}
