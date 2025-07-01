package com.emailreader.filter;

import com.emailreader.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class AuthFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(AuthFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization code if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);

        String requestURI = httpRequest.getRequestURI();
        
        // Allow access to login page and login processing
        if (isPublicResource(requestURI)) {
            chain.doFilter(request, response);
            return;
        }

        // Check if user is logged in
        if (session == null || session.getAttribute("user") == null) {
            logger.warn("Unauthorized access attempt to {}", requestURI);
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.jsp");
            return;
        }

        User user = (User) session.getAttribute("user");

        // Check role-based access
        if (requestURI.contains("/admin/") && !user.isAdmin()) {
            logger.warn("Non-admin user {} attempted to access admin resource: {}", 
                       user.getUsername(), requestURI);
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, 
                                 "Access denied. Admin privileges required.");
            return;
        }

        // For user-specific paths, ensure the user has appropriate role
        if (requestURI.contains("/user/") && !"user".equals(user.getRole())) {
            logger.warn("Unauthorized user {} attempted to access user resource: {}", 
                       user.getUsername(), requestURI);
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, 
                                 "Access denied. User privileges required.");
            return;
        }

        // Set character encoding for all requests
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        // Add security headers
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");
        httpResponse.setHeader("X-Frame-Options", "DENY");
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
        httpResponse.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        httpResponse.setHeader("Pragma", "no-cache");

        try {
            chain.doFilter(request, response);
        } catch (Exception e) {
            logger.error("Error processing request", e);
            throw e;
        }
    }

    @Override
    public void destroy() {
        // Cleanup code if needed
    }

    private boolean isPublicResource(String uri) {
        return uri.endsWith("login.jsp") || 
               uri.endsWith("/login") || 
               uri.contains("/css/") || 
               uri.contains("/js/") || 
               uri.contains("/images/") ||
               uri.endsWith(".ico");
    }
}
