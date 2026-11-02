package com.employeemanagement.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.employeemanagement.dao.EmployeeDAO;

import java.io.IOException;

@WebServlet("/employees/delete")
public class EmployeeDeleteServlet extends HttpServlet {
    private final EmployeeDAO employeeDAO= new EmployeeDAO();
    @Override
    protected void doPost(HttpServletRequest req,
                          HttpServletResponse res)
            throws ServletException, IOException {
        String idParam = req.getParameter("id");
        if (idParam == null || idParam.isBlank()) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Employee ID is required");
            return;
        }

        long id;
        try {
            id = Long.parseLong(idParam);
        } catch (NumberFormatException e) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid employee ID");
            return;
        }

        if (!employeeDAO.delete(id)) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND, "EMPLOYEE NOT FOUND!");
            return;
        }

        res.sendRedirect(req.getContextPath() + "/employees");
    }
}
