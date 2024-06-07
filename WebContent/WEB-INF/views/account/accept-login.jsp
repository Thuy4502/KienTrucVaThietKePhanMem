<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix='c'%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>SIGN IN</title>
<link rel="stylesheet" href="<c:url value = '/assets/css/toast.css' />" />
<link rel="stylesheet"
	href="https://cdn.jsdelivr.net/npm/boxicons@latest/css/boxicons.min.css">
<link rel="stylesheet"
	href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.2.1/css/all.min.css">
<link rel="stylesheet" href="../assets/css/swiper-bundle.min.css">
<link rel="stylesheet"
	href="<c:url value ='/assets/css/login-new.css' />" />
<link href="https://cdn.lineicons.com/4.0/lineicons.css"
	rel="stylesheet" />
<link href='https://unpkg.com/boxicons@2.0.9/css/boxicons.min.css'
	rel='stylesheet'>
</head>
<body>
     <div class="container" id="container">
        <div class="form-container register-container">
          <form style ="display: none;"action="#">
            <h1>Đăng Kí</h1>
            <input id="input__userName" type="text" name = "username" placeholder="Tên đăng nhập" oninput="validateUsername()" value = "${username }">
            <span class="txt_error"></span>
            <input  id="input__pass" type="password" name = "password" placeholder="Mật khẩu" oninput="validatePass()" value = "${password }">
            <span class="txt_error"></span>
            <input  id="input__rePass" type="password" name = "repassword" placeholder="Xác nhận mật khẩu" oninput="validateRePass()" value = "${repassword }">
            <span class="txt_error"></span>
            <input type="text" name = "phone" placeholder="Số điện thoại" value = "${phone }" >
            <input type="email" name = "email" placeholder="Email" value = "${email }" >
            <a class="button" id="btn__signUp" style="margin:0 0;"  href="#form__OTP">Đăng kí</a>
          </form >
          <form action ="<c:url value ='/signup/submit.htm' />"  id="form__OTP" method = "POST">
            <h1>Xác nhận email</h1>
            <small>Mã OTP đã được gửi đến mail bạn đã đăng kí</small>
            <input type="text" id = "input__OTP" name = "otp" placeholder="Nhập OTP">
            <button class="button" type="submit">Xác nhận</button>
          </form>
        </div>
      <div class="overlay-container" >
 
        <div class="overlay">
          <div class="overlay-panel overlay-left">
            <h1 class="title">Chào bạn,</h1>
            <p>Nếu bạn đã có tài khoản, vui lòng đăng nhập tại đây</p>
            <a class="ghost button" href = "<c:url value ='/login.htm' />"  id="login">Đăng nhập
              <i class="lni lni-arrow-left login"></i>
            </a>
          </div>
          <div class="overlay-panel overlay-right">
            <h2 class="title">Trải nghiệm mua sắm cùng</h2>
            <h1 class="logo" style="font-style:oblique;"><i class='bx bxl-medium-old' ></i> ST WATCH</h1>
            <p>Nếu bạn chưa có tài khoản, bạn có thể đăng ki tại đây.</p>
            <a class="ghost button" id="register">Đăng kí
              <i class="lni lni-arrow-right register"></i>
            </a>
          </div>
        </div>
      </div>
    </div>
    <script src="<c:url value = '/assets/js/toast.js' />"></script>
      <script>
        // ------LOGIN--------
const registerButton = document.getElementById("register");
const loginButton = document.getElementById("login");
const container = document.getElementById("container");

  container.classList.add("right-panel-active");

loginButton.addEventListener("click", () => {
  container.classList.remove("right-panel-active");
});
document.addEventListener("DOMContentLoaded", function() {
    //Kiểm tra đăng ký
	 <%if (request.getAttribute("check_otp") != null && request.getAttribute("check_otp").equals(0)) {%>
 	createToast('error', 'OTP không được để trống!');
		<%}%>
	});

      </script>
  </body>
</html>