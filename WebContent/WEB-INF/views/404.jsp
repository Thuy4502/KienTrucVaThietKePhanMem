<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    <%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix = 'c' %>
   <!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Error 404 - Page Not Found</title>
    <style>
        body {
            text-align: center;
            padding: 40px;
            font-family: Arial, sans-serif;
        }

        h1 {
            font-size: 36px;
            margin: 0;
        }

        p {
            font-size: 18px;
            margin-top: 20px;
        }
    </style>
</head>
<body>
    <h1>Error 404 - Page Not Found</h1>
    <p>The requested page could not be found. Please check the URL or try again later.</p>
    <a href = "<c:url value = '/home.htm' />"> Về trang chủ</a>
</body>
</html>






