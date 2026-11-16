package pe.utpvet.inventario.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import pe.utpvet.inventario.exception.ServicioNoDisponibleException;

/**
 * Cliente HTTP del Servicio de Citas. Es la primera integración entre servicios:
 * Inventario consulta a Citas por HTTP (REST/JSON) en lugar de leer su base de datos.
 * Reenvía el mismo token JWT del usuario, así Citas aplica su propia seguridad.
 */
@Component
public class CitasClient {

    private final RestClient restClient;

    public CitasClient(RestClient.Builder builder, @Value("${citas.service.url}") String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(4000);
        this.restClient = builder.baseUrl(baseUrl).requestFactory(factory).build();
    }

    /** @return true si la cita existe, false si Citas responde 404. */
    public boolean existeCita(Integer citaId, String authorization) {
        try {
            restClient.get()
                    .uri("/api/citas/{id}", citaId)
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        } catch (HttpClientErrorException e) {
            throw new ServicioNoDisponibleException(
                    "El servicio de Citas rechazó la consulta (HTTP " + e.getStatusCode().value() + ")");
        } catch (HttpServerErrorException | ResourceAccessException e) {
            throw new ServicioNoDisponibleException(
                    "El servicio de Citas no está disponible; no se pudo validar la cita " + citaId);
        }
    }
}
