package com.diario.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MidiaEntradaDTO {
    private Long id;
    private String nomeOriginalArquivo;
    private String caminhoArquivo;
    private String tipoArquivo;

}
