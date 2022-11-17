package vn.fs.controller.admin;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import vn.fs.entities.Diary;
import vn.fs.entities.User;
import vn.fs.repository.DiaryRepository;
import vn.fs.repository.UserRepository;


@Controller
@RequestMapping("/admin")
public class IndexAdminController{
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	DiaryRepository diaryRepository;
	
	@ModelAttribute(value = "user")
	public User user(Model model, Principal principal, User user) {

		if (principal != null) {
			model.addAttribute("user", new User());
			user = userRepository.findByEmail(principal.getName());
			model.addAttribute("user", user);
		}

		return user;
	}

	@GetMapping(value = "/home")
	public String index(Model model) {
		List<Diary> diaries = diaryRepository.findAll();
		int newDiary = diaries.size();
		model.addAttribute("diaries", diaries);
		model.addAttribute("newDiary", newDiary);
		return "admin/index";
	}
}
