<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Emails - ${account.email}</title>
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

        .back-btn {
            color: white;
            text-decoration: none;
            display: flex;
            align-items: center;
            gap: 0.5rem;
            font-size: 0.875rem;
        }

        .container {
            max-width: 1200px;
            margin: 2rem auto;
            padding: 0 1rem;
        }

        .email-header {
            background: white;
            border-radius: 8px;
            padding: 1.5rem;
            margin-bottom: 1.5rem;
            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
        }

        .email-header h2 {
            font-size: 1.25rem;
            color: #1a1a1a;
            margin-bottom: 0.5rem;
        }

        .email-header p {
            color: #666;
            font-size: 0.875rem;
        }

        .email-list {
            background: white;
            border-radius: 8px;
            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
        }

        .email-item {
            padding: 1.5rem;
            border-bottom: 1px solid #e2e8f0;
            cursor: pointer;
            transition: background-color 0.2s;
        }

        .email-item:last-child {
            border-bottom: none;
        }

        .email-item:hover {
            background-color: #f8fafc;
        }

        .email-item-header {
            display: flex;
            justify-content: space-between;
            margin-bottom: 0.5rem;
        }

        .email-sender {
            font-weight: 500;
            color: #1a1a1a;
        }

        .email-date {
            color: #666;
            font-size: 0.875rem;
        }

        .email-subject {
            font-size: 1rem;
            color: #1a1a1a;
            margin-bottom: 0.5rem;
        }

        .email-preview {
            color: #666;
            font-size: 0.875rem;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
        }

        .email-modal {
            display: none;
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0, 0, 0, 0.5);
            z-index: 1000;
        }

        .email-modal-content {
            position: relative;
            background-color: white;
            margin: 2rem auto;
            padding: 2rem;
            width: 90%;
            max-width: 800px;
            max-height: 90vh;
            overflow-y: auto;
            border-radius: 8px;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
        }

        .close-modal {
            position: absolute;
            top: 1rem;
            right: 1rem;
            font-size: 1.5rem;
            color: #666;
            cursor: pointer;
            border: none;
            background: none;
        }

        .pagination {
            display: flex;
            justify-content: center;
            gap: 0.5rem;
            margin-top: 1.5rem;
        }

        .pagination-btn {
            padding: 0.5rem 1rem;
            border: 1px solid #e2e8f0;
            border-radius: 4px;
            background-color: white;
            color: #1a1a1a;
            cursor: pointer;
            transition: all 0.2s;
        }

        .pagination-btn:hover {
            background-color: #f8fafc;
        }

        .pagination-btn:disabled {
            background-color: #f8fafc;
            color: #a0aec0;
            cursor: not-allowed;
        }

        @media (max-width: 768px) {
            .navbar {
                padding: 1rem;
            }

            .container {
                padding: 1rem;
            }

            .email-modal-content {
                margin: 1rem;
                padding: 1rem;
                width: calc(100% - 2rem);
            }
        }
    </style>
</head>
<body>
    <nav class="navbar">
        <a href="${pageContext.request.contextPath}/user/dashboard.jsp" class="back-btn">
            ← Back to Dashboard
        </a>
        <h1>${account.email}</h1>
    </nav>

    <div class="container">
        <div class="email-header">
            <h2>Inbox</h2>
            <p>Showing emails from ${account.displayName}</p>
        </div>

        <div class="email-list">
            <c:forEach items="${emails}" var="email">
                <div class="email-item" onclick="showEmail('${email.id}')">
                    <div class="email-item-header">
                        <span class="email-sender">${email.from}</span>
                        <span class="email-date">${email.receivedDateTime}</span>
                    </div>
                    <div class="email-subject">${email.subject}</div>
                    <div class="email-preview">${email.bodyPreview}</div>
                </div>
            </c:forEach>
            <c:if test="${empty emails}">
                <div style="text-align: center; padding: 3rem;">
                    <p>No emails found.</p>
                </div>
            </c:if>
        </div>

        <div class="pagination">
            <c:if test="${not empty prevPageToken}">
                <a href="?action=viewEmails&accountId=${account.id}&pageToken=${prevPageToken}" 
                   class="pagination-btn">
                    Previous
                </a>
            </c:if>
            <c:if test="${not empty nextPageToken}">
                <a href="?action=viewEmails&accountId=${account.id}&pageToken=${nextPageToken}" 
                   class="pagination-btn">
                    Next
                </a>
            </c:if>
        </div>
    </div>

    <div id="emailModal" class="email-modal">
        <div class="email-modal-content">
            <button class="close-modal" onclick="closeModal()">&times;</button>
            <div id="emailContent"></div>
        </div>
    </div>

    <script>
        function showEmail(emailId) {
            fetch('${pageContext.request.contextPath}/user/emails?action=viewEmail&accountId=${account.id}&emailId=' + emailId)
                .then(response => response.json())
                .then(data => {
                    const modal = document.getElementById('emailModal');
                    const content = document.getElementById('emailContent');
                    
                    content.innerHTML = `
                        <h2 style="margin-bottom: 1rem;">${'${data.subject}'}</h2>
                        <div style="margin-bottom: 1rem;">
                            <strong>From:</strong> ${'${data.from}'}<br>
                            <strong>Date:</strong> ${'${data.receivedDateTime}'}<br>
                            <strong>To:</strong> ${'${data.to}'}<br>
                            ${data.cc ? `<strong>CC:</strong> ${data.cc}<br>` : ''}
                        </div>
                        <div style="padding-top: 1rem; border-top: 1px solid #e2e8f0;">
                            ${data.contentType === 'html' ? data.content : '<pre style="white-space: pre-wrap;">' + data.content + '</pre>'}
                        </div>
                    `;
                    
                    modal.style.display = 'block';
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert('Error loading email content');
                });
        }

        function closeModal() {
            const modal = document.getElementById('emailModal');
            modal.style.display = 'none';
        }

        // Close modal when clicking outside
        window.onclick = function(event) {
            const modal = document.getElementById('emailModal');
            if (event.target === modal) {
                modal.style.display = 'none';
            }
        }
    </script>
</body>
</html>
