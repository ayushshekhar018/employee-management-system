package com.employeemanagement.servlet;

import com.employeemanagement.config.ThymeleafConfig;
import com.employeemanagement.dao.EmployeeDAO;
import com.employeemanagement.dao.ProjectDAO;
import com.employeemanagement.dao.TaskDAO;
import com.employeemanagement.model.Task;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

/** Handles task form rendering, validation, and task creation. */
@WebServlet("/tasks/save")
public class TaskSaveServlet extends HttpServlet {
    private final TaskDAO taskDao = new TaskDAO();
    private final ProjectDAO projectDao = new ProjectDAO();
    private final EmployeeDAO employeeDao = new EmployeeDAO();

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        renderForm(req, res, null);
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String title = req.getParameter("title");
        String priority = req.getParameter("priority");
        String status = req.getParameter("status");
        String projectParam = req.getParameter("projectId");
        String assigneeParam = req.getParameter("assigneeId");
        String dueParam = req.getParameter("dueDate");
        String error = null;
        long projectId = 0; Long assigneeId = null; LocalDate dueDate = null;
        // Validate request data before passing it to the persistence layer.
        try {
            if (title == null || title.isBlank() || title.trim().length() > 150) throw new IllegalArgumentException("Enter a task title (up to 150 characters).");
            if (!"low".equals(priority) && !"medium".equals(priority) && !"high".equals(priority)) throw new IllegalArgumentException("Choose a valid priority.");
            if (!"todo".equals(status) && !"in progress".equals(status) && !"done".equals(status)) throw new IllegalArgumentException("Choose a valid status.");
            projectId = Long.parseLong(projectParam);
            if (assigneeParam != null && !assigneeParam.isBlank()) assigneeId = Long.parseLong(assigneeParam);
            if (dueParam != null && !dueParam.isBlank()) dueDate = LocalDate.parse(dueParam);
            taskDao.create(title, req.getParameter("description"), priority, dueDate, projectId, assigneeId);
            HttpSession session = req.getSession(); session.setAttribute("taskMessage", "Task created successfully.");
            res.sendRedirect(req.getContextPath() + "/tasks"); return;
        } catch (NumberFormatException | DateTimeParseException e) { error = "Enter valid project, assignee, and due date values."; }
        catch (IllegalArgumentException e) { error = e.getMessage(); }
        renderForm(req, res, error);
    }

    private void renderForm(HttpServletRequest req, HttpServletResponse res, String error) throws ServletException, IOException {
        Map<String, Object> vars = new HashMap<>();
        vars.put("projects", projectDao.findAll()); vars.put("employees", employeeDao.findall());
        vars.put("task", new Task()); vars.put("error", error); vars.put("pageTitle", "Create Task");
        ThymeleafConfig.render(req, res, vars, "task-form");
    }
}
