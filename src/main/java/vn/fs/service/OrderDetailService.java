package vn.fs.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import vn.fs.entities.Order;
import vn.fs.entities.OrderDetail;
import vn.fs.repository.OrderDetailRepository;
import vn.fs.repository.OrderRepository;


@Service
public class OrderDetailService {

	@Autowired
	OrderRepository repo;
	
	@Autowired
	OrderDetailRepository orderDetailRepository;

	public List<Order> listAll() {
		return (List<Order>) repo.findAll();
	}
	
	public List<OrderDetail> listAllOrderDetail() {
		return (List<OrderDetail>) orderDetailRepository.findAll();
	}

}
