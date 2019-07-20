package com.eam.paula.cloudpanda.Utilidades;

import com.google.firebase.database.FirebaseDatabase;


/*Normalmente con esta linea se haria, pero debido a que esta solo se puede configurar una vez,
 * ya que si despues de configurado se vuelva a llamar y sale error, es necesario definir una clase
 * aparte para hacer esta configuracion, asociandolo al arranque del app para que lo llame una vez y
 * listo. Para hacerlo se implemeneta la clase MyFirebaseApp, se hereda de Application, y en el
 * manifiest, se asocia la propiedad android:name, y se apunta a esa clase*/
//firebaseDatabase.setPersistenceEnabled(true);

public class MyFirebaseApp extends  android.app.Application {

    @Override
    public void onCreate(){
        super.onCreate();

        /*Se habilita la persistencia de datos, es decir, si no se tiene conexion a internet, igual
         * se guarda la informacion pero en local, y cuando se conecte posteriormente a internet el
         * sincronizara todo, sin necesidad de reiniciar la app, solo con contectarse a intenret el
         * realizara todo*/

        /*Se activa la persistencia del Firebase*/
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

}

