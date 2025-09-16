package com.example.weekdaywidget;

public class AdaptiveGradientStyle {
    private final String name;
    private final String startColor;
    private final String endColor;
    private final int angle;
    private final boolean isDarkTheme;

    public AdaptiveGradientStyle(String name, String startColor, String endColor, int angle, boolean isDarkTheme) {
        this.name = name;
        this.startColor = startColor;
        this.endColor = endColor;
        this.angle = angle;
        this.isDarkTheme = isDarkTheme;
    }

    public String getName() { return name; }
    public String getStartColor() { return startColor; }
    public String getEndColor() { return endColor; }
    public int getAngle() { return angle; }
    public boolean isDarkTheme() { return isDarkTheme; }

    /**
     * Gets appropriate text color based on background theme
     */
    public int getTextColor() {
        return isDarkTheme ? 0xFFE0E0E0 : 0xFF2C3E50; // Light text for dark bg, dark text for light bg
    }

    /**
     * Gets semi-transparent overlay color for better text readability
     */
    public int getOverlayColor() {
        return isDarkTheme ? 0x40000000 : 0x20FFFFFF; // Dark overlay for light bg, light overlay for dark bg
    }
}
