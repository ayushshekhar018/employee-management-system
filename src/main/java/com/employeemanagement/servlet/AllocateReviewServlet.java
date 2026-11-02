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
import java.util.*;

@WebServlet("/allocate/review")
public class AllocateReviewServlet extends HttpServlet {

    private final EmployeeDAO employeeDao = new EmployeeDAO();
    private final ProjectDAO projectDao = new ProjectDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String employeeIdParam = req.getParameter("employeeId");
        String[] projectIdParams = req.getParameterValues("projectIds");

        if (employeeIdParam == null || employeeIdParam.isBlank()) {
            returnError(req, res, "Please select an employee.");
            return;
        }

        if (projectIdParams == null || projectIdParams.length == 0) {
            returnError(req, res, "Please select at least one project.");
            return;
        }

        long employeeId;
        List<Long> projectIds = new ArrayList<>();
        try {
            employeeId = Long.parseLong(employeeIdParam);
            for (String pid : projectIdParams) {
                projectIds.add(Long.parseLong(pid.trim()));
            }
        } catch (NumberFormatException e) {
            returnError(req, res, "Invalid selection. Please try again.");
            return;
        }

        Employee employee = employeeDao.findId(employeeId);
        if (employee == null) {
            returnError(req, res, "Selected employee not found.");
            return;
        }

        List<Map<String, Object>> allocationDetails = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        for (Long projectId : projectIds) {
            Project project = projectDao.findById(projectId);
            if (project == null) {
                warnings.add("Project ID " + projectId + " not found.");
                continue;
            }

            Map<String, Object> detail = new HashMap<>();
            detail.put("project", project);
            detail.put("projectId", projectId);

            String issue = null;
            if ("completed".equalsIgnoreCase(project.getStatus())) {
                issue = "Completed project — allocation will be rejected.";
            } else if (projectDao.getAllocatedCount(projectId) >= project.getMaximumTeamSize()) {
                issue = "Project at maximum capacity — allocation will be rejected.";
            } else if (projectDao.isEmployeeAllocated(projectId, employeeId)) {
                issue = "Employee already allocated to this project — will be skipped.";
            }
            detail.put("issue", issue);

            allocationDetails.add(detail);
        }

        if (allocationDetails.isEmpty()) {
            returnError(req, res, "No valid projects selected.");
            return;
        }

        HttpSession session = req.getSession(true);
        session.setAttribute("allocEmployeeId", employeeId);
        session.setAttribute("allocProjectIds", projectIds);

        List<Project> selectedProjects = new ArrayList<>();
        for (Map<String, Object> detail : allocationDetails) {
            selectedProjects.add((Project) detail.get("project"));
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("employee", employee);
        variables.put("allocationDetails", allocationDetails);
        variables.put("selectedProjects", selectedProjects);
        variables.put("warnings", warnings);

        ThymeleafConfig.render(req, res, variables, "multi-allocation-review");
    }

    private void returnError(HttpServletRequest req, HttpServletResponse res, String message)
            throws IOException {
        res.sendRedirect(req.getContextPath() + "/allocate?error=" +
                java.net.URLEncoder.encode(message, "UTF-8"));
    }
}
