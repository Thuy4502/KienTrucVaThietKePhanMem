package ptithcm.controller;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.hibernate.jpa.criteria.predicate.IsEmptyPredicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mchange.v2.beans.swing.TestBean;

import ptithcm.bean.productList;
import ptithcm.entity.watchs;
import ptithcm.entity.orders;
import ptithcm.entity.warranty;
import ptithcm.entity.address;
import ptithcm.entity.item_detail;
import ptithcm.entity.items;
import ptithcm.entity.order_detail;
import ptithcm.serviceimpl.brandserviceimpl;
import ptithcm.serviceimpl.categoryserviceimpl;
import ptithcm.serviceimpl.discountserviceimpl;
import ptithcm.serviceimpl.homeserviceimpl;
import ptithcm.serviceimpl.itemserviceimpl;
import ptithcm.serviceimpl.orderdetailserviceimpl;
import ptithcm.serviceimpl.orderserviceimpl;
import ptithcm.serviceimpl.warrantyserviceimpl;
import ptithcm.serviceimpl.watchserviceimpl;

@Controller
public class cartcontroller {

	@Autowired
	itemserviceimpl itemserviceimpl;

	@Autowired
	homeserviceimpl homeserviceimpl;

	@Autowired
	watchserviceimpl watchserviceimpl;

	@Autowired
	brandserviceimpl brandserviceimpl;

	@Autowired
	categoryserviceimpl categoryserviceimpl;

	@Autowired
	orderserviceimpl orderserviceimpl;

	@Autowired
	orderdetailserviceimpl orderdetailserviceimpl;

	@Autowired
	warrantyserviceimpl warrantyserviceimpl;

	@Autowired
	discountserviceimpl discountserviceimpl;
	@Autowired
	JavaMailSender mailer;

	private int currentpage = 1;
	private int maxpage = 1;
	private int pagesize = 1;
	private int hienthi = 1;

	@RequestMapping("product")
	public String cart(ModelMap model, HttpSession session, HttpServletRequest request, RedirectAttributes ra,
			@RequestParam(value = "brand_id", required = false) Long brand_id,
			@RequestParam(value = "category_id", required = false) Long category_id) {
		if (brand_id != null && category_id != null) {
			getMaxPageByCategoryAndBrand(category_id, brand_id);
			List<watchs> watchs = watchserviceimpl.watchPageByCategoryAndBrand(1, hienthi, category_id, brand_id);
			model.addAttribute("watch",watchs);
			int maxpage1 = getMaxPageByCategoryAndBrand(category_id, brand_id);
			if (maxpage == 1) {
				model.addAttribute("maxpage", 0);
			} else {
				model.addAttribute("maxpage", maxpage1);
			}
			currentpage = 1;
			model.addAttribute("currentpage", currentpage);
			model.addAttribute("pagesize", pagesize);
		} else if (brand_id != null) {
			getMaxPageByBrand(brand_id);
			List<watchs> watchs = watchserviceimpl.watchPageByBrand(1, hienthi, brand_id);
			model.addAttribute("watch", watchs);
			int maxpage1 = getMaxPageByBrand(brand_id);
			if (maxpage1 == 1) {
				model.addAttribute("maxpage", 0);
			} else {
				model.addAttribute("maxpage", maxpage1);
			}
			currentpage = 1;
			model.addAttribute("currentpage", currentpage);
			model.addAttribute("pagesize", pagesize);

		} else if (category_id != null) {
			getMaxPageByCategory(category_id);
			List<watchs> watchs =  watchserviceimpl.watchPageByCategory(1, hienthi, category_id); 
			model.addAttribute("watch",watchs);
			int maxpage1 = getMaxPageByCategory(category_id);
			if (maxpage1 == 1) {
				model.addAttribute("maxpage", 0);
			} else {
				model.addAttribute("maxpage", maxpage1);
			}
			currentpage = 1;
			model.addAttribute("currentpage", currentpage);
			model.addAttribute("pagesize", pagesize);
		} else {			
			getMaxPage();
			int maxpage1 = getMaxPage();
			List<watchs> watchs = homeserviceimpl.watchPageOfHome(1, hienthi);
			model.addAttribute("watch", watchs);
			if (maxpage1 == 1) {
				model.addAttribute("maxpage", 0);
			} else {
				model.addAttribute("maxpage", maxpage1);
			}
			List<watchs> watches = homeserviceimpl.watchPageOfHome(1, hienthi);
			List<Long> price_discount = new ArrayList<>();
			for (watchs watch : watches) {
				// lấy discountPrice trong danh sách đồng hồ
				Long discountPrice = discountserviceimpl.getPriceDiscountByWatch_id(watch.getWatch_id());
				if (discountPrice != null) {
					// nếu có thì add vô list
					price_discount.add(discountPrice);
				}
				//lấy discount_id
				Long discount_id = discountserviceimpl.getDiscount_idByWatch_id(watch.getWatch_id());
				if (discount_id != null) {
					//lấy discount_percent theo discount_id và watch_id
					Float discount_percent = discountserviceimpl.getPercentWatch(discount_id, watch.getWatch_id());
					int discount_percenr_round = (int) Math.round(discount_percent * 100);
					session.setAttribute("percent_discount", discount_percenr_round + "%");
				} else {
					session.setAttribute("percent_discount", null);
				}
			}
			// danh sách giá discount
			List<Long> price = new ArrayList<>();
			if (price_discount != null) {
				for (watchs watch : watches) {
					
					Long discountPrice = discountserviceimpl.getPriceDiscountByWatch_id(watch.getWatch_id());//lấy giá discount
					if (discountPrice != null) {
						price.add(discountPrice);
					} else {
						price.add(watch.getPrice());
					}
				}
			}
			model.addAttribute("discount_prices", price_discount);
			model.addAttribute("price_buy", price);
			currentpage = 1;
			model.addAttribute("currentpage", currentpage);
			model.addAttribute("pagesize", pagesize);
		}
		if (UserController.getUser() != null) {
			List<items> items = itemserviceimpl.findItemByIDUser(UserController.getUser().getUser_id());//giỏ hàng của user login
			Long item_id = itemserviceimpl.getItem_idByUser_id(UserController.getUser().getUser_id());
			model.addAttribute("cart", items);
			if (item_id != null) {
				List<item_detail> itemdetail = itemserviceimpl.getItem_detail(item_id);
				if (itemdetail != null) {
					model.addAttribute("cart_item", itemdetail);//list giỏ hàng of user
				}
			}
		}
		model.addAttribute("brands", brandserviceimpl.dsbrands());
		model.addAttribute("category", categoryserviceimpl.dscategory());
		return "account/cart";
	}

