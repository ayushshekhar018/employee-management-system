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

@WebServlet("/thymeleaf/forward")
public class ThymeleafForwardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        processTemplate(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        processTemplate(req, res);
    }

    private void processTemplate(HttpServletRequest req, HttpServletResponse res)
            throws IOException {

        String templateName = (String) req.getAttribute("_templateName");
        if (templateName == null) {
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No template specified for forwarding");
            return;
        }

        Map<String, Object> variables = new HashMap<>();
        req.getAttributeNames().asIterator().forEachRemaining(name -> {
            if (!name.startsWith("_")) {
                variables.put(name, req.getAttribute(name));
            }
        });

        ThymeleafConfig.render(req, res, variables, templateName);
    }
}
