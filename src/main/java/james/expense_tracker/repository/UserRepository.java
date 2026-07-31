package james.expense_tracker.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import james.expense_tracker.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndIdNot(String email, Long id);

    Optional<User> findByUsernameAndIdNot(String username, Long id);
}
