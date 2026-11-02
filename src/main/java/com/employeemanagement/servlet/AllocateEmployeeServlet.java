package com.employeemanagement.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.employeemanagement.dao.EmployeeDAO;
import com.employeemanagement.dao.ProjectDAO;
import com.employeemanagement.config.ThymeleafConfig;
import com.employeemanagement.model.Employee;
import com.employeemanagement.model.Project;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/projects/allocate")
public class AllocateEmployeeServlet extends HttpServlet {

    private final ProjectDAO projectDao = new ProjectDAO();
    private final EmployeeDAO employeeDao = new EmployeeDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String projectIdParam = req.getParameter("projectId");

        if (projectIdParam != null && !projectIdParam.isBlank()) {
            try {
                long projectId = Long.parseLong(projectIdParam);
                HttpSession session = req.getSession(true);
                session.setAttribute("selectedProjectId", projectId);
            } catch (NumberFormatException e) {
                res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid project ID");
                return;
            }
        } else {
            HttpSession session = req.getSession(false);
            if (session == null || session.getAttribute("selectedProjectId") == null) {
                res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Project ID is required");
                return;
            }
            projectIdParam = String.valueOf(session.getAttribute("selectedProjectId"));
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

        List<Employee> allEmployees = employeeDao.findall();
        List<Employee> allocatedEmployees = projectDao.getAllocatedEmployees(projectId);
        int allocatedCount = projectDao.getAllocatedCount(projectId);

        Map<String, Object> variables = new HashMap<>();
        variables.put("project", project);
        variables.put("allEmployees", allEmployees);
        variables.put("allocatedEmployees", allocatedEmployees);
        variables.put("allocatedCount", allocatedCount);
        variables.put("maxTeamSize", project.getMaximumTeamSize());

        ThymeleafConfig.render(req, res, variables, "allocation-form");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String projectIdParam = req.getParameter("projectId");
        String employeeIdParam = req.getParameter("employeeId");

        if (projectIdParam == null || projectIdParam.isBlank()) {
            HttpSession session = req.getSession(false);
            if (session != null && session.getAttribute("selectedProjectId") != null) {
                projectIdParam = String.valueOf(session.getAttribute("selectedProjectId"));
            }
        }

        if (projectIdParam == null || projectIdParam.isBlank() ||
            employeeIdParam == null || employeeIdParam.isBlank()) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Project ID and Employee ID are required");
            return;
        }

        long projectId;
        long employeeId;
        try {
            projectId = Long.parseLong(projectIdParam);
            employeeId = Long.parseLong(employeeIdParam);
        } catch (NumberFormatException e) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid project or employee ID");
            return;
        }

        HttpSession session = req.getSession(true);
        session.setAttribute("selectedProjectId", projectId);

        Project project = projectDao.findById(projectId);
        if (project == null) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found");
            return;
        }

        String errorMessage = null;

        if ("completed".equalsIgnoreCase(project.getStatus())) {
            errorMessage = "Cannot allocate to a completed project.";
        }
        else if (projectDao.getAllocatedCount(projectId) >= project.getMaximumTeamSize()) {
            errorMessage = "Project has reached its maximum team capacity.";
        }
        else if (projectDao.isEmployeeAllocated(projectId, employeeId)) {
            errorMessage = "This employee is already allocated to this project.";
        }

        if (errorMessage != null) {
            List<Employee> allEmployees = employeeDao.findall();
            List<Employee> allocatedEmployees = projectDao.getAllocatedEmployees(projectId);
            int allocatedCount = projectDao.getAllocatedCount(projectId);

            Map<String, Object> variables = new HashMap<>();
            variables.put("project", project);
            variables.put("allEmployees", allEmployees);
            variables.put("allocatedEmployees", allocatedEmployees);
            variables.put("allocatedCount", allocatedCount);
            variables.put("maxTeamSize", project.getMaximumTeamSize());
            variables.put("error", errorMessage);

            ThymeleafConfig.render(req, res, variables, "allocation-form");
            return;
        }

        boolean success = projectDao.allocateEmployee(projectId, employeeId);
        if (success) {
            session.removeAttribute("selectedProjectId");
            res.sendRedirect(req.getContextPath() + "/projects/employees?projectId=" + projectId);
        } else {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Failed to allocate employee");
        }
    }
}
