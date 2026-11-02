package com.employeemanagement.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;

class ProjectTest {

    @Test
    void defaultConstructor_initializesEmployees() {
        Project proj = new Project();
        assertNotNull(proj.getEmployees());
        assertTrue(proj.getEmployees().isEmpty());
    }

    @Test
    void constructor_setsFields() {
        Project proj = new Project("Alpha", "ClientA", LocalDate.of(2025, 1, 1), 100000.0, 5, "active");
        assertEquals("Alpha", proj.getProjectName());
        assertEquals("ClientA", proj.getClientName());
        assertEquals(LocalDate.of(2025, 1, 1), proj.getStartDate());
        assertEquals(100000.0, proj.getBudget());
        assertEquals(5, proj.getMaximumTeamSize());
        assertEquals("active", proj.getStatus());
    }

    @Test
    void setters_andGetters_work() {
        Project proj = new Project();
        proj.setId(1L);
        proj.setProjectName("Beta");
        proj.setClientName("ClientB");
        proj.setStartDate(LocalDate.of(2025, 6, 1));
        proj.setBudget(200000.0);
        proj.setMaximumTeamSize(10);
        proj.setStatus("completed");

        assertEquals(1L, proj.getId());
        assertEquals("Beta", proj.getProjectName());
        assertEquals("ClientB", proj.getClientName());
        assertEquals(LocalDate.of(2025, 6, 1), proj.getStartDate());
        assertEquals(200000.0, proj.getBudget());
        assertEquals(10, proj.getMaximumTeamSize());
        assertEquals("completed", proj.getStatus());
    }
}
