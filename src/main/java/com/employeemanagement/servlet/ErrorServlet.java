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

@WebServlet({"/error/404", "/error/500"})
public class ErrorServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        processError(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        processError(req, res);
    }

    private void processError(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        Integer statusCode = (Integer) req.getAttribute("jakarta.servlet.error.status_code");
        String errorMessage = (String) req.getAttribute("jakarta.servlet.error.message");
        String requestUri = (String) req.getAttribute("jakarta.servlet.error.request_uri");
        String exceptionType = (String) req.getAttribute("jakarta.servlet.error.exception_type");

        if (statusCode == null) {
            statusCode = 500;
        }
        // Do not expose internal exception messages or database details in a public error page.
        if (statusCode == 404) {
            errorMessage = "The requested page was not found.";
        } else if (statusCode >= 500) {
            errorMessage = "An internal server error occurred. Please try again later.";
        } else if (errorMessage == null || errorMessage.isBlank()) {
            errorMessage = "An unexpected error occurred.";
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("statusCode", statusCode);
        variables.put("errorMessage", errorMessage);
        variables.put("requestUri", requestUri != null ? requestUri : "");
        variables.put("exceptionType", exceptionType);

        String templateName = "error";

        ThymeleafConfig.render(req, res, variables, templateName);
    }
}
