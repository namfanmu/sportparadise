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
	@Query(value = "delete from sportparadise_shop.diary", nativeQuery = true)
	public void delDiary();

}
