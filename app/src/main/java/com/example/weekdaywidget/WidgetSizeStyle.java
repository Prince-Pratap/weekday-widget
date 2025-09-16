package com.example.weekdaywidget;

public enum WidgetSizeStyle {
    COMPACT(1, "Compact", 120, 40, "horizontal"),
    STANDARD(2, "Standard", 200, 80, "horizontal"), 
    BANNER_WIDE(3, "Wide Banner", 320, 80, "horizontal"),
    BANNER_TALL(4, "Tall Banner", 80, 320, "vertical"),
    SQUARE_SMALL(5, "Small Square", 120, 120, "center"),
    SQUARE_LARGE(6, "Large Square", 200, 200, "center");

    private final int id;
    private final String name;
    private final int width;
    private final int height;
    private final String textFlow;

    WidgetSizeStyle(int id, String name, int width, int height, String textFlow) {
        this.id = id;
        this.name = name;
        this.width = width;
        this.height = height;
        this.textFlow = textFlow;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public String getTextFlow() { return textFlow; }
    
    public float getAspectRatio() { 
        return (float) width / height; 
    }
    
    public boolean isBanner() { 
        return this == BANNER_WIDE || this == BANNER_TALL; 
    }
    
    public boolean isVerticalText() { 
        return textFlow.equals("vertical"); 
    }

    public static WidgetSizeStyle fromId(int id) {
        for (WidgetSizeStyle style : values()) {
            if (style.id == id) {
                return style;
            }
        }
        return STANDARD; // Default
    }
}
