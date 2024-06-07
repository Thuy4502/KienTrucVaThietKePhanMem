package ptithcm.admin.controller;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ptithcm.bean.productList;
import ptithcm.controller.UserController;
import ptithcm.entity.warranty;
import ptithcm.serviceimpl.orderdetailserviceimpl;
import ptithcm.serviceimpl.orderserviceimpl;
import ptithcm.serviceimpl.warrantyserviceimpl;

@Controller
public class deliverystaffcontroller {
	@Autowired
	orderserviceimpl orderserviceimpl;
	@Autowired
	orderdetailserviceimpl orderdetailserviceimpl;	
	
	@Autowired
	warrantyserviceimpl warrantyserviceimpl;
	private int currentpage = 1;
	private int maxpage = 1;
	private int pagesize = 1;
	private int hienthi = 1;
	private String order = "order_id";
	private String dir = "asc";
	private int status_var;
	
	@RequestMapping("staff/delivery")
	public String staffDelivery(ModelMap model, RedirectAttributes ra,HttpServletRequest request) {
		
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
		
		model.addAttribute("count", orderserviceimpl.getOrderCount(0));
		model.addAttribute("orders", orderserviceimpl.orderPage(currentpage, hienthi, order, dir, 0));
		model.addAttribute("maxpage", maxpage);
		model.addAttribute("status", 0);
		model.addAttribute("currentpage", 1);
		model.addAttribute("pagesize", pagesize);
		model.addAttribute("ds",orderdetailserviceimpl.dsOrder_detail());
		model.addAttribute("role_id", UserController.getUser().getRole_id());
		return "admin/delivery-staff";
	}
	

	@RequestMapping(value = "staff/delivery", params = "page")
	public String brandPage(@RequestParam(required = true) int page, @RequestParam("order") String o,
			@RequestParam("dir") String d, @RequestParam("status") int status,  HttpServletRequest request, ModelMap model) {
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
		request.setAttribute("orders", orderserviceimpl.orderPage(page, hienthi, order, dir, status));
		request.setAttribute("maxpage", maxpage);
		request.setAttribute("currentpage", page);
		request.setAttribute("pagesize", pagesize);
		request.setAttribute("status", status);
		request.setAttribute("count", orderserviceimpl.getOrderCount(status_var));
		model.addAttribute("ds", orderdetailserviceimpl.dsOrder_detail());
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
		return "admin/delivery-staff";
	}
	
	@SuppressWarnings("unused")
	@RequestMapping( value = "staff/update/status",method = RequestMethod.POST)
	public String staffUpdateStatus(@RequestParam("order_id") long order_id,RedirectAttributes ra) {
		Long check_user_id = UserController.getUser().getUser_id();
		if(check_user_id != null) {
			long updateStatus = orderserviceimpl.updateStatusOrderDelivery(order_id, 3,UserController.getUser().getUser_id());
			System.out.println(updateStatus);
			ra.addFlashAttribute("messgae",1);//thanh cong
		}else {
			ra.addFlashAttribute("messgae",2);//that bai
		}
		return "redirect:/staff/delivery.htm";
		
	}
	
	@SuppressWarnings("unused")
	@RequestMapping( value = "staff/success/order",method = RequestMethod.POST)
	public String staffOrderSuccess(@RequestParam("order_id") long order_id,@RequestParam("status") int status,
			@RequestParam("quantity_detail[]") List<Integer> quantity,@RequestParam("watchid[]") List<String> watch_id,
			@RequestParam("order_detail_id[]") List<Long> order_detail_id,RedirectAttributes ra,HttpSession session) {
		if(status == 4) {
			long updateStatus = orderserviceimpl.updateStatusOrderDelivery(order_id, status,UserController.getUser().getUser_id());
			if(updateStatus > 0) {
				long staffSuccess = orderserviceimpl.staffSuccessOrder(order_id,UserController.getUser().getUser_id());
			}
			warranty B = new warranty();
			java.util.Date date = new java.util.Date();
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			calendar.add(Calendar.YEAR, 1);
			Date end_date = calendar.getTime();
			for(int i = 0; i < watch_id.size();i++) {
			B.setWatch_id(watch_id.get(i));
			B.setStart_day(date);
			B.setEnd_day(end_date);		
			B.setOrder_detail_id(order_detail_id.get(i));
			long result = warrantyserviceimpl.addwarranty(B);
			}
			System.out.println(updateStatus);
			ra.addFlashAttribute("messgae",1);//thanh cong
		}else if(status == 5) {
			long updateStatus = orderserviceimpl.updateStatusOrderDelivery(order_id, status,UserController.getUser().getUser_id());
			if(updateStatus > 0) {
				long staffSuccess = orderserviceimpl.staffSuccessOrder(order_id,UserController.getUser().getUser_id());
			}
			for(int i = 0; i < watch_id.size();i++) {
				long update = orderserviceimpl.updateQuantityWatchCancel(watch_id.get(i), quantity.get(i));
			}
		}else {
			ra.addFlashAttribute("messgae",2);//that bai
		}		
		return "redirect:/staff/delivery.htm";	
	}
	
	public int getMaxPage(int status) {
		int ordercount = (int) orderserviceimpl.getOrderCount(status);
		if(ordercount == 0) {
			hienthi = 1;
		}else if(ordercount < 6) {
			hienthi = ordercount;
			pagesize = 1;
		}
		else if(ordercount < 11) {
			hienthi = 5;
			pagesize = 2;
		}else {
			hienthi = 5;
			pagesize = 3;
		}
		if(ordercount % hienthi == 0) {
			maxpage = ordercount / hienthi;
		}else {
			maxpage = (int)(ordercount / hienthi) + 1;
		}
		return maxpage;
	}
}
