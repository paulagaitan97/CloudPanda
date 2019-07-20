package com.eam.paula.cloudpanda.Servicios;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class BackgroundJobService extends JobService {

    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        Handler handler = new Handler(getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "BackGruond service", Toast.LENGTH_SHORT).show();
                Log.i("Background service", "Service running");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(new Intent(getApplicationContext(), GpsIntentService.class));
                    jobFinished(jobParameters, false);
                } else {
                    Intent intent = new Intent(getApplicationContext(), GpsIntentService.class);
                    startService(intent);
                    jobFinished(jobParameters, false);
                }
            }
        });

        BootReceiver.scheduleJob(getApplicationContext());
        return true;

    }

    @Override
    public boolean onStopJob(final JobParameters jobParameters) {
        return false;
    }

}
