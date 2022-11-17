package vn.fs.controller;

import java.util.Collection;
import java.util.Date;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;

import vn.fs.commom.CommomDataService;
import vn.fs.config.PaypalPaymentIntent;
import vn.fs.config.PaypalPaymentMethod;
import vn.fs.entities.CartItem;
import vn.fs.entities.Diary;
import vn.fs.entities.Order;
import vn.fs.entities.OrderDetail;
import vn.fs.entities.Product;
import vn.fs.entities.User;
import vn.fs.repository.DiaryRepository;
import vn.fs.repository.OrderDetailRepository;
import vn.fs.repository.OrderRepository;
import vn.fs.repository.ProductRepository;
import vn.fs.service.PaypalService;
import vn.fs.service.ShoppingCartService;
import vn.fs.util.Utils;

@Controller
public class CartController extends CommomController {

	@Autowired
	HttpSession session;

	@Autowired
	CommomDataService commomDataService;

	@Autowired
	ShoppingCartService shoppingCartService;

	@Autowired
	private PaypalService paypalService;

	@Autowired
	OrderRepository orderRepository;
	
	@Autowired
	ProductRepository productRepository;

	@Autowired
	OrderDetailRepository orderDetailRepository;
	
	@Autowired
	DiaryRepository diaryRepository;

	public Order orderFinal = new Order();

	public static final String URL_PAYPAL_SUCCESS = "pay/success";
	public static final String URL_PAYPAL_CANCEL = "pay/cancel";
	private Logger log = LoggerFactory.getLogger(getClass());

	@GetMapping(value = "/shoppingCart_checkout")
	public String shoppingCart(Model model) {

		Collection<CartItem> cartItems = shoppingCartService.getCartItems();
		model.addAttribute("cartItems", cartItems);
		model.addAttribute("total", shoppingCartService.getAmount());
		double totalPrice = 0;
		for (CartItem cartItem : cartItems) {
			double price = cartItem.getQuantity() * cartItem.getProduct().getPrice();
			totalPrice += price - (price * cartItem.getProduct().getDiscount() / 100);
		}

		model.addAttribute("totalPrice", totalPrice);
		model.addAttribute("totalCartItems", shoppingCartService.getCount());

		return "web/shoppingCart_checkout";
	}
	
	//Add to cart
	@RequestMapping(value="/addToCart", method=RequestMethod.POST, params="action=ADD")
	public String add(@RequestParam("productId") Long productId, HttpServletRequest request, Model model) {

		Product product = productRepository.findById(productId).orElse(null);
		String quantity = request.getParameter("quantity");
		String size = request.getParameter("size");
		session = request.getSession();
		Collection<CartItem> cartItems = shoppingCartService.getCartItems();
		if (product != null) {
			CartItem item = new CartItem();
			BeanUtils.copyProperties(product, item);
			item.setQuantity(Integer.parseInt(quantity));
			item.setSize(size);
			item.setProduct(product);
			item.setId(productId);
			shoppingCartService.add(item);
		}
		session.setAttribute("cartItems", cartItems);
		model.addAttribute("totalCartItems", shoppingCartService.getCount());
		return "redirect:/products";
	}
	
	// direct checkout
	@RequestMapping(value="/addToCart", method=RequestMethod.POST, params="action=CHECKOUT")
	public String directCheckout(@RequestParam("productId") Long productId, HttpServletRequest request, Model model, User user) {
		Product product = productRepository.findById(productId).orElse(null);
		String quantity = request.getParameter("quantity");
		String size = request.getParameter("size");
		Order order = new Order();
		session = request.getSession();
		if (product != null) {
			CartItem item = new CartItem();
			BeanUtils.copyProperties(product, item);
			item.setQuantity(Integer.parseInt(quantity));
			item.setSize(size);
			item.setProduct(product);
			item.setId(productId);
			shoppingCartService.add(item);
		}
		Collection<CartItem> cartItems = shoppingCartService.getCartItems();
		session.setAttribute("cartItems", cartItems);
		model.addAttribute("totalCartItems", shoppingCartService.getCount());
		model.addAttribute("order", order);
		model.addAttribute("cartItems", cartItems);
		model.addAttribute("total", shoppingCartService.getAmount());
		model.addAttribute("NoOfItems", shoppingCartService.getCount());
		double totalPrice = 0;
		for (CartItem cartItem : cartItems) {
			double price = cartItem.getQuantity() * cartItem.getProduct().getPrice();
			totalPrice += price - (price * cartItem.getProduct().getDiscount() / 100);
		}
		model.addAttribute("totalPrice", totalPrice);
		commomDataService.commonData(model, user);
		commomDataService.commonData(model, user);
		return "web/direct_checkout";
	}


