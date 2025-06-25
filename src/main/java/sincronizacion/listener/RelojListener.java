package sincronizacion.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sincronizacion.dto.HoraClienteDto;
import sincronizacion.services.SincronizacionService;


@Component
public class RelojListener {

    @Autowired
    private SincronizacionService sincronizacionService;

    @Autowired
    private ObjectMapper objectMapper;

    @RabbitListener(queues = "reloj.solicitud")
    public void recibirSolicitudReloj(String mensajeJson) {
        try {
            HoraClienteDto dto = objectMapper.readValue(mensajeJson, HoraClienteDto.class);
            System.out.println("Solicitud de reloj recibida: " + dto);
            sincronizacionService.registrarTiempo(dto);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
