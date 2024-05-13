package dev.fayzullokh.roombooking.repositories;

import dev.fayzullokh.roombooking.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("select c from users c where upper(c.username)=upper(?1)")
    Optional<User> findByUsername(String username);
}