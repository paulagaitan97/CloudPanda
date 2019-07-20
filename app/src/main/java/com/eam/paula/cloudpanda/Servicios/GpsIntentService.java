package com.eam.paula.cloudpanda.Servicios;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.android.volley.toolbox.JsonObjectRequest;
import com.eam.paula.cloudpanda.Modelo.Localizacion;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Este intent service es usado por el JobScheduler directamente y por el AlarmManager
 *  ocupa la libreria de google play-services-location es una librería con mejor performance para obtener la localización.
 *
 */
public class GpsIntentService extends IntentService implements LocationListener {

    private static final String TAG = "GpsTrackerIntentService";
    private long UPDATE_INTERVAL = 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */
    private GoogleApiClient mGoogleApiClient;
    JsonObjectRequest jsonObjectRequest;
    SharedPreferences preferences;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    private static  final String nombrePreferencias = "CrazzyPanda";
    public GpsIntentService() {
        super("GpsTrackerIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.v(TAG, "GpsTrackerIntentService ran!");
        startGoogleApiClient();
        preferences = getSharedPreferences(nombrePreferencias,MODE_PRIVATE);

    }

    @Override
    public void onCreate() {
        super.onCreate();
        configurarFirebase();
        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("")
                    .setContentText("").build();

            startForeground(1, notification);
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
    }
    private synchronized void startGoogleApiClient() {
        if(mGoogleApiClient == null){
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks(){

                        @Override
                        public void onConnected(@Nullable Bundle bundle) throws SecurityException {
                            Log.i(TAG, "Connection connected");
                            registerRequestUpdate();
                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            Log.i(TAG, "Connection suspended");
                            mGoogleApiClient.connect();
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                            Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
                        }
                    })
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
        }else{
            mGoogleApiClient.connect();
        }
    }

    private void registerRequestUpdate() throws SecurityException {
        LocationRequest mLocationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(UPDATE_INTERVAL)
            .setFastestInterval(FASTEST_INTERVAL);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            sendNewLocation(location);
        } else {
            Log.e(TAG, "Location no detected.");
        }

        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Log.v(TAG, "GoogleApiClient was disconnect.");
            mGoogleApiClient.disconnect();
        }
    }




    private void sendNewLocation(Location location){
        Date date = new Date();
        //Caso 3: obtenerhora y fecha y salida por pantalla con formato:
        DateFormat hourdateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Localizacion locationModel = new Localizacion();
        locationModel.setId(UUID.randomUUID().toString());
        locationModel.setLatitude(location.getLatitude());
        locationModel.setLongitude(location.getLongitude());
        locationModel.setDate(hourdateFormat.format(date));
        Toast.makeText(this, "Lat: "+locationModel.getLatitude()+" Log : "+locationModel.getLongitude(), Toast.LENGTH_SHORT).show();
        Log.i("locationObtenida",""+locationModel.getLongitude());

        String id=locationModel.getId();
        double latitud= locationModel.getLatitude();
        double longitud=locationModel.getLongitude();
        String fechaLocalizacion = String.valueOf(locationModel.getDate());
        String codigoUser=preferences.getString("CODIGO","");
        guardarFirebase(id,latitud,longitud,fechaLocalizacion,codigoUser);

    }


    public void guardarFirebase(String id,double latitud,double longitud,String fecha,String usuario){

       final Double ultimaLat=Double.parseDouble(preferences.getString("lat",""));
       final Double ultimaLong=Double.parseDouble(preferences.getString("long",""));

        final Double nuevaLatitud = round(latitud,4);
        final Double nuevaLongitud = round(longitud,4);
        if(ultimaLat.equals(nuevaLatitud)&&ultimaLong.equals(nuevaLongitud)){
            Toast.makeText(this, "ubicacion igual a la ultima", Toast.LENGTH_SHORT).show();
        }else {
            Map<String, Object> localizacion = new HashMap<>();
            localizacion.put("latitud", latitud);
            localizacion.put("longitud", longitud);
            localizacion.put("fecha", fecha);
            localizacion.put("codigousuario", usuario);

            databaseReference.child("gps").child(id).setValue(localizacion);
            Toast.makeText(this, "inserto nueva ubicacion", Toast.LENGTH_SHORT).show();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("lat", String.valueOf(round(latitud,4)));
            editor.putString("long", String.valueOf(round(longitud,4)));
            editor.commit();

            Toast.makeText(this, "LatLong : "+latitud+" "+longitud, Toast.LENGTH_SHORT).show();
        }
    }
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

}
