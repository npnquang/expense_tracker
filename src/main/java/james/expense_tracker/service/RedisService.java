package james.expense_tracker.service;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {
    private final StringRedisTemplate redis;

    public RedisService(
            StringRedisTemplate redis, @Value("${jwt.refresh-expiration-ms}") Long ttl) {
        this.redis = redis;
    }

    public void save(Long userId, String jti, String tokenHash, Duration ttl) {
        redis.opsForValue().set((key(userId, jti)), tokenHash, ttl);
    }

    public String key(Long userId, String jti) {
        return String.format("rt:%d:%s", userId, jti);
    }

    public boolean consume(Long userId, String jti, String tokenHash) {
        String key = key(userId, jti);
        String storedHash = redis.opsForValue().get(key);
        if (storedHash == null || !storedHash.equals(tokenHash)) {
            return false;
        }
        Boolean deleted = redis.delete(key);
        return Boolean.TRUE.equals(deleted);
    }

    public void revoke(Long userId, String jti) {
        redis.delete(key(userId, jti));
    }
}
