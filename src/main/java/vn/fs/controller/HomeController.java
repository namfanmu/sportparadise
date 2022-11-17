package vn.fs.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import vn.fs.commom.CommomDataService;
import vn.fs.entities.Favorite;
import vn.fs.entities.Order;
import vn.fs.entities.OrderDetail;
import vn.fs.entities.Product;
import vn.fs.entities.User;
import vn.fs.repository.FavoriteRepository;
import vn.fs.repository.OrderDetailRepository;
import vn.fs.repository.OrderRepository;
import vn.fs.repository.ProductRepository;


@Controller
public class HomeController extends CommomController {
	
	@Autowired
	ProductRepository productRepository;
	
	@Autowired
	CommomDataService commomDataService;
	
	@Autowired
	FavoriteRepository favoriteRepository;
	
	@Autowired
	OrderRepository orderRepository;
	
	@Autowired 
	OrderDetailRepository orderDetailRepository;

	@GetMapping(value = "/")
	public String home(Model model, User user) {

		commomDataService.commonData(model, user);
		bestSaleProduct20(model, user);
		recommendProduct(model, user);
		bestDiscountProduct(model, user);
		return "web/home";
	}
	
	// list product ở trang chủ limit 10 sản phẩm mới nhất
	@ModelAttribute("listProduct10")
	public List<Product> listproduct10(Model model) {
		List<Product> productList = productRepository.listProductNew20();
		model.addAttribute("productList", productList);
		return productList;
	}
	
	// Recommend product
	public void recommendProduct(Model model, User user) {
		Long userId = user.getUserId();
		List<Order> listOrder = orderRepository.findOrderByUserId(userId);
		List<OrderDetail> listOrderDetails = new ArrayList<>();
		if(listOrder!=null) {
			for (Order order : listOrder) {
				Long orderId = order.getOrderId();
				List<OrderDetail> lists = orderDetailRepository.findByOrderId(orderId);
				listOrderDetails.addAll(lists);
			}
			List<Product> productList = new ArrayList<>();
			for (OrderDetail orderDetail : listOrderDetails) {
				Product product = orderDetail.getProduct();
				productList.add(product);
			}
			if (productList != null) {
				ArrayList<Integer> listIdProductArrayList = new ArrayList<>();
				for (int i = 0; i < productList.size(); i++) {
					String id = String.valueOf(productList.get(i).getProductId());
					listIdProductArrayList.add(Integer.valueOf(id));
				}
				List<Product> listProducts = productRepository.findByInventoryIds(listIdProductArrayList);

				List<Product> listProductNew = new ArrayList<>();

				for (Product product : listProducts) {

					Product productEntity = new Product();

					BeanUtils.copyProperties(product, productEntity);

					Favorite save = favoriteRepository.selectSaves(productEntity.getProductId(), user.getUserId());

					if (save != null) {
						productEntity.favorite = true;
					} else {
						productEntity.favorite = false;
					}
					listProductNew.add(productEntity);

				}

				model.addAttribute("recommendProduct", listProductNew);
			}
		}
	}
	
	// Top 20 best sale.
	public void bestSaleProduct20(Model model, User customer) {
		List<Object[]> productList = productRepository.bestSaleProduct20();
		if (productList != null) {
			ArrayList<Integer> listIdProductArrayList = new ArrayList<>();
			for (int i = 0; i < productList.size(); i++) {
				String id = String.valueOf(productList.get(i)[0]);
				listIdProductArrayList.add(Integer.valueOf(id));
			}
			List<Product> listProducts = productRepository.findByInventoryIds(listIdProductArrayList);

			List<Product> listProductNew = new ArrayList<>();

			for (Product product : listProducts) {

				Product productEntity = new Product();

				BeanUtils.copyProperties(product, productEntity);

				Favorite save = favoriteRepository.selectSaves(productEntity.getProductId(), customer.getUserId());

				if (save != null) {
					productEntity.favorite = true;
				} else {
					productEntity.favorite = false;
				}
				listProductNew.add(productEntity);

			}

			model.addAttribute("bestSaleProduct20", listProductNew);
		}
	}

	// Top 10 best discount.
		public void bestDiscountProduct(Model model, User customer) {
			List<Product> productList = productRepository.listProductByDiscount();
			if (productList != null) {
				ArrayList<Integer> listIdProductArrayList = new ArrayList<>();
				for (int i = 0; i < productList.size(); i++) {
					String id = String.valueOf(productList.get(i).getProductId());
					listIdProductArrayList.add(Integer.valueOf(id));
				}
				List<Product> listProducts = productRepository.findByInventoryIds(listIdProductArrayList);

				List<Product> listProductNew = new ArrayList<>();

				for (Product product : listProducts) {

					Product productEntity = new Product();

					BeanUtils.copyProperties(product, productEntity);

					Favorite save = favoriteRepository.selectSaves(productEntity.getProductId(), customer.getUserId());

					if (save != null) {
						productEntity.favorite = true;
					} else {
						productEntity.favorite = false;
					}
					listProductNew.add(productEntity);

				}

				model.addAttribute("bestDiscountProduct", listProductNew);
			}
		}

}
