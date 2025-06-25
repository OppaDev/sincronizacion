package sincronizacion.services;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sincronizacion.dto.HoraClienteDto;
import sincronizacion.dto.HoraServidorDto;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SincronizacionService {
    private Map<String, Long> tiemposClientes = new ConcurrentHashMap<>();
    private static int INTERVALO_SEGUNDOS = 10;

    @Autowired
    private AmqpTemplate rabbitTemplate;

    public void registrarTiempo(HoraClienteDto dto) {

        tiemposClientes.put(dto.getNombreNodo(), dto.getHoraEnviada());
    }

    public void sincronizacionRelojes() {
        if (tiemposClientes.isEmpty()) {
            System.out.println("No hay clientes para sincronizar. Esperando...");
            return;
        }

        long ahoraServidor = Instant.now().toEpochMilli();
        Map<String, Long> diferencias = new HashMap<>();

        tiemposClientes.forEach((nombreNodo, horaCliente) -> {
            long diferencia = horaCliente - ahoraServidor;
            diferencias.put(nombreNodo, diferencia);
        });

        long sumaTiempos = ahoraServidor + tiemposClientes.values().stream().mapToLong(Long::longValue).sum();
        long horaSincronizadaPromedio = sumaTiempos / (tiemposClientes.size() + 1);

        HoraServidorDto ajusteDto = new HoraServidorDto(horaSincronizadaPromedio, diferencias);

        // AHORA ENVIAMOS UN MENSAJE A CADA CLIENTE
        enviarAjusteRelojes(ajusteDto);

        tiemposClientes.clear();
    }
    public void enviarAjusteRelojes (HoraServidorDto ajuste) {
        System.out.println("Iniciando envío de ajustes individuales...");
        // Iteramos sobre los clientes a los que debemos enviarles el ajuste.
        // La clave del mapa de diferencias es el nombre del nodo, que usaremos como routing key.
        ajuste.getDiferencias().keySet().forEach(nombreNodo -> {
            System.out.println(" -> Enviando ajuste a '" + nombreNodo + "' con datos: " + ajuste);

            // Enviamos el mensaje al exchange, especificando el nombre del nodo como routing key.
            rabbitTemplate.convertAndSend("reloj.intercambio", nombreNodo, ajuste);
        });
        System.out.println("Envío de ajustes finalizado.");
    }
}
