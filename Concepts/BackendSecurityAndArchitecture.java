import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Ejemplo conceptual y moderno en Java 21 de:
 *
 * 1. JWT (JSON Web Token)
 * 2. OAuth2 (modelo de autorización)
 * 3. Monolito (arquitectura backend)
 *
 * El objetivo es explicar el modelo mental y técnico,
 * no reemplazar frameworks reales (Spring Security, Keycloak, etc.).
 */
public class BackendSecurityAndArchitecture {

    /* =========================================================
       JWT (JSON WEB TOKEN)
       ========================================================= */

    /**
     * Implementación educativa de un JWT firmado de forma simple.
     * NO usar en producción: solo demuestra estructura y flujo.
     */
    static class SimpleJWT {

        private static final String SECRET = "clave-secreta";

        /**
         * Genera un JWT con claims básicos.
         */
        public static String generateToken(String user, String role, long ttlSeconds) {
            String header = base64("""
                {"alg":"HS256","typ":"JWT"}
                """);

            long exp = Instant.now().getEpochSecond() + ttlSeconds;

            String payload = base64(String.format("""
                {"sub":"%s","role":"%s","exp":%d}
                """, user, role, exp));

            String signature = base64(header + "." + payload + SECRET);

            return header + "." + payload + "." + signature;
        }

        /**
         * Valida firma y expiración.
         */
        public static boolean validate(String token) {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return false;

            String expectedSig = base64(parts[0] + "." + parts[1] + SECRET);
            if (!expectedSig.equals(parts[2])) return false;

            String payloadJson =
                    new String(Base64.getDecoder().decode(parts[1]), StandardCharsets.UTF_8);

            long exp = Long.parseLong(payloadJson.replaceAll(".*\"exp\":(\\d+).*", "$1"));
            return Instant.now().getEpochSecond() < exp;
        }

        private static String base64(String s) {
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(s.getBytes(StandardCharsets.UTF_8));
        }
    }

    /* =========================================================
       OAUTH2 (MODELO)
       ========================================================= */

    /**
     * Representa el flujo Authorization Code de OAuth2.
     * No es red real, solo el contrato lógico.
     */
    static class OAuth2AuthorizationServer {

        private final Map<String, String> authCodes = new HashMap<>();

        /**
         * Usuario concede permiso → servidor emite auth code.
         */
        public String authorize(String clientId, String user) {
            String code = "code-" + user;
            authCodes.put(code, user);
            return code;
        }

        /**
         * Cliente intercambia auth code por access token.
         */
        public String token(String code) {
            String user = authCodes.remove(code);
            if (user == null) throw new IllegalStateException("Código inválido");

            return SimpleJWT.generateToken(user, "USER", 3600);
        }
    }

    /**
     * Resource Server que valida el access token.
     */
    static class ProtectedApi {

        public String getSecureData(String jwt) {
            if (!SimpleJWT.validate(jwt)) {
                throw new SecurityException("Token inválido");
            }
            return "Datos protegidos";
        }
    }

    /* =========================================================
       MONOLITO
       ========================================================= */

    /**
     * Ejemplo de aplicación monolítica.
     *
     * Todas las capas viven en el mismo proceso:
     * - Autenticación
     * - Autorización
     * - Lógica de negocio
     * - Acceso a datos (simulado)
     */
    static class MonolithicApplication {

        private final OAuth2AuthorizationServer authServer = new OAuth2AuthorizationServer();
        private final ProtectedApi api = new ProtectedApi();

        /**
         * Flujo completo dentro de un solo despliegue.
         */
        public void run() {
            // Autorización
            String authCode = authServer.authorize("client-app", "alice");

            // Token
            String accessToken = authServer.token(authCode);

            // Uso del recurso
            String data = api.getSecureData(accessToken);
        }
    }

    /* =========================================================
       MAIN
       ========================================================= */

    public static void main(String[] args) {
        MonolithicApplication app = new MonolithicApplication();
        app.run();
    }
}
