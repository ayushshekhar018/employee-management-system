package com.employeemanagement.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.employeemanagement.config.ThymeleafConfig;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/allocate/result")
public class AllocateResultServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        Object employeeObj = session.getAttribute("allocResultEmployee");
        Object detailsObj = session.getAttribute("allocResultDetails");
        Object successObj = session.getAttribute("allocSuccessCount");
        Object failObj = session.getAttribute("allocFailCount");

        if (employeeObj == null || detailsObj == null) {
            res.sendRedirect(req.getContextPath() + "/allocate");
            return;
        }

        session.removeAttribute("allocResultEmployee");
        session.removeAttribute("allocResultDetails");
        session.removeAttribute("allocSuccessCount");
        session.removeAttribute("allocFailCount");

        Map<String, Object> variables = new HashMap<>();
        variables.put("employee", employeeObj);
        variables.put("resultDetails", detailsObj);
        variables.put("successCount", successObj != null ? successObj : 0);
        variables.put("failCount", failObj != null ? failObj : 0);

        ThymeleafConfig.render(req, res, variables, "multi-allocation-result");
    }
}
