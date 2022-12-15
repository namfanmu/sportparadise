package vn.fs.repository;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import vn.fs.entities.Diary;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {
	
	@Modifying
	@Transactional
	@Query(value = "delete FROM sportparadise_shop.diary\r\n"
			+ "where order_order_id = ?;", nativeQuery = true)
	public void delDiary(Long id);

}
