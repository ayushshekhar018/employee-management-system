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

import java.util.Map;
import java.io.IOException;

/** Loads and updates an existing employee record. */
@WebServlet("/employees/update")
public class EmployeeUpdateServlet extends HttpServlet {

    private final EmployeeDAO employeeDao = new EmployeeDAO();

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse res)
            throws ServletException, IOException {

        String idParam = req.getParameter("id");
        if (idParam == null || idParam.isBlank()) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Employee ID is required");
            return;
        }

        long id;
        try {
            id = Long.parseLong(idParam);
        } catch (NumberFormatException e) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid employee ID");
            return;
        }

        Employee employee = employeeDao.findId(id);
        if (employee == null) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND, "Employee not found");
            return;
        }

        ThymeleafConfig.render(req, res, Map.of("employee", employee), "update-employee");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        String idParam = req.getParameter("id");

        if (idParam == null || idParam.isBlank()) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Employee ID is missing");
            return;
        }

        long id;
        try {
            id = Long.parseLong(idParam);
        } catch (NumberFormatException e) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid employee ID");
            return;
        }

        String name = req.getParameter("name");
        String dept = req.getParameter("dept");
        String salaryParam = req.getParameter("salary");
        String newPassword = req.getParameter("newPassword");

        if (salaryParam == null || salaryParam.isBlank()) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Salary is missing from the form");
            return;
        }

        double salary;
        try {
            salary = Double.parseDouble(salaryParam);
        } catch (NumberFormatException e) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid salary value");
            return;
        }

        Employee employee = employeeDao.findId(id);
        if (employee == null) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND, "Employee not found");
            return;
        }

        employee.setName(name);
        employee.setDept(dept);
        employee.setSalary(salary);

        // An empty password field leaves the current password unchanged.
        if (newPassword != null && !newPassword.isEmpty()) {
            try {
                // The demo account follows its explicit plain-text exception; other accounts use BCrypt.
                employee.setPassword(PasswordUtil.hashForAccount(
                        employee.getEmail(),
                        employee.getRole(),
                        newPassword
                ));
            } catch (IllegalArgumentException e) {
                ThymeleafConfig.render(
                        req,
                        res,
                        Map.of("employee", employee, "error", e.getMessage()),
                        "update-employee"
                );
                return;
            }
        }

        employeeDao.update(employee);
        res.sendRedirect(req.getContextPath() + "/employees");
    }
}
