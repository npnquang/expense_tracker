package james.expense_tracker;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
class ExpenseTrackerApplicationTests {
    @DynamicPropertySource
    static void jwtProps(DynamicPropertyRegistry registry) {
        byte[] key = new byte[32];
        new java.security.SecureRandom().nextBytes(key);
        registry.add("jwt.secret", () -> java.util.Base64.getEncoder().encodeToString(key));
        registry.add("jwt.access-expiration-ms", () -> "3600000");
        registry.add("jwt.refresh-expiration-ms", () -> "86400000");
    }

    @Test
    void contextLoads() {}
}
