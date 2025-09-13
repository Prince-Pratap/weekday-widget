# Enhanced Date/Time Widget

A customizable Android home screen widget that displays date and time in multiple formats with a beautiful pastel gradient background.

## Features

### 🎨 **Visual Design**
- Beautiful pastel pink gradient background with rounded corners
- Custom "Dancing Script" font with italic styling
- Auto-sizing text that adapts to widget size
- Responsive layout that works on different widget sizes

### 📅 **Multiple Date/Time Formats**
The widget supports 11 different display formats:

1. **Day Only** - `Monday`
2. **Day + Date** - `Monday, 15`
3. **Day + Month** - `Monday, September`
4. **Full Date** - `Monday, September 15`
5. **Short Date** - `Mon, Sep 15`
6. **Numeric Date** - `09/15/2024`
7. **12-Hour Time** - `3:30 PM`
8. **24-Hour Time** - `15:30`
9. **Day + 12h Time** - `Monday 3:30 PM`
10. **Day + 24h Time** - `Monday 15:30`
11. **Full Date + Time** - `Mon, Sep 15, 3:30 PM`

### ⚙️ **Configuration**
- Easy-to-use configuration activity
- Live preview of each format
- Tap widget to reconfigure anytime
- Settings saved per widget instance

### 🔋 **Battery Optimized**
- Smart update scheduling based on format type
- Date-only formats update once daily at midnight
- Time formats update every minute
- Uses exact alarms for precise timing

### 🛠️ **Technical Features**
- Proper error handling and fallbacks
- Resizable widget (horizontal and vertical)
- Clean, modern Android architecture
- Supports Android API 21+

## Installation

1. Build the project using Android Studio or Gradle
2. Install the APK on your device
3. Add the widget to your home screen
4. Configure your preferred format

## Usage

1. **Adding Widget**: Long press on home screen → Widgets → Date Widget
2. **Configuration**: The configuration screen will appear automatically, or tap the widget to reconfigure
3. **Resizing**: Long press the widget and drag the resize handles
4. **Format Selection**: Choose from 11 different date/time formats with live preview

## Project Structure

```
app/
├── src/main/
│   ├── java/com/example/weekdaywidget/
│   │   ├── WeekDayWidget.java          # Main widget provider
│   │   ├── WidgetConfigActivity.java   # Configuration activity
│   │   ├── DateTimeFormat.java         # Format definitions
│   │   └── FormatAdapter.java          # List adapter for formats
│   ├── res/
│   │   ├── drawable/
│   │   │   └── pastel_gradient.xml     # Background gradient
│   │   ├── font/
│   │   │   └── dancing_script.ttf      # Custom font
│   │   ├── layout/
│   │   │   ├── widget_layout.xml       # Widget layout
│   │   │   ├── activity_widget_config.xml # Config activity layout
│   │   │   └── format_list_item.xml    # Format list item layout
│   │   ├── values/
│   │   │   └── strings.xml             # String resources
│   │   └── xml/
│   │       └── weekday_widget_info.xml # Widget metadata
│   └── AndroidManifest.xml
├── build.gradle
└── proguard-rules.pro
```

## Fixed Issues from Original

✅ **Project Structure**: Added proper Android build configuration  
✅ **Font Resources**: Moved font to correct `res/font/` directory  
✅ **Drawable Fix**: Fixed gradient drawable with proper shape wrapper  
✅ **Update Optimization**: Smart scheduling based on format type  
✅ **Error Handling**: Added comprehensive error handling and logging  
✅ **Permissions**: Removed unused permissions, added necessary alarm permissions  
✅ **Configuration**: Added full configuration system with live preview  
✅ **Responsiveness**: Made widget resizable and text auto-sizing  

## Requirements

- Android API 21+ (Android 5.0 Lollipop)
- Android Studio Arctic Fox or later
- Gradle 8.0+

## License

This project is open source and available under the MIT License.