	//chuyển đổi số thành chuỗi tiền tệ theo định dạng có dấu phẩy ngăn cách hàng nghìn
	public String ChuyenSoThanhTien(String s) {
		String temp = "";
		int pos = s.length() % 3 - 1;
		if (pos < 0) {
			pos = 2;
		}

		for (int i = 0; i < s.length(); ++i) {
			temp = temp + s.charAt(i);
			if (pos == i) {
				if (i != s.length() - 1) {
					temp = temp + ",";
				}
				pos += 3;
			}
		}
		return temp;
	}

	public void sentEmail_OrderCheck(String email, List<productList> productLists,String receiver_name, String address, String phone) {
	    try {
	        String body = "<html>\n" +
	            "  <link rel=\"stylesheet\" href=\"https://cdn.jsdelivr.net/npm/boxicons@latest/css/boxicons.min.css\">\n" +
	            "  <link href=\"https://cdn.lineicons.com/4.0/lineicons.css\" rel=\"stylesheet\" />\n" +
	            "</head>\n" +
	            "<body>\n" +
	            "  <div class=\"main__email\" style=\"width: 700px;justify-content: center;color: black;margin: 30px auto\">\n" +
	            "    <div class=\"title_email\" style=\"text-align: center;width: 100%;padding: 15px;border: 1px solid rgb(173, 168, 168);box-shadow: rgb(180, 179, 179) 2px 2px; margin-bottom: 10px;\">\n" +
	            "      <span>\n" +
	            "        <a style=\" margin-bottom: 10px; color: black; text-decoration: none; font-size: 25px; font-weight: bold;\" href=\"http://localhost:9999/WATCHSHOP/home.htm\" class=\"nav__logo\"><i class='bx bxl-medium-old'></i>TS-WATCH</a>\n" +
	            "        <p style=\"text-align: start;\">Xin chào: "+receiver_name+"</p>\n" +
	            "        <p>TS-WATCH đã nhận được yêu cầu đặt hàng của bạn và đang xử lí nhé. Khi đã được xác nhận đơn hàng của bạn sẽ được giao trong vòng 3 ngày. Hãy truy cập vào website để theo dõi đơn hàng nhé.</p>\n" +
	            "        <p style=\"margin-top: 20px;\">Truy cập vào website <a href=\"http://localhost:9999/WATCHSHOP/home.htm\"> tại đây </a></p>\n" +
	            "      </span>\n" +
	            "    </div>\n" +
	            "    <div class=\"content_email\" style=\"width: 100%;\">\n" +
	            "      <div class=\"div__info\" style=\"width: 100%;padding: 15px;border: 1px solid rgb(173, 168, 168);box-shadow: rgb(180, 179, 179) 2px 2px;\">\n" +
	            "        <span style=\"color: black;font-size: 18px; font-weight: bolder; \"><i class=\"lni lni-travel\"></i> Đơn hàng được giao đến</span>\n" +
	            "        <ul style=\"list-style: none;margin: 10px;\">\n" +
	            "          <li style=\"margin-bottom: 5px;\"><p>Tên: "+ receiver_name+ "</p></li>\n" +
	            "          <li style=\"margin-bottom: 5px;\"><p>Địa chỉ: "+address+" </p></li>\n" +
	            "          <li style=\"margin-bottom: 5px;\"><p>Điện thoại: "+phone+"</p></li>\n" +
	            "        </ul>\n" +
	            "      </div>\n" +
	            "        <div class=\"table-data\" style=\"width: 100%;padding: 15px;border: 1px solid rgb(173, 168, 168);box-shadow: rgb(180, 179, 179) 2px 2px; margin-bottom: 10px\">\n" +
	            "          <span style=\"color: black;font-size: 18px; font-weight: bolder; \"><i class=\"lni lni-package\"></i> Kiện hàng</span>\n" +
	            "          <div style=\"box-shadow: none;width: 100%;display: flex;flex-wrap: wrap;grid-gap: 24px;margin-top: 24px;\" class=\"order\">\n" +
	            "          <table style=\"width: 100%;border-collapse: collapse;\">\n" +
	            "              <thead> \n" +
	            "                  <tr>\n" +
	            "                      <th style=\"padding-bottom: 12px;font-size: 13px;text-align: left;border-bottom: 1px solid grey;\">STT</th>\n" +
	            "                      <th style=\"padding-bottom: 12px;font-size: 13px;text-align: left;border-bottom: 1px solid grey;\">Tên sản phẩm</th>\n" +
	            "                      <th style=\"padding-bottom: 12px;font-size: 13px;text-align: left;border-bottom: 1px solid grey;\">Số lượng</th>\n" +
	            "                      <th style=\"padding-bottom: 12px;font-size: 13px;text-align: left;border-bottom: 1px solid grey;\">Giá</th>\n" +
	            "                  </tr>\n" +
	            "              </thead>\n" +
	            "              <tbody>\n"; 
	      
	        int stt = 1;
	        long sum = 0;
	        //định dạng số thành chuỗi theo định dạng tiền tệ.
	        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
	        symbols.setGroupingSeparator('.'); //Thiết lập ký hiệu phân cách hàng nghìn
	        symbols.setDecimalSeparator(',');//Thiết lập ký hiệu phân cách thập phân
	        DecimalFormat decimalFormat = new DecimalFormat("#,##0", symbols);
	        
	        for (productList product : productLists) {
	            String watchName = product.getWatch_name();
	            long watchPrice = product.getPrice();
	            long watchQuantity = product.getQuantity();
	            long watchTotalPrice = watchPrice * watchQuantity;
	           
	            String formattedPrice = decimalFormat.format(watchPrice);
	            sum += watchTotalPrice;
	            body += "                  <tr style=\"font-size: 15px;\">\n" +
	                "                          <td style=\"padding: 12px 10px;align-items: center;display: flex;align-items: center;grid-gap: 12px;padding-left: 6px;\"><p>" + stt + "</p></td>\n" +
	                "                          <td style=\"padding: 12px 10px;align-items: center;\"><p>" + watchName + "</p></td>\n" +
	                "                          <td style=\"padding: 12px 10px;align-items: center;\"><p>" + watchQuantity + "</p></td>\n" +
	                "                          <td style=\"padding: 12px 10px;align-items: center;\">" +  formattedPrice + " VND</td>\n" +
	                "                      </tr>\n";
	            stt++;
	        }
	        String formattedSum = decimalFormat.format(sum);
	        body += "              </tbody>\n" +
	            "          </table>\n" +
	            "        </div>\n" +
	            "      <div style=\"font-weight:bolder; box-shadow: none;text-align:end;display:flex;justify-content:end;gap:10px;\" class=\"total_oder\"> \n" +
	            "            <p>Thành tiền: </p> <p> " + formattedSum + " VND</p>\n" +
	            "      </div>\n" +
	            "      </div>\n" +
	            "    </div>\n" +
	            "  </div>\n" +
	            "</body>\n" +
	            "</html>";

	        String from = "WATCHSTORE.PTIT.HCM";
	        String to = email;
	        String subject = "Đặt hàng thành công!";
	        MimeMessage mail = mailer.createMimeMessage();
	        MimeMessageHelper helper = new MimeMessageHelper(mail, "utf-8");
	        helper.setFrom(from, from);
	        helper.setTo(to);
	        helper.setReplyTo(from, from);
	        helper.setSubject(subject);
	        helper.setText(body, true);
	        mailer.send(mail);
	        System.out.println("Gửi mail thành công");
	    } catch (Exception e) {
	        System.out.println("Gửi mail thất bại");
	    }
	}
	@RequestMapping(value = "product", params = "page")
	public String productPage(@RequestParam(required = true) int page, ModelMap model) {
		getMaxPage();
		if (page > maxpage) {
			page = 1;
		}
		List<watchs> watches = homeserviceimpl.watchPageOfHome(page, hienthi);
		model.addAttribute("watch", watches);
		model.addAttribute("category", categoryserviceimpl.dscategory());
		model.addAttribute("brands", brandserviceimpl.dsbrands());
		model.addAttribute("maxpage", maxpage);
		model.addAttribute("currentpage", page);
		currentpage = page;
		model.addAttribute("pagesize", pagesize);
	    return "account/cart";	    
	}
	
