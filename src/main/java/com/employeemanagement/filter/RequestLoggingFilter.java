package com.employeemanagement.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

/**
 * Logs the application-relative request path and processing duration.
 */
/**
 * Logs the application-relative request path and processing duration.
 */
@WebFilter(filterName = "1_RequestLoggingFilter", urlPatterns = "/*")
public class RequestLoggingFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("RequestLoggingFilter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestedUrl = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String path = requestedUrl.substring(contextPath.length());

        // Measure the complete downstream filter and servlet execution time.
        long startTime = System.currentTimeMillis();

        System.out.println("[RequestLogging] Incoming request -> " + path);

        try {
            chain.doFilter(request, response);
        } finally {
            // Log completion even if downstream request processing fails.
            long endTime = System.currentTimeMillis();
            long processingTime = endTime - startTime;

            System.out.println("[RequestLogging] Request completed -> " + path
                    + " | Processing time: " + processingTime + " ms");
        }
    }

    @Override
    public void destroy() {
        System.out.println("RequestLoggingFilter destroyed");
    }
}
