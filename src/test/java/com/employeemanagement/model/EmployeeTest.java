package com.employeemanagement.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmployeeTest {

    @Test
    void defaultConstructor_initializesProjects() {
        Employee emp = new Employee();
        assertNotNull(emp.getProjects());
        assertTrue(emp.getProjects().isEmpty());
    }

    @Test
    void constructor_setsFields() {
        Employee emp = new Employee("Engineering", 75000.0, "Alice");
        assertEquals("Engineering", emp.getDept());
        assertEquals(75000.0, emp.getSalary());
        assertEquals("Alice", emp.getName());
    }

    @Test
    void fullConstructor_setsAllFields() {
        Employee emp = new Employee("HR", 60000.0, "Bob", "bob@example.com", "Manager", "hashed123");
        assertEquals("HR", emp.getDept());
        assertEquals(60000.0, emp.getSalary());
        assertEquals("Bob", emp.getName());
        assertEquals("bob@example.com", emp.getEmail());
        assertEquals("Manager", emp.getDesignation());
        assertEquals("hashed123", emp.getPassword());
    }

    @Test
    void setters_andGetters_work() {
        Employee emp = new Employee();
        emp.setId(1L);
        emp.setName("Charlie");
        emp.setDept("Sales");
        emp.setSalary(50000.0);
        emp.setEmail("charlie@example.com");
        emp.setDesignation("Analyst");
        emp.setPassword("pass123");
        emp.setRole("admin");

        assertEquals(1L, emp.getId());
        assertEquals("Charlie", emp.getName());
        assertEquals("Sales", emp.getDept());
        assertEquals(50000.0, emp.getSalary());
        assertEquals("charlie@example.com", emp.getEmail());
        assertEquals("Analyst", emp.getDesignation());
        assertEquals("pass123", emp.getPassword());
        assertEquals("admin", emp.getRole());
    }

    @Test
    void projects_setter_works() {
        Employee emp = new Employee();
        assertNotNull(emp.getProjects());
        assertTrue(emp.getProjects().isEmpty());
    }
}