	//tìm kiếm sản phẩm theo searchType
	@RequestMapping(value = "find/{searchType}", params = { "page" })
	public String findWatches(@PathVariable("searchType") String searchType, @RequestParam(required = true) int page,
			@RequestParam(value = "brand_id", required = false) Long brand_id,
			@RequestParam(value = "category_id", required = false) Long category_id, ModelMap model,
			HttpSession session) {
		if (searchType.equals("findbybrand")) {
			getMaxPageByBrand(brand_id);
			if (page > maxpage) {
				page = 1;
			}
			List<watchs> watch = watchserviceimpl.watchPageByBrand(page, hienthi, brand_id);
			model.addAttribute("watch", watch);
		} else if (searchType.equals("findbycategory")) {
			getMaxPageByCategory(category_id);
			if (page > maxpage) {
				page = 1;
			}
			List<watchs> watch = watchserviceimpl.watchPageByCategory(page, hienthi, category_id);
			model.addAttribute("watch", watch);
		} else if (searchType.equals("findbycategoryandbrand")) {
			getMaxPageByCategoryAndBrand(category_id, brand_id);
			if (page > maxpage) {
				page = 1;
			}
			List<watchs> watch = watchserviceimpl.watchPageByCategoryAndBrand(page, hienthi, category_id, brand_id);
			model.addAttribute("watch", watch);
		}	
		if (UserController.getUser() != null) {
			Long item_id = itemserviceimpl.getItem_idByUser_id(UserController.getUser().getUser_id());
			if (item_id != null) {
				model.addAttribute("cart_item", itemserviceimpl.getItem_detail(item_id));
			}
		}
		model.addAttribute("category", categoryserviceimpl.dscategory());
		model.addAttribute("brands", brandserviceimpl.dsbrands());
		model.addAttribute("maxpage", maxpage);
		model.addAttribute("currentpage", page);
		currentpage = page;
		model.addAttribute("pagesize", pagesize);
		return "account/cart";

	}

