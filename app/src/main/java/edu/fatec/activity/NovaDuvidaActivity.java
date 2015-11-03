package edu.fatec.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.gqueiroz.androidtcc.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.fatec.json.JsonDuvida;
import edu.fatec.model.Materia;
import edu.fatec.model.Usuario;
import edu.fatec.util.MultiSelectionSpinner;

public class NovaDuvidaActivity extends Activity {
    private EditText tituloNovaDuvida;
    private EditText conteudoNovaDuvida;
    private EditText tagsNovaDuvida;

    private LinearLayout infoNovaDuvida;
    private TextView textInfoNovaDuvida;
    private ProgressBar progressBarNovaDuvida;

    private MultiSelectionSpinner spinnerMaterias;
    private HashMap<String, Integer> spinnerValues = new HashMap<>();

    private FloatingActionButton inserirNovaDuvida;

    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nova_duvida);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        findViewsById();

        setSpinnerValues();
        setSpinnerAdapter();

        inserirNovaDuvida.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                volleyNovaDuvida();
            }
        });
    }

    public void findViewsById() {
        spinnerMaterias = (MultiSelectionSpinner) findViewById(R.id.spinnerMaterias);
        tituloNovaDuvida = (EditText) findViewById(R.id.tituloNovaDuvida);
        conteudoNovaDuvida = (EditText) findViewById(R.id.conteudoNovaDuvida);
        tagsNovaDuvida = (EditText) findViewById(R.id.tagsNovaDuvida);
        inserirNovaDuvida = (FloatingActionButton) findViewById(R.id.inserirNovaDuvida);
        infoNovaDuvida = (LinearLayout) findViewById(R.id.backgroundNovaDuvida);
        textInfoNovaDuvida = (TextView) findViewById(R.id.textInfoNovaDuvida);
        progressBarNovaDuvida = (ProgressBar) findViewById(R.id.progressBarDuvida);
    }

    public JsonDuvida novaDuvida() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String sharedUsuario = sharedPref.getString("jsonUsuario", "");
        Usuario usuario = new Gson().fromJson(sharedUsuario, Usuario.class);

        JsonDuvida d = new JsonDuvida();
        d.setIdUsuario(usuario.getIdUsuario());
        d.setTitulo(tituloNovaDuvida.getText().toString());
        d.setConteudo(conteudoNovaDuvida.getText().toString());
        d.setTags(tagsNovaDuvida.getText().toString().split("\\s+"));
        d.setMaterias(getSelectedMaterias());

        return d;
    }

    public int[] getSelectedMaterias() {
        List<String> materias = spinnerMaterias.getSelectedStrings();
        int[] items = new int[materias.size()];

        for (int i = 0; i < materias.size(); i++)
            items[i] = spinnerValues.get(materias.get(i));

        return items;
    }

    public void setSpinnerAdapter() {
        List<String> materias = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : spinnerValues.entrySet())
            materias.add(entry.getKey());

        Collections.sort(materias);
        spinnerMaterias.setItems(materias);
    }

    public void setSpinnerValues() {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        String todasMaterias = sharedPref.getString("jsonMaterias", "");
        Type listType = new TypeToken<ArrayList<Materia>>() {
        }.getType();

        List<Materia> materiasJson = new Gson().fromJson(todasMaterias, listType);

        for (Materia m : materiasJson)
            spinnerValues.put(m.getMateria(), m.getIdMateria());
    }

    public void volleyNovaDuvida() {
        infoNovaDuvida.setVisibility(View.VISIBLE);
        infoNovaDuvida.setBackgroundColor(Color.parseColor("#FFA726"));
        textInfoNovaDuvida.setText("Criando sua duvida");
        progressBarNovaDuvida.setVisibility(View.VISIBLE);

        String server = getString(R.string.wstcc);
        String url = server + "duvidas/adicionarDuvida";

        RequestQueue queue = Volley.newRequestQueue(this);
        Drawable loop = getResources().getDrawable(R.drawable.ic_loop_white_24dp);
        inserirNovaDuvida.setImageDrawable(loop);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(getApplicationContext(), "Duvida enviada com sucesso", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(NovaDuvidaActivity.this, MainActivity.class);
                        startActivity(i);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                infoNovaDuvida.setBackgroundColor(Color.parseColor("#ff4444"));
                textInfoNovaDuvida.setText("Erro ao inserir nova Duvida");
                progressBarNovaDuvida.setVisibility(View.GONE);
            }
        }) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                String duvida = new Gson().toJson(novaDuvida());
                return duvida.getBytes();
            }
        };
        queue.add(stringRequest);
    }

    public static boolean validaTitulo(View v) {
        EditText titulo = (EditText) v;
        if (TextUtils.isEmpty(titulo.getText())) {
            titulo.setError("Insira um titulo para a dúvida");
            titulo.setFocusable(true);
            return false;
        } else if (titulo.getText().length() < 5) {
            titulo.setError("Dúvida inválida. Tamanho mínimo de 5 caracteres.");
            titulo.setFocusable(true);
            return false;
        } else
            return true;
    }
}
