<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>User Dashboard - Email Reader</title>
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
        }

        .navbar {
            background-color: #1a1a1a;
            padding: 1rem 2rem;
            color: white;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .navbar h1 {
            font-size: 1.25rem;
            font-weight: 500;
        }

        .navbar-right {
            display: flex;
            align-items: center;
            gap: 1rem;
        }

        .user-info {
            font-size: 0.875rem;
        }

        .logout-btn {
            padding: 0.5rem 1rem;
            background-color: #dc2626;
            color: white;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 0.875rem;
            text-decoration: none;
        }

        .container {
            max-width: 1200px;
            margin: 2rem auto;
            padding: 0 1rem;
        }

        .accounts-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
            gap: 1.5rem;
            margin-top: 1.5rem;
        }

        .account-card {
            background: white;
            border-radius: 8px;
            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
            padding: 1.5rem;
            transition: transform 0.2s;
        }

        .account-card:hover {
            transform: translateY(-2px);
        }

        .account-header {
            display: flex;
            align-items: center;
            gap: 1rem;
            margin-bottom: 1rem;
        }

        .account-avatar {
            width: 48px;
            height: 48px;
            background-color: #e2e8f0;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.25rem;
            font-weight: 600;
            color: #4a5568;
        }

        .account-info h3 {
            font-size: 1.125rem;
            font-weight: 600;
            color: #1a1a1a;
            margin-bottom: 0.25rem;
        }

        .account-info p {
            font-size: 0.875rem;
            color: #666;
        }

        .view-emails-btn {
            display: inline-block;
            width: 100%;
            padding: 0.75rem;
            background-color: #3182ce;
            color: white;
            text-align: center;
            text-decoration: none;
            border-radius: 4px;
            font-weight: 500;
            margin-top: 1rem;
            transition: background-color 0.2s;
        }

        .view-emails-btn:hover {
            background-color: #2c5282;
        }

        .alert {
            padding: 1rem;
            border-radius: 4px;
            margin-bottom: 1.5rem;
        }

        .alert-success {
            background-color: #def7ec;
            color: #03543f;
        }

        .alert-error {
            background-color: #fde8e8;
            color: #9b1c1c;
        }

        .no-accounts {
            text-align: center;
            padding: 3rem;
            background: white;
            border-radius: 8px;
            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
        }

        .no-accounts h2 {
            color: #1a1a1a;
            font-size: 1.5rem;
            margin-bottom: 1rem;
        }

        .no-accounts p {
            color: #666;
            font-size: 1rem;
        }

        @media (max-width: 768px) {
            .navbar {
                padding: 1rem;
                flex-direction: column;
                gap: 1rem;
                text-align: center;
            }

            .navbar-right {
                flex-direction: column;
                gap: 0.5rem;
            }

            .container {
                padding: 1rem;
            }

            .accounts-grid {
                grid-template-columns: 1fr;
            }
        }
    </style>
</head>
<body>
    <nav class="navbar">
        <h1>Email Reader</h1>
        <div class="navbar-right">
            <span class="user-info">Welcome, ${user.username}</span>
            <form action="${pageContext.request.contextPath}/login" method="post" style="display: inline;">
                <input type="hidden" name="_method" value="DELETE">
                <button type="submit" class="logout-btn">Logout</button>
            </form>
        </div>
    </nav>

    <div class="container">
        <c:if test="${not empty success}">
            <div class="alert alert-success">
                ${success}
            </div>
        </c:if>

        <c:if test="${not empty error}">
            <div class="alert alert-error">
                ${error}
            </div>
        </c:if>

        <c:choose>
            <c:when test="${not empty accounts}">
                <div class="accounts-grid">
                    <c:forEach items="${accounts}" var="account">
                        <div class="account-card">
                            <div class="account-header">
                                <div class="account-avatar">
                                    ${account.displayName.charAt(0)}
                                </div>
                                <div class="account-info">
                                    <h3>${account.displayName}</h3>
                                    <p>${account.email}</p>
                                </div>
                            </div>
                            <a href="${pageContext.request.contextPath}/user/emails?action=viewEmails&accountId=${account.id}" 
                               class="view-emails-btn">
                                View Emails
                            </a>
                        </div>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise>
                <div class="no-accounts">
                    <h2>No Email Accounts Available</h2>
                    <p>Please contact your administrator to add Outlook accounts.</p>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</body>
</html>