	@RequestMapping(value = "account/cart/add", method = RequestMethod.POST)
	public String addCart(HttpSession session, @RequestParam("input_price") long price,
			@RequestParam("input_quantity") int quanty, @RequestParam("input_id") String watch_id,
			RedirectAttributes ra) {
		long item_id = itemserviceimpl.getItem_idByUser_id(UserController.getUser().getUser_id());
		int sum = 0;
		long check_detail_exist = itemserviceimpl.checkItem_detailExist(watch_id, item_id) ;//kiểm tra có sản phẩm trong giỏ hàng không
		if (check_detail_exist > 0) {
			int quantity_detail = itemserviceimpl.getQuantity_detail(item_id, watch_id);//lấy số lượng sản phẩm đó
			sum = quantity_detail + 1;
			long kq = itemserviceimpl.updateItem_detail(item_id, watch_id, sum, price);
			if (kq > 0 ) {
				ra.addFlashAttribute("message", 1);// thành công
			} else {
				ra.addFlashAttribute("message", 2);// thất bại
			}
			return "redirect:/product.htm";
		} else {
			item_detail A = new item_detail();
			A.setWatch_id(watch_id);
			A.setItem_id(item_id);
			A.setQuantity(quanty);
			A.setPrice(price);
			int kq = itemserviceimpl.addItem_detail(A);
			
			if (kq > 0 ) {
				ra.addFlashAttribute("message", 1);// thành công
			} else {
				ra.addFlashAttribute("message", 2);// thất bại
			}
		}
		return "redirect:/product.htm";
	}

