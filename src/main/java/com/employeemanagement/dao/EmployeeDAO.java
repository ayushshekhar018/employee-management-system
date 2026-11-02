
package com.employeemanagement.dao;

import com.employeemanagement.util.HibernateUtil;
import com.employeemanagement.model.Employee;
import com.employeemanagement.model.Project;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Data access operations for Employee records and employee-project relationships.
 */
public class EmployeeDAO {

    public List<Employee> findall() {
        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {

            return session.createQuery(
                    "from Employee order by id",
                    Employee.class
            ).getResultList();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Employee findId(long id) {
        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {

            return session.find(Employee.class, id);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Project> getEmployeeProjects(long employeeId) {
        try (Session session = HibernateUtil
                .getSessionFactory()
                .openSession()) {

            Employee employee = session.find(Employee.class, employeeId);
            if (employee == null) {
                return new ArrayList<>();
            }
            Set<Project> projects = employee.getProjects();
            return new ArrayList<>(projects);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public void save(Employee employee) {
        if (employee == null) return;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                // Persist the new entity inside an explicit transaction.
                session.persist(employee);
                transaction.commit();
            } catch (Exception e) {
                if (transaction.isActive()) transaction.rollback();
                throw new RuntimeException(e);
            }
        }
    }
    public boolean delete(long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();

            try {
                Employee employee = session.find(Employee.class, id);

                if (employee == null) {
                    transaction.rollback();
                    return false;
                }

                // Remove the employee from the inverse project collections before deletion.
                for (Project project : employee.getProjects()) {
                    project.getEmployees().remove(employee);
                }
                session.remove(employee);
                transaction.commit();
                return true;

            } catch (Exception e) {
                if (transaction.isActive()) transaction.rollback();
                throw new RuntimeException(e);
            }
        }
    }
    public void update(Employee employee) {
        if (employee == null) return;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();

            try {
                // Update the managed entity so Hibernate tracks the field changes.
                Employee existing = session.find(Employee.class, employee.getId());

                if (existing == null) {
                    transaction.rollback();
                    return;
                }

                existing.setName(employee.getName());
                existing.setDept(employee.getDept());
                existing.setSalary(employee.getSalary());
                existing.setEmail(employee.getEmail());
                existing.setDesignation(employee.getDesignation());
                existing.setPassword(employee.getPassword());
                existing.setRole(employee.getRole());

                transaction.commit();

            } catch (Exception e) {
                if (transaction.isActive()) transaction.rollback();
                throw new RuntimeException(e);
            }
        }
    }

}
