package vn.fs.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import vn.fs.entities.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
	
	// update category
			@Transactional
			@Modifying
			@Query(value = "update sportparadise_shop.categories\r\n"
					+ "set category_name = ?,\r\n"
					+ "size_id = ?\r\n"
					+ "where category_id = ?", nativeQuery = true)
			public void update(String name, String size, Long catId);


}
