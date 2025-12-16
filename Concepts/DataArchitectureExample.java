import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ejemplo moderno en Java 21 que integra:
 *
 * - Event Sourcing
 * - Modelado de datos
 * - Persistencia estilo SQL
 * - Persistencia estilo NoSQL
 *
 * El código es conceptual y arquitectónico.
 * No usa frameworks ni bases reales.
 */
public class DataArchitectureExample {

    /* =========================================================
       EVENT SOURCING
       ========================================================= */

    /**
     * Evento base del dominio.
     * Inmutable por definición.
     */
    sealed interface Event permits UserCreatedEvent, UserEmailUpdatedEvent {
        Instant occurredAt();
    }

    /**
     * Evento: creación de usuario.
     */
    record UserCreatedEvent(
            UUID userId,
            String name,
            String email,
            Instant occurredAt
    ) implements Event {}

    /**
     * Evento: actualización de email.
     */
    record UserEmailUpdatedEvent(
            UUID userId,
            String newEmail,
            Instant occurredAt
    ) implements Event {}

    /**
     * Event Store.
     * Fuente única de la verdad.
     */
    static class EventStore {

        private final List<Event> events = new ArrayList<>();

        public void append(Event event) {
            events.add(event);
        }

        public List<Event> getAll() {
            return List.copyOf(events);
        }
    }

    /* =========================================================
       DOMAIN MODEL (RECONSTRUIDO DESDE EVENTOS)
       ========================================================= */

    /**
     * Agregado de dominio.
     * Su estado se deriva de eventos.
     */
    static class UserAggregate {

        private UUID id;
        private String name;
        private String email;

        /**
         * Reproduce eventos para reconstruir estado.
         */
        public void apply(Event event) {
            switch (event) {
                case UserCreatedEvent e -> {
                    this.id = e.userId();
                    this.name = e.name();
                    this.email = e.email();
                }
                case UserEmailUpdatedEvent e -> {
                    this.email = e.newEmail();
                }
            }
        }

        public UUID id() { return id; }
        public String name() { return name; }
        public String email() { return email; }
    }

    /* =========================================================
       SQL STYLE STORAGE (NORMALIZADO)
       ========================================================= */

    /**
     * Simula almacenamiento SQL.
     * Modelo normalizado y relacional.
     */
    static class SqlUserRepository {

        /**
         * Tabla USERS (user_id PK).
         */
        private final Map<UUID, SqlUserRow> table = new ConcurrentHashMap<>();

        public void save(UserAggregate user) {
            table.put(
                    user.id(),
                    new SqlUserRow(user.id(), user.name(), user.email())
            );
        }

        public SqlUserRow findById(UUID id) {
            return table.get(id);
        }
    }

    /**
     * Fila SQL.
     */
    record SqlUserRow(
            UUID userId,
            String name,
            String email
    ) {}

    /* =========================================================
       NOSQL STYLE STORAGE (DESNORMALIZADO)
       ========================================================= */

    /**
     * Simula almacenamiento NoSQL (documentos).
     */
    static class NoSqlUserRepository {

        /**
         * Documento completo por usuario.
         */
        private final Map<UUID, Map<String, Object>> collection =
                new ConcurrentHashMap<>();

        public void save(UserAggregate user) {
            collection.put(user.id(), Map.of(
                    "id", user.id().toString(),
                    "name", user.name(),
                    "email", user.email(),
                    "lastUpdated", Instant.now().toString()
            ));
        }

        public Map<String, Object> findById(UUID id) {
            return collection.get(id);
        }
    }

    /* =========================================================
       APPLICATION FLOW
       ========================================================= */

    /**
     * Orquesta el flujo completo:
     * eventos → agregado → SQL + NoSQL.
     */
    static class ApplicationService {

        private final EventStore eventStore = new EventStore();
        private final SqlUserRepository sqlRepo = new SqlUserRepository();
        private final NoSqlUserRepository noSqlRepo = new NoSqlUserRepository();

        public void run() {
            UUID userId = UUID.randomUUID();

            // Event sourcing
            eventStore.append(new UserCreatedEvent(
                    userId,
                    "Alice",
                    "alice@mail.com",
                    Instant.now()
            ));

            eventStore.append(new UserEmailUpdatedEvent(
                    userId,
                    "alice@newmail.com",
                    Instant.now()
            ));

            // Rebuild state
            UserAggregate user = new UserAggregate();
            eventStore.getAll().forEach(user::apply);

            // Persistencia según modelo
            sqlRepo.save(user);
            noSqlRepo.save(user);
        }
    }

    /* =========================================================
       MAIN
       ========================================================= */

    public static void main(String[] args) {
        new ApplicationService().run();
    }
}
