package app.jimalvess.v1.diario.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import app.jimalvess.v1.diario.modelo.Anotacao;


public class AnotacaoDAO extends SQLiteOpenHelper {

//Construtor obrigatório pra quem estende o SQLiteOpenHelper:
    public AnotacaoDAO(@Nullable Context context) {
        //Parametros: o contexto vem de fora, nome do banco, factory é null, versão do nosso banco:
        super(context, "diario", null, 1); //Sempre que mudar algo, altera a versão aí ele usa o onUpdate
    }

    //Se precisar criar ou atualizar o banco, vai usar um destes dois métodos. Se não, não faz nada:
    @Override
    public void onCreate(SQLiteDatabase db) {

        //Cria a tabela:
        String sql = "CREATE TABLE Anotacoes " +
                    "(id INTEGER PRIMARY KEY, " +
                    "titulo TEXT NOT NULL, " +
                    "conteudo TEXT, " +
                    "data TEXT, " +
                    "observacoes TEXT, " +
                    "nota REAL);";
        db.execSQL(sql);
    }

    //Se precisar atualizar a versão do banco, vai usar isso:
    @Override
    public void onUpgrade(SQLiteDatabase db, int versaoVelha, int versaoNova) {

        //Deleta a tabela:
        String sql = "DROP TABLE IF EXISTS Anotacoes;";
        db.execSQL(sql);

        //Cria de novo:
        onCreate(db);

    }

    //Recebe a anotacao como parâmetro lá da activity e destrincha ela aqui:
    public void insere(Anotacao anotacao) {

        //Pega uma referência do nosso banco (pra gravar):
        SQLiteDatabase db =  getWritableDatabase();

        ContentValues dados = pegaDadosAnotacao(anotacao);

        //Parâmetros: nome da tabela, linha em branco, valores:
        db.insert("Anotacoes", null, dados);
    }

    private ContentValues pegaDadosAnotacao(Anotacao anotacao) {
        //Cria os valores de conteúdo que a gente quer inserir:
        ContentValues dados = new ContentValues();

        //Map (nome da coluna : valor recebido da activity e que veio pelo parametero anotacao) preenchendo o contentValue com os dados:
        dados.put("titulo", anotacao.getTitulo());
        dados.put("conteudo", anotacao.getConteudo());
        dados.put("data", anotacao.getData());
        dados.put("observacoes", anotacao.getObservacoes());
        dados.put("nota", anotacao.getNota());
        return dados;
    }

    public List<Anotacao> buscaAnotacoes() {

        //Pega uma referência do nosso banco (pra ler):
        SQLiteDatabase db =  getReadableDatabase();

        String sql = "SELECT * FROM Anotacoes;";

        //Cria um resultSet (do tipo Cursor). Usa null nos parâmetros, só pra não dar pau:
        Cursor c = db.rawQuery(sql,null);

        //Prepara um array pra pegar as anotacoes do resultSet:
        List<Anotacao> anotacoes = new ArrayList<Anotacao>();

        //Enquanto tiver próxima linha pro cursor (igual o next()):
        while (c.moveToNext()){

            //Cada resultado é uma anotacao:
            Anotacao anotacao = new Anotacao();

            //Pega as coisas do resultSet (cursor) e mete na anotacao:
            anotacao.setId(c.getLong(c.getColumnIndex("id"))); //pega o valor que tá no cursor que pega da coluna ID
            anotacao.setTitulo(c.getString(c.getColumnIndex("titulo")));
            anotacao.setConteudo(c.getString(c.getColumnIndex("conteudo")));
            anotacao.setData(c.getString(c.getColumnIndex("data")));
            anotacao.setObservacoes(c.getString(c.getColumnIndex("observacoes")));
            anotacao.setNota(c.getDouble(c.getColumnIndex("nota")));

            //coloca ele no array:
            anotacoes.add(anotacao);
        }
        //Fecha o cursor:
        c.close();

        return anotacoes;
    }

    //Apaga do banco:
    public void deleta(Anotacao anotacao) {

        //Referencia o banco (pra escrever)
        SQLiteDatabase db = getWritableDatabase();

        //Array de parâmetros pra query escondida pelo db.delete (um pra cada "?"):
        String [] params = {anotacao.getId().toString()};

        //Parametros: Tabela, where, array de parametros:
        db.delete("Anotacoes", "id = ?", params);
    }

    //Para editar uma anotacao:
    public void altera(Anotacao anotacao) {

        //Referencia um bd pra escrever:
        SQLiteDatabase db = getWritableDatabase();

        //Cria o conteúdo com os dados setados no método que preenche:
        ContentValues dados = pegaDadosAnotacao(anotacao);

        //Pra entrar nos parametros (?) do db.update:
        String [] params = {anotacao.getId().toString()};

        //Faz um update (Tabela, dados, where):
        db.update("Anotacoes", dados, "id = ?", params);
    }
}
