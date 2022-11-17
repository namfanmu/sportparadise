package vn.fs.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import vn.fs.entities.Role;
import vn.fs.entities.User;
import vn.fs.repository.UserRepository;
import vn.fs.service.SendMailService;

@Controller
public class RegisterController {

	@Autowired
	UserRepository userRepository;

	@Autowired
	SendMailService sendMailService;

	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	HttpSession session;

	@GetMapping("/register")
	public ModelAndView registerForm(ModelMap model) {
		model.addAttribute("user", new User());
		return new ModelAndView("web/register", model);
	}

	@PostMapping("/register")
	public String register(ModelMap model, @Validated @ModelAttribute("user") User dto, BindingResult result,
			@RequestParam("password") String password) {
		if (result.hasErrors()) {
			return "web/register";
		}
		if (!checkEmail(dto.getEmail())) {
			model.addAttribute("error", "Email này đã được sử dụng!");
			return "web/register";
		}
		session.removeAttribute("otp");
		int random_otp = (int) Math.floor(Math.random() * (999999 - 100000 + 1) + 100000);
		session.setAttribute("otp", random_otp);
		String body = "<div>\r\n" + "<h3>Mã xác thực OTP của bạn là: <span style=\"color:#119744; font-weight: bold;\">"
				+ random_otp + "</span></h3>\r\n" + "</div>";
		sendMailService.queue(dto.getEmail(), "Đăng kí tài khoản", body);

		model.addAttribute("user", dto);
		model.addAttribute("message", "Mã xác thực OTP đã được gửi tới Email : " + dto.getEmail() + " , hãy kiểm tra Email của bạn!");

		return "/web/confirmOtpRegister";
	}

	@PostMapping("/confirmOtpRegister")
	public ModelAndView confirmRegister(ModelMap model, @ModelAttribute("user") User dto,
			@RequestParam("password") String password, @RequestParam("otp") String otp) {
		if (otp.equals(String.valueOf(session.getAttribute("otp")))) {
			dto.setPassword(bCryptPasswordEncoder.encode(password));
			dto.setRegisterDate(new Date());
			dto.setStatus(true);
			dto.setAvatar("user.png");
			dto.setRoles(Arrays.asList(new Role("ROLE_USER")));
			userRepository.save(dto);

			session.removeAttribute("otp");
			model.addAttribute("message", "Đăng kí thành công");
			return new ModelAndView("web/login");
		}

		model.addAttribute("user", dto);
		model.addAttribute("error", "Mã xác thực OTP không chính xác, hãy thử lại!");
		return new ModelAndView("web/confirmOtpRegister", model);
	}

	// check email
	public boolean checkEmail(String email) {
		List<User> list = userRepository.findAll();
		for (User c : list) {
			if (c.getEmail().equalsIgnoreCase(email)) {
				return false;
			}
		}
		return true;
	}

}
