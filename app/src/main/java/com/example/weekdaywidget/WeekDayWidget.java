
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
    private static final String PREF_SIZE_KEY = "size_";
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
            editor.remove(PREF_GRADIENT_KEY + appWidgetId);
            editor.remove(PREF_FONT_KEY + appWidgetId);
            editor.remove(PREF_SIZE_KEY + appWidgetId);
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
            GradientStyle gradient = getWidgetGradient(context, appWidgetId);
            FontStyle font = getWidgetFont(context, appWidgetId);
            WidgetSizeStyle sizeStyle = getWidgetSize(context, appWidgetId);
            BoxDesignStyle boxStyle = getWidgetBoxDesign(context, appWidgetId);
            String formattedText = formatDateTime(format);

            // Choose appropriate layout based on size style
            int layoutId = getLayoutForSizeStyle(sizeStyle);
            RemoteViews views = new RemoteViews(context.getPackageName(), layoutId);
            
            // Handle adaptive gradients and backgrounds
            AdaptiveGradientStyle adaptiveGradient = null;
            if (gradient.isAdaptive()) {
                adaptiveGradient = WallpaperColorExtractor.createAdaptiveGradient(context);
            }

            // Create custom font bitmap to bypass system font forcing
            Bitmap textBitmap = createCustomFontBitmap(context, formattedText, font, gradient, adaptiveGradient, sizeStyle);
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
                
                // Set text color for adaptive themes
                if (adaptiveGradient != null) {
                    views.setTextColor(R.id.dayTextFallback, adaptiveGradient.getTextColor());
                }
            }

            // Set background (adaptive or static) with box design
            if (adaptiveGradient != null) {
                Bitmap backgroundBitmap = createAdaptiveBackgroundBitmap(context, adaptiveGradient, sizeStyle, boxStyle);
                if (backgroundBitmap != null) {
                    views.setImageViewBitmap(R.id.adaptive_background, backgroundBitmap);
                    views.setViewVisibility(R.id.adaptive_background, android.view.View.VISIBLE);
                    views.setViewVisibility(R.id.widget_background, android.view.View.GONE);
                } else {
                    // Fallback to static gradient
                    views.setViewVisibility(R.id.adaptive_background, android.view.View.GONE);
                    views.setViewVisibility(R.id.widget_background, android.view.View.VISIBLE);
                }
            } else {
                // Use static gradient with custom box design
                views.setViewVisibility(R.id.adaptive_background, android.view.View.GONE);
                views.setViewVisibility(R.id.widget_background, android.view.View.VISIBLE);
                int gradientResource = getGradientResource(gradient);
                if (gradientResource != 0) {
                    views.setInt(R.id.widget_background, "setBackgroundResource", gradientResource);
                }
            }

            // Add click listener to open configuration (works for both ImageView and TextView)
            Intent configIntent = new Intent(context, EnhancedWidgetConfigActivity.class);
            configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            configIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, 
                configIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            
            // Set click listener on all possible views
            views.setOnClickPendingIntent(R.id.dayText, pendingIntent);
            views.setOnClickPendingIntent(R.id.dayTextFallback, pendingIntent);
            views.setOnClickPendingIntent(R.id.widget_background, pendingIntent);
            views.setOnClickPendingIntent(R.id.adaptive_background, pendingIntent);

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

    static void saveWidgetSize(Context context, int appWidgetId, WidgetSizeStyle sizeStyle) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(PREF_SIZE_KEY + appWidgetId, sizeStyle.getId()).apply();
    }

    static void saveWidgetBoxDesign(Context context, int appWidgetId, BoxDesignStyle boxStyle) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(PREF_BOX_KEY + appWidgetId, boxStyle.getId()).apply();
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

    private static WidgetSizeStyle getWidgetSize(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int sizeId = prefs.getInt(PREF_SIZE_KEY + appWidgetId, WidgetSizeStyle.STANDARD.getId());
        return WidgetSizeStyle.fromId(sizeId);
    }

    private static BoxDesignStyle getWidgetBoxDesign(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int boxId = prefs.getInt(PREF_BOX_KEY + appWidgetId, BoxDesignStyle.ROUNDED_CORNERS.getId());
        return BoxDesignStyle.fromId(boxId);
    }

    private static int getLayoutForSizeStyle(WidgetSizeStyle sizeStyle) {
        switch (sizeStyle) {
            case BANNER_WIDE:
                return R.layout.widget_layout_banner_wide;
            case BANNER_TALL:
                return R.layout.widget_layout_banner_tall;
            case SQUARE_SMALL:
            case SQUARE_LARGE:
                return R.layout.widget_layout_square;
            case COMPACT:
            case STANDARD:
            default:
                return R.layout.widget_layout;
        }
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
            case WALLPAPER_ADAPTIVE: return 0; // Will be handled dynamically
            case DARK_MODE_ADAPTIVE: return 0; // Will be handled dynamically
            default: return R.drawable.pastel_gradient;
        }
    }

    /**
     * Creates a custom font bitmap to bypass system font forcing on widgets
     * This is the main workaround for Android's widget font limitations
     */
    private static Bitmap createCustomFontBitmap(Context context, String text, FontStyle fontStyle, GradientStyle gradientStyle, AdaptiveGradientStyle adaptiveGradient, WidgetSizeStyle sizeStyle) {
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
            
            // Use adaptive text color if available
            if (adaptiveGradient != null) {
                paint.setColor(adaptiveGradient.getTextColor());
            } else {
                paint.setColor(0xFF2C3E50); // Default dark text color
            }
            paint.setTextAlign(Paint.Align.CENTER);

            // Adjust text size based on widget size
            float baseTextSize = getTextSizeForWidgetSize(context, sizeStyle);
            paint.setTextSize(baseTextSize);

            // Handle vertical text for tall banners
            String displayText = text;
            if (sizeStyle.isVerticalText()) {
                displayText = formatTextForVertical(text);
            }

            // Measure text dimensions
            Paint.FontMetrics fontMetrics = paint.getFontMetrics();
            float textWidth = paint.measureText(displayText);
            float textHeight = fontMetrics.bottom - fontMetrics.top;

            // Create bitmap with size-appropriate dimensions
            int bitmapWidth, bitmapHeight;
            if (sizeStyle.isVerticalText()) {
                bitmapWidth = (int) (textHeight + 20);
                bitmapHeight = (int) (textWidth + 40);
            } else {
                bitmapWidth = (int) (textWidth + 40);
                bitmapHeight = (int) (textHeight + 20);
            }

            if (bitmapWidth <= 0 || bitmapHeight <= 0) {
                return null;
            }

            Bitmap bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);

            // Clear background (transparent)
            canvas.drawColor(0x00000000);

            // Draw text (with rotation for vertical text)
            if (sizeStyle.isVerticalText()) {
                canvas.save();
                canvas.rotate(-90, bitmapWidth / 2f, bitmapHeight / 2f);
                float x = bitmapHeight / 2f; // Swapped due to rotation
                float y = (bitmapWidth / 2f) - ((fontMetrics.descent + fontMetrics.ascent) / 2f);
                canvas.drawText(displayText, x, y, paint);
                canvas.restore();
            } else {
                float x = bitmapWidth / 2f;
                float y = (bitmapHeight / 2f) - ((fontMetrics.descent + fontMetrics.ascent) / 2f);
                canvas.drawText(displayText, x, y, paint);
            }

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

    /**
     * Creates an adaptive background bitmap based on wallpaper colors and system theme
     */
    private static Bitmap createAdaptiveBackgroundBitmap(Context context, AdaptiveGradientStyle adaptiveGradient, WidgetSizeStyle sizeStyle, BoxDesignStyle boxStyle) {
        try {
            // Use size style dimensions
            int width = sizeStyle.getWidth();
            int height = sizeStyle.getHeight();
            
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            
            // Create gradient paint
            Paint gradientPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            
            int startColor = android.graphics.Color.parseColor(adaptiveGradient.getStartColor());
            int endColor = android.graphics.Color.parseColor(adaptiveGradient.getEndColor());
            
            // Create linear gradient
            android.graphics.LinearGradient gradient = new android.graphics.LinearGradient(
                0, 0, 0, height,
                startColor, endColor,
                android.graphics.Shader.TileMode.CLAMP
            );
            
            gradientPaint.setShader(gradient);
            
            // Draw background with box design style
            android.graphics.RectF rect = new android.graphics.RectF(0, 0, width, height);
            float cornerRadius = boxStyle.getCornerRadius();
            
            if (boxStyle.isBorderOnly()) {
                // Draw border only
                Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                borderPaint.setStyle(Paint.Style.STROKE);
                borderPaint.setStrokeWidth(4);
                borderPaint.setShader(gradient);
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, borderPaint);
            } else {
                // Draw filled background
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, gradientPaint);
                
                // Add shadow effect if needed
                if (boxStyle.hasShadow()) {
                    Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                    shadowPaint.setColor(0x40000000);
                    android.graphics.RectF shadowRect = new android.graphics.RectF(2, 2, width + 2, height + 2);
                    canvas.drawRoundRect(shadowRect, cornerRadius, cornerRadius, shadowPaint);
                }
            }
            
            // Add subtle overlay for better text readability
            Paint overlayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            overlayPaint.setColor(adaptiveGradient.getOverlayColor());
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, overlayPaint);
            
            return bitmap;
            
        } catch (Exception e) {
            Log.e(TAG, "Error creating adaptive background bitmap", e);
            return null;
        }
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

    /**
     * Gets appropriate text size based on widget size style
     */
    private static float getTextSizeForWidgetSize(Context context, WidgetSizeStyle sizeStyle) {
        float baseSize;
        switch (sizeStyle) {
            case COMPACT:
                baseSize = 14f;
                break;
            case BANNER_WIDE:
                baseSize = 16f;
                break;
            case BANNER_TALL:
                baseSize = 12f;
                break;
            case SQUARE_SMALL:
                baseSize = 14f;
                break;
            case SQUARE_LARGE:
                baseSize = 20f;
                break;
            case STANDARD:
            default:
                baseSize = 18f;
                break;
        }
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, baseSize, context.getResources().getDisplayMetrics());
    }

    /**
     * Formats text for vertical display (adds line breaks between words)
     */
    private static String formatTextForVertical(String text) {
        // For vertical display, we can either rotate the text or format it line by line
        // For now, we'll just return the text as-is and handle rotation in the canvas
        return text;
    }
}
