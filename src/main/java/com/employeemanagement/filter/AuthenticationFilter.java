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
import java.util.Arrays;
import java.util.List;

/**
 * Applies route-level authentication and administrator access checks.
 */
/**
 * Applies route-level authentication and administrator access checks.
 */
@WebFilter(filterName = "2_AuthenticationFilter", urlPatterns = "/*")
public class AuthenticationFilter implements Filter {

    // These routes and static resources are available without an authenticated session.
    // Routes and static resources that can be requested without a signed-in session.
    private static final List<String> PUBLIC_URLS = Arrays.asList(
            "/login",
            "/logout",
            "/css/",
            "/js/",
            "/images/",
            "/assets/",
            "/thymeleaf/",
            "/error/"
    );

    // These routes require a signed-in user but are not restricted to administrators.
    // Routes available to any authenticated account.
    private static final List<String> AUTHENTICATED_PATTERNS = Arrays.asList(
            "/dashboard",
            "/my-projects",
            "/employees/projects",
            "/tasks",
            "/tasks/status",
            "/allocate/review",
            "/allocate/result"
    );

    // These routes are reserved for accounts whose session role is admin.
    // Routes reserved for users with the administrator role.
    private static final List<String> ADMIN_PATTERNS = Arrays.asList(
            "/employees",
            "/employees/new",
            "/employees/update",
            "/employees/delete",
            "/employees/save",
            "/projects",
            "/projects/save",
            "/projects/update",
            "/projects/allocate",
            "/projects/employees",
            "/tasks/save",
            "/tasks/delete",
            "/allocate",
            "/allocate/confirm"
    );

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String uri = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();

        String path = uri.substring(contextPath.length());

        // Public resources bypass session checks.
        // Public resources bypass the session checks.
        if (isPublicUrl(path)) {
            chain.doFilter(request, response);
            return;
        }

        if (isAuthenticatedUrl(path) || isAdminUrl(path)) {
            HttpSession session = httpRequest.getSession(false);

            if (session == null || session.getAttribute("userId") == null) {
                httpResponse.sendRedirect(contextPath + "/login");
                return;
            }

            // Authentication alone is not sufficient for administrative operations.
            // A signed-in session alone does not grant administrative access.
            if (isAdminUrl(path)) {
                String userRole = (String) session.getAttribute("userRole");
                if (userRole == null || !"admin".equalsIgnoreCase(userRole)) {
                    session.setAttribute("authError", "You do not have permission to access this page. Admin access required.");
                    httpResponse.sendRedirect(contextPath + "/dashboard");
                    return;
                }
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

    private boolean isPublicUrl(String path) {
        for (String publicUrl : PUBLIC_URLS) {
            if (path.equals(publicUrl) || path.startsWith(publicUrl)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAuthenticatedUrl(String path) {
        for (String pattern : AUTHENTICATED_PATTERNS) {
            if (path.equals(pattern) || path.startsWith(pattern)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAdminUrl(String path) {
        for (String pattern : ADMIN_PATTERNS) {
            if (path.equals(pattern) || path.startsWith(pattern)) {
                return true;
            }
        }
        return false;
    }
}
