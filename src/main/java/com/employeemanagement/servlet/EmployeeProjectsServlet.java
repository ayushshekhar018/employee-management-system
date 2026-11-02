package com.employeemanagement.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.employeemanagement.dao.EmployeeDAO;
import com.employeemanagement.config.ThymeleafConfig;
import com.employeemanagement.model.Employee;
import com.employeemanagement.model.Project;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/employees/projects")
public class EmployeeProjectsServlet extends HttpServlet {

    private final EmployeeDAO employeeDao = new EmployeeDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String employeeIdParam = req.getParameter("employeeId");
        if (employeeIdParam == null || employeeIdParam.isBlank()) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Employee ID is required");
            return;
        }

        long employeeId;
        try {
            employeeId = Long.parseLong(employeeIdParam);
        } catch (NumberFormatException e) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid employee ID");
            return;
        }

        Employee employee = employeeDao.findId(employeeId);
        if (employee == null) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND, "Employee not found");
            return;
        }

        List<Project> projects = employeeDao.getEmployeeProjects(employeeId);

        Map<String, Object> variables = new HashMap<>();
        variables.put("employee", employee);
        variables.put("projects", projects);

        ThymeleafConfig.render(req, res, variables, "employee-projects");
    }
}
