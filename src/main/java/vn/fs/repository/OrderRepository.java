package vn.fs.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import vn.fs.entities.Order;


@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

	@Query(value = "select * from orders where user_id = ?1", nativeQuery = true)
	List<Order> findOrderByUserId(Long id);
	
	@Query(value = "select * from orders where order_id = ?1", nativeQuery = true)
	Order findOrderById(Long id);
	
	@Query(value = "SELECT * FROM sportparadise_shop.orders\r\n"
			+ "	order by order_id desc", nativeQuery = true)
	List<Order> findAllByIdDESC();

}
