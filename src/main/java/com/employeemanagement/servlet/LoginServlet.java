package com.employeemanagement.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.employeemanagement.dao.UserDAO;
import com.employeemanagement.config.ThymeleafConfig;
import com.employeemanagement.model.Employee;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        Map<String, Object> variables = new HashMap<>();
        variables.put("login", new Employee());
        ThymeleafConfig.render(req, res, variables, "login");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String email = req.getParameter("email");
        String password = req.getParameter("password");

        Employee employee = userDAO.authenticate(email, password);

        if (employee != null) {
            HttpSession session = req.getSession(true);
            session.setAttribute("userId", employee.getId());
            session.setAttribute("userName", employee.getName());
            session.setAttribute("userRole", employee.getRole());
            session.setAttribute("userEmail", employee.getEmail());
            session.setMaxInactiveInterval(30 * 60);

            res.sendRedirect(req.getContextPath() + "/dashboard");
        } else {
            Map<String, Object> variables = new HashMap<>();
            Employee login = new Employee();
            login.setEmail(email);
            variables.put("login", login);
            variables.put("error", "Invalid email or password. Please try again.");
            ThymeleafConfig.render(req, res, variables, "login");
        }
    }
}
