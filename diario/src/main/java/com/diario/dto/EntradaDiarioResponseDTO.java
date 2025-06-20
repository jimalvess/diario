package com.diario.dto;

import com.diario.model.EntradaDiario;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntradaDiarioResponseDTO {

    private Long id;
    private LocalDate data;

    private String titulo;
    private String conteudo;
    private String caminhoImagem;
    private Long usuarioId;
    private String usuarioNome;

    // Lista de mídias, usando o MidiaEntradaDTO
    private List<MidiaEntradaDTO> midias;

    // Construtor para mapear de EntradaDiario (entidade) para EntradaDiarioResponseDTO
    public EntradaDiarioResponseDTO(EntradaDiario entrada) {
        this.id = entrada.getId();
        this.data = entrada.getData();
        this.titulo = entrada.getTitulo();
        this.conteudo = entrada.getConteudo();
        this.caminhoImagem = entrada.getCaminhoImagem();

        if (entrada.getUsuario() != null) {
            this.usuarioId = entrada.getUsuario().getId();
            this.usuarioNome = entrada.getUsuario().getNome();
        }

        // Mapeia a lista de MidiaEntrada para MidiaEntradaDTO
        if (entrada.getMidias() != null) {
            this.midias = entrada.getMidias().stream()
                    .map(midia -> new MidiaEntradaDTO(
                            midia.getId(),
                            midia.getNomeOriginalArquivo(),
                            midia.getCaminhoArquivo(),
                            midia.getTipoArquivo()
                    ))
                    .collect(Collectors.toList());
        } else {
            this.midias = List.of(); // Garante uma lista vazia, não nula, te liga
        }
    }
}
