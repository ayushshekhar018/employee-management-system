package com.employeemanagement.dao;

import com.employeemanagement.util.HibernateUtil;
import com.employeemanagement.model.Employee;
import com.employeemanagement.model.Project;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data access operations for projects and their many-to-many employee allocations.
 */
public class ProjectDAO {

    public List<Project> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from Project order by id", Project.class).getResultList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Project findById(long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.find(Project.class, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void save(Project project) {
        if (project == null) return;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                session.persist(project);
                transaction.commit();
            } catch (Exception e) {
                transaction.rollback();
                throw new RuntimeException(e);
            }
        }
    }

    public void update(Project project) {
        if (project == null) return;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                Project existing = session.find(Project.class, project.getId());
                if (existing == null) {
                    transaction.rollback();
                    return;
                }

                existing.setProjectName(project.getProjectName());
                existing.setClientName(project.getClientName());
                existing.setStartDate(project.getStartDate());
                existing.setBudget(project.getBudget());
                existing.setMaximumTeamSize(project.getMaximumTeamSize());
                existing.setStatus(project.getStatus());

                transaction.commit();
            } catch (Exception e) {
                if (transaction.isActive()) transaction.rollback();
                throw new RuntimeException(e);
            }
        }
    }

    public void delete(long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                Project project = session.find(Project.class, id);
                if (project == null) {
                    transaction.rollback();
                    return;
                }

                // Clear the join-table relationship before deleting the project.
                project.getEmployees().clear();

                session.remove(project);
                transaction.commit();
            } catch (Exception e) {
                if (transaction.isActive()) transaction.rollback();
                throw new RuntimeException(e);
            }
        }
    }

    public List<Employee> getAllocatedEmployees(long projectId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Project project = session.find(Project.class, projectId);
            if (project == null) {
                return new ArrayList<>();
            }
            return new ArrayList<>(project.getEmployees());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int getAllocatedCount(long projectId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                    "select count(e.id) from Project p left join p.employees e where p.id = :projectId",
                    Long.class
            ).setParameter("projectId", projectId).getSingleResult();
            return Math.toIntExact(count);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Map<Long, Integer> getAllocatedCounts() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Object[]> rows = session.createQuery(
                    "select p.id, count(e.id) from Project p left join p.employees e group by p.id",
                    Object[].class
            ).getResultList();

            Map<Long, Integer> counts = new HashMap<>();
            for (Object[] row : rows) {
                counts.put(((Number) row[0]).longValue(), ((Number) row[1]).intValue());
            }
            return counts;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isEmployeeAllocated(long projectId, long employeeId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                    "select count(e.id) from Project p join p.employees e " +
                            "where p.id = :projectId and e.id = :employeeId",
                    Long.class
            ).setParameter("projectId", projectId)
             .setParameter("employeeId", employeeId)
             .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean allocateEmployee(long projectId, long employeeId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                Project project = session.find(Project.class, projectId);
                if (project == null) {
                    transaction.rollback();
                    return false;
                }

                if ("completed".equalsIgnoreCase(project.getStatus())) {
                    transaction.rollback();
                    return false;
                }

                // Enforce the configured team capacity before modifying either side of the relationship.
                if (project.getEmployees().size() >= project.getMaximumTeamSize()) {
                    transaction.rollback();
                    return false;
                }

                Employee employee = session.find(Employee.class, employeeId);
                if (employee == null) {
                    transaction.rollback();
                    return false;
                }

                if (project.getEmployees().contains(employee)) {
                    transaction.rollback();
                    return false;
                }

                // Keep both sides of the bidirectional many-to-many association in sync.
                project.getEmployees().add(employee);
                employee.getProjects().add(project);

                transaction.commit();
                return true;
            } catch (Exception e) {
                if (transaction.isActive()) transaction.rollback();
                throw new RuntimeException(e);
            }
        }
    }

    public boolean removeAllocation(long projectId, long employeeId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                Project project = session.find(Project.class, projectId);
                Employee employee = session.find(Employee.class, employeeId);

                if (project == null || employee == null) {
                    transaction.rollback();
                    return false;
                }

                project.getEmployees().remove(employee);
                employee.getProjects().remove(project);

                transaction.commit();
                return true;
            } catch (Exception e) {
                if (transaction.isActive()) transaction.rollback();
                throw new RuntimeException(e);
            }
        }
    }

    public Map<Long, String> batchAllocateEmployee(long employeeId, List<Long> projectIds) {
        Map<Long, String> errors = new HashMap<>();

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                Employee employee = session.find(Employee.class, employeeId);
                if (employee == null) {
                    transaction.rollback();
                    for (Long pid : projectIds) {
                        errors.put(pid, "Employee not found.");
                    }
                    return errors;
                }

                // Validate independently so one invalid project does not block other eligible allocations.
                for (Long projectId : projectIds) {
                    Project project = session.find(Project.class, projectId);
                    if (project == null) {
                        errors.put(projectId, "Project not found.");
                        continue;
                    }

                    if ("completed".equalsIgnoreCase(project.getStatus())) {
                        errors.put(projectId, "Cannot allocate to completed project: " + project.getProjectName() + ".");
                        continue;
                    }

                    if (project.getEmployees().size() >= project.getMaximumTeamSize()) {
                        errors.put(projectId, "Project " + project.getProjectName() + " has reached its maximum team capacity.");
                        continue;
                    }

                    if (project.getEmployees().contains(employee)) {
                        errors.put(projectId, "Employee is already allocated to " + project.getProjectName() + ".");
                        continue;
                    }

                    project.getEmployees().add(employee);
                    employee.getProjects().add(project);
                }

                // A failure for one project should not cancel valid allocations to others.
                transaction.commit();
            } catch (Exception e) {
                if (transaction.isActive()) transaction.rollback();
                throw new RuntimeException(e);
            }
        }

        return errors;
    }
}
