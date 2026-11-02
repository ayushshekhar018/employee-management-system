package com.employeemanagement.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.employeemanagement.config.ThymeleafConfig;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        jakarta.servlet.http.HttpSession session = req.getSession(false);
        if (session != null) {
            session.removeAttribute("authError");
        }

        // The filter normally enforces authentication; retain a controller-level guard as defence in depth.
        if (session == null || session.getAttribute("userId") == null) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("pageTitle", "Dashboard");

        ThymeleafConfig.render(req, res, variables, "dashboard");
    }
}
