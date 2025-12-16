import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ejemplo moderno en Java 21 que explica y aplica:
 *
 * - Índices
 * - ACID
 * - Redis (cache en memoria)
 * - Estrategias de invalidación
 *
 * Código conceptual, arquitectónico y correcto a nivel profesional.
 * No usa drivers reales ni frameworks.
 */
public class BackendPersistenceAndCacheExample {

    /* =========================================================
       MODELO DE DATOS
       ========================================================= */

    /**
     * Entidad de dominio.
     */
    record User(
            UUID id,
            String email,
            String name,
            Instant createdAt
    ) {}

    /* =========================================================
       SQL DATABASE (ACID + ÍNDICES)
       ========================================================= */

    /**
     * Simula una base de datos SQL.
     * Cumple ACID a nivel conceptual.
     */
    static class SqlDatabase {

        /**
         * Tabla USERS (PK = id).
         */
        private final Map<UUID, User> table = new ConcurrentHashMap<>();

        /**
         * Índice secundario (email → id).
         * Evita full table scan.
         */
        private final Map<String, UUID> emailIndex = new ConcurrentHashMap<>();

        /**
         * Transacción ACID simplificada.
         */
        public synchronized void save(User user) {
            // Atomicity: todo o nada
            table.put(user.id(), user);
            emailIndex.put(user.email(), user.id());
            // Durability: asumida tras commit
        }

        /**
         * Búsqueda usando índice.
         */
        public Optional<User> findByEmail(String email) {
            UUID id = emailIndex.get(email);
            if (id == null) return Optional.empty();
            return Optional.ofNullable(table.get(id));
        }

        /**
         * Actualización consistente.
         */
        public synchronized void updateEmail(UUID id, String newEmail) {
            User user = table.get(id);
            if (user == null) return;

            emailIndex.remove(user.email());

            User updated = new User(
                    user.id(),
                    newEmail,
                    user.name(),
                    user.createdAt()
            );

            table.put(id, updated);
            emailIndex.put(newEmail, id);
        }
    }

    /* =========================================================
       REDIS (CACHE EN MEMORIA)
       ========================================================= */

    /**
     * Simula Redis como cache key-value.
     */
    static class RedisCache {

        /**
         * Cache en memoria.
         */
        private final Map<String, CachedValue> cache =
                new ConcurrentHashMap<>();

        /**
         * Almacena valor con TTL.
         */
        public void put(String key, Object value, long ttlSeconds) {
            cache.put(key, new CachedValue(
                    value,
                    Instant.now().plusSeconds(ttlSeconds)
            ));
        }

        /**
         * Obtiene valor si no expiró.
         */
        public Optional<Object> get(String key) {
            CachedValue cached = cache.get(key);
            if (cached == null) return Optional.empty();

            if (Instant.now().isAfter(cached.expiresAt())) {
                cache.remove(key);
                return Optional.empty();
            }
            return Optional.of(cached.value());
        }

        /**
         * Invalida entrada manualmente.
         */
        public void invalidate(String key) {
            cache.remove(key);
        }
    }

    /**
     * Valor cacheado con expiración (TTL).
     */
    record CachedValue(
            Object value,
            Instant expiresAt
    ) {}

    /* =========================================================
       CACHE-ASIDE STRATEGY
       ========================================================= */

    /**
     * Servicio de aplicación.
     * Implementa cache-aside + invalidación.
     */
    static class UserService {

        private final SqlDatabase database = new SqlDatabase();
        private final RedisCache cache = new RedisCache();

        /**
         * Guarda usuario (write-through manual).
         */
        public void createUser(User user) {
            database.save(user);

            // Invalida cache relacionada
            cache.invalidate("user:email:" + user.email());
        }

        /**
         * Lee usando cache-aside.
         */
        public Optional<User> getByEmail(String email) {
            String key = "user:email:" + email;

            // 1. Cache
            Optional<Object> cached = cache.get(key);
            if (cached.isPresent()) {
                return Optional.of((User) cached.get());
            }

            // 2. Database
            Optional<User> user = database.findByEmail(email);

            // 3. Cache population
            user.ifPresent(u ->
                    cache.put(key, u, 30)
            );

            return user;
        }

        /**
         * Actualiza email con invalidación explícita.
         */
        public void updateEmail(UUID id, String newEmail, String oldEmail) {
            database.updateEmail(id, newEmail);

            // Estrategia de invalidación
            cache.invalidate("user:email:" + oldEmail);
            cache.invalidate("user:email:" + newEmail);
        }
    }

    /* =========================================================
       MAIN
       ========================================================= */

    public static void main(String[] args) {
        UserService service = new UserService();

        User user = new User(
                UUID.randomUUID(),
                "alice@mail.com",
                "Alice",
                Instant.now()
        );

        service.createUser(user);
        service.getByEmail("alice@mail.com");
        service.updateEmail(user.id(), "alice@new.com", "alice@mail.com");
    }
}
