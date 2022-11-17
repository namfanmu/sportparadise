package vn.fs.controller;

import java.security.Principal;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import vn.fs.commom.CommomDataService;
import vn.fs.entities.Comment;
import vn.fs.entities.Product;
import vn.fs.entities.User;
import vn.fs.repository.CommentRepository;
import vn.fs.repository.OrderDetailRepository;
import vn.fs.repository.OrderRepository;
import vn.fs.repository.ProductRepository;
import vn.fs.repository.UserRepository;

@Controller
public class CommentController {
	
	@Autowired
	CommentRepository commentRepository;
	
	@Autowired
	UserRepository userRepository;

	@Autowired
	OrderRepository orderRepository;
	
	@Autowired
	OrderDetailRepository orderDetailRepository;
	
	@Autowired
	ProductRepository productRepository;

	@Autowired
	CommomDataService commomDataService;
	
	@Autowired
	HttpSession session;
	
	// sent review
		@PostMapping(value = "/sentReview")
		public String add(@RequestParam("productId") Long productId, HttpServletRequest request, Model model, Principal principal, User user) {

			if (principal != null) {

				model.addAttribute("user", new User());
				user = userRepository.findByEmail(principal.getName());
				model.addAttribute("user", user);
			}
			Product product = productRepository.findProductById(productId);
			
		    Date date = new Date();  
			String rating = request.getParameter("rating");
			String review = request.getParameter("comment");
			Comment comment = new Comment();
			comment.setRating(Integer.parseInt(rating));
			comment.setContent(review);
			comment.setUser(user);
			comment.setRateDate(date);
			comment.setProduct(product);
			commentRepository.save(comment);
			return "redirect:/productDetail?id=" + productId;
		}

}
