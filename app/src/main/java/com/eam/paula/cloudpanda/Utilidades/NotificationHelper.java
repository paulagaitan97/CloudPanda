package com.eam.paula.cloudpanda.Utilidades;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;

import com.eam.paula.cloudpanda.ControladorVistas.MainActivity;
import com.eam.paula.cloudpanda.R;


public class NotificationHelper {

    /*Contexto de la app*/
    private Context mContext;
    /*Gestor de notificaciones*/
    private NotificationManager mNotificationManager;
    /*Contructor de la notificacion*/
    private NotificationCompat.Builder mBuilder;
    /*Id general de las notificaciones*/
    public static final String NOTIFICATION_CHANNEL_ID = "10001";


    public NotificationHelper(Context context) {
        mContext = context;
    }



    public void createNotification(String title, String message)
    {
        /**Se define un intent, el cual ejecutara la apartura de una activity cuando se presione
         * la notificacion mostrada**/
        Intent resultIntent = new Intent(mContext , MainActivity.class);
        /*Se indica con una bandera que esta activivity ejecutara una nueva tarea*/
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        /*Se defina un pending intent para asociar el intent, mandandole un cero como codigo de
        * respuesta esperada y una bandeta que indique que actualizara el proceso actual que se
        * este ejecutando al momento de seleccionar la notificacion*/
        PendingIntent resultPendingIntent = PendingIntent.getActivity(mContext,
                0 /* Request code */, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);



        /*ICONO GRANDE EN LA PARTE DE LA DERECHA*/
        /*Para personalizar aun mas la notificacion, se crea un bitmap para agregarlo en la
         * propiedad de BIG ICON*/
        Bitmap notificationLargeIconBitmap = BitmapFactory.decodeResource(
                mContext.getResources(),
                R.drawable.crazzypanda);



        /*Se inicia la construccion de la notificacion*/
        mBuilder = new NotificationCompat.Builder(mContext);
        /*Se le asocia una imagen, titulo, mensaje, sonido, el autocancel indica que la notificacion desaparece
        * tan pronto como el usuario la toca, y tambien se le asocia la activity que se ejecutara cuando esta es
        * presionada*/

        mBuilder.setSmallIcon(R.drawable.crazzypanda)
                .setLargeIcon(notificationLargeIconBitmap)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(false)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setContentIntent(resultPendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message));

        /*Se asocia la notificacion al notification manager*/
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        /*Si es android 8 o superior, asignamos los canales de notificacion*/
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {

            /*Niveles de importancia del gestor de notificaciones
            *IMPORTANCE_DEFAULT: Se muestra en cualquier lugar, hace ruido pero no se invasivo visualmente
            *IMPORTANCE_HIGH:  Se muestra en cualquier lugar, hace ruido y es invasivo visualmente
            *IMPORTANCE_LOW: Se muestra en cualquier lugar pero no es intrusivo
            *IMPORTANCE_MIN: Solo muestra una sombra en la parte inferior del menu superior}
            *IMPORTANCE_NONE: No se muestra en ningun lado, toca ir a notificaciones para visualizarla
            * */
            int importance = NotificationManager.IMPORTANCE_HIGH;

            /*Creamos un canal de notificacion, con un ID y un name el cual se muestra si se notifica, ademas de la importancia de la notificacion*/
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "NOTIFICATION_CHANNEL_NAME", importance);
            /*Se habilitan las luces para la notificacion*/
            notificationChannel.enableLights(true);
            /*Se indica el color*/
            notificationChannel.setLightColor(Color.RED);
            /*Se habilitan las notificaciones*/
            notificationChannel.enableVibration(true);
            /*Se indica el patron de vibracion*/
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            /*Si se pudo crear el gestor de notificaciones, se le asocia el id canal de escucha y el canal de escucha creado*/
            if(mNotificationManager != null) {
                mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
                mNotificationManager.createNotificationChannel(notificationChannel);
            }
        }

        /*Si se creo el gestor de notificaciones*/
        if(mNotificationManager != null) {
            /*Se ejecuta la notificacion creada, indicando el codifo de respuesta con 0 y la notificacion estructurada*/
            mNotificationManager.notify(0 /* Request Code */, mBuilder.build());
        }
    }

}
