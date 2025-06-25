package sincronizacion.services;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    @Autowired
    private ObjectMapper objectMapper;

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


        enviarAjusteRelojes(ajusteDto);

        tiemposClientes.clear();
    }

    public void enviarAjusteRelojes(HoraServidorDto ajuste) {
        System.out.println("Iniciando envío de ajustes individuales...");
        ajuste.getDiferencias().keySet().forEach(nombreNodo -> {
            try {
                String mensajeJson = objectMapper.writeValueAsString(ajuste);
                System.out.println(" -> Enviando ajuste a '" + nombreNodo + "' con datos: " + mensajeJson);
                rabbitTemplate.convertAndSend("reloj.intercambio", nombreNodo, mensajeJson);
            } catch (Exception e) {
                System.err.println("Error al serializar o enviar ajuste para el nodo " + nombreNodo);
                e.printStackTrace();
            }
        });
        System.out.println("Envío de ajustes finalizado.");
    }
}
