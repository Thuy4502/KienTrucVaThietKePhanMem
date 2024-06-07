package ptithcm.admin.controller;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ptithcm.controller.UserController;
import ptithcm.entity.users;
import ptithcm.entity.customer;
import ptithcm.serviceimpl.userserviceimpl;
import ptithcm.serviceimpl.brandserviceimpl;
import ptithcm.serviceimpl.categoryserviceimpl;
import ptithcm.serviceimpl.customerserviceimpl;
import ptithcm.serviceimpl.statisticsserviceimpl;

@Controller
public class customeradmincontroller {
	@Autowired
	userserviceimpl accountServiceImpl;

	@Autowired
	statisticsserviceimpl statisticsServiceImpl;

	@Autowired
	categoryserviceimpl categoryserviceimpl;

	@Autowired
	brandserviceimpl brandserviceimpl;

	@Autowired
	customerserviceimpl customerserviceimpl;
	private int maxpage = 1;
	private int pagesize = 1;
	private int hienthi = 5;
	private int page = 1;
	private String order = "customer_name";
	private String dir = "asc";

	@RequestMapping("admin/customer")
	public String staffAdmin(ModelMap model, RedirectAttributes ra, HttpServletRequest request, HttpSession ss) {
		model.addAttribute("categories", categoryserviceimpl.dscategory());
		model.addAttribute("brands", brandserviceimpl.dsbrands());
		if (request.getParameter("page") != "" && request.getParameter("page") != null) {//ktra so trang 
			page = Integer.parseInt(request.getParameter("page"));
		}
		if (request.getParameter("keyword") != "" && request.getParameter("keyword") != null) {//kiem tra keyword
			request.setAttribute("count", customerserviceimpl.searchCustomerCount(request.getParameter("keyword")));
			request.setAttribute("customer", customerserviceimpl.searchCustomer(request.getParameter("keyword")));
			request.setAttribute("maxpage", 0);
			request.setAttribute("currentpage", 1);
			request.setAttribute("pagesize", 0);
		} else {
			model.addAttribute("maxpage", (int) Math.ceil((double) customerserviceimpl.getCustomerCount() / hienthi));
			model.addAttribute("currentpage", page);
			model.addAttribute("pagesize", pagesize);
			model.addAttribute("customer", customerserviceimpl.CustomerPage(page, hienthi, order, dir));
			model.addAttribute("count", customerserviceimpl.getCustomerCount());
		}
		List<users> users = accountServiceImpl.dsUser();

			model.addAttribute("userLogin", UserController.getUser());
			model.addAttribute("total_staff", customerserviceimpl.getCustomerCount());
			model.addAttribute("customerList", customerserviceimpl.dsCustomer());
			model.addAttribute("users", users);
		if(UserController.getUser() != null) {
			if(UserController.getUser().getRole_id() == 1) {
				ss.setAttribute("sudung", true);
			}else {
				ss.setAttribute("sudung", false);
			}
		}
		model.addAttribute("role_id", UserController.getUser().getRole_id());
		return "admin/customer";
	}

	@RequestMapping(value = "admin/customer/edit", method = RequestMethod.POST)
	public String customerEdit(ModelMap model, @RequestParam("user_id") Long user_id,
			@RequestParam("status") String status, RedirectAttributes ra) throws ParseException {
		if (user_id != null) {
			int result = accountServiceImpl.updateUserCustomer(user_id, status);
			ra.addFlashAttribute("message", 6);// update thanh cong
		} 
		return "redirect:/admin/customer.htm";
	}
		
	@RequestMapping(value = "admin/customer", params = "keyword")
	public String customerSearch(@RequestParam(required = true) String keyword, HttpServletRequest request) {
		getMaxPage();
		request.setAttribute("count", customerserviceimpl.searchCustomerCount(keyword));
		request.setAttribute("customer", customerserviceimpl.searchCustomer(keyword));
		request.setAttribute("maxpage", 0);
		request.setAttribute("currentpage", 1);
		request.setAttribute("pagesize", 0);
		request.setAttribute("orderLink", "asc");
		return "admin/customer";
	}

	public int getMaxPage() {
		int customercount = (int) customerserviceimpl.getCustomerCount();
		if (customercount == 0) {
			hienthi = 1;
		} else if (customercount < 6) {
			hienthi = customercount;
			pagesize = 1;
		} else if (customercount < 11) {
			hienthi = 5;
			pagesize = 2;
		} else {
			hienthi = 5;
			pagesize = 3;
		}
		if (customercount % hienthi == 0) {
			maxpage = customercount / hienthi;
		} else {
			maxpage = (int) (customercount / hienthi) + 1;
		}
		return maxpage;
	}
}
