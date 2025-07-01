package com.emailreader.controller;

import com.emailreader.dao.OutlookAccountDAO;
import com.emailreader.model.OutlookAccount;
import com.emailreader.model.User;
import com.emailreader.util.GraphAPIUtil;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

@WebServlet("/user/emails")
public class EmailListServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(EmailListServlet.class);
    private final OutlookAccountDAO outlookAccountDAO = new OutlookAccountDAO();
    private static final int PAGE_SIZE = 20;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        
        if (session == null || session.getAttribute("user") == null) {
            logger.warn("Unauthorized access attempt to email list");
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        try {
            String action = request.getParameter("action");
            
            if ("listAccounts".equals(action)) {
                handleListAccounts(request, response);
            } else if ("viewEmails".equals(action)) {
                handleViewEmails(request, response);
            } else if ("viewEmail".equals(action)) {
                handleViewEmailContent(request, response);
            } else {
                handleListAccounts(request, response);
            }
            
        } catch (Exception e) {
            logger.error("Error processing email list request", e);
            request.setAttribute("error", "Error retrieving emails. Please try again.");
            request.getRequestDispatcher("/user/dashboard.jsp").forward(request, response);
        }
    }

    private void handleListAccounts(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        List<OutlookAccount> accounts = outlookAccountDAO.findAll();
        request.setAttribute("accounts", accounts);
        request.getRequestDispatcher("/WEB-INF/jsp/email/account_list.jsp").forward(request, response);
    }

    private void handleViewEmails(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String accountIdStr = request.getParameter("accountId");
        String pageTokenParam = request.getParameter("pageToken");
        
        if (accountIdStr == null || accountIdStr.trim().isEmpty()) {
            request.setAttribute("error", "No account selected");
            handleListAccounts(request, response);
            return;
        }

        try {
            Long accountId = Long.parseLong(accountIdStr);
            Optional<OutlookAccount> accountOpt = outlookAccountDAO.findById(accountId);
            
            if (!accountOpt.isPresent()) {
                request.setAttribute("error", "Account not found");
                handleListAccounts(request, response);
                return;
            }

            OutlookAccount account = accountOpt.get();
            
            // Check if token needs refresh
            if (account.isTokenExpired()) {
                refreshToken(account);
            }

            // Fetch emails using Graph API
            List<Map<String, String>> emails = GraphAPIUtil.fetchEmails(
                account.getAccessToken(), 
                PAGE_SIZE,
                pageTokenParam
            );

            request.setAttribute("emails", emails);
            request.setAttribute("account", account);
            request.getRequestDispatcher("/WEB-INF/jsp/email/email_list.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            logger.error("Invalid account ID format: {}", accountIdStr);
            request.setAttribute("error", "Invalid account ID");
            handleListAccounts(request, response);
        }
    }

    private void handleViewEmailContent(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String accountIdStr = request.getParameter("accountId");
        String emailId = request.getParameter("emailId");
        
        if (accountIdStr == null || emailId == null) {
            request.setAttribute("error", "Missing required parameters");
            handleListAccounts(request, response);
            return;
        }

        try {
            Long accountId = Long.parseLong(accountIdStr);
            Optional<OutlookAccount> accountOpt = outlookAccountDAO.findById(accountId);
            
            if (!accountOpt.isPresent()) {
                request.setAttribute("error", "Account not found");
                handleListAccounts(request, response);
                return;
            }

            OutlookAccount account = accountOpt.get();
            
            // Check if token needs refresh
            if (account.isTokenExpired()) {
                refreshToken(account);
            }

            // Fetch email content
            Map<String, String> emailContent = GraphAPIUtil.fetchEmailContent(
                account.getAccessToken(), 
                emailId
            );

            request.setAttribute("emailContent", emailContent);
            request.setAttribute("account", account);
            request.getRequestDispatcher("/WEB-INF/jsp/email/email_content.jsp")
                   .forward(request, response);

        } catch (NumberFormatException e) {
            logger.error("Invalid account ID format: {}", accountIdStr);
            request.setAttribute("error", "Invalid account ID");
            handleListAccounts(request, response);
        }
    }

    private void refreshToken(OutlookAccount account) {
        try {
            Map<String, Object> tokenInfo = OAuthUtil.refreshToken(account.getRefreshToken());
            
            account.setAccessToken((String) tokenInfo.get("access_token"));
            account.setRefreshToken((String) tokenInfo.get("refresh_token"));
            account.setExpiresAt((LocalDateTime) tokenInfo.get("expires_at"));
            
            outlookAccountDAO.updateTokens(
                account.getId(),
                account.getAccessToken(),
                account.getRefreshToken(),
                account.getExpiresAt()
            );
            
            logger.info("Successfully refreshed token for account: {}", account.getEmail());
        } catch (Exception e) {
            logger.error("Error refreshing token for account: {}", account.getEmail(), e);
            throw new RuntimeException("Failed to refresh token", e);
        }
    }
}
