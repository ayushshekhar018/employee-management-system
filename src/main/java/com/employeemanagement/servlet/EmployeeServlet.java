package com.employeemanagement.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.employeemanagement.dao.EmployeeDAO;
import com.employeemanagement.config.ThymeleafConfig;
import com.employeemanagement.model.Employee;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/employees")
public class EmployeeServlet extends HttpServlet {
    private EmployeeDAO employeeDAO= new EmployeeDAO();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        List<Employee> employees=employeeDAO.findall();
        ThymeleafConfig.render(req,res, Map.of("employees",employees),"employee");
    }
}
