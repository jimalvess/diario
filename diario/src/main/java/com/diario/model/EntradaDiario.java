package com.diario.model;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class EntradaDiario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonBackReference
    private Usuario usuario;

    private LocalDate data;

    @Column(columnDefinition = "TEXT")
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String conteudo;

    private String caminhoImagem;

    @OneToMany(mappedBy = "entradaDiario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<MidiaEntrada> midias = new ArrayList<>();

    // Pra adicionar/remover mídias
    public void addMidia(MidiaEntrada midia) {
        if (this.midias == null) {
            this.midias = new ArrayList<>();
        }
        this.midias.add(midia);
        midia.setEntradaDiario(this);
    }

    public void removeMidia(MidiaEntrada midia) {
        if (this.midias != null) {
            this.midias.remove(midia);
            midia.setEntradaDiario(null);
        }
    }
}