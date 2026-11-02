package com.employeemanagement.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

/**
 * Applies authentication by default and restricts management routes to administrators.
 */
@WebFilter(filterName = "2_AuthenticationFilter", urlPatterns = "/*")
public class AuthenticationFilter implements Filter {

    private static final List<String> PUBLIC_EXACT_PATHS = List.of(
            "/", "/login", "/logout", "/favicon.ico"
    );

    private static final List<String> PUBLIC_PATH_PREFIXES = List.of(
            "/css/", "/js/", "/images/", "/assets/", "/error/"
    );

    // All employee/project management and allocation routes are administrator-only.
    private static final List<String> ADMIN_PATHS = List.of(
            "/employees", "/projects", "/allocate", "/tasks/save", "/tasks/delete"
    );

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No filter-specific initialization is required.
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String contextPath = httpRequest.getContextPath();
        String path = httpRequest.getRequestURI().substring(contextPath.length());

        if (isPublicPath(path)) {
            if ("/".equals(path)) {
                httpResponse.sendRedirect(contextPath + "/login");
                return;
            }
            chain.doFilter(request, response);
            return;
        }

        // Protect every non-public route, including future servlet mappings.
        HttpSession session = httpRequest.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            httpResponse.sendRedirect(contextPath + "/login");
            return;
        }

        if (isAdminPath(path)) {
            Object role = session.getAttribute("userRole");
            if (role == null || !"admin".equalsIgnoreCase(role.toString())) {
                session.setAttribute(
                        "authError",
                        "You do not have permission to access this page. Admin access required."
                );
                httpResponse.sendRedirect(contextPath + "/dashboard");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // No resources to release.
    }

    private boolean isPublicPath(String path) {
        if (PUBLIC_EXACT_PATHS.contains(path)) {
            return true;
        }
        return PUBLIC_PATH_PREFIXES.stream().anyMatch(path::startsWith);
    }

    private boolean isAdminPath(String path) {
        return ADMIN_PATHS.stream().anyMatch(pattern ->
                path.equals(pattern) || path.startsWith(pattern + "/"));
    }
}
