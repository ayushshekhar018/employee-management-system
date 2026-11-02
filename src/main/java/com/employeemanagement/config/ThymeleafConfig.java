package com.employeemanagement.config;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.employeemanagement.util.HibernateUtil;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;
import java.util.Map;
/**
 * Initializes the Thymeleaf renderer; Hibernate is initialized lazily on first data access.
 */
@WebListener
public class ThymeleafConfig implements ServletContextListener {

    private static JakartaServletWebApplication application;
    private static TemplateEngine templateEngine;

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        // Adapt the ServletContext to Thymeleaf's Jakarta Servlet integration.
        application = JakartaServletWebApplication.buildApplication(
                sce.getServletContext()
        );

        WebApplicationTemplateResolver resolver =
                new WebApplicationTemplateResolver(application);

        resolver.setPrefix("/WEB-INF/templates/");
        resolver.setSuffix(".html");

        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(resolver);

        // Keep web startup independent of database availability.
        // HibernateUtil creates the SessionFactory lazily on the first DAO operation.
        // This allows the login page and Render health check to start even if the
        // external database is temporarily unreachable; database errors still surface
        // when a database-backed action is attempted.
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Release the shared SessionFactory when the application is stopped.
        HibernateUtil.shutdown();
    }

    public static void render(
            HttpServletRequest req,
            HttpServletResponse res,
            Map<String, Object> variables,
            String templateName
    ) throws IOException {

        // Create a request-scoped Thymeleaf context and expose controller-supplied model data.
        // Build a request-scoped rendering context and expose controller-supplied model data.
        IWebExchange exchange = application.buildExchange(req, res);

        WebContext webContext = new WebContext(exchange);
        webContext.setVariables(variables);

        templateEngine.process(
                templateName,
                webContext,
                res.getWriter()
        );
    }

    public static void forward(
            HttpServletRequest req,
            HttpServletResponse res,
            Map<String, Object> variables,
            String templateName
    ) throws ServletException, IOException {

        // Copy model values to request attributes for the forwarding servlet.
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            req.setAttribute(entry.getKey(), entry.getValue());
        }
        req.setAttribute("_templateName", templateName);

        ServletContext context = req.getServletContext();
        RequestDispatcher dispatcher = context.getRequestDispatcher("/thymeleaf/forward");
        dispatcher.forward(req, res);
    }

    public static void include(
            HttpServletRequest req,
            HttpServletResponse res,
            Map<String, Object> variables,
            String templateName
    ) throws ServletException, IOException {

        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            req.setAttribute(entry.getKey(), entry.getValue());
        }
        req.setAttribute("_templateName", templateName);

        ServletContext context = req.getServletContext();
        RequestDispatcher dispatcher = context.getRequestDispatcher("/thymeleaf/include");
        dispatcher.include(req, res);
    }
}