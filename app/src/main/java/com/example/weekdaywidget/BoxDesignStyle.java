package com.example.weekdaywidget;

public enum BoxDesignStyle {
    ROUNDED_CORNERS(1, "Rounded Corners", 8f, false, false),
    SHARP_CORNERS(2, "Sharp Corners", 0f, false, false),
    PILL_SHAPE(3, "Pill Shape", 40f, false, false),
    BORDER_ONLY(4, "Border Only", 8f, true, false),
    SHADOW_BOX(5, "Shadow Box", 8f, false, true),
    MINIMAL_BORDER(6, "Minimal Border", 4f, true, false),
    CIRCULAR(7, "Circular", 100f, false, true),
    HEXAGON(8, "Hexagon", 0f, false, false);

    private final int id;
    private final String name;
    private final float cornerRadius;
    private final boolean borderOnly;
    private final boolean hasShadow;

    BoxDesignStyle(int id, String name, float cornerRadius, boolean borderOnly, boolean hasShadow) {
        this.id = id;
        this.name = name;
        this.cornerRadius = cornerRadius;
        this.borderOnly = borderOnly;
        this.hasShadow = hasShadow;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public float getCornerRadius() { return cornerRadius; }
    public boolean isBorderOnly() { return borderOnly; }
    public boolean hasShadow() { return hasShadow; }

    public static BoxDesignStyle fromId(int id) {
        for (BoxDesignStyle style : values()) {
            if (style.id == id) {
                return style;
            }
        }
        return ROUNDED_CORNERS; // Default
    }
}
