package com.diario.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "midia_entrada")
public class MidiaEntrada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_entrada_diario", nullable = false)
    @JsonBackReference // Fala pro Jackson pra ignorar este lado na serialização
    private EntradaDiario entradaDiario;

    @Column(name = "tipo_arquivo", nullable = false, length = 50)
    private String tipoArquivo;

    @Column(name = "caminho_arquivo", nullable = false, length = 255)
    private String caminhoArquivo; // Caminho no local ou nuvem

    @Column(name = "nome_original_arquivo", length = 255)
    private String nomeOriginalArquivo;

    @Column(name = "data_upload", nullable = false)
    private LocalDateTime dataUpload;

    public MidiaEntrada() {
    }

    public MidiaEntrada(EntradaDiario entradaDiario, String tipoArquivo, String caminhoArquivo, String nomeOriginalArquivo) {
        this.entradaDiario = entradaDiario;
        this.tipoArquivo = tipoArquivo;
        this.caminhoArquivo = caminhoArquivo;
        this.nomeOriginalArquivo = nomeOriginalArquivo;
        this.dataUpload = LocalDateTime.now();
    }

    // Uso na lógica
    @Override
    public String toString() {
        return "MidiaEntrada{" +
                "id=" + id +
                ", entradaDiarioId=" + (entradaDiario != null ? entradaDiario.getId() : "null") +
                ", tipoArquivo='" + tipoArquivo + '\'' +
                ", caminhoArquivo='" + caminhoArquivo + '\'' +
                ", nomeOriginalArquivo='" + nomeOriginalArquivo + '\'' +
                ", dataUpload=" + dataUpload +
                '}';
    }
}
