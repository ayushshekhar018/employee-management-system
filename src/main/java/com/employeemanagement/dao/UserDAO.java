package com.employeemanagement.dao;

import com.employeemanagement.util.HibernateUtil;
import com.employeemanagement.util.PasswordUtil;
import com.employeemanagement.model.Employee;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class UserDAO {

    public Employee authenticate(String email, String password) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Employee> employees = session.createQuery(
                    "from Employee e where e.email = :email",
                    Employee.class
            )
            .setParameter("email", email)
            .getResultList();

            if (employees != null && !employees.isEmpty()) {
                Employee employee = employees.get(0);
                if (PasswordUtil.verify(password, employee.getPassword())) {
                    // Rehash password if it uses outdated settings (e.g., low log rounds)
                    if (PasswordUtil.needsRehash(employee.getPassword())) {
                        String newHash = PasswordUtil.hash(password);
                        Transaction tx = session.beginTransaction();
                        employee.setPassword(newHash);
                        session.merge(employee);
                        tx.commit();
                    }
                    return employee;
                }
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Employee findByEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Employee> employees = session.createQuery(
                    "from Employee e where e.email = :email",
                    Employee.class
            )
            .setParameter("email", email)
            .getResultList();

            if (employees != null && !employees.isEmpty()) {
                return employees.get(0);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Update an employee's password with a new BCrypt hash.
     *
     * @param employeeId the employee ID
     * @param newPassword the plain-text new password
     * @return true if the password was updated successfully
     */
    public boolean updatePassword(long employeeId, String newPassword) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Employee employee = session.find(Employee.class, employeeId);
            if (employee == null) {
                return false;
            }
            String newHash = PasswordUtil.hash(newPassword);
            Transaction tx = session.beginTransaction();
            employee.setPassword(newHash);
            session.merge(employee);
            tx.commit();
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
