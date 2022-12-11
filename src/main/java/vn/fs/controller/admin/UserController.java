package vn.fs.controller.admin;

import java.security.Principal;
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
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import vn.fs.commom.CommomDataService;
import vn.fs.entities.User;
import vn.fs.repository.UserRepository;

@Controller
public class UserController{

	@Autowired
	UserRepository userRepository;
	
	@Autowired
	CommomDataService commomDataService;
	
	@GetMapping(value = "/admin/users")
	public String orders(Model model, Principal principal, User user, Pageable pageable,
			@RequestParam("page") Optional<Integer> page, @RequestParam("size") Optional<Integer> size) {

		User user1 = userRepository.findByEmail(principal.getName());
		model.addAttribute("user", user1);
		int currentPage = page.orElse(1);
		int pageSize = size.orElse(6);

		Page<User> userPage = findPaginated(PageRequest.of(currentPage - 1, pageSize), user);

		int totalPages = userPage.getTotalPages();
		if (totalPages > 0) {
			List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages).boxed().collect(Collectors.toList());
			model.addAttribute("pageNumbers", pageNumbers);
		}

		commomDataService.commonData(model, user);
		model.addAttribute("users", userPage);

		return "/admin/users";
	}

	public Page<User> findPaginated(Pageable pageable, User user) {

		List<User> userPage = userRepository.findAll();
		Collections.reverse(userPage);

		int pageSize = pageable.getPageSize();
		int currentPage = pageable.getPageNumber();
		int startItem = currentPage * pageSize;
		List<User> list;

		if (userPage.size() < startItem) {
			list = Collections.emptyList();
		} else {
			int toIndex = Math.min(startItem + pageSize, userPage.size());
			list = userPage.subList(startItem, toIndex);
		}

		Page<User> userPages = new PageImpl<User>(list, PageRequest.of(currentPage, pageSize), userPage.size());

		return userPages;
	}
	
	@RequestMapping("/admin/users/lock/{user_id}")
	public ModelAndView lock(ModelMap model, @PathVariable("user_id") Long id) {
		Optional<User> o = userRepository.findById(id);
		if (o.isEmpty()) {
			return new ModelAndView("forward:/admin/users", model);
		}
		User oReal = o.get();
		oReal.setStatus(false);
		userRepository.save(oReal);

		return new ModelAndView("forward:/admin/users", model);
	}
	
	@RequestMapping("/admin/users/unlock/{user_id}")
	public ModelAndView unlock(ModelMap model, @PathVariable("user_id") Long id) {
		Optional<User> o = userRepository.findById(id);
		if (o.isEmpty()) {
			return new ModelAndView("forward:/admin/users", model);
		}
		User oReal = o.get();
		oReal.setStatus(true);
		userRepository.save(oReal);

		return new ModelAndView("forward:/admin/users", model);
	}
	
}
