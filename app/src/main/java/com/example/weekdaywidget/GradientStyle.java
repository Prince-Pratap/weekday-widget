package com.example.weekdaywidget;

public enum GradientStyle {
    PASTEL_PINK(1, "Pastel Pink", "#FFD6E8", "#FFB6C1", 270),
    OCEAN_BLUE(2, "Ocean Blue", "#74B9FF", "#0984E3", 270),
    SUNSET_ORANGE(3, "Sunset Orange", "#FD79A8", "#E84393", 45),
    FOREST_GREEN(4, "Forest Green", "#81C784", "#4CAF50", 270),
    PURPLE_DREAM(5, "Purple Dream", "#B39DDB", "#9C27B0", 135),
    GOLDEN_HOUR(6, "Golden Hour", "#FFD54F", "#FFC107", 270),
    MINT_FRESH(7, "Mint Fresh", "#81E6D9", "#4FD1C7", 270),
    CORAL_REEF(8, "Coral Reef", "#FF8A80", "#FF5722", 270),
    LAVENDER_MIST(9, "Lavender Mist", "#E1BEE7", "#9C27B0", 270),
    ARCTIC_BLUE(10, "Arctic Blue", "#B3E5FC", "#03A9F4", 270),
    WARM_SUNSET(11, "Warm Sunset", "#FFAB91", "#FF7043", 45),
    EMERALD_SHINE(12, "Emerald Shine", "#A5D6A7", "#66BB6A", 270);

    private final int id;
    private final String name;
    private final String startColor;
    private final String endColor;
    private final int angle;

    GradientStyle(int id, String name, String startColor, String endColor, int angle) {
        this.id = id;
        this.name = name;
        this.startColor = startColor;
        this.endColor = endColor;
        this.angle = angle;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getStartColor() { return startColor; }
    public String getEndColor() { return endColor; }
    public int getAngle() { return angle; }

    public static GradientStyle fromId(int id) {
        for (GradientStyle style : values()) {
            if (style.id == id) {
                return style;
            }
        }
        return PASTEL_PINK; // Default
    }
}
