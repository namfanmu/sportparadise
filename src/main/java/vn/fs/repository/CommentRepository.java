package vn.fs.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import vn.fs.entities.Comment;


@Repository
public interface CommentRepository extends JpaRepository<Comment, Long>{

	// List comment by product_id
		@Query(value = "SELECT * FROM comments WHERE product_id = ?", nativeQuery = true)
		public List<Comment> listCommentByProductId(Long productId);
		
		@Query(value = "SELECT * FROM comments", nativeQuery = true)
		public List<Comment> getAllComment();
		
		// Get Average rating
		@Query(value = "SELECT AVG(rating) FROM sportparadise_shop.comments\r\n"
				+ "where product_id = ?", nativeQuery = true)
		public double getAvgRating(Long productId);
		

}
