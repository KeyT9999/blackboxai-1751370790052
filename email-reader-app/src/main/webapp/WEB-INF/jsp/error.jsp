<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Error - Email Reader</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Inter', sans-serif;
            background-color: #f5f5f5;
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 1rem;
        }

        .error-container {
            background: white;
            padding: 2rem;
            border-radius: 8px;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
            text-align: center;
            max-width: 500px;
            width: 100%;
        }

        .error-icon {
            font-size: 4rem;
            color: #dc2626;
            margin-bottom: 1.5rem;
        }

        .error-title {
            font-size: 1.5rem;
            font-weight: 600;
            color: #1a1a1a;
            margin-bottom: 1rem;
        }

        .error-message {
            color: #666;
            margin-bottom: 2rem;
            line-height: 1.5;
        }

        .error-details {
            background-color: #f8fafc;
            padding: 1rem;
            border-radius: 4px;
            margin-bottom: 2rem;
            text-align: left;
            font-family: monospace;
            font-size: 0.875rem;
            color: #4a5568;
            white-space: pre-wrap;
            word-break: break-word;
        }

        .back-btn {
            display: inline-block;
            padding: 0.75rem 1.5rem;
            background-color: #3182ce;
            color: white;
            text-decoration: none;
            border-radius: 4px;
            font-weight: 500;
            transition: background-color 0.2s;
        }

        .back-btn:hover {
            background-color: #2c5282;
        }

        .contact-support {
            margin-top: 1.5rem;
            font-size: 0.875rem;
            color: #666;
        }

        .contact-support a {
            color: #3182ce;
            text-decoration: none;
        }

        .contact-support a:hover {
            text-decoration: underline;
        }

        @media (max-width: 480px) {
            .error-container {
                padding: 1.5rem;
            }

            .error-icon {
                font-size: 3rem;
            }

            .error-title {
                font-size: 1.25rem;
            }
        }
    </style>
</head>
<body>
    <div class="error-container">
        <div class="error-icon">⚠️</div>
        
        <h1 class="error-title">
            <c:choose>
                <c:when test="${pageContext.errorData.statusCode == 404}">
                    Page Not Found
                </c:when>
                <c:when test="${pageContext.errorData.statusCode == 403}">
                    Access Denied
                </c:when>
                <c:when test="${pageContext.errorData.statusCode == 500}">
                    Internal Server Error
                </c:when>
                <c:otherwise>
                    An Error Occurred
                </c:otherwise>
            </c:choose>
        </h1>

        <p class="error-message">
            <c:choose>
                <c:when test="${pageContext.errorData.statusCode == 404}">
                    The page you're looking for doesn't exist or has been moved.
                </c:when>
                <c:when test="${pageContext.errorData.statusCode == 403}">
                    You don't have permission to access this resource.
                </c:when>
                <c:when test="${pageContext.errorData.statusCode == 500}">
                    Something went wrong on our end. Please try again later.
                </c:when>
                <c:otherwise>
                    An unexpected error occurred. Please try again later.
                </c:otherwise>
            </c:choose>
        </p>

        <% if (exception != null && request.getRemoteUser() != null && 
               request.isUserInRole("admin")) { %>
            <div class="error-details">
                <%= exception.getMessage() %>
            </div>
        <% } %>

        <a href="${pageContext.request.contextPath}/" class="back-btn">
            Return to Home
        </a>

        <p class="contact-support">
            Need help? <a href="mailto:support@example.com">Contact Support</a>
        </p>
    </div>
</body>
</html>
