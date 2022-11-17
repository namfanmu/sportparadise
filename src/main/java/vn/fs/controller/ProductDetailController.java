package vn.fs.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import vn.fs.commom.CommomDataService;
import vn.fs.entities.Comment;
import vn.fs.entities.OrderDetail;
import vn.fs.entities.Product;
import vn.fs.entities.User;
import vn.fs.repository.CommentRepository;
import vn.fs.repository.OrderDetailRepository;
import vn.fs.repository.ProductRepository;


@Controller
public class ProductDetailController extends CommomController{
	
	@Autowired
	ProductRepository productRepository;
	
	@Autowired
	OrderDetailRepository orderDetailRepository;
	
	@Autowired
	CommentRepository commentRepository;
	
	@Autowired
	CommomDataService commomDataService;

	@GetMapping(value = "productDetail")
	public String productDetail(@RequestParam("id") Long id, Model model, User user) {

		Product product = productRepository.findById(id).orElse(null);
		model.addAttribute("product", product);
		commomDataService.commonData(model, user);
		listProductByCategory10(model, product.getCategory().getCategoryId());
		
		List<Comment> listComment = commentRepository.listCommentByProductId(id);
		model.addAttribute("listComment", listComment);
		if(!(listComment.isEmpty())) {
			double avgRating = commentRepository.getAvgRating(id);
			model.addAttribute("avgRating", avgRating);
		}
		
		List<OrderDetail> listOrderDetails = orderDetailRepository.listOrderDetailByProductId(id);
		model.addAttribute("listOrderDetails", listOrderDetails);
		if(!(listOrderDetails.isEmpty())) {
			int soldQuantity = productRepository.getSoldQuantity(id);
			model.addAttribute("soldQuantity", soldQuantity);
		}
		
		return "web/productDetail";
	}
	
	// Gợi ý top 10 sản phẩm cùng loại
	public void listProductByCategory10(Model model, Long categoryId) {
		List<Product> products = productRepository.listProductByCategory10(categoryId);
		model.addAttribute("productByCategory", products);
	}
	
}
