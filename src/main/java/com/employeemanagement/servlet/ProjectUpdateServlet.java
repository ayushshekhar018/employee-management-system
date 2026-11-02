package com.employeemanagement.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.employeemanagement.dao.ProjectDAO;
import com.employeemanagement.config.ThymeleafConfig;
import com.employeemanagement.model.Project;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

@WebServlet("/projects/update")
public class ProjectUpdateServlet extends HttpServlet {

    private final ProjectDAO projectDao = new ProjectDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String idParam = req.getParameter("id");
        if (idParam == null || idParam.isBlank()) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Project ID is required");
            return;
        }

        long id;
        try {
            id = Long.parseLong(idParam);
        } catch (NumberFormatException e) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid project ID");
            return;
        }

        Project project = projectDao.findById(id);

        if (project == null) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found");
            return;
        }

        ThymeleafConfig.render(req, res, Map.of("project", project), "project-form");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String idParam = req.getParameter("id");
        if (idParam == null || idParam.isBlank()) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Project ID is missing");
            return;
        }

        long id;
        try {
            id = Long.parseLong(idParam);
        } catch (NumberFormatException e) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid project ID");
            return;
        }

        Project existing = projectDao.findById(id);
        if (existing == null) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found");
            return;
        }

        String projectName = req.getParameter("projectName");
        String clientName = req.getParameter("clientName");
        String startDateParam = req.getParameter("startDate");
        String budgetParam = req.getParameter("budget");
        String maxTeamSizeParam = req.getParameter("maximumTeamSize");
        String status = req.getParameter("status");

        LocalDate startDate = null;
        if (startDateParam != null && !startDateParam.isBlank()) {
            try {
                startDate = LocalDate.parse(startDateParam);
            } catch (java.time.format.DateTimeParseException e) {
                res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid date format. Use YYYY-MM-DD.");
                return;
            }
        }

        double budget = 0.0;
        if (budgetParam != null && !budgetParam.isBlank()) {
            try {
                budget = Double.parseDouble(budgetParam);
            } catch (NumberFormatException e) {
                res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid budget value");
                return;
            }
        }

        int maximumTeamSize = 0;
        if (maxTeamSizeParam != null && !maxTeamSizeParam.isBlank()) {
            try {
                maximumTeamSize = Integer.parseInt(maxTeamSizeParam);
            } catch (NumberFormatException e) {
                res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid team size value");
                return;
            }
        }

        existing.setProjectName(projectName);
        existing.setClientName(clientName);
        existing.setStartDate(startDate);
        existing.setBudget(budget);
        existing.setMaximumTeamSize(maximumTeamSize);
        existing.setStatus(status);

        projectDao.update(existing);
        res.sendRedirect(req.getContextPath() + "/projects");
    }
}
