package com.aether.health.appwidget;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.PorterDuff.Mode;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

public class UpdateService extends Service {
    private final String TAG = "HealthWidgetProvider.UpdateService";

    private HeartRateView mHeartRateView;
    private static Bitmap mBitmap;
    private static Canvas mCanvas;
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        mBitmap = Bitmap.createBitmap(400, 260, Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mHeartRateView = new HeartRateView(this);
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            updateWidget(0);
        }
        
        return super.onStartCommand(intent, flags, startId);
    }
    
    private void updateWidget(int widgetId) {
        Log.v(TAG, "updateWidget");
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
        RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.heart_rate_widget);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                new ComponentName(getApplicationContext(), HealthWidgetProvider.class));
        for (int appWidgetId : appWidgetIds) {
        	mCanvas.drawColor(0, Mode.CLEAR);
            mHeartRateView.draw(mCanvas);
            views.setImageViewBitmap(R.id.remoteview, mBitmap);
//            Log.v(TAG, "updateWidget: setImageViewBitmap");
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

}
