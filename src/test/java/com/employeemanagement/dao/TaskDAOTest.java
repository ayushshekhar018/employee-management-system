package com.employeemanagement.dao;

import com.employeemanagement.model.Employee;
import com.employeemanagement.model.Project;
import com.employeemanagement.model.Task;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskDAOTest extends BaseDAOTest {

    private final TaskDAO dao = new TaskDAO();
    private final ProjectDAO projDao = new ProjectDAO();
    private final EmployeeDAO empDao = new EmployeeDAO();

    private Project project;
    private Employee employee;

    @BeforeEach
    void setUp() {
        project = new Project("TaskProj", "ClientT", LocalDate.of(2025, 1, 1), 100000.0, 5, "active");
        projDao.save(project);

        employee = new Employee("Engineering", 75000.0, "Tester", "tester@test.com", "Dev", "pass123");
        empDao.save(employee);
    }

    @Test
    void create_andFindAll() {
        dao.create("Fix login bug", "Users cannot log in", "high", LocalDate.of(2025, 12, 31), project.getId(), employee.getId());

        List<Task> tasks = dao.findAll();
        assertFalse(tasks.isEmpty());
        // Find the task we just created by title, as other tests may have left tasks
        Task task = tasks.stream()
                .filter(t -> "Fix login bug".equals(t.getTitle()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Task not found"));
        assertEquals("Fix login bug", task.getTitle());
        assertEquals("high", task.getPriority());
    }

    @Test
    void create_withNoAssignee() {
        dao.create("Review code", "Code review needed", "medium", LocalDate.of(2025, 11, 30), project.getId(), null);

        List<Task> tasks = dao.findAll();
        assertFalse(tasks.isEmpty());
        Task task = tasks.get(0);
        assertEquals("Review code", task.getTitle());
        assertNull(task.getAssignee());
    }

    @Test
    void create_invalidProject_throws() {
        assertThrows(RuntimeException.class, () ->
            dao.create("Bad task", "desc", "low", null, 9999L, null)
        );
    }

    @Test
    void create_invalidAssignee_throws() {
        assertThrows(RuntimeException.class, () ->
            dao.create("Bad assignee", "desc", "low", null, project.getId(), 9999L)
        );
    }

    @Test
    void updateStatus_administratorCanUpdate() {
        dao.create("Status task", "Test status", "medium", null, project.getId(), employee.getId());
        List<Task> tasks = dao.findAll();
        long taskId = tasks.get(0).getId();

        assertTrue(dao.updateStatus(taskId, "done", employee.getId(), true));
    }

    @Test
    void updateStatus_assigneeCanUpdate() {
        dao.create("Assignee task", "Test assignee update", "medium", null, project.getId(), employee.getId());
        List<Task> tasks = dao.findAll();
        long taskId = tasks.get(0).getId();

        assertTrue(dao.updateStatus(taskId, "in_progress", employee.getId(), false));
    }

    @Test
    void updateStatus_nonAssigneeCannotUpdate() {
        Employee other = new Employee("HR", 60000.0, "Other", "other@test.com", "HR", "pass123");
        empDao.save(other);

        dao.create("Other task", "Test non-assignee", "medium", null, project.getId(), employee.getId());
        List<Task> tasks = dao.findAll();
        long taskId = tasks.get(0).getId();

        assertFalse(dao.updateStatus(taskId, "done", other.getId(), false));
    }

    @Test
    void updateStatus_nonExistent_returnsFalse() {
        assertFalse(dao.updateStatus(9999L, "done", employee.getId(), true));
    }

    @Test
    void delete_existingTask() {
        dao.create("Delete me", "Will be deleted", "low", null, project.getId(), null);
        List<Task> tasks = dao.findAll();
        long taskId = tasks.get(0).getId();

        assertTrue(dao.delete(taskId));
    }

    @Test
    void delete_nonExistent_returnsFalse() {
        assertFalse(dao.delete(9999L));
    }

    @Test
    void findForEmployee_returnsAssignedTasks() {
        dao.create("My task", "Assigned to me", "high", null, project.getId(), employee.getId());
        Employee other = new Employee("Sales", 55000.0, "Other", "other@test.com", "Rep", "pass123");
        empDao.save(other);
        dao.create("Other task", "Assigned to other", "low", null, project.getId(), other.getId());

        List<Task> myTasks = dao.findForEmployee(employee.getId());
        assertFalse(myTasks.isEmpty());
        for (Task t : myTasks) {
            assertNotNull(t.getAssignee());
            assertEquals(employee.getId(), t.getAssignee().getId());
        }
    }

    @Test
    void findForEmployee_noTasks_returnsEmpty() {
        List<Task> tasks = dao.findForEmployee(employee.getId());
        assertTrue(tasks.isEmpty());
    }
}
