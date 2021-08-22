package app.jimalvess.v1.diario.modelo;

import java.io.Serializable;

//Tem que ser serializabre pra poder enviar anotacoes
//de uma activity pra outra:
public class Anotacao implements Serializable {

    private Long id;
    private String titulo;
    private String conteudo;
    private String data;
    private String observacoes;
    private Double nota;

    //Pra evitar que o adapter lá da activity da lista
    //converta pra uma string loca o objeto anotacao que ele recebe:
    @Override
    public String toString() {
        return getData() + " - " + getTitulo();
    }

    public Long getId() { return id; }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public Double getNota() {
        return nota;
    }

    public void setNota(Double nota) {
        this.nota = nota;
    }
}
