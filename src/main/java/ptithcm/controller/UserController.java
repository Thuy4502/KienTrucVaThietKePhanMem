package ptithcm.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.sql.Date;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ptithcm.entity.users;
import ptithcm.entity.customer;
import ptithcm.entity.items;
import ptithcm.entity.orders;
import ptithcm.entity.reviews;
import ptithcm.entity.staffs;
import ptithcm.serviceimpl.userserviceimpl;
import ptithcm.serviceimpl.brandserviceimpl;
import ptithcm.serviceimpl.categoryserviceimpl;
import ptithcm.serviceimpl.customerserviceimpl;
import ptithcm.serviceimpl.homeserviceimpl;
import ptithcm.serviceimpl.itemserviceimpl;
import ptithcm.serviceimpl.orderdetailserviceimpl;
import ptithcm.serviceimpl.orderserviceimpl;
import ptithcm.serviceimpl.reviewserviceimpl;
import ptithcm.serviceimpl.staffserviceimpl;
import ptithcm.serviceimpl.statisticsserviceimpl;
import ptithcm.security.PasswordGenerator;

@Controller
public class UserController {
	@Autowired
	userserviceimpl accountServiceImpl;

	@Autowired
	itemserviceimpl itemServiceImpl;

	@Autowired
	homeserviceimpl homeServiceImpl;

	@Autowired
	statisticsserviceimpl statisticsServiceImpl;

	@Autowired
	staffserviceimpl staffserviceimpl;

	@Autowired
	customerserviceimpl customerserviceimpl;

	@Autowired
	brandserviceimpl brandserviceimpl;

	@Autowired
	categoryserviceimpl categoryserviceimpl;

	@Autowired
	JavaMailSender mailer;
	private static users user;
	private static staffs staffs;
	private static customer customer;

	@RequestMapping("login")
	public String login(HttpServletRequest request) {
		HttpSession session = request.getSession();
		if (session != null) {
			if (session.getAttribute("username") != null && user != null && user.getRole_id() == 3) {
				return "redirect:/account/infoCustomer.htm";
			} else if (session.getAttribute("username") != null && user != null && user.getRole_id() == 2) {
				return "redirect:/staff/info.htm";
			} else if (session.getAttribute("username") != null && user != null && user.getRole_id() == 1) {
				return "redirect:/admin/info.htm";
			}
		}
		return "account/login";
	}

