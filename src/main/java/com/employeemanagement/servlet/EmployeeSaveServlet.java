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

@WebServlet("/employees/save")
public class EmployeeSaveServlet extends HttpServlet {

    private final EmployeeDAO employeeDao = new EmployeeDAO();

    private void renderFormError(HttpServletRequest req, HttpServletResponse res, String error) throws IOException {
        Map<String, Object> vars = new HashMap<>();
        vars.put("error", error);
        vars.put("name", req.getParameter("name"));
        vars.put("dept", req.getParameter("dept"));
        vars.put("salary", req.getParameter("salary"));
        vars.put("email", req.getParameter("email"));
        vars.put("designation", req.getParameter("designation"));
        vars.put("role", req.getParameter("role"));
        ThymeleafConfig.render(req, res, vars, "add-new-employee-form");
    }

    @Override
    protected void doPost(HttpServletRequest req,
                          HttpServletResponse res)
            throws ServletException, IOException {
        String name = req.getParameter("name");
        String dept = req.getParameter("dept");
        String salaryParam = req.getParameter("salary");
        String email = req.getParameter("email");
        String designation = req.getParameter("designation");
        String plainPassword = req.getParameter("password");
        String role = req.getParameter("role");

        // Validate password
        if (plainPassword == null || plainPassword.isEmpty()) {
            renderFormError(req, res, "Password is required");
            return;
        }

        double salary;
        try {
            salary = Double.parseDouble(salaryParam != null ? salaryParam : "0");
        } catch (NumberFormatException e) {
            renderFormError(req, res, "Invalid salary value");
            return;
        }

        Employee employee = new Employee(dept, salary, name);
        employee.setEmail(email);
        employee.setDesignation(designation);

        try {
            employee.setPassword(PasswordUtil.hash(plainPassword));
        } catch (IllegalArgumentException e) {
            renderFormError(req, res, e.getMessage());
            return;
        }

        employee.setRole(role);

        employeeDao.save(employee);

        res.sendRedirect(req.getContextPath() + "/employees");
    }
}
