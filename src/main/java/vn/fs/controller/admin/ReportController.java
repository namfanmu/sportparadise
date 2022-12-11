package vn.fs.controller.admin;

import java.security.Principal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import vn.fs.commom.CommomDataService;
import vn.fs.entities.OrderDetail;
import vn.fs.entities.User;
import vn.fs.repository.OrderDetailRepository;
import vn.fs.repository.UserRepository;


@Controller
public class ReportController {

	@Autowired
	UserRepository userRepository;

	@Autowired
	OrderDetailRepository orderDetailRepository;
	
	@Autowired
	CommomDataService commomDataService;

	// Statistics by product sold
//	@GetMapping(value = "/admin/statisticProductByQty")
//	public String statisticProductByQty(Model model, Principal principal) throws SQLException {
//		User user = userRepository.findByEmail(principal.getName());
//		model.addAttribute("user", user);
//
//		OrderDetail orderDetail = new OrderDetail();
//		model.addAttribute("orderDetail", orderDetail);
//		List<Object[]> listReportCommon = orderDetailRepository.repoQty();
//		model.addAttribute("listReportCommon", listReportCommon);
//
//
//		return "admin/statisticProduct";
//	}
//	
//	@GetMapping(value = "/admin/statisticProductByTo")
//	public String statisticProductByTo(Model model, Principal principal) throws SQLException {
//		User user = userRepository.findByEmail(principal.getName());
//		model.addAttribute("user", user);
//
//		OrderDetail orderDetail = new OrderDetail();
//		model.addAttribute("orderDetail", orderDetail);
//		List<Object[]> listReportCommon = orderDetailRepository.repoTo();
//		model.addAttribute("listReportCommon", listReportCommon);
//
//
//		return "admin/statisticProduct";
//	}
	
	@GetMapping(value = "/admin/statisticProduct")
	public String statisticProduct(Model model, Pageable pageable, @RequestParam("page") Optional<Integer> page,
			@RequestParam("size") Optional<Integer> size, @RequestParam(name = "sort", required = false, defaultValue = "qty") String sort, 
			User user, Principal principal) throws SQLException {
		user = userRepository.findByEmail(principal.getName());
		model.addAttribute("user", user);
		
		OrderDetail orderDetail = new OrderDetail();
		model.addAttribute("orderDetail", orderDetail);
		
		int currentPage = page.orElse(1);
		int pageSize = size.orElse(6);
		Page<Object[]> productPage = findPaginated(PageRequest.of(currentPage - 1, pageSize), sort);
		

		int totalPages = productPage.getTotalPages();
		if (totalPages > 0) {
			List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages).boxed().collect(Collectors.toList());
			model.addAttribute("pageNumbers", pageNumbers);
		}

		commomDataService.commonData(model, user);
		model.addAttribute("listReportCommon", productPage);
		if(sort.equals("qty")) {
			model.addAttribute("sort", "qty");
		} else if(sort.equals("to")) {
			model.addAttribute("sort", "to");
		}  
		return "admin/statisticProduct";
	}
	
	public Page<Object[]> findPaginated(Pageable pageable, String sort) {
		List<Object[]> productPage = new ArrayList<>();;
		if (sort.equals("qty")) {
			productPage = orderDetailRepository.repoQty();
		    }
	    if (sort.equals("to")) {
	    	productPage = orderDetailRepository.repoTo();
	    }
	    
		int pageSize = pageable.getPageSize();
		int currentPage = pageable.getPageNumber();
		int startItem = currentPage * pageSize;
		List<Object[]> list;

		if (productPage.size() < startItem) {
			list = Collections.emptyList();
		} else {
			int toIndex = Math.min(startItem + pageSize, productPage.size());
			list = productPage.subList(startItem, toIndex);
		}

		Page<Object[]> productPages = new PageImpl<Object[]>(list, PageRequest.of(currentPage, pageSize),productPage.size());

		return productPages;
	}


	// Statistics of products sold by month
	@RequestMapping(value = "/admin/reportMonth")
	public String reportmonth(Model model, Principal principal) throws SQLException {
		User user = userRepository.findByEmail(principal.getName());
		model.addAttribute("user", user);

		OrderDetail orderDetail = new OrderDetail();
		model.addAttribute("orderDetail", orderDetail);
		List<Object[]> listReportCommon = orderDetailRepository.repoWhereMonth();
		model.addAttribute("listReportCommon", listReportCommon);

		return "admin/statisticMonth";
	}


	// Statistics by user
	@RequestMapping(value = "/admin/reportOrderCustomer")
	public String reportordercustomer(Model model, Principal principal, Pageable pageable, @RequestParam("page") Optional<Integer> page, 
			@RequestParam("size") Optional<Integer> size) throws SQLException {
		User user = userRepository.findByEmail(principal.getName());
		model.addAttribute("user", user);

		OrderDetail orderDetail = new OrderDetail();
		model.addAttribute("orderDetail", orderDetail);
		
		int currentPage = page.orElse(1);
		int pageSize = size.orElse(6);
		Page<Object[]> userPage = findPaginatedUser(PageRequest.of(currentPage - 1, pageSize));

		int totalPages = userPage.getTotalPages();
		if (totalPages > 0) {
			List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages).boxed().collect(Collectors.toList());
			model.addAttribute("pageNumbers", pageNumbers);
		}

		commomDataService.commonData(model, user);
		model.addAttribute("listReportCommon", userPage);

		return "admin/statisticUser";
	}
	

	public Page<Object[]> findPaginatedUser(Pageable pageable) {
		List<Object[]> userPage = orderDetailRepository.reportCustommer();
		int pageSize = pageable.getPageSize();
		int currentPage = pageable.getPageNumber();
		int startItem = currentPage * pageSize;
		List<Object[]> list;

		if (userPage.size() < startItem) {
			list = Collections.emptyList();
		} else {
			int toIndex = Math.min(startItem + pageSize, userPage.size());
			list = userPage.subList(startItem, toIndex);
		}

		Page<Object[]> userPages = new PageImpl<Object[]>(list, PageRequest.of(currentPage, pageSize),userPage.size());

		return userPages;
	}
	

}
