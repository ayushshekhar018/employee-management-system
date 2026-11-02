package com.employeemanagement.servlet;

import com.employeemanagement.dao.TaskDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@WebServlet("/tasks/status")
public class TaskStatusServlet extends HttpServlet {
    private final TaskDAO taskDao = new TaskDAO();
    private static final List<String> STATUSES = Arrays.asList("todo", "in progress", "done");

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session = req.getSession(false);
        String path = req.getContextPath() + "/tasks";
        if (session == null || session.getAttribute("userId") == null) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        try {
            long taskId = Long.parseLong(req.getParameter("taskId"));
            String status = req.getParameter("status");
            boolean admin = "admin".equalsIgnoreCase(String.valueOf(session.getAttribute("userRole")));
            long userId = ((Number) session.getAttribute("userId")).longValue();
            boolean updated = STATUSES.contains(status) && taskDao.updateStatus(taskId, status, userId, admin);
            session.setAttribute("taskMessage", updated ? "Task status updated." : "Task could not be updated.");
        } catch (RuntimeException e) { session.setAttribute("taskMessage", "Task could not be updated. Check the task and status values."); }
        res.sendRedirect(path);
    }
}
