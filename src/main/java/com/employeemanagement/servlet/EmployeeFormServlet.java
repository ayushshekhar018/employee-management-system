package com.employeemanagement.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.employeemanagement.config.ThymeleafConfig;
import com.employeemanagement.model.Employee;

import java.io.IOException;
import java.util.Map;
@WebServlet("/employees/new")
public class EmployeeFormServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)throws ServletException, IOException {
        ThymeleafConfig.render(req,res, Map.of("employee",new Employee()),"add-new-employee-form");
    }
}