	@RequestMapping(value = "account/cart/edit-quantity", method = RequestMethod.POST)
	public String editCart(@RequestParam("item_id_detail") long id, @RequestParam("quantity_edit") int quantity,
			HttpSession session, RedirectAttributes ra) {
		long kq = itemserviceimpl.updateQuantity_detail(id, quantity);
		if (kq > 0) {
			ra.addFlashAttribute("message", 3);// thành công
		} else {
			ra.addFlashAttribute("message", 4);// thất bại
		}
		return "redirect:/product.htm";
	}

	@RequestMapping(value = "account/cart/delete", method = RequestMethod.POST)
	public String deleteCart(HttpSession session, @RequestParam("item_detail_id") int item_detial_id,
			RedirectAttributes ra) {
		long result = itemserviceimpl.deleteItem(item_detial_id);
		if (result > 0) {
			ra.addFlashAttribute("message", 5);// thanh cong
		} else {
			ra.addFlashAttribute("message", 6);// that bai
		}
		System.out.println(result);
		return "redirect:/product.htm";
	}

	@RequestMapping(value = "account/buyitem", method = RequestMethod.POST)
	public String addToTempCart(@RequestParam("item_detail_id") List<Long> item_detail_ids,
			@RequestParam("watch_name") List<String> watch_names, @RequestParam("picture") List<String> pictures,
			HttpServletRequest request, HttpSession session, ModelMap model) {
		List<productList> list = itemserviceimpl.getListDetail(item_detail_ids, pictures, watch_names);//lấy danh sách sản phẩm từ item_detail
		session.setAttribute("cart_checkout", list);//lưu trữ dữ liệu +
		return "redirect:/account/checkout.htm";
	}

	@RequestMapping("account/checkout")
	@SuppressWarnings("unchecked")
	public String showTempCart(HttpSession session, ModelMap model) {
		
		List<productList> list = (List<productList>) session.getAttribute("cart_checkout");//lấy lại dữ liệu đã save
		model.addAttribute("productList", list);
		session.setAttribute("address",orderserviceimpl.getAdressByUser_id(UserController.getUser().getUser_id()));
		long sum = 0;
		int soluongdem = 0;
		for (productList sList : list) {
			soluongdem++;
			long soluong = sList.getQuantity();
			long dongia = sList.getPrice();
			long tonggia = soluong * dongia;
			sum += tonggia;
		}
		session.setAttribute("soluong", soluongdem);
		session.setAttribute("tonggia", sum);
		return "account/checkout";
	}

	@RequestMapping(value = "account/cart/checkout", method = RequestMethod.POST)
	public String checkout(@RequestParam("address_id_send") long address_id,
			@RequestParam("address_send") String address_send,
			@RequestParam("receiver_name_send") String receiver_name, @RequestParam("phone_send") String phone_send,
			@RequestParam("item_detail_id[]") List<Long> item_detail_id, @RequestParam("tonggia") long tonggia,
			@RequestParam("watchid[]") List<String> watch_id, @RequestParam("quantity_detail[]") List<Integer> quantity,
			@RequestParam("price_detail[]") List<Long> price, HttpSession session, RedirectAttributes ra) {
		@SuppressWarnings("unchecked")
		List<productList> list = (List<productList>) session.getAttribute("cart_checkout");
		if (list != null) {
			orders A = new orders();
			java.util.Date date = new java.util.Date();
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			calendar.add(Calendar.YEAR, 1);
			Date end_date = calendar.getTime();
			A.setOrder_day(date);
			A.setTotal_price(tonggia);
			A.setOrder_status(1);
			A.setUser_id(UserController.getUser().getUser_id());
			A.setAddress_id(address_id);
			long result = orderserviceimpl.addOrder(A);
			for (int i = 0; i < watch_id.size(); i++) {
				order_detail od = new order_detail();
				od.setWatch_id(watch_id.get(i));
				od.setQuantity(Integer.valueOf(quantity.get(i)));
				od.setPrice(Long.valueOf(price.get(i)));
				od.setOrder_id(A.getOrder_id());
				long uday = orderdetailserviceimpl.addOrderDetail(od);
				long result1 = orderserviceimpl.updateQuantityWatchs(watch_id.get(i), Integer.valueOf(quantity.get(i)));	
				sentEmail_OrderCheck(UserController.getUser().getEmail(), list, receiver_name, address_send, phone_send);
			}
			for (int i = 0; i < item_detail_id.size(); i++) {
				long kq = itemserviceimpl.deleteItem(Long.valueOf(item_detail_id.get(i)));//xoá sp trong cart đã buy
			}
		} else {
			ra.addFlashAttribute("message", 7);// khong co san pham
			return "redirect:/account/order.htm";
		}
		return "redirect:/account/order.htm";
	}

