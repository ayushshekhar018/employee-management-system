package com.employeemanagement.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.employeemanagement.dao.ProjectDAO;
import com.employeemanagement.config.ThymeleafConfig;
import com.employeemanagement.model.Project;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/projects")
public class ProjectServlet extends HttpServlet {

    private final ProjectDAO projectDao = new ProjectDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        List<Project> projects = projectDao.findAll();

        Map<Long, Integer> allocatedCounts = projectDao.getAllocatedCounts();

        Map<String, Object> variables = new HashMap<>();
        variables.put("projects", projects);
        variables.put("allocatedCounts", allocatedCounts);

        ThymeleafConfig.render(req, res, variables, "project-list");
    }
}
