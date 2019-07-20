package com.eam.paula.cloudpanda.Servicios;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

public class BootReceiver extends BroadcastReceiver {

    private  static final int PERIODO_MS = 20000;
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Se recibio la peticion de  iniciarlizacion: Boot service", Toast.LENGTH_SHORT).show();
        Log.i("Boot service", "Service running");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Intent intentt = new Intent(context, BackgroundJobService.class);
            PendingIntent pendingIntent = PendingIntent.getService(
                    context, 1, intent,
                    PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager alarmManager = (AlarmManager)
                    context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.setInexactRepeating(AlarmManager.RTC,
                    System.currentTimeMillis(), PERIODO_MS,
                    pendingIntent);
        } else {
            scheduleJob(context);
        }
    }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public static void scheduleJob(Context context){
            ComponentName componentName = new ComponentName(context, BackgroundJobService.class);
            JobInfo.Builder builder = new JobInfo.Builder(0, componentName);
            builder.setMinimumLatency(PERIODO_MS);
            JobScheduler jobScheduler = (JobScheduler)
                    context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            jobScheduler.schedule(builder.build());
        }



    }

