package james.expense_tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import james.expense_tracker.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

    User findByEmail(String email);

    User findByEmailAndIdNot(String email, Long id);

    User findByUsernameAndIdNot(String username, Long id);
}
