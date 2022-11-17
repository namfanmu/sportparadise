package vn.fs.service.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import vn.fs.entities.CartItem;
import vn.fs.entities.Product;
import vn.fs.service.ShoppingCartService;


@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

	private Map<Long, CartItem> map = new HashMap<Long, CartItem>();

	@Override
	public void add(CartItem item) {
		CartItem existedItem = map.get(item.getId());

		if (existedItem != null) {
			existedItem.setQuantity(item.getQuantity() + existedItem.getQuantity());
			existedItem.setTotalPrice(item.getTotalPrice() + existedItem.getUnitPrice() * existedItem.getQuantity());
		} else {
			map.put(item.getId(), item);
		}
	}

	@Override
	public void remove(CartItem item) {

		map.remove(item.getId());

	}

	@Override
	public Collection<CartItem> getCartItems() {
		return map.values();
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public double getAmount() {
		return map.values().stream().mapToDouble(item -> item.getQuantity() * item.getUnitPrice()).sum();
	}

	@Override
	public int getCount() {
		if (map.isEmpty()) {
			return 0;
		}

		return map.values().size();
	}

	@Override
	public void remove(Product product) {

	}
}
