
package ptithcm.admin.controller;

import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ptithcm.controller.UserController;
import ptithcm.serviceimpl.brandserviceimpl;
import ptithcm.serviceimpl.categoryserviceimpl;
import ptithcm.serviceimpl.statisticsserviceimpl;

@Controller
public class statisticsadmincontroller {
	@Autowired
	statisticsserviceimpl statisticsServiceImpl;
	
	@Autowired
	brandserviceimpl brandserviceimpl;
	
	@Autowired
	categoryserviceimpl categoryserviceimpl;
	@RequestMapping("admin/statistics")
	public String statisticsAdmin(ModelMap model, RedirectAttributes ra,HttpSession ss) {
		
		model.addAttribute("categories", categoryserviceimpl.dscategory());
		model.addAttribute("brands",brandserviceimpl.dsbrands());
		ra.addFlashAttribute("username", UserController.getUser().getUsername());
		model.addAttribute("total_profit", statisticsServiceImpl.getProfitThisYear());
		model.addAttribute("total_orders", statisticsServiceImpl.getOrdersCountThisYear());
		model.addAttribute("total_users", statisticsServiceImpl.getUsersCount());
		model.addAttribute("role_id", UserController.getUser().getRole_id());
		String s = "";
		int year = Calendar.getInstance().get(Calendar.YEAR);
		long [] arr = statisticsServiceImpl.getDetailProfitMonthOfYear(year);
		for(int i = 0; i<arr.length; i++) {
			System.out.println("Thang "+(i+1)+": "+arr[i]);
			s += " "+ arr[i];
		}
		model.addAttribute("role_id", UserController.getUser().getRole_id());
		model.addAttribute("year_profit", year);
		model.addAttribute("profit_detail", s.trim());
		return "admin/statistics";
	}
	
	@RequestMapping(value = "admin/statistics", method = RequestMethod.POST)
	public String statisticsAdminYear(@RequestParam("year") int year, RedirectAttributes ra, ModelMap model) {	
		String s = "";
		System.out.println("YEAR = "+year);
		long [] arr = statisticsServiceImpl.getDetailProfitMonthOfYear(year);
		for(int i = 0; i<arr.length; i++) {
			System.out.println("Thang "+(i+1)+": "+arr[i]);
			s += " "+ arr[i];
		}
		
		ra.addFlashAttribute("profit_detail_month", s.trim());
		ra.addFlashAttribute("year_profit_detail", year);
		ra.addFlashAttribute("year_use_detail", 1);
		return "redirect:/admin/statistics.htm";
	}
}
