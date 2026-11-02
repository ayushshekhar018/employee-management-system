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
import java.util.logging.Level;
import java.util.logging.Logger;

/** Authenticates credentials and establishes the user's HTTP session. */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(LoginServlet.class.getName());
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

        req.setCharacterEncoding("UTF-8");

        // Normalize surrounding whitespace in the email, but never modify the password.
        String email = req.getParameter("email");
        email = email == null ? null : email.trim();
        String password = req.getParameter("password");

        if (email == null || email.isBlank() || password == null || password.isEmpty()) {
            renderLoginError(req, res, email, "Enter both your email address and password.");
            return;
        }

        try {
            Employee employee = userDAO.authenticate(email, password);

            if (employee == null) {
                renderLoginError(req, res, email, "Invalid email or password. Please try again.");
                return;
            }

            // Rotate the session after successful authentication to prevent session fixation.
            HttpSession previousSession = req.getSession(false);
            if (previousSession != null) {
                previousSession.invalidate();
            }

            HttpSession session = req.getSession(true);
            session.setAttribute("userId", employee.getId());
            session.setAttribute("userName", employee.getName());
            session.setAttribute("userRole", employee.getRole());
            session.setAttribute("userEmail", employee.getEmail());
            session.setMaxInactiveInterval(30 * 60);

            res.sendRedirect(req.getContextPath() + "/dashboard");
        } catch (RuntimeException exception) {
            // Preserve the stack trace in server logs; never expose DB details to visitors.
            LOGGER.log(Level.SEVERE, "Login failed while accessing the authentication store.", exception);
            renderLoginError(
                    req,
                    res,
                    email,
                    "Sign-in is temporarily unavailable. Please try again later."
            );
        }
    }

    private void renderLoginError(
            HttpServletRequest req,
            HttpServletResponse res,
            String email,
            String message
    ) throws IOException {
        Map<String, Object> variables = new HashMap<>();
        Employee login = new Employee();
        login.setEmail(email);
        variables.put("login", login);
        variables.put("error", message);
        ThymeleafConfig.render(req, res, variables, "login");
    }
}