	public boolean SentNewPassWord(String email, String username, String password) {
		try {
			String from = "T3SWATCH.PTIT.HCM";
			String to = email;
			String body = "<h2>Thế giới đồng hồ số một Việt Nam xin cập nhật mật khẩu đăng nhập hiện tại của bạn là: "
					+ password + "</h2>";
			String subject = "Cấp lại mật khẩu đăng nhập của tài khoản " + username
					+ " để đăng nhập vào kênh thế giới mua sắm đồng hồ số một Việt Nam";
			MimeMessage mail = mailer.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mail);
			helper.setFrom(from, from);
			helper.setTo(to);
			helper.setReplyTo(from, from);
			helper.setSubject(subject);
			helper.setText(body, true);
			mailer.send(mail);
			System.out.println("Gửi mail thành công");
			return true;
		} catch (Exception e) {
			System.out.println("Gửi mail thất bại");
			return false;
		}
	}

	public boolean checkLogin(HttpServletRequest request) {
		HttpSession session = request.getSession();
		if (session != null) {
			if (session.getAttribute("username") != null && user != null && user.getRole_id() == 3) {
				return true;
			}
		}
		return false;
	}

	public boolean checkAmin(HttpServletRequest request) {
		HttpSession session = request.getSession();
		if (session != null) {
			if (session.getAttribute("username") != null && user != null && user.getRole_id() == 1) {
				return true;
			}
		}
		return false;
	}

	public boolean checkStaff(HttpServletRequest request) {
		HttpSession session = request.getSession();
		if (session != null) {
			if (session.getAttribute("username") != null && user != null && user.getRole_id() == 2) {
				return true;
			}
		}
		return false;
	}

	@RequestMapping("signup")
	public String signup(HttpSession session) {
		if (session != null) {
			if (session.getAttribute("username") != null && user != null) {
				return "redirect:/account/infoCustomer.htm";
			}
		}
		return "account/signup";
	}

	@RequestMapping("account/login/forgetpass")
	public String forgetpass(HttpSession session) {
		if (session != null) {
			if (session.getAttribute("username") != null && user != null) {
				return "redirect:/account/infoCustomer.htm";
			}
		}
		return "account/forget-password";
	}

	@RequestMapping(value = "account/login/forgetpass/submit", method = RequestMethod.POST)
	public String register(@RequestParam("username") String username, @RequestParam("email") String email,
			RedirectAttributes ra) {
		username = username.trim();
		email = email.trim();
		if (username.length() < 3 || email.length() == 0 || username.contains(" ") == true
				|| email.contains(" ") == true || email.length() == 0
				|| accountServiceImpl.checkUsernameExist(username) == false) {
			ra.addFlashAttribute("check_forget", 1); // Lỗi username...
		} else if (accountServiceImpl.checkInfoGetPasswordExist(username, email) == null) {
			ra.addFlashAttribute("check_forget", 2); // Username + email chưa chính xác
		} else {
			users B = accountServiceImpl.checkInfoGetPasswordExist(username, email);
			String passwordRandom = PasswordGenerator.generateRandomPassword(6);
			System.out.println("passrandom : " + passwordRandom);
			long kq = accountServiceImpl.updatePassword(passwordRandom, username);
			if (kq > 0) {
				boolean result = SentNewPassWord(email, username, passwordRandom);
				if (result == true) {
					ra.addFlashAttribute("check_forget", 3); // Thành công
				} else {
					ra.addFlashAttribute("check_forget", 4); // Thất bại /account/updatepassword/submit.htm
				}
			}
		}
		return "redirect:/account/login/forgetpass.htm";

	}

	@RequestMapping(value = "updatepassword", method = RequestMethod.POST)
	public String updatepasswordsubmit(@RequestParam("oldpassword") String oldpassword,
			@RequestParam("newpassword") String newpassword, @RequestParam("renewpassword") String renewpassword,
			RedirectAttributes ra) {
		oldpassword = oldpassword.trim();
		renewpassword = renewpassword.trim();
		newpassword = newpassword.trim();
		String username_login = UserController.getUser().getUsername();
		if (oldpassword.equals("") || oldpassword.equals("") || newpassword.equals("")) {
			ra.addFlashAttribute("message", 1); // Lỗi trống hoặc sai ký tự
		} else if (accountServiceImpl.checkOldPassWordExactly(oldpassword, username_login) == false) {
			ra.addFlashAttribute("message", 2); // Mật khẩu cũ không chính xác
		} else if (renewpassword.equals(newpassword) == false) {
			ra.addFlashAttribute("message", 3); // Hai mật khẩu không chính xác
		} else {
			int result = accountServiceImpl.updatePassword(renewpassword, username_login);
			if (result > 0) {
				ra.addFlashAttribute("message", 4); // Thành công
			} else {
				ra.addFlashAttribute("message", 5); // Thất bại
			}
		}
		long role_id = UserController.getUser().getRole_id();
		if(role_id == 1) {
			return "redirect:/admin/info.htm";
		}else if(role_id == 2) {
			return "redirect:/staff/info.htm";
		}else {
			return "redirect:/account/infoCustomer.htm";
		}
		

	}
	
	@RequestMapping("accept-login")
	public String acceptlogin() {

		return "account/accept-login";
	}

	@RequestMapping(value = "accept/login/otp", method = RequestMethod.POST)
	public String acceptLogin(@RequestParam("username") String username, @RequestParam("phone") String phone,
			@RequestParam("password") String password, @RequestParam("repassword") String repassword,
			HttpSession session, RedirectAttributes ra, @RequestParam("email") String email) {
		if (username.equals("") || phone.equals("") || password.equals("") || repassword.equals("")
				|| email.equals("")) {
			ra.addFlashAttribute("message", 1); // ký tự trống
			return "redirect:/login.htm";
		} else if (accountServiceImpl.checkUsernameExist(username)) {
			ra.addFlashAttribute("message", 2);// username tồn tại
			return "redirect:/login.htm";
		} else if (accountServiceImpl.checkPhoneExist(phone)) {
			ra.addFlashAttribute("message", 3);// trùng sdt
			return "redirect:/login.htm";
		} else if (accountServiceImpl.checkEmailExist(email)) {
			ra.addFlashAttribute("message", 4);// trung email
			return "redirect:/login.htm";
		} else {			
			String OTP_RANDOM = PasswordGenerator.generateRandomOTP(6);
			session.setAttribute("username", username);
			session.setAttribute("phone", phone);
			session.setAttribute("password", password);
			session.setAttribute("repassword", repassword);
			session.setAttribute("email", email);
			session.setAttribute("otp", OTP_RANDOM);
			System.out.println("passrandom : " + OTP_RANDOM);
			try {
				String body = "<p style=\"text-align: start;\">Mã OTP của bạn là: " + OTP_RANDOM +  "</p>\n <p style=\"text-align: start;\"> "
						+ "Vui lòng lấy mã OTP để xác nhận hoàn tất đăng ký. </p>\n";
				String from = "WATCHSTORE.PTIT.HCM";
				String to = email;
				String subject = "XÁC NHẬN EMAIL - TSWATCH";
				MimeMessage mail = mailer.createMimeMessage();
				MimeMessageHelper helper = new MimeMessageHelper(mail, "utf-8");
				helper.setFrom(from, from);
				helper.setTo(to);
				helper.setReplyTo(from, from);
				helper.setSubject(subject);
				helper.setText(body, true);
				mailer.send(mail);
				session.setAttribute("otp", OTP_RANDOM);
				System.out.println("Gửi mail thành công");
			} catch (Exception e) {
				System.out.println("Gửi mail thất bại");
			}
		}
		return "account/accept-login";
	}

	@RequestMapping(value = "signup/submit", method = RequestMethod.POST)
	public String register(HttpSession session, @RequestParam("otp") String otp_send, RedirectAttributes ra) {
		String username = (String) session.getAttribute("username");
		String phone = (String) session.getAttribute("phone");
		String password = (String) session.getAttribute("password");
		String repassword = (String) session.getAttribute("repassword");
		String email = (String) session.getAttribute("email");
		String otp = (String) session.getAttribute("otp");
		int maxAttempts = 4;
		if(otp_send.equals(null)) {
			ra.addFlashAttribute("check_otp",0);
			return "redirect:/accept-login.htm";
		}
		Integer attempts = (Integer) session.getAttribute("attempts");
		if (attempts == null) {
		    attempts = 0;
		}
		if (otp_send.equals(otp)) {
		    attempts = 0;
		} else {
		    attempts++;
		    if (attempts >= maxAttempts) {
		        attempts = 0; 
		        session.removeAttribute("attempts");
		    } else {
		        session.setAttribute("attempts", attempts);
		    }
		}
	if(attempts == 3) {
		ra.addFlashAttribute("check_otp",1);
		return "redirect:/login.htm";
	}
		if (otp_send.equals(otp)) {
			username = username.trim();
			phone = phone.trim();
			repassword = repassword.trim();
			password = password.trim();
			java.util.Date currentDate = new java.util.Date();
			// Chuyển đổi thành java.sql.Date
			java.sql.Date sqlDate = new java.sql.Date(currentDate.getTime());
			long role = 3;
			String status = "0";
			users A = new users();
			A.setRole_id(role);
			A.setPhone(phone);
			A.setUsername(username);
			A.setPassword(password);
			A.setEmail(email);
			A.setStatus(status);
			int result = accountServiceImpl.addAccount(A);
			if (result > 0) {
				customer B = new customer();
				B.setAddress("");
				B.setBirthday(null);
				B.setCustomer_name(A.getUsername());
				B.setGender("");
				B.setPhone(phone);
				B.setUser_id(A.getUser_id());
				long result1 = customerserviceimpl.addCustomer(B);
				if (result1 > 0) {
					items C = new items();
					C.setUser_id(A.getUser_id());
					long result2 = itemServiceImpl.addItem(C);
					System.out.println("ket qua item :" + result2);
					if (result > 0 && result1 > 0 && result2 > 0) {
						ra.addFlashAttribute("message", 5);// thành công
						return "redirect:/login.htm";
					} else {
						System.out.println("ERORR");
						ra.addFlashAttribute("message", 6);
					}
				}
			}

		} 
		session.removeAttribute("username");
		session.removeAttribute("phone");
		session.removeAttribute("password");
		session.removeAttribute("repassword");
		session.removeAttribute("email");
		return "account/accept-login";

	}

	@RequestMapping("account/infoCustomer")
	public String info(ModelMap model, HttpServletRequest request) {
		if (checkLogin(request) == false) {
			return "redirect:/login.htm"; // "account/login";
		}
		// Đang đăng nhập mà nếu bị xóa tài khoản thì sẽ bị logout
		if (user != null && accountServiceImpl.checkIDandUsernameExist(UserController.getUser().getUsername(),
				UserController.getUser().getUser_id()) == false) {
			return "redirect:/logout.htm";
		}
		System.out.println("GT" + customer.getGender());
		model.addAttribute("user", user);
		model.addAttribute("customer", customer);
		model.addAttribute("customers", customerserviceimpl.dsCustomer());
		model.addAttribute("brands", brandserviceimpl.dsbrands());
		model.addAttribute("categories", categoryserviceimpl.dscategory());
		long user_id = UserController.getUser().getUser_id();
		
		return "account/infoCustomer";
	}

	@RequestMapping("admin/info")
	public String infoAdmin(ModelMap model, HttpServletRequest request) {
		if (checkAmin(request) == false) {
			return "redirect:/login.htm"; // "account/login";
		}
		// Đang đăng nhập mà nếu bị xóa tài khoản thì sẽ bị logout
		if (user != null && accountServiceImpl.checkIDandUsernameExist(UserController.getUser().getUsername(),
				UserController.getUser().getUser_id()) == false) {
			return "redirect:/logout.htm";
		}
		request.setAttribute("role_id", UserController.getUser().getRole_id());
		model.addAttribute("brands", brandserviceimpl.dsbrands());
		model.addAttribute("categories", categoryserviceimpl.dscategory());
		model.addAttribute("user", user);
		model.addAttribute("staff", staffs);
		System.out.println("gioi tinh " + staffs.getGender());
		long user_id = UserController.getUser().getUser_id();
		return "admin/info";
	}

	@RequestMapping("staff/info")
	public String infoStaff(ModelMap model, HttpServletRequest request) {
		if (checkStaff(request) == false) {
			return "redirect:/login.htm"; // "account/login";
		}
		// Đang đăng nhập mà nếu bị xóa tài khoản thì sẽ bị logout
		if (user != null && accountServiceImpl.checkIDandUsernameExist(UserController.getUser().getUsername(),
				UserController.getUser().getUser_id()) == false) {
			return "redirect:/account/logout.htm";
		}

		model.addAttribute("brands", brandserviceimpl.dsbrands());
		model.addAttribute("categories", categoryserviceimpl.dscategory());
		model.addAttribute("user", user);
		model.addAttribute("staff", staffs);
		long user_id = UserController.getUser().getUser_id();
		
		return "admin/info";

	}

	@RequestMapping(value = "account/infoCustomer", method = RequestMethod.POST)
	public String updateInfo(@RequestParam("email") String email, @RequestParam("phone") String phone,
			@RequestParam("customer_name") String customer_name, @RequestParam("birthday") Date birthday,
			@RequestParam("address") String address, @RequestParam("gender") String gender,
			@RequestParam("customer_id") long customer_id, @RequestParam("role_id") int role_id,
			@RequestParam("user_id") long user_id, ModelMap model, RedirectAttributes ra, HttpServletRequest request)
			throws ParseException {
		String username = user.getUsername();
		SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd");
		email = email.trim();
		phone = phone.trim();
		String phone_login = UserController.getUser().getPhone();
		if (phone.equals(phone_login) == false && phone.length() != 0
				&& accountServiceImpl.checkPhoneUser(phone, user_id) == false) {
			if (accountServiceImpl.checkPhoneExist(phone) == true) {
				return "redirect:/account/infoCustomer.htm";
			}
		}
		long result = accountServiceImpl.updateUser(user_id, email, phone);
		long result1 = customerserviceimpl.updateCustomer(customer_id, customer_name, gender, address, phone, birthday,user_id);
		if (result > 0 && result1 > 0) {
			user = accountServiceImpl.updateUserAfterUpdate(username);
			customer = customerserviceimpl.updateCustomerAfterUpdate(customer_id);
			ra.addFlashAttribute("check_edit", 2);// thanh cong
		} else {
			ra.addFlashAttribute("check_edit", 3);// that bai
		}
		ra.addFlashAttribute("user", user);
		ra.addFlashAttribute("customer", customer);
		return "redirect:/account/infoCustomer.htm";
	}

	@RequestMapping(value = "admin/info/edit", method = RequestMethod.POST)
	public String updateAdminInfo(@RequestParam("email") String email, @RequestParam("phone") String phone,
			@RequestParam("staff_name") String staff_name, @RequestParam("birthday") Date birthday,
			@RequestParam("address") String address, @RequestParam("gender") String gender,
			@RequestParam("staff_id") long staff_id, @RequestParam("user_id") long user_id, ModelMap model,
			RedirectAttributes ra, HttpServletRequest request) throws ParseException {
		String username = user.getUsername();		
		email = email.trim();
		phone = phone.trim();
		String email_login = UserController.getUser().getEmail();
		String phone_login = UserController.getUser().getPhone();
		if (phone.equals(phone_login) == false && phone.length() != 0
				&& accountServiceImpl.checkPhoneUser(phone, user_id) == false) {
			if (accountServiceImpl.checkPhoneExist(phone) == true) {
				System.out.println("VAO DAY LAM 2");
				ra.addFlashAttribute("check_edit", 3);// trung sodt
				return "redirect:/admin/info.htm";
			}
		}
		long result = accountServiceImpl.updateUser(user_id, email, phone);
		long result1 = staffserviceimpl.updateStaff(staff_id, staff_name, gender, phone, birthday, address, user_id);
		if (result > 0 && result1 > 0) {
			user = accountServiceImpl.updateUserAfterUpdate(username);
			staffs = accountServiceImpl.updateStaffAfterUpdate(staff_id);
			ra.addFlashAttribute("check_edit", 2);// thanh cong
		} else {
			ra.addFlashAttribute("check_edit", 3);// that bai
		}
		return "redirect:/admin/info.htm";
	}

	@RequestMapping(value = "login/submit", method = RequestMethod.POST)
	public String checklogin(HttpSession ss, HttpServletRequest request, @RequestParam("username") String username,
			@RequestParam("password") String password, HttpServletResponse response, ModelMap model,
			RedirectAttributes ra) {
		if (username.equals("") || password.equals("")) {
			ra.addFlashAttribute("login_result", 1);
			return "redirect:/login.htm";// Login không hợp lệ
		} else if (accountServiceImpl.checkUsernameExist(username) == false) {
			System.out.println(accountServiceImpl.checkUsernameExist(username));
			ra.addFlashAttribute("login_result", 2);
			return "redirect:/login.htm";// username không hợp lệ
		} else if (accountServiceImpl.checkOldPassWordExactly(password, username) == false) {
			System.out.println(accountServiceImpl.checkOldPassWordExactly(password, username));
			ra.addFlashAttribute("login_result", 3);
			return "redirect:/login.htm";// sai pass
		} else if (accountServiceImpl.getStatusUserByUsername(username).equals("1")) {
			ra.addFlashAttribute("login_result", 5);// check status
			return "redirect:/login.htm";// sai pass
		}
		
		if (accountServiceImpl.getLogin(username, password) != null) {
			user = accountServiceImpl.getLogin(username, password);

			if (user != null && user.getRole_id() == 1) {
				staffs = accountServiceImpl.getInfoStaffLogin(user.getUser_id());
				ss.setAttribute("username", username);
				ss.setAttribute("staff_name", staffs.getStaff_name());
				SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
				String jdate = date.format(staffs.getBirthday());
				ss.setAttribute("birthday", jdate);
				ss.setAttribute("address", staffs.getAddress());
				ss.setAttribute("staff_id", staffs.getStaff_id());
				ss.setAttribute("gender", staffs.getGender());
				ss.setAttribute("role_id", 1);
				
				ra.addFlashAttribute("login_result", 4);// thành công
				
				return "redirect:/admin/info.htm"; // "account/info";
			} else if (user != null && user.getRole_id() == 2) {
				staffs = accountServiceImpl.getInfoStaffLogin(user.getUser_id());
				ss.setAttribute("username", username);
				ss.setAttribute("staff_name", staffs.getStaff_name());
				SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
				ss.setAttribute("birthday", staffs.getBirthday());
				ss.setAttribute("address", staffs.getAddress());
				ss.setAttribute("staff_id", staffs.getStaff_id());
				ss.setAttribute("gender", staffs.getGender());
				ss.setAttribute("role_id", 2);
				

				ra.addFlashAttribute("login_result", 4);// thành công
				
				return "redirect:/staff/info.htm"; // "account/info";
			}

			else if (user != null && user.getRole_id() == 3) {
				user = accountServiceImpl.getInfoCustomerLogin(username, password);
				customer = customerserviceimpl.getInfoCustomerLogin(user.getUser_id());
				ss.setAttribute("username", username);
				ss.setAttribute("customer_name", customer.getCustomer_name());

				if (customer.getBirthday() != null) {
					ss.setAttribute("birthday", customer.getBirthday());
				} else {
					ss.setAttribute("birthday", "");
				}
				ss.setAttribute("address", customer.getAddress());
				ss.setAttribute("customer_id", customer.getCustomer_id());
				ss.setAttribute("gender", customer.getGender());
				ss.setAttribute("role_id", 3);
				ra.addFlashAttribute("login_result", 4);// thành công
				return "redirect:/home.htm"; // "account/info";
			}
		}
		return "account/login";
	}

	@RequestMapping("logout")
	public String logout(HttpServletRequest request) {
		HttpSession session = request.getSession();
		session.removeAttribute("username");
		session.removeAttribute("role_id");
		user = null;
		// session.setAttribute(null, session);
		session.setAttribute("cart", null);
		return "redirect:/login.htm"; // "account/login";
	}

	public static users getUser() {
		return user;
	}

	public static staffs getStaff() {
		return staffs;
	}

	public static customer getCustomer() {
		return customer;
	}

}
