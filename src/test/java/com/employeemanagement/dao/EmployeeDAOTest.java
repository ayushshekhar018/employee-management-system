package com.employeemanagement.dao;

import com.employeemanagement.model.Employee;
import org.junit.jupiter.api.*;
import org.hibernate.Session;
import org.hibernate.Transaction;
import com.employeemanagement.util.TestHibernateUtil;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeDAOTest extends BaseDAOTest {

    private final EmployeeDAO dao = new EmployeeDAO();

    @Test
    void save_andFindId() {
        Employee emp = new Employee("Engineering", 80000.0, "Alice", "alice@test.com", "Dev", "pass123");
        emp.setRole("user");
        dao.save(emp);

        Employee found = dao.findId(emp.getId());
        assertNotNull(found);
        assertEquals("Alice", found.getName());
        assertEquals("Engineering", found.getDept());
        assertEquals(80000.0, found.getSalary());
    }

    @Test
    void save_andFindall() {
        Employee emp1 = new Employee("HR", 60000.0, "Bob", "bob@test.com", "Manager", "pass123");
        Employee emp2 = new Employee("Sales", 55000.0, "Carol", "carol@test.com", "Rep", "pass123");
        dao.save(emp1);
        dao.save(emp2);

        List<Employee> all = dao.findall();
        assertTrue(all.size() >= 2);
    }

    @Test
    void update_existingEmployee() {
        Employee emp = new Employee("IT", 70000.0, "Dave", "dave@test.com", "Admin", "pass123");
        dao.save(emp);

        emp.setName("David");
        emp.setSalary(75000.0);
        dao.update(emp);

        Employee updated = dao.findId(emp.getId());
        assertEquals("David", updated.getName());
        assertEquals(75000.0, updated.getSalary());
    }

    @Test
    void delete_existingEmployee() {
        Employee emp = new Employee("Finance", 90000.0, "Eve", "eve@test.com", "CFO", "pass123");
        dao.save(emp);
        long id = emp.getId();

        assertTrue(dao.delete(id));
        assertNull(dao.findId(id));
    }

    @Test
    void delete_nonExistent_returnsFalse() {
        assertFalse(dao.delete(9999L));
    }

    @Test
    void findId_nonExistent_returnsNull() {
        assertNull(dao.findId(9999L));
    }

    @Test
    void update_nullEmployee_noOp() {
        assertDoesNotThrow(() -> dao.update(null));
    }

    @Test
    void save_nullEmployee_noOp() {
        assertDoesNotThrow(() -> dao.save(null));
    }
}
