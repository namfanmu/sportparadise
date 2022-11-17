package vn.fs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.fs.entities.User;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	User findByEmail(String email);

}
