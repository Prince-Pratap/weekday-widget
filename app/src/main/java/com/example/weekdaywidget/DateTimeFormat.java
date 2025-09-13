package com.example.weekdaywidget;

public enum DateTimeFormat {
    DAY_ONLY(0, "EEEE"),
    DAY_DATE(1, "EEEE, d"),
    DAY_MONTH(2, "EEEE, MMMM"),
    FULL_DATE(3, "EEEE, MMMM d"),
    SHORT_DATE(4, "EEE, MMM d"),
    NUMERIC_DATE(5, "MM/dd/yyyy"),
    TIME_12H(6, "h:mm a"),
    TIME_24H(7, "HH:mm"),
    DAY_TIME_12H(8, "EEEE h:mm a"),
    DAY_TIME_24H(9, "EEEE HH:mm"),
    FULL_DATETIME(10, "EEE, MMM d, h:mm a");

    private final int id;
    private final String pattern;

    DateTimeFormat(int id, String pattern) {
        this.id = id;
        this.pattern = pattern;
    }

    public int getId() {
        return id;
    }

    public String getPattern() {
        return pattern;
    }

    public static DateTimeFormat fromId(int id) {
        for (DateTimeFormat format : values()) {
            if (format.id == id) {
                return format;
            }
        }
        return DAY_ONLY; // Default fallback
    }

    public boolean needsFrequentUpdates() {
        return this == TIME_12H || this == TIME_24H || 
               this == DAY_TIME_12H || this == DAY_TIME_24H || 
               this == FULL_DATETIME;
    }
}
