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

@WebServlet("/employees/update")
public class EmployeeUpdateServlet extends HttpServlet {

    private EmployeeDAO employeeDao = new EmployeeDAO();
    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse res)
            throws ServletException, IOException {

        String idParam = req.getParameter("id");
        if (idParam == null || idParam.isBlank()) {
            res.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Employee ID is required"
            );
            return;
        }

        long id;
        try {
            id = Long.parseLong(idParam);
        } catch (NumberFormatException e) {
            res.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid employee ID"
            );
            return;
        }

        Employee employee = employeeDao.findId(id);

        if (employee == null) {
            res.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Employee not found"
            );
            return;
        }

        ThymeleafConfig.render(
                req,
                res,
                Map.of("employee", employee),
                "update-employee"
        );
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String idParam = req.getParameter("id");

        if (idParam == null || idParam.isBlank()) {
            res.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Employee ID is missing"
            );
            return;
        }

        Long id;
        try {
            id = Long.parseLong(idParam);
        } catch (NumberFormatException e) {
            res.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid employee ID"
            );
            return;
        }

        String name = req.getParameter("name");
        String dept = req.getParameter("dept");
        String salaryParam = req.getParameter("salary");
        String newPassword = req.getParameter("newPassword");

        if (salaryParam == null || salaryParam.isBlank()) {
            res.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Salary is missing from the form"
            );
            return;
        }

        double salary;

        try {
            salary = Double.parseDouble(salaryParam);
        } catch (NumberFormatException e) {
            res.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid salary value"
            );
            return;
        }

        Employee employee = employeeDao.findId(id);

        if(employee == null){
            res.sendError(404,"Not Found");
            return;
        }

        employee.setName(name);
        employee.setDept(dept);
        employee.setSalary(salary);

        // Update password only if a new password is provided
        if (newPassword != null && !newPassword.isEmpty()) {
            try {
                employee.setPassword(PasswordUtil.hash(newPassword));
            } catch (IllegalArgumentException e) {
                req.setAttribute("error", e.getMessage());
                req.setAttribute("employee", employee);
                ThymeleafConfig.render(req, res, Map.of("employee", employee, "error", e.getMessage()), "update-employee");
                return;
            }
        }

        employeeDao.update(employee);

        
        res.sendRedirect(req.getContextPath()+"/employees");
    }
}
