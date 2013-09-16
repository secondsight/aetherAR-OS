package com.aether.health.appwidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.text.format.Time;
import android.util.Log;

public class HealthWidgetProvider extends AppWidgetProvider {
    private final String TAG = "HealthWidgetProvider";
    
    private final static int INTERVAL_MILLIS_GET_BPM = 1000;
    
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.v(TAG, "onUpdate");
        Intent intent = new Intent(context, UpdateService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Time time = new Time();
        time.setToNow();
        alarm.setRepeating(AlarmManager.RTC, time.toMillis(true), INTERVAL_MILLIS_GET_BPM, pendingIntent);
    }
    
}
