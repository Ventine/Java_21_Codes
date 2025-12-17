import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Ejemplo moderno en Java 21 que integra conceptos de:
 *
 * - OWASP (seguridad backend)
 * - Mitigación de ataques
 * - Observabilidad (logs, métricas, trazas)
 * - DOM (concepto frontend modelado desde backend)
 *
 * Código conceptual, enfocado en arquitectura y buenas prácticas reales.
 * No sustituye frameworks de seguridad u observabilidad.
 */
public class SecurityAndObservabilityExample {

    /* =========================================================
       OWASP + MITIGACIÓN DE ATAQUES
       ========================================================= */

    /**
     * Servicio de validación de entrada.
     * Mitiga inyecciones (OWASP A03).
     */
    static class InputValidator {

        private static final Pattern SAFE_TEXT =
                Pattern.compile("^[a-zA-Z0-9 _-]+$");

        /**
         * Valida entrada de usuario.
         */
        public static void validate(String input) {
            if (input == null || !SAFE_TEXT.matcher(input).matches()) {
                throw new SecurityException("Entrada inválida");
            }
        }
    }

    /**
     * Control de rate limiting básico.
     * Mitiga brute force y DoS.
     */
    static class RateLimiter {

        private final Map<String, Integer> requests = new ConcurrentHashMap<>();

        public void check(String clientId) {
            int count = requests.merge(clientId, 1, Integer::sum);
            if (count > 5) {
                throw new SecurityException("Rate limit excedido");
            }
        }
    }

    /* =========================================================
       OBSERVABILIDAD
       ========================================================= */

    /**
     * Log estructurado.
     */
    record LogEntry(
            UUID traceId,
            String message,
            Instant timestamp
    ) {}

    /**
     * Métrica simple.
     */
    record Metric(
            String name,
            long value
    ) {}

    /**
     * Sistema de observabilidad.
     */
    static class Observability {

        private final Map<String, Long> metrics = new ConcurrentHashMap<>();

        public void log(LogEntry entry) {
            // En producción iría a ELK / Loki
        }

        public void incrementMetric(String name) {
            metrics.merge(name, 1L, Long::sum);
        }

        public long getMetric(String name) {
            return metrics.getOrDefault(name, 0L);
        }
    }

    /* =========================================================
       DOM (FRONTEND CONCEPTUAL)
       ========================================================= */

    /**
     * Nodo del DOM.
     */
    sealed interface DomNode permits ElementNode, TextNode {}

    /**
     * Elemento HTML.
     */
    record ElementNode(
            String tag,
            List<DomNode> children
    ) implements DomNode {}

    /**
     * Texto dentro del DOM.
     */
    record TextNode(String text) implements DomNode {}

    /**
     * Renderizador DOM seguro.
     * Evita XSS al escapar contenido.
     */
    static class DomRenderer {

        /**
         * Renderiza el DOM a HTML.
         */
        public String render(DomNode node) {
            return switch (node) {
                case TextNode t -> escape(t.text());
                case ElementNode e -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append("<").append(e.tag()).append(">");
                    for (DomNode child : e.children()) {
                        sb.append(render(child));
                    }
                    sb.append("</").append(e.tag()).append(">");
                    yield sb.toString();
                }
            };
        }

        /**
         * Sanitización básica contra XSS.
         */
        private String escape(String text) {
            return text
                    .replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;");
        }
    }

    /* =========================================================
       APLICACIÓN
       ========================================================= */

    /**
     * Simula flujo completo backend + frontend.
     */
    static class Application {

        private final RateLimiter rateLimiter = new RateLimiter();
        private final Observability observability = new Observability();
        private final DomRenderer domRenderer = new DomRenderer();

        public void handleRequest(String clientId, String userInput) {
            UUID traceId = UUID.randomUUID();

            // Observabilidad: trace
            observability.log(new LogEntry(
                    traceId,
                    "Request recibido",
                    Instant.now()
            ));

            // Seguridad
            rateLimiter.check(clientId);
            InputValidator.validate(userInput);

            observability.incrementMetric("requests_ok");

            // DOM seguro
            DomNode dom = new ElementNode(
                    "div",
                    List.of(
                            new TextNode("Hola "),
                            new TextNode(userInput)
                    )
            );

            String html = domRenderer.render(dom);
        }
    }

    /* =========================================================
       MAIN
       ========================================================= */

    public static void main(String[] args) {
        Application app = new Application();
        app.handleRequest("client-1", "UsuarioSeguro");
    }
}
