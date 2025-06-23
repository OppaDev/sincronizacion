package sincronizacion.services;

import org.springframework.stereotype.Service;
import sincronizacion.dto.HoraClienteDto;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SincronizacionService {
    private Map<String, Long> tiemposClientes = new ConcurrentHashMap<>();
    private static int INTERVALO_SEGUNDOS = 10;

    public void registrarTiempo(HoraClienteDto dto) {
        tiemposClientes.put(dto.getNombreNodo(), dto.getHoraEnviada());
    }

    public void sincronizacionRelojes() {
        if (tiemposClientes.size() >= 2) {
            long ahora = Instant.now().toEpochMilli();
            long promedio = (ahora + tiemposClientes.values().stream().mapToLong(Long::longValue).sum())
                    /(tiemposClientes.size() + 1);
            tiemposClientes.clear();
            enviarAjusteRelojes(promedio);
        }
    }
    public void enviarAjusteRelojes (Long horaServidor) {
        System.out.println("SincronizacionService.enviarAjusteRelojes()"+ horaServidor);
    }
}
