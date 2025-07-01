<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard - Email Reader</title>
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

        .card {
            background: white;
            border-radius: 8px;
            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
            padding: 1.5rem;
            margin-bottom: 1.5rem;
        }

        .card-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 1rem;
        }

        .card-title {
            font-size: 1.25rem;
            font-weight: 600;
            color: #1a1a1a;
        }

        .add-account-btn {
            display: inline-block;
            padding: 0.75rem 1.5rem;
            background-color: #3182ce;
            color: white;
            text-decoration: none;
            border-radius: 4px;
            font-weight: 500;
            transition: background-color 0.2s;
        }

        .add-account-btn:hover {
            background-color: #2c5282;
        }

        .accounts-table {
            width: 100%;
            border-collapse: collapse;
        }

        .accounts-table th,
        .accounts-table td {
            padding: 0.75rem;
            text-align: left;
            border-bottom: 1px solid #e2e8f0;
        }

        .accounts-table th {
            background-color: #f8fafc;
            font-weight: 500;
            color: #4a5568;
        }

        .accounts-table tr:hover {
            background-color: #f8fafc;
        }

        .status-badge {
            display: inline-block;
            padding: 0.25rem 0.5rem;
            border-radius: 9999px;
            font-size: 0.75rem;
            font-weight: 500;
        }

        .status-active {
            background-color: #def7ec;
            color: #03543f;
        }

        .status-expired {
            background-color: #fde8e8;
            color: #9b1c1c;
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

            .card-header {
                flex-direction: column;
                gap: 1rem;
                text-align: center;
            }

            .accounts-table {
                display: block;
                overflow-x: auto;
                white-space: nowrap;
            }
        }
    </style>
</head>
<body>
    <nav class="navbar">
        <h1>Email Reader Admin</h1>
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

        <div class="card">
            <div class="card-header">
                <h2 class="card-title">Outlook Accounts</h2>
                <a href="${pageContext.request.contextPath}/admin/outlook-auth" class="add-account-btn">
                    Add Outlook Account
                </a>
            </div>

            <table class="accounts-table">
                <thead>
                    <tr>
                        <th>Email</th>
                        <th>Display Name</th>
                        <th>Added Date</th>
                        <th>Status</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${accounts}" var="account">
                        <tr>
                            <td>${account.email}</td>
                            <td>${account.displayName}</td>
                            <td>${account.addedDate}</td>
                            <td>
                                <span class="status-badge ${account.isTokenExpired() ? 'status-expired' : 'status-active'}">
                                    ${account.isTokenExpired() ? 'Token Expired' : 'Active'}
                                </span>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty accounts}">
                        <tr>
                            <td colspan="4" style="text-align: center; padding: 2rem;">
                                No Outlook accounts added yet.
                            </td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>
</body>
</html>
