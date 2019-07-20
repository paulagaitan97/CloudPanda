package com.eam.paula.cloudpanda.ControladorVistas;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.toolbox.JsonObjectRequest;
import com.eam.paula.cloudpanda.Modelo.Busqueda;
import com.eam.paula.cloudpanda.R;
import com.eam.paula.cloudpanda.Servicios.BackgroundJobService;
import com.eam.paula.cloudpanda.Utilidades.NotificationHelper;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PrincipalPaciente extends AppCompatActivity {
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final int PERIOD_MS = 5000;
    private TextView lblUsername;
    Button btn_iniciar;
    String codigoUsuario;
    int cantidadBusqueda;
    JsonObjectRequest jsonObjectRequest;
    private static final String nombrePreferencias = "CrazzyPanda";
    SharedPreferences preferences;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    ArrayList<Busqueda> busquedasUsuario;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal_paciente);
        preferences = getSharedPreferences(nombrePreferencias, MODE_PRIVATE);
        codigoUsuario = preferences.getString("CODIGO", "");
        // cargarWebService();
        lblUsername = (TextView) findViewById(R.id.lblUsername);
        lblUsername.setText(preferences.getString("NOMBRE", "") + " - " + preferences.getString("CODIGO", ""));
        btn_iniciar = (Button) findViewById(R.id.btn_iniciar);

    }


    public void iniciar(View v) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {
                iniciarServicio();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
        } else {
            iniciarServicio();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    iniciarServicio();
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void scheduleJob(Context context) {
        ComponentName componentName = new ComponentName(context, BackgroundJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(0, componentName);
        builder.setMinimumLatency(PERIOD_MS);
        JobScheduler jobScheduler = (JobScheduler)
                context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(builder.build());
    }

    public void iniciarServicio() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("estadoBoton", true);
        editor.commit();
        Toast.makeText(this, "Se recibio la peticion de  iniciarlizacion: Boot service", Toast.LENGTH_SHORT).show();
        Log.i("Boot service", "Service running");
        btn_iniciar.setText("Servicio corriendo");
        btn_iniciar.setEnabled(false);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = new Intent(getApplicationContext(), BackgroundJobService.class);
            PendingIntent pendingIntent = PendingIntent.getService(
                    getApplicationContext(), 1, intent,
                    PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager alarmManager = (AlarmManager)
                    getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            alarmManager.setInexactRepeating(AlarmManager.RTC,
                    System.currentTimeMillis(), PERIOD_MS,
                    pendingIntent);
        } else {
            scheduleJob(getApplicationContext());
        }
    }

    private void configurarFirebase() {
        FirebaseApp.initializeApp(this);
        firebaseDatabase = FirebaseDatabase.getInstance();

        /*Se habilita la persistencia de datos, es decir, si no se tiene conexion a internet, igual
         * se guarda la informacion pero en local, y cuando se conecte posteriormente a internet el
         * sincronizara todo, sin necesidad de reiniciar la app, solo con contectarse a intenret el
         * realizara todo*/

        /*Normalmente con esta linea se haria, pero debido a que esta solo se puede configurar una vez,
         * ya que si despues de configurado se vuelva a llamar y sale error, es necesario definir una clase
         * aparte para hacer esta configuracion, asociandolo al arranque del app para que lo llame una vez y
         * listo. Para hacerlo se implemeneta la clase MyFirebaseApp, se hereda de Application, y en el
         * manifiest, se asocia la propiedad android:name, y se apunta a esa clase*/
//        firebaseDatabase.setPersistenceEnabled(true);
        databaseReference = firebaseDatabase.getReference();
        databaseReference.keepSynced(true);
    }


    @Override
    public void onResume() {
        super.onResume();
        configurarFirebase();
        listarBusqueda();
    }


    public void listarBusqueda() {
        busquedasUsuario = new ArrayList<>();
        final DatabaseReference userNameRef = databaseReference.child("busquedas");
        userNameRef.orderByChild("codigousuario").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Busqueda nuevaBusqueda = null;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    nuevaBusqueda = new Busqueda();
                    Busqueda obtenerBusqueda = snapshot.getValue(Busqueda.class);
                    nuevaBusqueda.setCantidad(obtenerBusqueda.getCantidad());
                    nuevaBusqueda.setCodigousuario(obtenerBusqueda.getCodigousuario());
                    busquedasUsuario.add(nuevaBusqueda);
                }
                actualizarBusqueda();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void actualizarBusqueda(){
        for(int p=0;p<busquedasUsuario.size();p++){
            if(busquedasUsuario.get(p).getCodigousuario().equals(codigoUsuario)){
                int obtenerCantidad=busquedasUsuario.get(p).getCantidad();
                if(obtenerCantidad==0){
                    NotificationHelper helper = new NotificationHelper(getApplicationContext());
                    helper.createNotification("CloudPanda informa ", "No te han buscado, disfruta de tu libertad");
                }else {
                    NotificationHelper helper = new NotificationHelper(getApplicationContext());
                    helper.createNotification("CloudPanda informa", "Te han buscado " + obtenerCantidad+" veces, huye lo mas pronto!!");
                }
            }
        }
    }

}