	@RequestMapping(value = "account/add/address", method = RequestMethod.POST)
	public String addAdress(@RequestParam("receiver_name") String receiver_name, @RequestParam("phone") String phone,
			@RequestParam("addresDetail") String addresDetail, @RequestParam("address") String address,
			RedirectAttributes ra) {
		address A = new address();
		if (UserController.getUser() != null) {
			if(receiver_name.equals("") || phone.equals("") || addresDetail.equals("") || address.equals("")) {
				ra.addFlashAttribute("message",0);//thong tin error
			}else {
			A.setPhone(phone);
			A.setReceiver_name(receiver_name);
			A.setStatus("0");
			String address_total = addresDetail + ", " + address;
			A.setUser_id(UserController.getUser().getUser_id());
			A.setAddress(address_total);
			long result = orderserviceimpl.addAddress(A);
			if (result > 0) {
				ra.addFlashAttribute("message", 1);// them thanh cong\
			} else {
				ra.addFlashAttribute("message", 2);// them that bại
				System.out.println("That bai rôi" + result);
				}
			}
		}
		return "redirect:/account/checkout.htm";
	}

	@RequestMapping(value = "account/address/delete", method = RequestMethod.POST)
	public String deleteAddress(@RequestParam("idAddress") long addressId, RedirectAttributes ra) {
		long result = orderserviceimpl.deleteAddress(addressId);
		if (result > 0) {
			ra.addFlashAttribute("message", 1); // Xóa thành công
			System.out.println("Xóa thành công: " + result);
		} else {
			ra.addFlashAttribute("message", 2); // Xóa thất bại
			System.out.println("Xóa thất bại: " + result);
		}
		return "redirect:/account/checkout.htm";
	}

	
	@RequestMapping(value = "find/findbybrand", method = RequestMethod.POST)
	public String searchBrand(ModelMap model, HttpServletRequest request, @RequestParam("brand_id") long brand_id,
			HttpSession session) {
		getMaxPageByBrand(brand_id);
		if (UserController.getUser() != null) {
			List<items> items = itemserviceimpl.findItemByIDUser(UserController.getUser().getUser_id());
			Long item_id = itemserviceimpl.getItem_idByUser_id(UserController.getUser().getUser_id());
			model.addAttribute("cart", itemserviceimpl.findItemByIDUser(UserController.getUser().getUser_id()));
			if (item_id != null) {
				model.addAttribute("cart_item", itemserviceimpl.getItem_detail(item_id));
			}
		}
		model.addAttribute("brands", brandserviceimpl.dsbrands());
		model.addAttribute("category", categoryserviceimpl.dscategory());
		request.setAttribute("count", watchserviceimpl.searchWatchCountByBrand(brand_id));
		List<watchs> watches = watchserviceimpl.watchPageByBrand(1, hienthi, brand_id);
		List<Long> price_discount = new ArrayList<>();
		for (watchs watch : watches) {
		Long discountPrice = discountserviceimpl.getPriceDiscountByWatch_id(watch.getWatch_id());
			if (discountPrice != null) {
				price_discount.add(discountPrice);
			}
			Long discount_id = discountserviceimpl.getDiscount_idByWatch_id(watch.getWatch_id());
			if (discount_id != null) {
				Float discount_percent = discountserviceimpl.getPercentWatch(discount_id, watch.getWatch_id());
				System.out.println("percent" + discount_percent);
				int discount_percenr_round = (int) Math.round(discount_percent * 100);
				session.setAttribute("percent_discount", discount_percenr_round + "%");
			} else {
				session.setAttribute("percent_discount", null);
			}
		}
		List<Long> price = new ArrayList<>();
		if (price_discount != null) {
			for (watchs watch : watches) {
				Long discountPrice = discountserviceimpl.getPriceDiscountByWatch_id(watch.getWatch_id());
				if (discountPrice != null) {
					price.add(discountPrice);

				} else {
					price.add(watch.getPrice());
				}
			}
		}
		model.addAttribute("discount_prices", price_discount);
		model.addAttribute("price_buy", price);
		request.setAttribute("watch", watches);
		request.setAttribute("maxpage", maxpage);
		request.setAttribute("currentpage", 1);
		request.setAttribute("pagesize", pagesize);
		request.setAttribute("orderLink", "asc");
		return "account/cart";
	}

