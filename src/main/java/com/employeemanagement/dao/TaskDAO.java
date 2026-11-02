package com.employeemanagement.dao;

import com.employeemanagement.model.Employee;
import com.employeemanagement.model.Project;
import com.employeemanagement.model.Task;
import com.employeemanagement.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/** Persistence operations for project tasks, including assignment and status updates. */
public class TaskDAO {

    private static final String TASK_GRAPH =
            "select t from Task t join fetch t.project left join fetch t.assignee ";
    private static final Set<String> ALLOWED_PRIORITIES = Set.of("low", "medium", "high");
    private static final Set<String> ALLOWED_STATUSES = Set.of("todo", "in progress", "done");

    public List<Task> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    TASK_GRAPH + "order by t.dueDate asc, t.priority desc, t.id desc",
                    Task.class
            ).getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Unable to retrieve tasks.", e);
        }
    }

    public List<Task> findForEmployee(long employeeId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    TASK_GRAPH + "where t.assignee.id = :employeeId order by t.dueDate asc, t.id desc",
                    Task.class
            ).setParameter("employeeId", employeeId).getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Unable to retrieve employee tasks.", e);
        }
    }

    /** Backward-compatible creation method; tasks default to "todo". */
    public void create(
            String title,
            String description,
            String priority,
            LocalDate dueDate,
            long projectId,
            Long assigneeId
    ) {
        create(title, description, priority, "todo", dueDate, projectId, assigneeId);
    }

    public void create(
            String title,
            String description,
            String priority,
            String status,
            LocalDate dueDate,
            long projectId,
            Long assigneeId
    ) {
        if (title == null || title.isBlank() || title.trim().length() > 150) {
            throw new IllegalArgumentException("Enter a task title (up to 150 characters).");
        }
        if (!ALLOWED_PRIORITIES.contains(priority)) {
            throw new IllegalArgumentException("Choose a valid priority.");
        }
        if (!ALLOWED_STATUSES.contains(status)) {
            throw new IllegalArgumentException("Choose a valid task status.");
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Project project = session.find(Project.class, projectId);
                if (project == null) {
                    throw new IllegalArgumentException("Choose a valid project.");
                }

                Task task = new Task();
                task.setTitle(title.trim());
                task.setDescription(description == null ? "" : description.trim());
                task.setPriority(priority);
                task.setStatus(status);
                task.setDueDate(dueDate);
                task.setProject(project);

                if (assigneeId != null) {
                    Employee assignee = session.find(Employee.class, assigneeId);
                    if (assignee == null) {
                        throw new IllegalArgumentException("Choose a valid assignee.");
                    }
                    task.setAssignee(assignee);
                }

                session.persist(task);
                tx.commit();
            } catch (Exception e) {
                if (tx.isActive()) {
                    tx.rollback();
                }
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new RuntimeException("Unable to create task.", e);
            }
        }
    }

    public boolean updateStatus(long taskId, String status, long employeeId, boolean administrator) {
        if (!ALLOWED_STATUSES.contains(status)) {
            return false;
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Task task = session.find(Task.class, taskId);
                if (task == null || (!administrator
                        && (task.getAssignee() == null || task.getAssignee().getId() != employeeId))) {
                    tx.rollback();
                    return false;
                }

                task.setStatus(status);
                tx.commit();
                return true;
            } catch (Exception e) {
                if (tx.isActive()) {
                    tx.rollback();
                }
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new RuntimeException("Unable to update task status.", e);
            }
        }
    }

    public boolean delete(long taskId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Task task = session.find(Task.class, taskId);
                if (task == null) {
                    tx.rollback();
                    return false;
                }
                session.remove(task);
                tx.commit();
                return true;
            } catch (Exception e) {
                if (tx.isActive()) {
                    tx.rollback();
                }
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new RuntimeException("Unable to delete task.", e);
            }
        }
    }
}
