package com.example.weekdaywidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class WeekDayWidgetSimple extends AppWidgetProvider {
    private static final String TAG = "WeekDayWidget";
    private static final String PREFS_NAME = "com.example.weekdaywidget.preferences";
    private static final String PREF_FORMAT_KEY = "format_";
    private static final String PREF_BOX_KEY = "box_";
    private static final String ACTION_UPDATE_WIDGET = "com.example.weekdaywidget.UPDATE_WIDGET";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
            scheduleNextUpdate(context, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // Clean up preferences when widgets are deleted
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        for (int appWidgetId : appWidgetIds) {
            editor.remove(PREF_FORMAT_KEY + appWidgetId);
            editor.remove(PREF_BOX_KEY + appWidgetId);
            cancelUpdate(context, appWidgetId);
        }
        editor.apply();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        
        if (ACTION_UPDATE_WIDGET.equals(intent.getAction())) {
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 
                                                AppWidgetManager.INVALID_APPWIDGET_ID);
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                updateAppWidget(context, appWidgetManager, appWidgetId);
                scheduleNextUpdate(context, appWidgetId);
            }
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        try {
            DateTimeFormat format = getWidgetFormat(context, appWidgetId);
            BoxDesignStyle boxStyle = getWidgetBoxStyle(context, appWidgetId);
            String formattedText = formatDateTime(format);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout_simple);
            views.setTextViewText(R.id.dayText, formattedText);

            // Apply box style if it's not the default
            if (boxStyle != BoxDesignStyle.ROUNDED_CORNERS) {
                // For now, just log the box style - we'll implement visual changes later
                Log.d(TAG, "Box style selected: " + boxStyle.getName());
            }

            // Add click listener to open configuration
            Intent configIntent = new Intent(context, WidgetConfigActivity.class);
            configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            configIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, 
                configIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.dayText, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        } catch (Exception e) {
            Log.e(TAG, "Error updating widget " + appWidgetId, e);
            // Fallback to default display
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout_simple);
            views.setTextViewText(R.id.dayText, "Error");
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    private static String formatDateTime(DateTimeFormat format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format.getPattern(), Locale.getDefault());
            return sdf.format(new Date());
        } catch (Exception e) {
            Log.e(TAG, "Error formatting date", e);
            return "Error";
        }
    }

    private static DateTimeFormat getWidgetFormat(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int formatId = prefs.getInt(PREF_FORMAT_KEY + appWidgetId, DateTimeFormat.DAY_ONLY.getId());
        return DateTimeFormat.fromId(formatId);
    }

    private static BoxDesignStyle getWidgetBoxStyle(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int boxId = prefs.getInt(PREF_BOX_KEY + appWidgetId, BoxDesignStyle.ROUNDED_CORNERS.getId());
        return BoxDesignStyle.fromId(boxId);
    }

    static void saveWidgetFormat(Context context, int appWidgetId, DateTimeFormat format) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(PREF_FORMAT_KEY + appWidgetId, format.getId()).apply();
    }

    static void saveWidgetBoxStyle(Context context, int appWidgetId, BoxDesignStyle boxStyle) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(PREF_BOX_KEY + appWidgetId, boxStyle.getId()).apply();
    }

    private void scheduleNextUpdate(Context context, int appWidgetId) {
        DateTimeFormat format = getWidgetFormat(context, appWidgetId);
        
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, WeekDayWidgetSimple.class);
        intent.setAction(ACTION_UPDATE_WIDGET);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Cancel any existing alarm
        alarmManager.cancel(pendingIntent);

        // Schedule next update based on format
        long updateInterval;
        if (format.needsFrequentUpdates()) {
            // Update every minute for time formats
            updateInterval = 60 * 1000;
        } else {
            // Update at midnight for date-only formats
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            
            long nextMidnight = calendar.getTimeInMillis();
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC, nextMidnight, pendingIntent);
            return;
        }

        long nextUpdate = SystemClock.elapsedRealtime() + updateInterval;
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME, nextUpdate, pendingIntent);
    }

    private void cancelUpdate(Context context, int appWidgetId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, WeekDayWidgetSimple.class);
        intent.setAction(ACTION_UPDATE_WIDGET);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
    }
}
