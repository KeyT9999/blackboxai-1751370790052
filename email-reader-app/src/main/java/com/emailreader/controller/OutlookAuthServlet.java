package com.emailreader.controller;

import com.emailreader.model.User;
import com.emailreader.util.OAuthUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.UUID;

@WebServlet("/admin/outlook-auth")
public class OutlookAuthServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(OutlookAuthServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        
        // Check if user is logged in and is admin
        if (session == null || session.getAttribute("user") == null) {
            logger.warn("Unauthorized access attempt to Outlook authorization");
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        User user = (User) session.getAttribute("user");
        if (!user.isAdmin()) {
            logger.warn("Non-admin user {} attempted to access Outlook authorization", 
                       user.getUsername());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, 
                             "Access denied. Admin privileges required.");
            return;
        }

        try {
            // Generate and store state parameter to prevent CSRF
            String state = UUID.randomUUID().toString();
            session.setAttribute("oauth_state", state);

            // Store admin ID in session for callback
            session.setAttribute("admin_id", user.getId());

            // Build authorization URL
            String authUrl = OAuthUtil.buildAuthorizationUrl(state);

            // Log the authorization attempt
            logger.info("Admin {} initiating Outlook authorization", user.getUsername());

            // Redirect to Microsoft's authorization endpoint
            response.sendRedirect(authUrl);

        } catch (Exception e) {
            logger.error("Error initiating Outlook authorization", e);
            session.setAttribute("error", "Failed to initiate Outlook authorization. Please try again.");
            response.sendRedirect(request.getContextPath() + "/admin/dashboard.jsp");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Handle any POST requests (if needed)
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Handle any DELETE requests (if needed)
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Handle any PUT requests (if needed)
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Override
    public void init() throws ServletException {
        // Initialization code if needed
        super.init();
    }

    @Override
    public void destroy() {
        // Cleanup code if needed
        super.destroy();
    }
}
