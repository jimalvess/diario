package app.jimalvess.v1.diario;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import app.jimalvess.v1.diario.dao.AnotacaoDAO;
import app.jimalvess.v1.diario.modelo.Anotacao;

//AppCompatActivity diz que tem que ter uma barra superior:
public class FormularioActivity extends AppCompatActivity {

    //Seta um atributo helper nosso:
    private FormularioHelper helper;

    /////////////// TENTANDO O DATEPICKER ///////////////////
    EditText editText = findViewById(R.id.formulario_data);
    final Calendar myCalendar = Calendar.getInstance();
    //////////// FIM DA TENTATIVA //////////////////////////

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario);

        //Instancia com o parâmetro desta activity:
        helper = new FormularioHelper(this);

        //Recupera a intent que foi usada pra abrir, no caso de edição
        //de anotação, ela tem os dados da anotacao que vai ser editada:
        Intent intent = getIntent();

        //Recupera a anotacao que veio junto (só funciona com classe serializable):
        Anotacao anotacao = (Anotacao) intent.getSerializableExtra("anotacao");

        //Caso venha sem a anotacao (outra intent que chamou esta activity):
        if (anotacao != null) {
            //Preenche pra edição:
            helper.preencheFormulario(anotacao);
        }

        /////////////// TENTANDO O DATEPICKER ///////////////////
        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }
        };

        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(FormularioActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        //////////// FIM DA TENTATIVA //////////////////////////

    }

    /////////////// TENTANDO O DATEPICKER ///////////////////
    private void updateLabel() {
        String myFormat = "MM/dd/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        editText.setText(sdf.format(myCalendar.getTime()));
    }
//////////// FIM DA TENTATIVA //////////////////////////

    //Cria um menu na barra superior:
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Pra criar o menu baseado no xml da pasta menus:
        MenuInflater inflater = getMenuInflater();

        //parametros: qual xml, onde usar (menu, que vem por parametro):
        inflater.inflate(R.menu.menu_formulario, menu);

        return super.onCreateOptionsMenu(menu);
    }

    //Retorna quem foi clicado no menu da barra superior:
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){

///////////////// INSERIR ANOTACAO ////////////////////////
            //Se foi o botão de OK:
            case R.id.menu_formulario_ok:

                //Pra poder recuperar o que foi digitado:
                Anotacao anotacao = helper.pegaAnotacao();

                //Instancia o DAO que tem conexões com o banco, passando esta activity:
                AnotacaoDAO dao = new AnotacaoDAO(this);

                //Se tem ID é que é pra edição:
                if (anotacao.getId() != null){

                    //Aí é pra alterar:
                    dao.altera(anotacao);
                } else {

                    //Se não tem ID, ele é novo:
                    dao.insere(anotacao);
                }

                //Fecha a conexão:
                dao.close();

                //Parametros: activity atual, mensagem, duração:
                Toast.makeText(FormularioActivity.this, "Anotacao " + anotacao.getTitulo() + " salva!", Toast.LENGTH_SHORT).show();

                //Mata a activity, senão vai ficar no histórico do voltar do celular:
                finish();
        break;
        }
        return super.onOptionsItemSelected(item);
    }
}