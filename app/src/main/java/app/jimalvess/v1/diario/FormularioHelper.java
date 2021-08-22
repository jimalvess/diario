//Na real, isso aqui vai pra um Util ;-)

package app.jimalvess.v1.diario;

import android.widget.EditText;
import android.widget.RatingBar;

import app.jimalvess.v1.diario.modelo.Anotacao;

public class FormularioHelper {

    //Pra todo mundo nesta classe poder ver
    private final EditText campoTitulo;
    private final EditText campoConteudo;
    private final EditText campoData;
    private final EditText campoObservacoes;
    private final RatingBar campoNota;

    private Anotacao anotacao;


    //Construtorzinho. Tem que dizer a que activity pertence as IDs:
    public FormularioHelper(FormularioActivity activity){

        //Seto quem é quem
        campoTitulo = activity.findViewById(R.id.formulario_titulo);
        campoConteudo = activity.findViewById(R.id.formulario_conteudo);
        campoData = activity.findViewById(R.id.formulario_data);
        campoObservacoes = activity.findViewById(R.id.formulario_observacoes);
        campoNota = activity.findViewById(R.id.formulario_nota);

        anotacao = new Anotacao();

    }

    public Anotacao pegaAnotacao() {

        //Seto tudo no objeto Anotacao:

        anotacao.setTitulo(campoTitulo.getText().toString());
        anotacao.setConteudo(campoConteudo.getText().toString());
        anotacao.setData(campoData.getText().toString());
        anotacao.setObservacoes(campoObservacoes.getText().toString());
        anotacao.setNota(Double.valueOf(campoNota.getProgress()));
        return anotacao;
    }

    //Pra preencher o formulário quando editar uma anotacao:
    public void preencheFormulario(Anotacao anotacao) {

        campoTitulo.setText(anotacao.getTitulo());
        campoConteudo.setText(anotacao.getConteudo());
        campoData.setText(anotacao.getData());
        campoObservacoes.setText(anotacao.getObservacoes());
        campoNota.setProgress(anotacao.getNota().intValue());

        //O objeto anotacao daqui recebe o objeto anotacao que veio com a intent:
        this.anotacao = anotacao;
    }
}
