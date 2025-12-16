import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Ejemplo conceptual en Java 21 que demuestra:
 *
 * - Microservicios (servicios desacoplados)
 * - Arquitectura Event-driven
 * - Patrón CQRS
 *
 * No usa frameworks. Modela los conceptos a nivel estructural y mental.
 */
public class ModernBackendArchitecture {

    /* =========================================================
       EVENTOS (EVENT-DRIVEN)
       ========================================================= */

    /**
     * Evento base del sistema.
     */
    sealed interface Event permits OrderCreatedEvent {
        Instant occurredAt();
    }

    /**
     * Evento emitido cuando se crea una orden.
     */
    record OrderCreatedEvent(
            UUID orderId,
            String product,
            Instant occurredAt
    ) implements Event {}

    /**
     * Event Bus simple (simula Kafka / RabbitMQ).
     */
    static class EventBus {

        private final List<Consumer<Event>> subscribers = new ArrayList<>();

        public void subscribe(Consumer<Event> handler) {
            subscribers.add(handler);
        }

        public void publish(Event event) {
            subscribers.forEach(h -> h.accept(event));
        }
    }

    /* =========================================================
       CQRS – COMMAND SIDE (WRITE MODEL)
       ========================================================= */

    /**
     * Command: intención de cambiar el estado.
     */
    record CreateOrderCommand(String product) {}

    /**
     * Microservicio de escritura.
     * Responsable únicamente de comandos.
     */
    static class OrderCommandService {

        private final EventBus eventBus;

        public OrderCommandService(EventBus eventBus) {
            this.eventBus = eventBus;
        }

        /**
         * Maneja el comando y emite eventos.
         */
        public UUID handle(CreateOrderCommand command) {
            UUID orderId = UUID.randomUUID();

            // Regla de negocio (write model)
            OrderCreatedEvent event = new OrderCreatedEvent(
                    orderId,
                    command.product(),
                    Instant.now()
            );

            eventBus.publish(event);
            return orderId;
        }
    }

    /* =========================================================
       CQRS – QUERY SIDE (READ MODEL)
       ========================================================= */

    /**
     * Proyección de lectura optimizada para consultas.
     */
    static class OrderReadModel {

        private final Map<UUID, String> orders = new ConcurrentHashMap<>();

        /**
         * Actualiza el modelo de lectura a partir de eventos.
         */
        public void on(Event event) {
            if (event instanceof OrderCreatedEvent e) {
                orders.put(e.orderId(), e.product());
            }
        }

        /**
         * Consulta sin lógica de negocio.
         */
        public String findProductByOrderId(UUID orderId) {
            return orders.get(orderId);
        }
    }

    /**
     * Microservicio de consultas.
     */
    static class OrderQueryService {

        private final OrderReadModel readModel;

        public OrderQueryService(OrderReadModel readModel) {
            this.readModel = readModel;
        }

        public String getOrder(UUID orderId) {
            return readModel.findProductByOrderId(orderId);
        }
    }

    /* =========================================================
       MICROservices ORCHESTRATION
       ========================================================= */

    /**
     * Simula un sistema compuesto por microservicios independientes.
     */
    static class MicroserviceSystem {

        private final EventBus eventBus = new EventBus();
        private final OrderReadModel readModel = new OrderReadModel();

        private final OrderCommandService commandService =
                new OrderCommandService(eventBus);

        private final OrderQueryService queryService =
                new OrderQueryService(readModel);

        public MicroserviceSystem() {
            // Suscripción event-driven
            eventBus.subscribe(readModel::on);
        }

        public void run() {
            UUID orderId = commandService.handle(
                    new CreateOrderCommand("Laptop")
            );

            String product = queryService.getOrder(orderId);
        }
    }

    /* =========================================================
       MAIN
       ========================================================= */

    public static void main(String[] args) {
        new MicroserviceSystem().run();
    }
}
