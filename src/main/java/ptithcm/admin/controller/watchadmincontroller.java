package ptithcm.admin.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.sql.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ptithcm.controller.UserController;
import ptithcm.entity.watchs;
import ptithcm.serviceimpl.brandserviceimpl;
import ptithcm.serviceimpl.categoryserviceimpl;
import ptithcm.serviceimpl.watchserviceimpl;

@Controller
public class watchadmincontroller {
	@Autowired
	SessionFactory factory;

	@Autowired
	watchserviceimpl watchServicelmpl;

	watchs watchUpdate;
	
	@Autowired
	categoryserviceimpl categoryserviceimpl;
	
	@Autowired
	brandserviceimpl brandserviceimpl;
	
	private int currentpage = 1;
	private int maxpage = 1;
	private int pagesize = 1;
	private int hienthi = 1;
	private String order = "watch_id";
	private String dir = "asc";

	@RequestMapping("admin/watch")
	public String orderAdmin(ModelMap model, RedirectAttributes ra,HttpServletRequest request,HttpSession ss) {
		getMaxPage();
		ra.addFlashAttribute("username", UserController.getUser().getUsername());
		if(order.equals("watch_id")) {
			request.setAttribute("orderpage", "watch_id");
		}
		request.setAttribute("dirpage", dir);
		if(dir.equals("asc")) {
			request.setAttribute("orderLink", "desc");
		}else {
			request.setAttribute("orderLink", "asc");
		}
		model.addAttribute("role_id", UserController.getUser().getRole_id());
		model.addAttribute("categories", categoryserviceimpl.dscategory());
		model.addAttribute("brands",brandserviceimpl.dsbrands());
		model.addAttribute("count", watchServicelmpl.getWatchCount());
		model.addAttribute("watchs", watchServicelmpl.watchPage(currentpage, hienthi, order, dir));
		model.addAttribute("maxpage", maxpage);
		model.addAttribute("currentpage", 1);
		model.addAttribute("pagesize", pagesize);
		if(UserController.getUser() != null) {
			if(UserController.getUser().getRole_id() == 1) {
				ss.setAttribute("sudung", true);
			}else {
				ss.setAttribute("sudung", false);
			}
		}
		return "admin/watch";
	}

	public int getMaxPage() {
		int watchcount = (int) watchServicelmpl.getWatchCount();
		if(watchcount == 0) {
			hienthi = 1;
		}else if(watchcount < 6) {
			hienthi = watchcount;
			pagesize = 1;
		}
		else if(watchcount < 11) {
			hienthi = 5;
			pagesize = 2;
		}else {
			hienthi = 5;
			pagesize = 3;
		}
		if(watchcount % hienthi == 0) {
			maxpage = watchcount / hienthi;
		}else {
			maxpage = (int)(watchcount / hienthi) + 1;
		}
		return maxpage;
	}
	
	@RequestMapping(value = "admin/watch", params = "page")
	public String categoryPage(@RequestParam(required = true) int page, @RequestParam("order") String o,
			@RequestParam("dir") String d, HttpServletRequest request,ModelMap model) {
		if (o.equals("id")) {
			order = "watch_id";
		}

		if (d.equals("asc")) {
			dir = "asc";
		} else if (d.equals("desc")) {
			dir = "desc";
		}

		getMaxPage();
		if (page > maxpage) {
			page = 1;
		}
		request.setAttribute("count", watchServicelmpl.getWatchCount());
		request.setAttribute("watchs", watchServicelmpl.watchPage(page, hienthi, order, dir));
		request.setAttribute("maxpage", maxpage);
		request.setAttribute("currentpage", page);
		request.setAttribute("pagesize", pagesize);
		model.addAttribute("categories", categoryserviceimpl.dscategory());
		model.addAttribute("brands",brandserviceimpl.dsbrands());
		//
		currentpage = page;
		if (order.equals("watch_id")) {
			request.setAttribute("orderpage", "id");
		} else {
			request.setAttribute("orderpage", "name");
		}
		request.setAttribute("dirpage", dir);
		if (dir.equals("asc")) {
			request.setAttribute("orderLink", "desc");
		} else {
			request.setAttribute("orderLink", "asc");
		}

		return "admin/watch";
	}
	
	
	@RequestMapping(value = "admin/watch/search", params = "keyword")
	public String brandSearch(@RequestParam(required = true) String keyword, HttpServletRequest request,
			ModelMap model) {
		getMaxPage();
		request.setAttribute("count", watchServicelmpl.searchWatchCount(keyword));
		request.setAttribute("watchs", watchServicelmpl.searchWatch(keyword));
		request.setAttribute("maxpage", 0);
		request.setAttribute("currentpage", 1);
		request.setAttribute("pagesize", 0);
		request.setAttribute("orderLink", "asc");
		return "admin/watch";
	}

	@RequestMapping(value = "admin/watch/edit", method = RequestMethod.POST)
	public String editWatch(ModelMap model,RedirectAttributes ra, @RequestParam("watch_id") String watch_id, @RequestParam("describe") 
	String describe, @RequestParam("watch_name") String watch_name, @RequestParam("picture") String picture, @RequestParam("price") long price,
	@RequestParam("brand_id") Long  brand_id, @RequestParam("category_id") Long category_id, @RequestParam("total_quantity") int total_quantity,
	@RequestParam("size") String size,@RequestParam("crystal") String crystal,@RequestParam("bracelet_material") String bracelet_material, 
	@RequestParam("movement") String movement) throws ParseException {	
	if(watch_id.equals("") || watch_name.equals("") || picture.equals("") || price == 0 || brand_id == null || category_id == null   ) {
		ra.addFlashAttribute("edit_watch",1); //lỗi để trông thông tin
	}
	else if(watch_id.equals(watchServicelmpl.findWatch_id(watch_id.trim())) && !watchServicelmpl.findWatch_id(watch_id).equals(null)) {	
		System.out.println(watchServicelmpl.findWatch_id(watch_id));
		System.out.println("khong duoc vao day: " + watch_name);	
		long result = watchServicelmpl.updateEditWatch(watch_id, watch_name, describe, picture, total_quantity,price,
				brand_id, category_id,size,crystal,bracelet_material,movement);
		ra.addFlashAttribute("edit_watch",3);//update thành công
		}else{	
			watchs A = new watchs();
			A.setWatch_id(watch_id);
			A.setBrand_id(brand_id);
			A.setCategory_id(category_id);
			A.setDescribe(describe);
			A.setPicture(picture);
			A.setWatch_name(watch_name);
			A.setTotal_quantity(total_quantity);
			A.setPrice(price);
			long addWatch = watchServicelmpl.insertWatch(A);		
			if(addWatch > 0) {
				ra.addFlashAttribute("edit_watch", 2);// thêm thành công
			}else {
				ra.addFlashAttribute("edit_watch",4);//thất bại		
			}	
		}
		return "redirect:/admin/watch.htm";
		
	}
}

