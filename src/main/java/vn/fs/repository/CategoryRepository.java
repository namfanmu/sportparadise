package vn.fs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.fs.entities.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

}
