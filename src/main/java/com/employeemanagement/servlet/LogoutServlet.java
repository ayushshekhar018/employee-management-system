package com.employeemanagement.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);

        if (session != null) {
            session.removeAttribute("userId");
            session.removeAttribute("userName");
            session.removeAttribute("userRole");
            session.removeAttribute("userEmail");
            session.removeAttribute("authError");
            session.removeAttribute("selectedProjectId");
            session.removeAttribute("allocEmployeeId");
            session.removeAttribute("allocProjectIds");
            session.removeAttribute("allocResultEmployee");
            session.removeAttribute("allocResultDetails");
            session.removeAttribute("allocSuccessCount");
            session.removeAttribute("allocFailCount");

            session.invalidate();
        }

        res.sendRedirect(req.getContextPath() + "/login");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        doGet(req, res);
    }
}
