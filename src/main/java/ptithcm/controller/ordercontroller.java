package ptithcm.controller;

import java.sql.Date;
import java.time.LocalDate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ptithcm.entity.orders;
import ptithcm.entity.reviews;
import ptithcm.serviceimpl.orderdetailserviceimpl;
import ptithcm.serviceimpl.orderserviceimpl;
import ptithcm.serviceimpl.reviewserviceimpl;
@Controller
public class ordercontroller {
	private int currentpage = 1;
	private int maxpage = 1;
	private int pagesize = 1;
	private int hienthi = 1;
	private String order = "order_id"; 
	private String dir = "asc";
	private int status_var;

	@Autowired
	reviewserviceimpl reviewserviceimpl;
	
	@Autowired
	orderdetailserviceimpl orderdetailserviceimpl;
	
	@Autowired
	orderserviceimpl orderServiceImpl;
	
	@RequestMapping("account/order")
	public String orderAdmin(ModelMap model, RedirectAttributes ra, HttpServletRequest request,HttpSession ss) {
		ra.addFlashAttribute("username", UserController.getUser().getUsername());
		getMaxPage(0);
		status_var = 0;
		if(order.equals("order_id")) {
			model.addAttribute("orderpage", "id");
		}else if(order.equals("order_day")){
			model.addAttribute("orderpage", "date");
		}else if(order.equals("total_price")){
			model.addAttribute("orderpage", "price");
		}
		
		model.addAttribute("dirpage", dir);
		if(dir.equals("asc")) {
			model.addAttribute("orderLink", "desc");
		}else {
			model.addAttribute("orderLink", "asc");
		}
		model.addAttribute("bill",orderServiceImpl.getListBill());
		model.addAttribute("count", orderServiceImpl.getOrderCount(0));
		model.addAttribute("orders", orderServiceImpl.orderPageCustomer(currentpage, hienthi, order, dir, 0,UserController.getUser().getUser_id()));
		model.addAttribute("maxpage", maxpage);
		model.addAttribute("status", 0);
		model.addAttribute("currentpage", 1);
		model.addAttribute("pagesize", pagesize);
		model.addAttribute("ds",orderdetailserviceimpl.dsOrder_detail());
		for(orders sOrders :  orderServiceImpl.orderPageCustomer(currentpage, hienthi, order, dir, 0,UserController.getUser().getUser_id())) {
			long kq = orderServiceImpl.setTotal_price(sOrders.getOrder_id());
		}
		return "account/order";
	}
	
	@RequestMapping(value = "account/order", params = "page")
	public String orderPage(@RequestParam(required = true) int page, @RequestParam("order") String o,
			@RequestParam("dir") String d, @RequestParam("status") int status, HttpServletRequest request,
			ModelMap model) {
		System.out.println("status " + status_var);
		status_var = status;
		if(o.equals("id")) {
			order = "order_id";
		}else if(o.equals("name")) {
			order = "receiver_name";
		}else if(o.equals("date")) {
			order = "order_day";
		}else if(o.equals("price")) {
			order = "total_price";
		}
		if(d.equals("asc")) {
			dir = "asc";
		}else if(d.equals("desc")) {
			dir = "desc";
		}
		getMaxPage(status);
		if(page > maxpage) {
			page = 1;
		}
		request.setAttribute("orders", orderServiceImpl.orderPageCustomer(page, hienthi, order, dir, status,UserController.getUser().getUser_id()));
		request.setAttribute("maxpage", maxpage);
		request.setAttribute("currentpage", page);
		request.setAttribute("pagesize", pagesize);
		request.setAttribute("status", status);
		request.setAttribute("count", orderServiceImpl.getOrderCount(status_var));
		model.addAttribute("ds", orderdetailserviceimpl.dsOrder_detail());
		for (orders sOrders : orderServiceImpl.orderPageCustomer(currentpage, hienthi, order, dir, 0,UserController.getUser().getUser_id())) {
			long kq = orderServiceImpl.setTotal_price(sOrders.getOrder_id());
		}
		currentpage = page;
		if(order.equals("order_id")) {
			request.setAttribute("orderpage", "id");
		}else if(order.equals("order_day")){
			request.setAttribute("orderpage", "date");
		}else if(order.equals("total_price")){
			request.setAttribute("orderpage", "price");
		}
		request.setAttribute("dirpage", dir);
		if(dir.equals("asc")) {
			request.setAttribute("orderLink", "desc");
		}else {
			request.setAttribute("orderLink", "asc");
		}
		return "account/order";
	}
	
	@RequestMapping(value = "account/find/date", method = RequestMethod.POST)
	public String orderSearch(@RequestParam(required = true) Date startDate, @RequestParam("endDate") Date endDate,
			HttpServletRequest request,RedirectAttributes ra) {
		if(startDate != null || endDate != null) {
			request.setAttribute("orders", orderServiceImpl.searchOrder(startDate, endDate, UserController.getUser().getUser_id()));
			request.setAttribute("count", orderServiceImpl.searchOrder(startDate, endDate, UserController.getUser().getUser_id()).size());
			request.setAttribute("maxpage", 0);
			request.setAttribute("currentpage", 1);
			request.setAttribute("pagesize", 0);
			request.setAttribute("orderLink", "asc");
			request.setAttribute("status", 0);
		}else {
			ra.addFlashAttribute("message",0);
		}
		
		
		return "account/order";
	}
	
	@RequestMapping(value = "account/order/cancel", method = RequestMethod.POST)
	public String cancelOrder(@RequestParam("order_id") long order_id, ModelMap model,RedirectAttributes ra, HttpServletRequest request) {
		long result = orderServiceImpl.saveOrder(5, order_id);
		return "redirect:/account/order.htm";
	}
	
	@RequestMapping(value = "account/review",method = RequestMethod.POST)
	public String userReview(@RequestParam("idProduct") String idProduct, @RequestParam("star") int star,@RequestParam("contentInput") String content ) {
		LocalDate currentDate = LocalDate.now();
		java.sql.Date sqlDate = Date.valueOf(currentDate);	
		reviews A = new reviews();
		A.setComments(content);
		A.setDate(sqlDate);
		A.setStar(star);
		A.setUser_id(UserController.getUser().getUser_id());
		A.setWatch_id(idProduct);
		long addReview = reviewserviceimpl.addReview(A);
		return "redirect:/account/order.htm";
	}
	
	public int getMaxPage(int status) {
		int ordercount = (int) orderServiceImpl.getOrderCountCustomer(status,UserController.getUser().getUser_id());
		if (ordercount == 0) {
			hienthi = 1;
		} else if (ordercount < 6) {
			hienthi = ordercount;
			pagesize = 1;
		} else if (ordercount < 11) {
			hienthi = 5;
			pagesize = 2;
		} else {
			hienthi = 5;
			pagesize = 3;
		}
		if (ordercount % hienthi == 0) {
			maxpage = ordercount / hienthi;
		} else {
			maxpage = (int) (ordercount / hienthi) + 1;
		}
		return maxpage;
	}

}
