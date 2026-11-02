package com.employeemanagement.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.employeemanagement.dao.ProjectDAO;
import com.employeemanagement.config.ThymeleafConfig;
import com.employeemanagement.model.Employee;
import com.employeemanagement.model.Project;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/projects/employees")
public class ProjectEmployeesServlet extends HttpServlet {

    private final ProjectDAO projectDao = new ProjectDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String projectIdParam = req.getParameter("projectId");
        if (projectIdParam == null || projectIdParam.isBlank()) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Project ID is required");
            return;
        }

        long projectId;
        try {
            projectId = Long.parseLong(projectIdParam);
        } catch (NumberFormatException e) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid project ID");
            return;
        }

        Project project = projectDao.findById(projectId);
        if (project == null) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found");
            return;
        }

        List<Employee> allocatedEmployees = projectDao.getAllocatedEmployees(projectId);
        int allocatedCount = projectDao.getAllocatedCount(projectId);

        Map<String, Object> variables = new HashMap<>();
        variables.put("project", project);
        variables.put("allocatedEmployees", allocatedEmployees);
        variables.put("allocatedCount", allocatedCount);
        variables.put("maxTeamSize", project.getMaximumTeamSize());

        ThymeleafConfig.render(req, res, variables, "allocation-review");
    }
}
