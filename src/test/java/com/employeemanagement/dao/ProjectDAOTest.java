package com.employeemanagement.dao;

import com.employeemanagement.model.Employee;
import com.employeemanagement.model.Project;
import org.junit.jupiter.api.*;
import com.employeemanagement.util.PasswordUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProjectDAOTest extends BaseDAOTest {

    private final ProjectDAO dao = new ProjectDAO();
    private final EmployeeDAO empDao = new EmployeeDAO();

    @Test
    void save_andFindById() {
        Project proj = new Project("Alpha", "ClientA", LocalDate.of(2025, 1, 1), 100000.0, 5, "active");
        dao.save(proj);

        Project found = dao.findById(proj.getId());
        assertNotNull(found);
        assertEquals("Alpha", found.getProjectName());
        assertEquals("ClientA", found.getClientName());
        assertEquals(100000.0, found.getBudget());
    }

    @Test
    void findAll_returnsAllProjects() {
        dao.save(new Project("P1", "C1", LocalDate.of(2025, 1, 1), 50000.0, 3, "active"));
        dao.save(new Project("P2", "C2", LocalDate.of(2025, 2, 1), 60000.0, 4, "active"));

        List<Project> all = dao.findAll();
        assertTrue(all.size() >= 2);
    }

    @Test
    void update_existingProject() {
        Project proj = new Project("Beta", "ClientB", LocalDate.of(2025, 3, 1), 80000.0, 5, "active");
        dao.save(proj);

        proj.setProjectName("Beta Updated");
        proj.setBudget(90000.0);
        dao.update(proj);

        Project updated = dao.findById(proj.getId());
        assertEquals("Beta Updated", updated.getProjectName());
        assertEquals(90000.0, updated.getBudget());
    }

    @Test
    void delete_existingProject() {
        Project proj = new Project("Gamma", "ClientC", LocalDate.of(2025, 4, 1), 70000.0, 3, "active");
        dao.save(proj);
        long id = proj.getId();

        dao.delete(id);
        assertNull(dao.findById(id));
    }

    @Test
    void findById_nonExistent_returnsNull() {
        assertNull(dao.findById(9999L));
    }

    @Test
    void allocateEmployee_success() {
        Project proj = new Project("Delta", "ClientD", LocalDate.of(2025, 5, 1), 100000.0, 5, "active");
        dao.save(proj);

        Employee emp = new Employee("Engineering", 75000.0, "Frank", "frank@test.com", "Dev", "pass123");
        empDao.save(emp);

        assertTrue(dao.allocateEmployee(proj.getId(), emp.getId()));
        assertTrue(dao.isEmployeeAllocated(proj.getId(), emp.getId()));
    }

    @Test
    void allocateEmployee_completedProject_returnsFalse() {
        Project proj = new Project("Done", "ClientE", LocalDate.of(2025, 1, 1), 50000.0, 5, "completed");
        dao.save(proj);

        Employee emp = new Employee("HR", 60000.0, "Grace", "grace@test.com", "HR", "pass123");
        empDao.save(emp);

        assertFalse(dao.allocateEmployee(proj.getId(), emp.getId()));
    }

    @Test
    void allocateEmployee_teamFull_returnsFalse() {
        Project proj = new Project("Full", "ClientF", LocalDate.of(2025, 6, 1), 80000.0, 1, "active");
        dao.save(proj);

        Employee emp1 = new Employee("IT", 70000.0, "Hank", "hank@test.com", "Dev", "pass123");
        Employee emp2 = new Employee("IT", 70000.0, "Ivy", "ivy@test.com", "Dev", "pass123");
        empDao.save(emp1);
        empDao.save(emp2);

        dao.allocateEmployee(proj.getId(), emp1.getId());
        assertFalse(dao.allocateEmployee(proj.getId(), emp2.getId()));
    }

    @Test
    void allocateEmployee_alreadyAllocated_returnsFalse() {
        Project proj = new Project("Epsilon", "ClientG", LocalDate.of(2025, 7, 1), 90000.0, 5, "active");
        dao.save(proj);

        Employee emp = new Employee("Sales", 55000.0, "Jack", "jack@test.com", "Rep", "pass123");
        empDao.save(emp);

        dao.allocateEmployee(proj.getId(), emp.getId());
        assertFalse(dao.allocateEmployee(proj.getId(), emp.getId()));
    }

    @Test
    void removeAllocation_success() {
        Project proj = new Project("Zeta", "ClientH", LocalDate.of(2025, 8, 1), 100000.0, 5, "active");
        dao.save(proj);

        Employee emp = new Employee("Finance", 85000.0, "Kate", "kate@test.com", "Analyst", "pass123");
        empDao.save(emp);

        dao.allocateEmployee(proj.getId(), emp.getId());
        assertTrue(dao.isEmployeeAllocated(proj.getId(), emp.getId()));

        assertTrue(dao.removeAllocation(proj.getId(), emp.getId()));
        assertFalse(dao.isEmployeeAllocated(proj.getId(), emp.getId()));
    }

    @Test
    void getAllocatedCount_returnsCorrectCount() {
        Project proj = new Project("Eta", "ClientI", LocalDate.of(2025, 9, 1), 120000.0, 5, "active");
        dao.save(proj);

        Employee emp1 = new Employee("Eng", 70000.0, "Leo", "leo@test.com", "Dev", "pass123");
        Employee emp2 = new Employee("Eng", 70000.0, "Mia", "mia@test.com", "Dev", "pass123");
        empDao.save(emp1);
        empDao.save(emp2);

        dao.allocateEmployee(proj.getId(), emp1.getId());
        dao.allocateEmployee(proj.getId(), emp2.getId());

        assertEquals(2, dao.getAllocatedCount(proj.getId()));
    }

    @Test
    void getAllocatedEmployees_returnsList() {
        Project proj = new Project("Theta", "ClientJ", LocalDate.of(2025, 10, 1), 110000.0, 5, "active");
        dao.save(proj);

        Employee emp = new Employee("Design", 65000.0, "Nina", "nina@test.com", "Designer", "pass123");
        empDao.save(emp);

        dao.allocateEmployee(proj.getId(), emp.getId());
        List<Employee> allocated = dao.getAllocatedEmployees(proj.getId());
        assertEquals(1, allocated.size());
        assertEquals("Nina", allocated.get(0).getName());
    }

    @Test
    void batchAllocateEmployee_success() {
        Project proj1 = new Project("Batch1", "C1", LocalDate.of(2025, 1, 1), 50000.0, 5, "active");
        Project proj2 = new Project("Batch2", "C2", LocalDate.of(2025, 2, 1), 60000.0, 5, "active");
        dao.save(proj1);
        dao.save(proj2);

        Employee emp = new Employee("Multi", 70000.0, "Oscar", "oscar@test.com", "Dev", "pass123");
        empDao.save(emp);

        // Record counts before allocation
        int count1Before = dao.getAllocatedCount(proj1.getId());
        int count2Before = dao.getAllocatedCount(proj2.getId());

        Map<Long, String> errors = dao.batchAllocateEmployee(emp.getId(), List.of(proj1.getId(), proj2.getId()));
        assertTrue(errors.isEmpty());
        // Verify each project gained exactly one employee
        assertEquals(count1Before + 1, dao.getAllocatedCount(proj1.getId()));
        assertEquals(count2Before + 1, dao.getAllocatedCount(proj2.getId()));
    }

    @Test
    void batchAllocateEmployee_partialFailure() {
        Project proj1 = new Project("Partial1", "C1", LocalDate.of(2025, 1, 1), 50000.0, 5, "active");
        Project proj2 = new Project("Partial2", "C2", LocalDate.of(2025, 2, 1), 60000.0, 5, "completed");
        dao.save(proj1);
        dao.save(proj2);

        Employee emp = new Employee("Partial", 70000.0, "Pam", "pam@test.com", "Dev", "pass123");
        empDao.save(emp);

        Map<Long, String> errors = dao.batchAllocateEmployee(emp.getId(), List.of(proj1.getId(), proj2.getId()));
        assertFalse(errors.containsKey(proj1.getId()));
        assertTrue(errors.containsKey(proj2.getId()));
    }

    @Test
    void save_rejectsInvalidProjectValues() {
        assertThrows(IllegalArgumentException.class, () ->
                dao.save(new Project("Negative budget", "Client", LocalDate.now(), -1.0, 2, "active")));
        assertThrows(IllegalArgumentException.class, () ->
                dao.save(new Project("NaN budget", "Client", LocalDate.now(), Double.NaN, 2, "active")));
        assertThrows(IllegalArgumentException.class, () ->
                dao.save(new Project("Negative team", "Client", LocalDate.now(), 100.0, -1, "active")));
        assertThrows(IllegalArgumentException.class, () ->
                dao.save(new Project("Bad status", "Client", LocalDate.now(), 100.0, 2, "archived")));
    }

    @Test
    void delete_projectWithTasksIsRejectedWithoutDeletingProjectOrTask() {
        Project project = new Project(
                "Project with task",
                "Test Client",
                LocalDate.now(),
                1000.0,
                2,
                "active"
        );
        dao.save(project);

        TaskDAO taskDao = new TaskDAO();
        taskDao.create(
                "Task that keeps project alive",
                "A project cannot be deleted while tasks reference it.",
                "medium",
                "todo",
                LocalDate.now().plusDays(1),
                project.getId(),
                null
        );

        assertThrows(IllegalStateException.class, () -> dao.delete(project.getId()));
        assertNotNull(dao.findById(project.getId()));
        assertTrue(taskDao.findAll().stream()
                .anyMatch(task -> "Task that keeps project alive".equals(task.getTitle())));
    }

}