	@RequestMapping(value = "find/findbycategory", method = RequestMethod.POST)
	public String searchCategory(ModelMap model, HttpServletRequest request,
			@RequestParam("category_id") long category_id, HttpSession session) {
		getMaxPageByCategory(category_id);
		if (UserController.getUser() != null) {
			List<items> items = itemserviceimpl.findItemByIDUser(UserController.getUser().getUser_id());
			Long item_id = itemserviceimpl.getItem_idByUser_id(UserController.getUser().getUser_id());
			model.addAttribute("cart", itemserviceimpl.findItemByIDUser(UserController.getUser().getUser_id()));
			if (item_id != null) {
				model.addAttribute("cart_item", itemserviceimpl.getItem_detail(item_id));
			}
		}
		model.addAttribute("brands", brandserviceimpl.dsbrands());
		model.addAttribute("category", categoryserviceimpl.dscategory());
		List<watchs> watches = watchserviceimpl.watchPageByCategory(1, hienthi, category_id);
		List<Long> price_discount = new ArrayList<>();
		for (watchs watch : watches) {
			Long discountPrice = discountserviceimpl.getPriceDiscountByWatch_id(watch.getWatch_id());
			if (discountPrice != null) {
				price_discount.add(discountPrice);
			}
			Long discount_id = discountserviceimpl.getDiscount_idByWatch_id(watch.getWatch_id());
			if (discount_id != null) {
				Float discount_percent = discountserviceimpl.getPercentWatch(discount_id, watch.getWatch_id());
				System.out.println("percent" + discount_percent);
				int discount_percenr_round = (int) Math.round(discount_percent * 100);
				session.setAttribute("percent_discount", discount_percenr_round + "%");
			} else {
				session.setAttribute("percent_discount", null);
			}
		}
		List<Long> price = new ArrayList<>();
		if (price_discount != null) {
			for (watchs watch : watches) {
				Long discountPrice = discountserviceimpl.getPriceDiscountByWatch_id(watch.getWatch_id());
				if (discountPrice != null) {
					price.add(discountPrice);
				} else {
					price.add(watch.getPrice());
				}
			}
		}
		model.addAttribute("discount_prices", price_discount);
		model.addAttribute("price_buy", price);
		request.setAttribute("watch", watches);
		request.setAttribute("maxpage", maxpage);
		request.setAttribute("currentpage", 1);
		request.setAttribute("pagesize", pagesize);
		request.setAttribute("orderLink", "asc");
		return "account/cart";
	}

	@RequestMapping(value = "find/findbycategoryandbrand", method = RequestMethod.POST)
	public String searchCategoryAndBrand(ModelMap model, HttpServletRequest request,
			@RequestParam("category_id1") long category_id, @RequestParam("brand_id1") long brand_id,
			HttpSession session) {
		getMaxPageByCategoryAndBrand(category_id, brand_id);
		if (UserController.getUser() != null) {
			List<items> items = itemserviceimpl.findItemByIDUser(UserController.getUser().getUser_id());
			Long item_id = itemserviceimpl.getItem_idByUser_id(UserController.getUser().getUser_id());
			model.addAttribute("cart", itemserviceimpl.findItemByIDUser(UserController.getUser().getUser_id()));
			if (item_id != null) {
				model.addAttribute("cart_item", itemserviceimpl.getItem_detail(item_id));
			}
		}
		model.addAttribute("brands", brandserviceimpl.dsbrands());
		model.addAttribute("category", categoryserviceimpl.dscategory());
		List<watchs> watches = watchserviceimpl.watchPageByCategoryAndBrand(1, hienthi, category_id, brand_id);
		List<Long> price_discount = new ArrayList<>();
		List<watchs> watchs = watchserviceimpl.watchPageByCategoryAndBrand(1, hienthi, category_id, brand_id);
		for (watchs watch : watchs) {
			Long discountPrice = discountserviceimpl.getPriceDiscountByWatch_id(watch.getWatch_id());
			if (discountPrice != null) {
				price_discount.add(discountPrice);
			}
			Long discount_id = discountserviceimpl.getDiscount_idByWatch_id(watch.getWatch_id());
			if (discount_id != null) {
				Float discount_percent = discountserviceimpl.getPercentWatch(discount_id, watch.getWatch_id());
				System.out.println("percent" + discount_percent);
				int discount_percenr_round = (int) Math.round(discount_percent * 100);
				session.setAttribute("percent_discount", discount_percenr_round + "%");
			} else {
				session.setAttribute("percent_discount", null);
			}
		}
		List<Long> price = new ArrayList<>();
		if (price_discount != null) {
			for (watchs watch : watchs) {
				Long discountPrice = discountserviceimpl.getPriceDiscountByWatch_id(watch.getWatch_id());
				if (discountPrice != null) {
					price.add(discountPrice);
				} else {
					price.add(watch.getPrice());
				}
			}
		}
		model.addAttribute("discount_prices", price_discount);
		model.addAttribute("price_buy", price);
		request.setAttribute("watch", watches);
		request.setAttribute("maxpage", maxpage);
		request.setAttribute("currentpage", 1);
		request.setAttribute("pagesize", pagesize);
		request.setAttribute("orderLink", "asc");
		return "account/cart";
	}

