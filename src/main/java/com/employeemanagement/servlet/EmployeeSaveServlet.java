package com.employeemanagement.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.employeemanagement.dao.EmployeeDAO;
import com.employeemanagement.util.PasswordUtil;
import com.employeemanagement.model.Employee;
import com.employeemanagement.config.ThymeleafConfig;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** Handles employee creation submitted from the employee form. */
@WebServlet("/employees/save")
public class EmployeeSaveServlet extends HttpServlet {

    private final EmployeeDAO employeeDao = new EmployeeDAO();

    private void renderFormError(HttpServletRequest req, HttpServletResponse res, String error)
            throws IOException {
        Employee employee = new Employee();
        employee.setName(req.getParameter("name"));
        employee.setDept(req.getParameter("dept"));
        employee.setEmail(req.getParameter("email"));
        employee.setDesignation(req.getParameter("designation"));
        employee.setRole(req.getParameter("role"));

        String salaryParam = req.getParameter("salary");
        if (salaryParam != null && !salaryParam.isBlank()) {
            try {
                employee.setSalary(Double.parseDouble(salaryParam));
            } catch (NumberFormatException ignored) {
                employee.setSalary(0.0);
            }
        }

        Map<String, Object> vars = new HashMap<>();
        vars.put("employee", employee);
        vars.put("error", error);
        ThymeleafConfig.render(req, res, vars, "add-new-employee-form");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String name = req.getParameter("name");
        String dept = req.getParameter("dept");
        String salaryParam = req.getParameter("salary");
        String email = req.getParameter("email");
        String designation = req.getParameter("designation");
        String plainPassword = req.getParameter("password");
        String role = req.getParameter("role");

        if (name == null || name.isBlank()) {
            renderFormError(req, res, "Employee name is required.");
            return;
        }
        if (dept == null || dept.isBlank()) {
            renderFormError(req, res, "Department is required.");
            return;
        }
        if (email == null || email.isBlank()) {
            renderFormError(req, res, "Email address is required.");
            return;
        }
        if (!"admin".equalsIgnoreCase(role) && !"employee".equalsIgnoreCase(role)) {
            renderFormError(req, res, "Choose a valid employee role.");
            return;
        }
        if (plainPassword == null || plainPassword.isEmpty()) {
            renderFormError(req, res, "Password is required.");
            return;
        }

        double salary;
        try {
            salary = Double.parseDouble(salaryParam != null ? salaryParam : "0");
            if (!Double.isFinite(salary) || salary < 0) {
                throw new NumberFormatException("Salary must be a non-negative finite number.");
            }
        } catch (NumberFormatException e) {
            renderFormError(req, res, "Enter a valid non-negative salary.");
            return;
        }

        Employee employee = new Employee(dept.trim(), salary, name.trim());
        employee.setEmail(email.trim());
        employee.setDesignation(designation == null ? null : designation.trim());
        employee.setRole(role.toLowerCase());

        // Only the published demo email with the employee role bypasses BCrypt.
        try {
            employee.setPassword(PasswordUtil.hashForAccount(email, role, plainPassword));
        } catch (IllegalArgumentException e) {
            renderFormError(req, res, e.getMessage());
            return;
        }

        employeeDao.save(employee);
        res.sendRedirect(req.getContextPath() + "/employees");
    }
}
