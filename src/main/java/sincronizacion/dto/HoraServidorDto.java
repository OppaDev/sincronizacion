package sincronizacion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HoraServidorDto {
    private long horaServidor;
    private Map<String, Long> diferencias;
}
