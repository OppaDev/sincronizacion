package sincronizacion.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import sincronizacion.services.SincronizacionService;

@Configuration
@EnableScheduling
public class SchedulerConfig {
    @Autowired
    private SincronizacionService sincronizacionService;

    // Cambia el valor a una expresión válida para Spring
    @Scheduled(fixedRateString = "${reloj.intervalo:10s}")
    private void sincronizarRelojes() {
        System.out.println("Ejecuntando sincronización de relojes...");
        sincronizacionService.sincronizacionRelojes();
    }
}
