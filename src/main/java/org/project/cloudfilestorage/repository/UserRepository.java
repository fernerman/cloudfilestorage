package org.project.cloudfilestorage.repository;

import java.util.Optional;
import org.project.cloudfilestorage.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {

  Optional<User> findByUsername(String username);

}
