package com.example.weekdaywidget;

public enum FontStyle {
    DANCING_SCRIPT(1, "Dancing Script", "dancing_script", true),
    ROBOTO(2, "Roboto", "roboto", false),
    ROBOTO_SLAB(3, "Roboto Slab", "roboto_slab", false),
    OPEN_SANS(4, "Open Sans", "open_sans", false),
    LATO(5, "Lato", "lato", false),
    MONTSERRAT(6, "Montserrat", "montserrat", false),
    POPPINS(7, "Poppins", "poppins", false),
    NUNITO(8, "Nunito", "nunito", false),
    PLAYFAIR_DISPLAY(9, "Playfair Display", "playfair_display", false),
    MERRIWEATHER(10, "Merriweather", "merriweather", false),
    OSWALD(11, "Oswald", "oswald", false),
    RALEWAY(12, "Raleway", "raleway", false);

    private final int id;
    private final String name;
    private final String fileName;
    private final boolean isPreInstalled;

    FontStyle(int id, String name, String fileName, boolean isPreInstalled) {
        this.id = id;
        this.name = name;
        this.fileName = fileName;
        this.isPreInstalled = isPreInstalled;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getFileName() { return fileName; }
    public boolean isPreInstalled() { return isPreInstalled; }
    
    public String getFontPath() {
        return "@font/" + fileName;
    }

    public static FontStyle fromId(int id) {
        for (FontStyle style : values()) {
            if (style.id == id) {
                return style;
            }
        }
        return DANCING_SCRIPT; // Default
    }
}