	// delete cartItem
	@SuppressWarnings("unlikely-arg-type")
	@GetMapping(value = "/remove/{id}")
	public String remove(@PathVariable("id") Long id, HttpServletRequest request, Model model) {
		Product product = productRepository.findById(id).orElse(null);

		Collection<CartItem> cartItems = shoppingCartService.getCartItems();
		session = request.getSession();
		if (product != null) {
			CartItem item = new CartItem();
			BeanUtils.copyProperties(product, item);
			item.setProduct(product);
			item.setId(id);
			cartItems.remove(session);
			shoppingCartService.remove(item);
		}
		model.addAttribute("totalCartItems", shoppingCartService.getCount());
		return "redirect:/checkout";
	}

	// show check out
	@GetMapping(value = "/checkout")
	public String checkOut(Model model, User user) {

		Order order = new Order();
		model.addAttribute("order", order);

		Collection<CartItem> cartItems = shoppingCartService.getCartItems();
		model.addAttribute("cartItems", cartItems);
		model.addAttribute("total", shoppingCartService.getAmount());
		model.addAttribute("NoOfItems", shoppingCartService.getCount());
		double totalPrice = 0;
		for (CartItem cartItem : cartItems) {
			double price = cartItem.getQuantity() * cartItem.getProduct().getPrice();
			totalPrice += price - (price * cartItem.getProduct().getDiscount() / 100);
		}

		model.addAttribute("totalPrice", totalPrice);
		commomDataService.commonData(model, user);

		return "web/shoppingCart_checkout";
	}

	// submit checkout
	@PostMapping(value = "/checkout")
	@Transactional
	public String checkedOut(Model model, Order order, HttpServletRequest request, User user)
			throws MessagingException {

		String checkOut = request.getParameter("checkOut");

		Collection<CartItem> cartItems = shoppingCartService.getCartItems();

		double totalPrice = 0;
		for (CartItem cartItem : cartItems) {
			double price = cartItem.getQuantity() * cartItem.getProduct().getPrice();
			totalPrice += price - (price * cartItem.getProduct().getDiscount() / 100);
		}

		BeanUtils.copyProperties(order, orderFinal);
		if (StringUtils.equals(checkOut, "paypal")) {

			String cancelUrl = Utils.getBaseURL(request) + "/" + URL_PAYPAL_CANCEL;
			String successUrl = Utils.getBaseURL(request) + "/" + URL_PAYPAL_SUCCESS;
			try {
				totalPrice = totalPrice / 24865;
				Payment payment = paypalService.createPayment(totalPrice, "USD", PaypalPaymentMethod.paypal,
						PaypalPaymentIntent.sale, "payment description", cancelUrl, successUrl);
				for (Links links : payment.getLinks()) {
					if (links.getRel().equals("approval_url")) {
						return "redirect:" + links.getHref();
					}
				}
			} catch (PayPalRESTException e) {
				log.error(e.getMessage());
			}

		}

		session = request.getSession();
		Date date = new Date();
		order.setOrderDate(date);
		order.setStatus(0);
		order.getOrderId();
		order.setAmount(totalPrice);
		order.setUser(user);

		orderRepository.save(order);

		for (CartItem cartItem : cartItems) {
			OrderDetail orderDetail = new OrderDetail();
			orderDetail.setQuantity(cartItem.getQuantity());
			orderDetail.setSize(cartItem.getSize());
			orderDetail.setOrder(order);
			orderDetail.setProduct(cartItem.getProduct());
			double unitPrice = cartItem.getProduct().getPrice();
			orderDetail.setPrice(unitPrice);
			// Cap nhat lai so luong san pham
			int updatedQuantity = cartItem.getProduct().getQuantity() - cartItem.getQuantity(); // so luong ban dau - so luong trong gio hang
			Product product = cartItem.getProduct();
			product.setQuantity(updatedQuantity);
			productRepository.save(product);
			orderDetailRepository.save(orderDetail);
		}

		// sendMail
		commomDataService.sendSimpleEmail(user.getEmail(), "SportParadise-Shop Xác Nhận Đơn hàng", "aaaa", cartItems,
				totalPrice, order);

		shoppingCartService.clear();
		session.removeAttribute("cartItems");
		model.addAttribute("orderId", order.getOrderId());
		Diary diary = new Diary();
		diary.setOrder(order);
		diaryRepository.save(diary);
		return "redirect:/checkout_success";
	}

