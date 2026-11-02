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

@WebServlet("/projects/save")
public class ProjectSaveServlet extends HttpServlet {

    private final ProjectDAO projectDao = new ProjectDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        ThymeleafConfig.render(req, res, Map.of("project", new Project()), "project-form");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

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
                Project emptyProject = new Project();
                emptyProject.setProjectName(projectName);
                emptyProject.setClientName(clientName);
                emptyProject.setBudget(0.0);
                emptyProject.setMaximumTeamSize(0);
                emptyProject.setStatus(status != null && !status.isBlank() ? status : "planned");
                ThymeleafConfig.render(req, res, Map.of("project", emptyProject, "error", "Invalid date format. Use YYYY-MM-DD."), "project-form");
                return;
            }
        }

        double budget = 0.0;
        if (budgetParam != null && !budgetParam.isBlank()) {
            try {
                budget = Double.parseDouble(budgetParam);
            } catch (NumberFormatException e) {
                Project emptyProject = new Project();
                emptyProject.setProjectName(projectName);
                emptyProject.setClientName(clientName);
                emptyProject.setStartDate(startDate);
                emptyProject.setMaximumTeamSize(0);
                emptyProject.setStatus(status != null && !status.isBlank() ? status : "planned");
                ThymeleafConfig.render(req, res, Map.of("project", emptyProject, "error", "Invalid budget value"), "project-form");
                return;
            }
        }

        int maximumTeamSize = 0;
        if (maxTeamSizeParam != null && !maxTeamSizeParam.isBlank()) {
            try {
                maximumTeamSize = Integer.parseInt(maxTeamSizeParam);
            } catch (NumberFormatException e) {
                Project emptyProject = new Project();
                emptyProject.setProjectName(projectName);
                emptyProject.setClientName(clientName);
                emptyProject.setStartDate(startDate);
                emptyProject.setBudget(budget);
                emptyProject.setStatus(status != null && !status.isBlank() ? status : "planned");
                ThymeleafConfig.render(req, res, Map.of("project", emptyProject, "error", "Invalid team size value"), "project-form");
                return;
            }
        }

        if (status == null || status.isBlank()) {
            status = "planned";
        }

        if (projectName == null || projectName.isBlank()) {
            Project emptyProject = new Project();
            emptyProject.setProjectName(projectName);
            emptyProject.setClientName(clientName);
            emptyProject.setStartDate(startDate);
            emptyProject.setBudget(budget);
            emptyProject.setMaximumTeamSize(maximumTeamSize);
            emptyProject.setStatus(status);
            ThymeleafConfig.render(req, res, Map.of("project", emptyProject, "error", "Project name is required"), "project-form");
            return;
        }

        if (!Double.isFinite(budget) || budget < 0) {
            Project invalidProject = new Project(projectName, clientName, startDate, budget, maximumTeamSize, status);
            ThymeleafConfig.render(req, res, Map.of(
                    "project", invalidProject,
                    "error", "Budget must be a finite, non-negative number."
            ), "project-form");
            return;
        }
        if (maximumTeamSize < 0) {
            Project invalidProject = new Project(projectName, clientName, startDate, budget, maximumTeamSize, status);
            ThymeleafConfig.render(req, res, Map.of(
                    "project", invalidProject,
                    "error", "Maximum team size cannot be negative."
            ), "project-form");
            return;
        }
        if (projectName.length() > 150 || (clientName != null && clientName.length() > 150)) {
            Project invalidProject = new Project(projectName, clientName, startDate, budget, maximumTeamSize, status);
            ThymeleafConfig.render(req, res, Map.of(
                    "project", invalidProject,
                    "error", "Project and client names must be at most 150 characters."
            ), "project-form");
            return;
        }
        if (!"planned".equals(status) && !"active".equals(status) && !"completed".equals(status)) {
            Project invalidProject = new Project(projectName, clientName, startDate, budget, maximumTeamSize, status);
            ThymeleafConfig.render(req, res, Map.of(
                    "project", invalidProject,
                    "error", "Choose a valid project status."
            ), "project-form");
            return;
        }

        Project project = new Project(projectName, clientName, startDate, budget, maximumTeamSize, status);
        projectDao.save(project);

        res.sendRedirect(req.getContextPath() + "/projects");
    }
}
