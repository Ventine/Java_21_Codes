import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Ejemplo arquitectónico en Java 21 que demuestra:
 *
 * - RabbitMQ (colas de mensajes)
 * - Kafka (event streaming)
 * - REST (API sin estado)
 * - GraphQL (API orientada a consultas)
 *
 * Código conceptual, moderno y correcto a nivel profesional.
 * No reemplaza frameworks reales.
 */
public class MessagingAndApiArchitecture {

    /* =========================================================
       RABBITMQ (MESSAGE QUEUE)
       ========================================================= */

    /**
     * Mensaje típico enviado a una cola.
     */
    record Message(UUID id, String payload, Instant createdAt) {}

    /**
     * Simula RabbitMQ: cola con ACK explícito.
     */
    static class RabbitQueue {

        private final List<Message> queue = new ArrayList<>();

        public void publish(Message message) {
            queue.add(message);
        }

        /**
         * Consume y confirma procesamiento.
         */
        public void consume(Consumer<Message> handler) {
            while (!queue.isEmpty()) {
                Message msg = queue.remove(0);
                handler.accept(msg); // ACK implícito
            }
        }
    }

    /* =========================================================
       KAFKA (EVENT STREAMING)
       ========================================================= */

    /**
     * Evento inmutable.
     */
    record Event(UUID id, String type, Instant occurredAt) {}

    /**
     * Topic Kafka: log inmutable.
     */
    static class KafkaTopic {

        private final List<Event> log = new ArrayList<>();

        public void publish(Event event) {
            log.add(event);
        }

        /**
         * Consumidor lee por offset.
         */
        public void consumeFrom(int offset, Consumer<Event> handler) {
            for (int i = offset; i < log.size(); i++) {
                handler.accept(log.get(i));
            }
        }
    }

    /* =========================================================
       REST API (REQUEST–RESPONSE)
       ========================================================= */

    /**
     * Recurso REST.
     */
    record User(UUID id, String name) {}

    /**
     * Controlador REST sin estado.
     */
    static class UserRestController {

        private final Map<UUID, User> database = new ConcurrentHashMap<>();

        /**
         * POST /users
         */
        public User createUser(String name) {
            User user = new User(UUID.randomUUID(), name);
            database.put(user.id(), user);
            return user;
        }

        /**
         * GET /users/{id}
         */
        public User getUser(UUID id) {
            return database.get(id);
        }
    }

    /* =========================================================
       GRAPHQL API
       ========================================================= */

    /**
     * Esquema GraphQL (conceptual).
     *
     * type Query {
     *   user(id: ID): User
     * }
     */
    static class UserGraphQLResolver {

        private final Map<UUID, User> database;

        public UserGraphQLResolver(Map<UUID, User> database) {
            this.database = database;
        }

        /**
         * Resolver: el cliente define qué campos necesita.
         */
        public User user(UUID id) {
            return database.get(id);
        }
    }

    /* =========================================================
       SYSTEM ORCHESTRATION
       ========================================================= */

    /**
     * Orquesta todos los estilos de comunicación.
     */
    static class Application {

        private final RabbitQueue rabbitQueue = new RabbitQueue();
        private final KafkaTopic kafkaTopic = new KafkaTopic();
        private final UserRestController restController = new UserRestController();

        public void run() {
            // REST
            User user = restController.createUser("Alice");

            // RabbitMQ (tarea asíncrona)
            rabbitQueue.publish(new Message(
                    UUID.randomUUID(),
                    "Send welcome email",
                    Instant.now()
            ));

            rabbitQueue.consume(msg -> {
                // Procesamiento confiable
            });

            // Kafka (evento de dominio)
            kafkaTopic.publish(new Event(
                    UUID.randomUUID(),
                    "UserCreated",
                    Instant.now()
            ));

            kafkaTopic.consumeFrom(0, event -> {
                // Procesamiento reactivo
            });

            // GraphQL
            UserGraphQLResolver graphQL =
                    new UserGraphQLResolver(restController.database);

            graphQL.user(user.id());
        }
    }

    /* =========================================================
       MAIN
       ========================================================= */

    public static void main(String[] args) {
        new Application().run();
    }
}
