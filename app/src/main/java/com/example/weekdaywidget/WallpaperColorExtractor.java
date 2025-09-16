package com.example.weekdaywidget;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import androidx.palette.graphics.Palette;
import java.util.ArrayList;
import java.util.List;

public class WallpaperColorExtractor {
    private static final String TAG = "WallpaperColorExtractor";

    public static class WallpaperColors {
        public final int primary;
        public final int secondary;
        public final int accent;
        public final boolean isDark;

        public WallpaperColors(int primary, int secondary, int accent, boolean isDark) {
            this.primary = primary;
            this.secondary = secondary;
            this.accent = accent;
            this.isDark = isDark;
        }
    }

    /**
     * Extracts dominant colors from the current wallpaper
     */
    public static WallpaperColors extractWallpaperColors(Context context) {
        try {
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
            
            // Try to get wallpaper drawable with proper error handling
            Drawable wallpaperDrawable = null;
            try {
                wallpaperDrawable = wallpaperManager.getDrawable();
            } catch (SecurityException e) {
                Log.w(TAG, "No permission to access wallpaper, using default colors");
                return getDefaultColors(context);
            } catch (Exception e) {
                Log.w(TAG, "Error accessing wallpaper, using default colors", e);
                return getDefaultColors(context);
            }
            
            if (wallpaperDrawable == null) {
                return getDefaultColors(context);
            }

            // Convert drawable to bitmap
            Bitmap wallpaperBitmap = drawableToBitmap(wallpaperDrawable);
            if (wallpaperBitmap == null) {
                return getDefaultColors(context);
            }

            // Scale down bitmap for faster processing
            Bitmap scaledBitmap = scaleBitmap(wallpaperBitmap, 100, 100);
            
            // Use Palette library to extract colors
            Palette palette = Palette.from(scaledBitmap).generate();
            
            // Get dominant colors
            int primaryColor = palette.getDominantColor(Color.parseColor("#6200EE"));
            int vibrantColor = palette.getVibrantColor(primaryColor);
            int mutedColor = palette.getMutedColor(primaryColor);
            
            // Determine if the wallpaper is dark
            boolean isDark = isColorDark(primaryColor);
            
            // Clean up
            if (scaledBitmap != wallpaperBitmap) {
                scaledBitmap.recycle();
            }
            wallpaperBitmap.recycle();
            
            return new WallpaperColors(primaryColor, vibrantColor, mutedColor, isDark);
            
        } catch (Exception e) {
            Log.e(TAG, "Error extracting wallpaper colors", e);
            return getDefaultColors(context);
        }
    }

    /**
     * Creates adaptive gradient based on wallpaper colors and system theme
     */
    public static AdaptiveGradientStyle createAdaptiveGradient(Context context) {
        WallpaperColors colors = extractWallpaperColors(context);
        boolean isSystemDarkMode = isSystemInDarkMode(context);
        
        // Combine wallpaper darkness with system dark mode
        boolean shouldUseDarkTheme = colors.isDark || isSystemDarkMode;
        
        if (shouldUseDarkTheme) {
            return createDarkAdaptiveGradient(colors);
        } else {
            return createLightAdaptiveGradient(colors);
        }
    }

    private static AdaptiveGradientStyle createDarkAdaptiveGradient(WallpaperColors colors) {
        // Create darker, muted versions of wallpaper colors
        int darkStart = darkenColor(colors.primary, 0.7f);
        int darkEnd = darkenColor(colors.secondary, 0.8f);
        
        return new AdaptiveGradientStyle(
            "Dark Adaptive",
            String.format("#%06X", (0xFFFFFF & darkStart)),
            String.format("#%06X", (0xFFFFFF & darkEnd)),
            270,
            true
        );
    }

    private static AdaptiveGradientStyle createLightAdaptiveGradient(WallpaperColors colors) {
        // Create lighter, more vibrant versions of wallpaper colors
        int lightStart = lightenColor(colors.primary, 0.3f);
        int lightEnd = lightenColor(colors.secondary, 0.4f);
        
        return new AdaptiveGradientStyle(
            "Light Adaptive",
            String.format("#%06X", (0xFFFFFF & lightStart)),
            String.format("#%06X", (0xFFFFFF & lightEnd)),
            270,
            false
        );
    }

    /**
     * Checks if the system is in dark mode
     */
    public static boolean isSystemInDarkMode(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            int nightModeFlags = context.getResources().getConfiguration().uiMode 
                & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
            return nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES;
        }
        return false;
    }

    private static Bitmap drawableToBitmap(Drawable drawable) {
        try {
            int width = Math.min(drawable.getIntrinsicWidth(), 500);
            int height = Math.min(drawable.getIntrinsicHeight(), 500);
            
            if (width <= 0 || height <= 0) {
                width = height = 100; // Fallback size
            }
            
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (Exception e) {
            Log.e(TAG, "Error converting drawable to bitmap", e);
            return null;
        }
    }

    private static Bitmap scaleBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        try {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            
            if (width <= maxWidth && height <= maxHeight) {
                return bitmap;
            }
            
            float scaleWidth = ((float) maxWidth) / width;
            float scaleHeight = ((float) maxHeight) / height;
            float scale = Math.min(scaleWidth, scaleHeight);
            
            int newWidth = Math.round(width * scale);
            int newHeight = Math.round(height * scale);
            
            return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
        } catch (Exception e) {
            Log.e(TAG, "Error scaling bitmap", e);
            return bitmap;
        }
    }

    private static boolean isColorDark(int color) {
        // Calculate luminance using standard formula
        double red = Color.red(color) / 255.0;
        double green = Color.green(color) / 255.0;
        double blue = Color.blue(color) / 255.0;
        
        // Apply gamma correction
        red = red < 0.03928 ? red / 12.92 : Math.pow((red + 0.055) / 1.055, 2.4);
        green = green < 0.03928 ? green / 12.92 : Math.pow((green + 0.055) / 1.055, 2.4);
        blue = blue < 0.03928 ? blue / 12.92 : Math.pow((blue + 0.055) / 1.055, 2.4);
        
        double luminance = 0.2126 * red + 0.7152 * green + 0.0722 * blue;
        return luminance < 0.5;
    }

    private static int darkenColor(int color, float factor) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= factor; // Reduce brightness
        return Color.HSVToColor(hsv);
    }

    private static int lightenColor(int color, float factor) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] = Math.min(1.0f, hsv[2] + factor); // Increase brightness
        return Color.HSVToColor(hsv);
    }

    private static WallpaperColors getDefaultColors(Context context) {
        boolean isDark = isSystemInDarkMode(context);
        if (isDark) {
            return new WallpaperColors(
                Color.parseColor("#2C3E50"),
                Color.parseColor("#34495E"),
                Color.parseColor("#3498DB"),
                true
            );
        } else {
            return new WallpaperColors(
                Color.parseColor("#3498DB"),
                Color.parseColor("#2980B9"),
                Color.parseColor("#E74C3C"),
                false
            );
        }
    }
}
