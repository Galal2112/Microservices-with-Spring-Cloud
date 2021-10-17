package htwb.ai.authservice.repository;

import htwb.ai.authservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    @Query("select u from User u where u.authToken = :authToken")
    Optional<User> findUserByAuthToken(String authToken);
}
