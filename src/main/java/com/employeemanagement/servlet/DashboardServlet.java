package com.employeemanagement.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.employeemanagement.config.ThymeleafConfig;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("pageTitle", "Dashboard");

        // Copy the one-time authorization message before removing it from the session.
        Object authError = session.getAttribute("authError");
        if (authError != null) {
            variables.put("authError", authError);
            session.removeAttribute("authError");
        }

        ThymeleafConfig.render(req, res, variables, "dashboard");
    }
}
