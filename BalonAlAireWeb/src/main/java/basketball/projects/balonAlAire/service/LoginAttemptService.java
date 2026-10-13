package basketball.projects.balonAlAire.service;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * Límite de intentos en memoria (por instancia) para el login del CMS y el pedido de recuperación de contraseña.
 * Cada contador vive WINDOW desde su primer intento: al superar el máximo, el bloqueo dura hasta que expira.
 */
@Service
public class LoginAttemptService {

    public static final Duration WINDOW = Duration.ofMinutes(15);
    static final int MAX_FAILURES_PER_USER = 5;
    static final int MAX_FAILURES_PER_IP = 20;
    static final int MAX_RESET_REQUESTS_PER_IP = 5;

    private final Cache<String, AtomicInteger> loginFailures = newCache();
    private final Cache<String, AtomicInteger> resetRequests = newCache();

    private static Cache<String, AtomicInteger> newCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(WINDOW)
                .maximumSize(50_000)
                .build();
    }

    public boolean isLoginBlocked(String ip, String username) {
        return count(loginFailures, ipKey(ip)) >= MAX_FAILURES_PER_IP
                || count(loginFailures, userKey(username)) >= MAX_FAILURES_PER_USER;
    }

    public void recordLoginFailure(String ip, String username) {
        increment(loginFailures, ipKey(ip));
        increment(loginFailures, userKey(username));
    }

    public void recordLoginSuccess(String username) {
        loginFailures.invalidate(userKey(username));
    }

    /** Cuenta el pedido y devuelve false si esa IP ya superó el máximo en la ventana. */
    public boolean allowPasswordResetRequest(String ip) {
        return increment(resetRequests, ipKey(ip)) <= MAX_RESET_REQUESTS_PER_IP;
    }

    private static int count(Cache<String, AtomicInteger> cache, String key) {
        AtomicInteger counter = cache.getIfPresent(key);
        return counter == null ? 0 : counter.get();
    }

    private static int increment(Cache<String, AtomicInteger> cache, String key) {
        return cache.get(key, k -> new AtomicInteger()).incrementAndGet();
    }

    private static String ipKey(String ip) {
        return "ip:" + ip;
    }

    private static String userKey(String username) {
        return "user:" + (username == null ? "" : username.trim().toLowerCase());
    }
}
