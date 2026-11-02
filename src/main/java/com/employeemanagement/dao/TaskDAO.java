package com.employeemanagement.dao;

import com.employeemanagement.model.Employee;
import com.employeemanagement.model.Project;
import com.employeemanagement.model.Task;
import com.employeemanagement.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.List;

/**
 * Persistence operations for project tasks, including assignment and status updates.
 */
/**
 * Persistence operations for tasks, including assignment and status updates.
 */
public class TaskDAO {
    // Fetch the associated project and optional assignee while the session is open.
    // Fetch project and optional assignee while the Hibernate session is open.
    // Fetch the project and optional assignee while the Hibernate session is open.
    private static final String TASK_GRAPH = "select t from Task t join fetch t.project left join fetch t.assignee ";

    public List<Task> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(TASK_GRAPH + "order by t.dueDate asc, t.priority desc, t.id desc", Task.class).getResultList();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public List<Task> findForEmployee(long employeeId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(TASK_GRAPH + "where t.assignee.id = :employeeId order by t.dueDate asc, t.id desc", Task.class)
                    .setParameter("employeeId", employeeId).getResultList();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public void create(String title, String description, String priority, java.time.LocalDate dueDate, long projectId, Long assigneeId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Project project = session.find(Project.class, projectId);
                if (project == null) throw new IllegalArgumentException("Choose a valid project.");
                Task task = new Task();
                task.setTitle(title.trim()); task.setDescription(description == null ? "" : description.trim());
                task.setPriority(priority); task.setDueDate(dueDate); task.setProject(project);
                if (assigneeId != null) {
                    Employee assignee = session.find(Employee.class, assigneeId);
                    if (assignee == null) throw new IllegalArgumentException("Choose a valid assignee.");
                    task.setAssignee(assignee);
                }
                session.persist(task);
                tx.commit();
            } catch (Exception e) {
                if (tx.isActive()) tx.rollback();
                if (e instanceof RuntimeException) throw (RuntimeException) e;
                throw new RuntimeException(e);
            }
        }
    }

    public boolean updateStatus(long taskId, String status, long employeeId, boolean administrator) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Task task = session.find(Task.class, taskId);
                // Employees may update only their own assigned tasks; administrators can update any task.
                // Employees may update only their assigned tasks; administrators can update any task.
                // Employees may update only assigned tasks; administrators can update any task.
                if (task == null || (!administrator && (task.getAssignee() == null || task.getAssignee().getId() != employeeId))) {
                    tx.rollback(); return false;
                }
                task.setStatus(status);
                tx.commit(); return true;
            } catch (Exception e) {
                if (tx.isActive()) tx.rollback();
                if (e instanceof RuntimeException) throw (RuntimeException) e;
                throw new RuntimeException(e);
            }
        }
    }

    public boolean delete(long taskId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Task task = session.find(Task.class, taskId);
                if (task == null) { tx.rollback(); return false; }
                session.remove(task); tx.commit(); return true;
            } catch (Exception e) {
                if (tx.isActive()) tx.rollback();
                if (e instanceof RuntimeException) throw (RuntimeException) e;
                throw new RuntimeException(e);
            }
        }
    }
}
