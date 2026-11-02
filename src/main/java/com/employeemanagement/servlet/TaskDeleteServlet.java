package com.employeemanagement.servlet;

import com.employeemanagement.dao.TaskDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/tasks/delete")
public class TaskDeleteServlet extends HttpServlet {
    private final TaskDAO taskDao = new TaskDAO();
    @Override protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        try {
            boolean deleted = taskDao.delete(Long.parseLong(req.getParameter("taskId")));
            session.setAttribute("taskMessage", deleted ? "Task deleted." : "Task was not found.");
        } catch (RuntimeException e) { session.setAttribute("taskMessage", "Task could not be deleted."); }
        res.sendRedirect(req.getContextPath() + "/tasks");
    }
}
