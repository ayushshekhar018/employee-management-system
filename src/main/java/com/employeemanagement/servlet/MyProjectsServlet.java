package com.employeemanagement.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.employeemanagement.dao.EmployeeDAO;
import com.employeemanagement.config.ThymeleafConfig;
import com.employeemanagement.model.Employee;
import com.employeemanagement.model.Project;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/my-projects")
public class MyProjectsServlet extends HttpServlet {

    private final EmployeeDAO employeeDao = new EmployeeDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        long employeeId = (Long) session.getAttribute("userId");
        Employee employee = employeeDao.findId(employeeId);

        if (employee == null) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND, "Employee not found");
            return;
        }

        List<Project> projects = employeeDao.getEmployeeProjects(employeeId);

        Map<String, Object> variables = new HashMap<>();
        variables.put("employee", employee);
        variables.put("projects", projects);
        variables.put("isOwnProfile", true);

        ThymeleafConfig.render(req, res, variables, "employee-projects");
    }
}
