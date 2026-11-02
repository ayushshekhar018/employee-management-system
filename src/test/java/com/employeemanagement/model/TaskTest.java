package com.employeemanagement.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

class TaskTest {

    @Test
    void defaultValues_areSet() {
        Task task = new Task();
        assertEquals("todo", task.getStatus());
        assertEquals("medium", task.getPriority());
        assertNotNull(task.getCreatedAt());
    }

    @Test
    void setters_andGetters_work() {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Fix bug");
        task.setDescription("Resolve login issue");
        task.setStatus("in_progress");
        task.setPriority("high");
        task.setDueDate(LocalDate.of(2025, 12, 31));
        task.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));

        assertEquals(1L, task.getId());
        assertEquals("Fix bug", task.getTitle());
        assertEquals("Resolve login issue", task.getDescription());
        assertEquals("in_progress", task.getStatus());
        assertEquals("high", task.getPriority());
        assertEquals(LocalDate.of(2025, 12, 31), task.getDueDate());
        assertEquals(LocalDateTime.of(2025, 1, 1, 10, 0), task.getCreatedAt());
    }

    @Test
    void project_andAssignee_setters_work() {
        Task task = new Task();
        Project project = new Project();
        project.setId(1L);
        Employee employee = new Employee();
        employee.setId(1L);

        task.setProject(project);
        task.setAssignee(employee);

        assertEquals(project, task.getProject());
        assertEquals(employee, task.getAssignee());
    }
}
