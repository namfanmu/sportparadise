package vn.fs.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import vn.fs.commom.CommomDataService;
import vn.fs.entities.Category;
import vn.fs.entities.Favorite;
import vn.fs.entities.Product;
import vn.fs.entities.User;
import vn.fs.repository.FavoriteRepository;
import vn.fs.repository.ProductRepository;

@Controller
public class ShopController extends CommomController {

	@Autowired
	ProductRepository productRepository;
	
	@Autowired
	FavoriteRepository favoriteRepository;
	
	@Autowired
	CommomDataService commomDataService;

	@GetMapping(value = "/products")
	public String shop(Model model, Pageable pageable, @RequestParam("page") Optional<Integer> page,
			@RequestParam("size") Optional<Integer> size, @RequestParam(name = "sort", required = false, defaultValue = "") String sort, User user) {

		int currentPage = page.orElse(1);
		int pageSize = size.orElse(12);
		Page<Product> productPage = findPaginated(PageRequest.of(currentPage - 1, pageSize), sort);
		

		int totalPages = productPage.getTotalPages();
		if (totalPages > 0) {
			List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages).boxed().collect(Collectors.toList());
			model.addAttribute("pageNumbers", pageNumbers);
		}

		commomDataService.commonData(model, user);
		model.addAttribute("products", productPage);
		if(sort.equals("ASC")) {
			model.addAttribute("sort", "ASC");
		} else if(sort.equals("DESC")) {
			model.addAttribute("sort", "DESC");
		}  else if(sort.equals("")) {
			model.addAttribute("sort", "");
		}
		return "web/shop";
	}

	public Page<Product> findPaginated(Pageable pageable, String sort) {
		List<Product> productPage = new ArrayList<>();;
		if (sort.equals("ASC")) {
			productPage = productRepository.findAll();
			productPage.sort(Comparator.comparing(Product::getPrice));
		    }
	    if (sort.equals("DESC")) {
	    	productPage = productRepository.findAll();
	    	productPage.sort(Comparator.comparing(Product::getPrice).reversed());
	    }
	    if (sort.equals("")) {
	    	productPage = productRepository.findAll();
	    	Collections.reverse(productPage);
	    }
		int pageSize = pageable.getPageSize();
		int currentPage = pageable.getPageNumber();
		int startItem = currentPage * pageSize;
		List<Product> list;

		if (productPage.size() < startItem) {
			list = Collections.emptyList();
		} else {
			int toIndex = Math.min(startItem + pageSize, productPage.size());
			list = productPage.subList(startItem, toIndex);
		}

		Page<Product> productPages = new PageImpl<Product>(list, PageRequest.of(currentPage, pageSize),productPage.size());

		return productPages;
	}
	
	// search product
	@GetMapping(value = "/searchProduct")
	public String showsearch(Model model, Pageable pageable, @RequestParam("keyword") String keyword,
			@RequestParam("size") Optional<Integer> size, @RequestParam("page") Optional<Integer> page,
			@RequestParam(name = "sort", required = false, defaultValue = "") String sort, User user) {
	
		int currentPage = page.orElse(1);
		int pageSize = size.orElse(12);

		Page<Product> productPage = findPaginatSearch(PageRequest.of(currentPage - 1, pageSize), keyword, sort);

		int totalPages = productPage.getTotalPages();
		if (totalPages > 0) {
			List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages).boxed().collect(Collectors.toList());
			model.addAttribute("pageNumbers", pageNumbers);
		}
		commomDataService.commonData(model, user);
		model.addAttribute("keyword", keyword);
		model.addAttribute("products", productPage);
		int entities = productPage.getNumberOfElements();
		model.addAttribute("entities", entities);
		return "web/searchProduct";
	}
	
	public Page<Product> findPaginatSearch(Pageable pageable, @RequestParam("keyword") String keyword, String sort) {
		List<Product> productPage = new ArrayList<>();;
		if (sort.equals("ASC")) {
			productPage = productRepository.searchProduct(keyword);
			productPage.sort(Comparator.comparing(Product::getPrice));
		    }
	    if (sort.equals("DESC")) {
	    	productPage = productRepository.searchProduct(keyword);
	    	productPage.sort(Comparator.comparing(Product::getPrice).reversed());
	    }
	    if (sort.equals("")) {
	    	productPage = productRepository.searchProduct(keyword);
	    	Collections.reverse(productPage);
	    }

		int pageSize = pageable.getPageSize();
		int currentPage = pageable.getPageNumber();
		int startItem = currentPage * pageSize;
		List<Product> list;

		if (productPage.size() < startItem) {
			list = Collections.emptyList();
		} else {
			int toIndex = Math.min(startItem + pageSize, productPage.size());
			list = productPage.subList(startItem, toIndex);
		}

		Page<Product> productPages = new PageImpl<Product>(list, PageRequest.of(currentPage, pageSize),productPage.size());

		return productPages;
	}
	
	// list products by category
	@GetMapping(value = "/productByCategory")
	public String listProductbyid(Model model, @RequestParam("id") Long id, @RequestParam(name = "sort", required = false, defaultValue = "") String sort, User user) {
		List<Product> products = new ArrayList<>();;
		
		if (sort.equals("ASC")) {
			products = productRepository.listProductByCategory(id);
			products.sort(Comparator.comparing(Product::getPrice));
		    }
	    if (sort.equals("DESC")) {
	    	products = productRepository.listProductByCategory(id);
	    	products.sort(Comparator.comparing(Product::getPrice).reversed());
	    }
	    if (sort.equals("")) {
	    	products = productRepository.listProductByCategory(id);
	    	Collections.reverse(products);
	    }

		List<Product> listProductNew = new ArrayList<>();

		for (Product product : products) {

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
		Category category = listProductNew.get(0).getCategory();
		model.addAttribute("products", listProductNew);
		model.addAttribute("category", category);
		commomDataService.commonData(model, user);
		return "web/productByCat";
	}

}
