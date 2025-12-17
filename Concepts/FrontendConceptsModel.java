import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Ejemplo conceptual en Java 21 que modela principios de frontend:
 *
 * - Eventos
 * - Ciclo de render
 * - HTML semántico
 * - Flexbox
 *
 * El objetivo es explicar el comportamiento interno del navegador
 * desde una perspectiva backend/arquitectónica.
 * No es un framework UI ni reemplaza HTML/CSS reales.
 */
public class FrontendConceptsModel {

    /* =========================================================
       EVENTOS
       ========================================================= */

    /**
     * Evento base del sistema UI.
     */
    sealed interface UiEvent permits ClickEvent {
        UUID id();
        Instant occurredAt();
    }

    /**
     * Evento de clic.
     */
    record ClickEvent(
            UUID id,
            String targetId,
            Instant occurredAt
    ) implements UiEvent {}

    /**
     * Dispatcher de eventos.
     * Simula bubbling y ejecución de handlers.
     */
    static class EventDispatcher {

        private final List<Consumer<UiEvent>> listeners = new ArrayList<>();

        public void addListener(Consumer<UiEvent> listener) {
            listeners.add(listener);
        }

        /**
         * Dispara evento (bubbling simplificado).
         */
        public void dispatch(UiEvent event) {
            for (Consumer<UiEvent> listener : listeners) {
                listener.accept(event);
            }
        }
    }

    /* =========================================================
       HTML SEMÁNTICO (MODELO)
       ========================================================= */

    /**
     * Nodo semántico HTML.
     */
    sealed interface HtmlNode permits HtmlElement, HtmlText {}

    /**
     * Elemento HTML semántico.
     */
    record HtmlElement(
            String tag,
            List<HtmlNode> children
    ) implements HtmlNode {}

    /**
     * Texto HTML.
     */
    record HtmlText(String value) implements HtmlNode {}

    /* =========================================================
       FLEXBOX (LAYOUT MODEL)
       ========================================================= */

    /**
     * Contenedor Flexbox.
     */
    static class FlexContainer {

        enum Direction { ROW, COLUMN }
        enum Justify { START, CENTER, SPACE_BETWEEN }

        private final Direction direction;
        private final Justify justify;
        private final List<FlexItem> items = new ArrayList<>();

        public FlexContainer(Direction direction, Justify justify) {
            this.direction = direction;
            this.justify = justify;
        }

        public void addItem(FlexItem item) {
            items.add(item);
        }

        /**
         * Calcula layout (conceptual).
         */
        public void layout() {
            // En un navegador real esto ocurre en la fase de layout/reflow
        }
    }

    /**
     * Item dentro de Flexbox.
     */
    record FlexItem(
            String id,
            int flexGrow
    ) {}

    /* =========================================================
       CICLO DE RENDER
       ========================================================= */

    /**
     * Render engine conceptual del navegador.
     */
    static class RenderEngine {

        /**
         * Ejecuta el ciclo de render.
         */
        public void render(HtmlNode dom) {
            parseDom(dom);
            buildRenderTree();
            layout();
            paint();
            composite();
        }

        private void parseDom(HtmlNode dom) {
            // DOM ya construido
        }

        private void buildRenderTree() {
            // DOM + CSSOM
        }

        private void layout() {
            // Reflow: cálculo de posiciones
        }

        private void paint() {
            // Pintado de píxeles
        }

        private void composite() {
            // Composición final en GPU
        }
    }

    /* =========================================================
       APLICACIÓN
       ========================================================= */

    /**
     * Simula una aplicación frontend.
     */
    static class FrontendApp {

        private final EventDispatcher dispatcher = new EventDispatcher();
        private final RenderEngine renderer = new RenderEngine();

        public FrontendApp() {
            dispatcher.addListener(event -> {
                // Handler liviano: evita bloquear render
            });
        }

        public void run() {
            // HTML semántico
            HtmlNode dom = new HtmlElement(
                    "main",
                    List.of(
                            new HtmlElement(
                                    "section",
                                    List.of(
                                            new HtmlText("Contenido principal")
                                    )
                            )
                    )
            );

            // Flexbox
            FlexContainer flex = new FlexContainer(
                    FlexContainer.Direction.ROW,
                    FlexContainer.Justify.SPACE_BETWEEN
            );

            flex.addItem(new FlexItem("item-1", 1));
            flex.addItem(new FlexItem("item-2", 2));
            flex.layout();

            // Render inicial
            renderer.render(dom);

            // Evento
            dispatcher.dispatch(new ClickEvent(
                    UUID.randomUUID(),
                    "item-1",
                    Instant.now()
            ));
        }
    }

    /* =========================================================
       MAIN
       ========================================================= */

    public static void main(String[] args) {
        new FrontendApp().run();
    }
}
