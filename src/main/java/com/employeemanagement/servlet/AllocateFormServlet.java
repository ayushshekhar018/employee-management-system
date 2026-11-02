package com.employeemanagement.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.employeemanagement.dao.EmployeeDAO;
import com.employeemanagement.dao.ProjectDAO;
import com.employeemanagement.config.ThymeleafConfig;
import com.employeemanagement.model.Employee;
import com.employeemanagement.model.Project;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/allocate")
public class AllocateFormServlet extends HttpServlet {

    private final EmployeeDAO employeeDao = new EmployeeDAO();
    private final ProjectDAO projectDao = new ProjectDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        List<Employee> allEmployees = employeeDao.findall();
        List<Project> allProjects = projectDao.findAll();

        Map<Long, Integer> allocatedCounts = projectDao.getAllocatedCounts();

        String error = null;
        String errorParam = req.getParameter("error");
        if (errorParam != null && !errorParam.isBlank()) {
            error = URLDecoder.decode(errorParam, StandardCharsets.UTF_8);
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("allEmployees", allEmployees);
        variables.put("allProjects", allProjects);
        variables.put("allocatedCounts", allocatedCounts);
        variables.put("error", error);

        ThymeleafConfig.render(req, res, variables, "multi-allocation-form");
    }
}
