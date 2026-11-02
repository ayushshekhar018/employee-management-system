package com.employeemanagement.util;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;


public class HibernateUtil {
    private static SessionFactory sessionFactory;
    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            sessionFactory = new Configuration().configure().buildSessionFactory();

        }
        return sessionFactory;
    }
    public static void shutdown(){
            if(sessionFactory==null)return;
            sessionFactory.close();
            sessionFactory=null;
        }

}
