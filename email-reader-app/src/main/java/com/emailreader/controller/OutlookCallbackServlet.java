package com.emailreader.controller;

import com.emailreader.dao.OutlookAccountDAO;
import com.emailreader.model.OutlookAccount;
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
import java.time.LocalDateTime;
import java.util.Map;

@WebServlet("/admin/outlook-callback")
public class OutlookCallbackServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(OutlookCallbackServlet.class);
    private final OutlookAccountDAO outlookAccountDAO = new OutlookAccountDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        
        // Verify session and user authentication
        if (session == null || session.getAttribute("user") == null) {
            logger.warn("Unauthorized callback access attempt");
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        User user = (User) session.getAttribute("user");
        if (!user.isAdmin()) {
            logger.warn("Non-admin user {} attempted to access callback", user.getUsername());
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        try {
            // Verify state parameter to prevent CSRF
            String expectedState = (String) session.getAttribute("oauth_state");
            String actualState = request.getParameter("state");
            
            if (expectedState == null || !expectedState.equals(actualState)) {
                logger.warn("OAuth state mismatch for user {}", user.getUsername());
                setErrorAndRedirect(request, response, "Invalid OAuth state");
                return;
            }

            // Clear the state from session
            session.removeAttribute("oauth_state");

            // Check for error response
            String error = request.getParameter("error");
            String errorDescription = request.getParameter("error_description");
            
            if (error != null) {
                logger.error("OAuth error: {} - {}", error, errorDescription);
                setErrorAndRedirect(request, response, 
                    "Authorization failed: " + errorDescription);
                return;
            }

            // Get the authorization code
            String code = request.getParameter("code");
            if (code == null || code.trim().isEmpty()) {
                logger.error("No authorization code received");
                setErrorAndRedirect(request, response, "No authorization code received");
                return;
            }

            // Exchange code for tokens
            Map<String, Object> tokenInfo = OAuthUtil.exchangeCodeForToken(code);
            
            // Get user info from Microsoft Graph API
            String accessToken = (String) tokenInfo.get("access_token");
            Map<String, String> userInfo = OAuthUtil.getUserInfo(accessToken);

            // Create and save Outlook account
            OutlookAccount account = new OutlookAccount();
            account.setEmail(userInfo.get("email"));
            account.setDisplayName(userInfo.get("displayName"));
            account.setAccessToken(accessToken);
            account.setRefreshToken((String) tokenInfo.get("refresh_token"));
            account.setExpiresAt((LocalDateTime) tokenInfo.get("expires_at"));
            account.setAddedByAdminId(user.getId());

            outlookAccountDAO.save(account);

            // Log successful authorization
            logger.info("Successfully authorized Outlook account for: {}", account.getEmail());

            // Set success message and redirect
            session.setAttribute("success", 
                "Successfully added Outlook account: " + account.getEmail());
            response.sendRedirect(request.getContextPath() + "/admin/dashboard.jsp");

        } catch (Exception e) {
            logger.error("Error processing OAuth callback", e);
            setErrorAndRedirect(request, response, 
                "Error processing authorization. Please try again.");
        }
    }

    private void setErrorAndRedirect(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   String errorMessage) 
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.setAttribute("error", errorMessage);
        }
        response.sendRedirect(request.getContextPath() + "/admin/dashboard.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // OAuth2 callback should be GET only
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Override
    public void init() throws ServletException {
        super.init();
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
