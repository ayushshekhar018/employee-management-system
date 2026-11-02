package com.employeemanagement.dao;

import com.employeemanagement.util.HibernateUtil;
import com.employeemanagement.util.PasswordUtil;
import com.employeemanagement.model.Employee;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

/** Authentication and account lookup operations backed by Employee records. */
public class UserDAO {

    public Employee authenticate(String email, String password) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Bind the email as a parameter instead of concatenating user input into HQL.
            List<Employee> employees = session.createQuery(
                    "from Employee e where e.email = :email",
                    Employee.class
            )
            .setParameter("email", email)
            .getResultList();

            if (employees != null && !employees.isEmpty()) {
                Employee employee = employees.get(0);

                if (PasswordUtil.verifyForAccount(
                        employee.getEmail(),
                        employee.getRole(),
                        password,
                        employee.getPassword()
                )) {
                    // Only BCrypt accounts participate in automatic hash upgrades.
                    if (PasswordUtil.needsRehashForAccount(
                            employee.getEmail(),
                            employee.getRole(),
                            employee.getPassword()
                    )) {
                        String newHash = PasswordUtil.hashForAccount(
                                employee.getEmail(),
                                employee.getRole(),
                                password
                        );
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
     * Update an employee password using the account's configured password policy.
     */
    public boolean updatePassword(long employeeId, String newPassword) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Employee employee = session.find(Employee.class, employeeId);
            if (employee == null) {
                return false;
            }

            String storedPassword = PasswordUtil.hashForAccount(
                    employee.getEmail(),
                    employee.getRole(),
                    newPassword
            );
            Transaction tx = session.beginTransaction();
            employee.setPassword(storedPassword);
            session.merge(employee);
            tx.commit();
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