	public int getMaxPage() {
		int WatchCount = (int) homeserviceimpl.getWatchCount();
		System.out.println("watchcountne: " + WatchCount);
		if (WatchCount == 0) {
			hienthi = 1;
			currentpage = 0;
		} else if (WatchCount < 9) {
			hienthi = WatchCount;
			pagesize = 1;
		} else if (WatchCount < 17) {
			hienthi = 8;
			pagesize = 2;
		} else {
			hienthi = 8;
			pagesize = 3;
		}
		if (WatchCount % hienthi == 0) { 
			maxpage = WatchCount / hienthi;
		} else {
			maxpage = (int) (WatchCount / hienthi) + 1;
		}
		return maxpage;
	}

	public int getMaxPageByBrand(long brand_id) {
		int CountByBrand = (int) watchserviceimpl.searchWatchCountByBrand(brand_id);
		System.out.println("watchcountne: " + CountByBrand);
		if (CountByBrand == 0) {
			hienthi = 1;
			currentpage = 0;
		} else if (CountByBrand < 9) {
			hienthi = CountByBrand;
			pagesize = 1;
		} else if (CountByBrand < 17) {
			hienthi = 8;
			pagesize = 2;
		} else {
			hienthi = 8;
			pagesize = 3;
		}
		if (CountByBrand % hienthi == 0) {
			maxpage = CountByBrand / hienthi;
		} else {
			maxpage = (int) (CountByBrand / hienthi) + 1;
		}
		return maxpage;
	}

	public int getMaxPageByCategory(long category_id) {
		int WatchCountByCategory = (int) watchserviceimpl.searchWatchCountByCategory(category_id);
		System.out.println("watchcountne: " + WatchCountByCategory);
		if (WatchCountByCategory == 0) {
			hienthi = 1;
			currentpage = 0;
		} else if (WatchCountByCategory < 9) {
			hienthi = WatchCountByCategory;
			pagesize = 1;
		} else if (WatchCountByCategory < 17) {
			hienthi = 8;
			pagesize = 2;
		} else {
			hienthi = 8;
			pagesize = 3;
		}
		if (WatchCountByCategory % hienthi == 0) {
			maxpage = WatchCountByCategory / hienthi;
		} else {
			maxpage = (int) (WatchCountByCategory / hienthi) + 1;
		}
		return maxpage;
	}

	public int getMaxPageByCategoryAndBrand(long category_id, long brand_id) {
		int CategoryAndBrandCount = (int) watchserviceimpl.searchWatchByCategoryAndBrandCount(category_id, brand_id);
		if (CategoryAndBrandCount == 0) {
			hienthi = 1;
			currentpage = 0;
		} else if (CategoryAndBrandCount < 9) {
			hienthi = CategoryAndBrandCount;
			pagesize = 1;
		} else if (CategoryAndBrandCount < 17) {
			hienthi = 8;
			pagesize = 2;
		} else {
			hienthi = 8;
			pagesize = 3;
		}
		if (CategoryAndBrandCount % hienthi == 0) {
			maxpage = CategoryAndBrandCount / hienthi;
		} else {
			maxpage = (int) (CategoryAndBrandCount / hienthi) + 1;
		}
		return maxpage;
	}

}
