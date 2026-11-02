package com.employeemanagement.servlet;

import com.employeemanagement.config.ThymeleafConfig;
import com.employeemanagement.dao.TaskDAO;
import com.employeemanagement.model.Task;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/tasks")
public class TasksServlet extends HttpServlet {
    private final TaskDAO taskDao = new TaskDAO();

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        boolean admin = "admin".equalsIgnoreCase(String.valueOf(session.getAttribute("userRole")));
        long userId = ((Number) session.getAttribute("userId")).longValue();
        List<Task> tasks = admin ? taskDao.findAll() : taskDao.findForEmployee(userId);
        String statusFilter = req.getParameter("status");
        if (statusFilter != null && !statusFilter.isBlank() && !"all".equals(statusFilter)) {
            tasks.removeIf(task -> !statusFilter.equalsIgnoreCase(task.getStatus()));
        }
        Map<String, Object> vars = new HashMap<>();
        vars.put("tasks", tasks); vars.put("isAdmin", admin); vars.put("statusFilter", statusFilter == null ? "all" : statusFilter);
        vars.put("pageTitle", "Task Board");
        if (session.getAttribute("taskMessage") != null) {
            vars.put("message", session.getAttribute("taskMessage")); session.removeAttribute("taskMessage");
        }
        ThymeleafConfig.render(req, res, vars, "tasks");
    }
}
