package com.eam.paula.cloudpanda.ControladorVistas;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.eam.paula.cloudpanda.R;

public class MainActivity extends AppCompatActivity {
    EditText txtNombreCompleto,txtUsername,txtPassword,txtEdad,txtCodigo;
    Button btn_registro;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final int  PERIOD_MS = 5000;
    private static  final String nombrePreferencias = "CrazzyPanda";
    private  boolean pacienteExiste=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtNombreCompleto = (EditText) findViewById(R.id.txtNombreCompleto);
        txtUsername = (EditText) findViewById(R.id.txtUsername);
        txtPassword = (EditText) findViewById(R.id.txtPassword);
        txtEdad = (EditText) findViewById(R.id.txtEdad);
        txtCodigo = (EditText) findViewById(R.id.txtCodigo);
        btn_registro = (Button)findViewById(R.id.btn_registro);
        SharedPreferences preferences = getSharedPreferences(nombrePreferencias,MODE_PRIVATE);

        if(preferences.contains("EXISTE")){
            pacienteExiste = preferences.getBoolean("EXISTE",false);
            Intent intent = new Intent(MainActivity.this,PrincipalPaciente.class);
            startActivity(intent);
            finish();
        }else{
            if(preferences.contains("EXISTE")){
                Intent intent = new Intent(MainActivity.this,PrincipalPaciente.class);
                startActivity(intent);
                finish();
            }
        }

        btn_registro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences preferences = getSharedPreferences(nombrePreferencias,MODE_PRIVATE);
                if(pacienteExiste){
                    String username = txtUsername.getText().toString();
                    String clave = txtPassword.getText().toString();
                    if(username.equals(preferences.getString("USERNAME",""))){
                        if(clave.equals(preferences.getString("CLAVE",""))){
                            // cuando el usuario existe
                            Intent intent = new Intent(MainActivity.this,PrincipalPaciente.class);
                            startActivity(intent);
                            finish();
                        }else{
                            Toast.makeText(MainActivity.this, "Username incorrecta", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else{
                        Toast.makeText(MainActivity.this, "Username incorrecto", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    String nombreCompleto=txtNombreCompleto.getText().toString();
                    String username=txtUsername.getText().toString();
                    String edad=txtEdad.getText().toString();
                    String password=txtPassword.getText().toString();
                    String codigo=txtCodigo.getText().toString();
                    if(nombreCompleto.equals("")||username.equals("")||edad.equals("")||password.equals("")||codigo.equals("")){
                        Toast.makeText(MainActivity.this, "Datos Incompletos!!", Toast.LENGTH_SHORT).show();
                    }else {
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean("EXISTE", true);
                        editor.putString("NOMBRE", txtNombreCompleto.getText().toString());
                        editor.putString("USERNAME", txtUsername.getText().toString());
                        editor.putString("EDAD", txtEdad.getText().toString());
                        editor.putString("CLAVE", txtPassword.getText().toString());
                        editor.putString("CODIGO", txtCodigo.getText().toString());
                        editor.putString("lat", "0.00");
                        editor.putString("long", "0.00");
                        editor.commit();

                        Intent intent = new Intent(MainActivity.this, PrincipalPaciente.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        });
    }
}
