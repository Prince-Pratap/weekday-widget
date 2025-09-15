
package com.example.weekdaywidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class WeekDayWidget extends AppWidgetProvider {
    private static final String TAG = "WeekDayWidget";
    private static final String PREFS_NAME = "com.example.weekdaywidget.preferences";
    private static final String PREF_FORMAT_KEY = "format_";
    private static final String PREF_GRADIENT_KEY = "gradient_";
    private static final String PREF_FONT_KEY = "font_";
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
            editor.remove(PREF_GRADIENT_KEY + appWidgetId);
            editor.remove(PREF_FONT_KEY + appWidgetId);
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
            GradientStyle gradient = getWidgetGradient(context, appWidgetId);
            FontStyle font = getWidgetFont(context, appWidgetId);
            String formattedText = formatDateTime(format);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            
            // Create custom font bitmap to bypass system font forcing
            Bitmap textBitmap = createCustomFontBitmap(context, formattedText, font, gradient);
            if (textBitmap != null) {
                // Use custom font bitmap (bypasses system font forcing)
                views.setImageViewBitmap(R.id.dayText, textBitmap);
                views.setViewVisibility(R.id.dayText, android.view.View.VISIBLE);
                views.setViewVisibility(R.id.dayTextFallback, android.view.View.GONE);
            } else {
                // Fallback to regular text view
                views.setViewVisibility(R.id.dayText, android.view.View.GONE);
                views.setViewVisibility(R.id.dayTextFallback, android.view.View.VISIBLE);
                views.setTextViewText(R.id.dayTextFallback, formattedText);
            }

            // Set background gradient
            int gradientResource = getGradientResource(gradient);
            if (gradientResource != 0) {
                views.setInt(R.id.widget_background, "setBackgroundResource", gradientResource);
            }

            // Add click listener to open configuration (works for both ImageView and TextView)
            Intent configIntent = new Intent(context, EnhancedWidgetConfigActivity.class);
            configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            configIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, 
                configIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            
            // Set click listener on both views and the background
            views.setOnClickPendingIntent(R.id.dayText, pendingIntent);
            views.setOnClickPendingIntent(R.id.dayTextFallback, pendingIntent);
            views.setOnClickPendingIntent(R.id.widget_background, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        } catch (Exception e) {
            Log.e(TAG, "Error updating widget " + appWidgetId, e);
            // Fallback to default display
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
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

    static void saveWidgetFormat(Context context, int appWidgetId, DateTimeFormat format) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(PREF_FORMAT_KEY + appWidgetId, format.getId()).apply();
    }

    static void saveWidgetGradient(Context context, int appWidgetId, GradientStyle gradient) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(PREF_GRADIENT_KEY + appWidgetId, gradient.getId()).apply();
    }

    static void saveWidgetFont(Context context, int appWidgetId, FontStyle font) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(PREF_FONT_KEY + appWidgetId, font.getId()).apply();
    }

    private static GradientStyle getWidgetGradient(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int gradientId = prefs.getInt(PREF_GRADIENT_KEY + appWidgetId, GradientStyle.PASTEL_PINK.getId());
        return GradientStyle.fromId(gradientId);
    }

    private static FontStyle getWidgetFont(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int fontId = prefs.getInt(PREF_FONT_KEY + appWidgetId, FontStyle.DANCING_SCRIPT.getId());
        return FontStyle.fromId(fontId);
    }

    private static int getGradientResource(GradientStyle gradient) {
        switch (gradient) {
            case PASTEL_PINK: return R.drawable.pastel_gradient;
            case OCEAN_BLUE: return R.drawable.gradient_ocean_blue;
            case SUNSET_ORANGE: return R.drawable.gradient_sunset_orange;
            case FOREST_GREEN: return R.drawable.gradient_forest_green;
            case PURPLE_DREAM: return R.drawable.gradient_purple_dream;
            case GOLDEN_HOUR: return R.drawable.gradient_golden_hour;
            case MINT_FRESH: return R.drawable.gradient_mint_fresh;
            default: return R.drawable.pastel_gradient;
        }
    }

    /**
     * Creates a custom font bitmap to bypass system font forcing on widgets
     * This is the main workaround for Android's widget font limitations
     */
    private static Bitmap createCustomFontBitmap(Context context, String text, FontStyle fontStyle, GradientStyle gradientStyle) {
        try {
            // Load the custom font
            Typeface customFont = loadCustomFont(context, fontStyle);
            if (customFont == null) {
                return null; // Fallback to default text
            }

            // Set up paint with custom font
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setTypeface(customFont);
            paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 18, context.getResources().getDisplayMetrics()));
            paint.setColor(0xFF2C3E50); // Dark text color for readability
            paint.setTextAlign(Paint.Align.CENTER);

            // Measure text dimensions
            Paint.FontMetrics fontMetrics = paint.getFontMetrics();
            float textWidth = paint.measureText(text);
            float textHeight = fontMetrics.bottom - fontMetrics.top;

            // Create bitmap with padding
            int bitmapWidth = (int) (textWidth + 40); // 20px padding on each side
            int bitmapHeight = (int) (textHeight + 20); // 10px padding top/bottom

            if (bitmapWidth <= 0 || bitmapHeight <= 0) {
                return null;
            }

            Bitmap bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);

            // Clear background (transparent)
            canvas.drawColor(0x00000000);

            // Draw text centered
            float x = bitmapWidth / 2f;
            float y = (bitmapHeight / 2f) - ((fontMetrics.descent + fontMetrics.ascent) / 2f);
            canvas.drawText(text, x, y, paint);

            return bitmap;

        } catch (Exception e) {
            Log.e(TAG, "Error creating custom font bitmap", e);
            return null;
        }
    }

    /**
     * Loads custom font based on FontStyle, with fallback mechanisms
     */
    private static Typeface loadCustomFont(Context context, FontStyle fontStyle) {
        try {
            if (fontStyle.isPreInstalled()) {
                // Try to load from resources
                int fontResource = context.getResources().getIdentifier(
                    fontStyle.getFileName(), "font", context.getPackageName());
                if (fontResource != 0) {
                    return androidx.core.content.res.ResourcesCompat.getFont(context, fontResource);
                }
            } else {
                // Try to load downloaded font
                java.io.File fontFile = new java.io.File(context.getFilesDir(), "fonts/" + fontStyle.getFileName() + ".ttf");
                if (fontFile.exists()) {
                    return Typeface.createFromFile(fontFile);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading custom font: " + fontStyle.getName(), e);
        }
        
        // Return null to use fallback
        return null;
    }

    private void scheduleNextUpdate(Context context, int appWidgetId) {
        DateTimeFormat format = getWidgetFormat(context, appWidgetId);
        
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, WeekDayWidget.class);
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC, nextMidnight, pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC, nextMidnight, pendingIntent);
            }
            return;
        }

        long nextUpdate = SystemClock.elapsedRealtime() + updateInterval;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME, nextUpdate, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME, nextUpdate, pendingIntent);
        }
    }

    private void cancelUpdate(Context context, int appWidgetId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, WeekDayWidget.class);
        intent.setAction(ACTION_UPDATE_WIDGET);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
    }
}
