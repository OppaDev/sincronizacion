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

    @Scheduled(fixedRate = 10000)
    private void sincronizarRelojes() {
        System.out.println("Ejecuntando sincronización de relojes...");
        sincronizacionService.sincronizacionRelojes();
    }
}
