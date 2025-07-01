package com.emailreader.controller;

import com.emailreader.dao.UserDAO;
import com.emailreader.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(LoginServlet.class);
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Check if user is already logged in
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            User user = (User) session.getAttribute("user");
            redirectBasedOnRole(response, user);
            return;
        }

        // If not logged in, forward to login page
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // Validate input
        if (username == null || username.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            setErrorAndForward(request, response, "Username and password are required");
            return;
        }

        try {
            // Attempt authentication
            if (userDAO.authenticate(username, password)) {
                Optional<User> userOpt = userDAO.findByUsername(username);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    
                    // Create new session (invalidate existing if any)
                    HttpSession session = request.getSession(false);
                    if (session != null) {
                        session.invalidate();
                    }
                    session = request.getSession(true);
                    
                    // Set session attributes
                    session.setAttribute("user", user);
                    session.setMaxInactiveInterval(30 * 60); // 30 minutes

                    // Log successful login
                    logger.info("User {} logged in successfully", username);

                    // Redirect based on role
                    redirectBasedOnRole(response, user);
                    return;
                }
            }

            // If we get here, authentication failed
            logger.warn("Failed login attempt for username: {}", username);
            setErrorAndForward(request, response, "Invalid username or password");

        } catch (Exception e) {
            logger.error("Error during login process", e);
            setErrorAndForward(request, response, "An error occurred during login. Please try again.");
        }
    }

    private void redirectBasedOnRole(HttpServletResponse response, User user) throws IOException {
        String contextPath = getServletContext().getContextPath();
        if (user.isAdmin()) {
            response.sendRedirect(contextPath + "/admin/dashboard.jsp");
        } else {
            response.sendRedirect(contextPath + "/user/dashboard.jsp");
        }
    }

    private void setErrorAndForward(HttpServletRequest request, HttpServletResponse response, 
                                  String errorMessage) throws ServletException, IOException {
        request.setAttribute("error", errorMessage);
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Handle logout
        HttpSession session = request.getSession(false);
        if (session != null) {
            String username = "";
            if (session.getAttribute("user") != null) {
                username = ((User) session.getAttribute("user")).getUsername();
            }
            session.invalidate();
            logger.info("User {} logged out", username);
        }
        response.sendRedirect(request.getContextPath() + "/login.jsp");
    }
}