	// paypal
	@GetMapping(URL_PAYPAL_SUCCESS)
	public String successPay(@RequestParam("" + "" + "") String paymentId, @RequestParam("PayerID") String payerId,
			HttpServletRequest request, User user, Model model) throws MessagingException {
		Collection<CartItem> cartItems = shoppingCartService.getCartItems();
		model.addAttribute("cartItems", cartItems);
		model.addAttribute("total", shoppingCartService.getAmount());

		double totalPrice = 0;
		for (CartItem cartItem : cartItems) {
			double price = cartItem.getQuantity() * cartItem.getProduct().getPrice();
			totalPrice += price - (price * cartItem.getProduct().getDiscount() / 100);
		}
		model.addAttribute("totalPrice", totalPrice);
		model.addAttribute("totalCartItems", shoppingCartService.getCount());

		try {
			Payment payment = paypalService.executePayment(paymentId, payerId);
			if (payment.getState().equals("approved")) {

				session = request.getSession();
				Date date = new Date();
				orderFinal.setOrderDate(date);
				orderFinal.setStatus(2);
				orderFinal.getOrderId();
				orderFinal.setUser(user);
				orderFinal.setAmount(totalPrice);
				orderRepository.save(orderFinal);

				for (CartItem cartItem : cartItems) {
					OrderDetail orderDetail = new OrderDetail();
					orderDetail.setQuantity(cartItem.getQuantity());
					orderDetail.setSize(cartItem.getSize());
					orderDetail.setOrder(orderFinal);
					orderDetail.setProduct(cartItem.getProduct());
					double unitPrice = cartItem.getProduct().getPrice();
					orderDetail.setPrice(unitPrice);
					int updatedQuantity = cartItem.getProduct().getQuantity() - cartItem.getQuantity(); // so luong ban dau - so luong trong gio hang
					Product product = cartItem.getProduct();
					product.setQuantity(updatedQuantity);
					productRepository.save(product);
					orderDetailRepository.save(orderDetail);
				}

				// sendMail
				commomDataService.sendSimpleEmail(user.getEmail(), "SportParadise-Shop Xác Nhận Đơn hàng", "aaaa", cartItems,
						totalPrice, orderFinal);

				shoppingCartService.clear();
				session.removeAttribute("cartItems");
				model.addAttribute("orderId", orderFinal.getOrderId());
				Diary diary = new Diary();
				diary.setOrder(orderFinal);
				diaryRepository.save(diary);
				orderFinal = new Order();
				return "redirect:/checkout_paypal_success";
			}
		} catch (PayPalRESTException e) {
			log.error(e.getMessage());
		}
		
		return "redirect:/";
	}

	// done checkout ship cod
	@GetMapping(value = "/checkout_success")
	public String checkoutSuccess(Model model, User user) {
		commomDataService.commonData(model, user);

		return "web/checkout_success";

	}

	// done checkout paypal
	@GetMapping(value = "/checkout_paypal_success")
	public String paypalSuccess(Model model, User user) {
		commomDataService.commonData(model, user);

		return "web/checkout_paypal_success";

	}

}
