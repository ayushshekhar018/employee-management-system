package com.employeemanagement.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.employeemanagement.dao.EmployeeDAO;
import com.employeemanagement.dao.ProjectDAO;
import com.employeemanagement.config.ThymeleafConfig;
import com.employeemanagement.model.Employee;
import com.employeemanagement.model.Project;

import java.io.IOException;
import java.util.*;

@WebServlet("/allocate/confirm")
public class AllocateConfirmServlet extends HttpServlet {

    private final EmployeeDAO employeeDao = new EmployeeDAO();
    private final ProjectDAO projectDao = new ProjectDAO();

    @Override
    @SuppressWarnings("unchecked")
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        Object empIdObj = session.getAttribute("allocEmployeeId");
        Object projIdsObj = session.getAttribute("allocProjectIds");

        if (empIdObj == null || projIdsObj == null) {
            res.sendRedirect(req.getContextPath() + "/allocate");
            return;
        }

        long employeeId = ((Number) empIdObj).longValue();
        List<Long> projectIds = (List<Long>) projIdsObj;

        Employee employee = employeeDao.findId(employeeId);
        if (employee == null) {
            session.removeAttribute("allocEmployeeId");
            session.removeAttribute("allocProjectIds");
            res.sendRedirect(req.getContextPath() + "/allocate");
            return;
        }

        Map<Long, String> errors = projectDao.batchAllocateEmployee(employeeId, projectIds);

        session.removeAttribute("allocEmployeeId");
        session.removeAttribute("allocProjectIds");

        List<Map<String, Object>> resultDetails = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;

        for (Long projectId : projectIds) {
            Map<String, Object> detail = new HashMap<>();
            Project project = projectDao.findById(projectId);
            if (project != null) {
                detail.put("project", project);
            }

            String error = errors.get(projectId);
            if (error != null) {
                detail.put("success", false);
                detail.put("message", error);
                failCount++;
            } else {
                detail.put("success", true);
                detail.put("message", "Successfully allocated.");
                successCount++;
            }
            resultDetails.add(detail);
        }

        session.setAttribute("allocResultEmployee", employee);
        session.setAttribute("allocResultDetails", resultDetails);
        session.setAttribute("allocSuccessCount", successCount);
        session.setAttribute("allocFailCount", failCount);

        res.sendRedirect(req.getContextPath() + "/allocate/result");
    }
}
