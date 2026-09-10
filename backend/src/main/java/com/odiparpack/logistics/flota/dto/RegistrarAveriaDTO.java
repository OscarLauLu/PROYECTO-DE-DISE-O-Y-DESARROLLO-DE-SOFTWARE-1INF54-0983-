package com.odiparpack.logistics.flota.dto;

import com.odiparpack.logistics.flota.model.TipoAveria;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrarAveriaDTO {

    @NotNull(message = "El tipo de avería es obligatorio (TIPO_1, TIPO_2, TIPO_3)")
    private TipoAveria tipo;

    private int ubicacionX;
    private int ubicacionY;

    @Builder.Default
    private boolean origenManual = true;
}
