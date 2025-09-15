package com.example.weekdaywidget;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EnhancedWidgetConfigActivity extends AppCompatActivity {
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private ListView formatListView;
    private RecyclerView gradientRecyclerView;
    private RecyclerView fontRecyclerView;
    private Button saveButton;
    private Button cancelButton;
    private RelativeLayout widgetPreview;
    private TextView previewText;
    
    private DateTimeFormat selectedFormat = DateTimeFormat.DAY_ONLY;
    private GradientStyle selectedGradient = GradientStyle.PASTEL_PINK;
    private FontStyle selectedFont = FontStyle.DANCING_SCRIPT;
    
    private GradientAdapter gradientAdapter;
    private FontAdapter fontAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_config_enhanced);

        // Set result to CANCELED initially
        setResult(RESULT_CANCELED);

        // Get widget ID from intent
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, 
                                      AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        initViews();
        setupFormatList();
        setupGradientRecyclerView();
        setupFontRecyclerView();
        setupButtons();
        updatePreview();
    }

    private void initViews() {
        formatListView = findViewById(R.id.formatListView);
        gradientRecyclerView = findViewById(R.id.gradientRecyclerView);
        fontRecyclerView = findViewById(R.id.fontRecyclerView);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);
        widgetPreview = findViewById(R.id.widgetPreview);
        previewText = findViewById(R.id.previewText);
    }

    private void setupFormatList() {
        List<WidgetConfigActivity.FormatItem> formatItems = new ArrayList<>();
        
        for (DateTimeFormat format : DateTimeFormat.values()) {
            String preview = getPreviewText(format);
            String description = getFormatDescription(format);
            formatItems.add(new WidgetConfigActivity.FormatItem(format, description, preview));
        }

        FormatAdapter adapter = new FormatAdapter(this, formatItems);
        formatListView.setAdapter(adapter);
        
        formatListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedFormat = formatItems.get(position).format;
                updatePreview();
                // Update selection visual feedback
                for (int i = 0; i < parent.getChildCount(); i++) {
                    parent.getChildAt(i).setSelected(i == position);
                }
            }
        });
        
        // Select first item by default
        formatListView.setItemChecked(0, true);
    }

    private void setupGradientRecyclerView() {
        List<GradientStyle> gradients = Arrays.asList(GradientStyle.values());
        gradientAdapter = new GradientAdapter(this, gradients);
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        gradientRecyclerView.setLayoutManager(layoutManager);
        gradientRecyclerView.setAdapter(gradientAdapter);
        
        gradientAdapter.setOnGradientSelectedListener(new GradientAdapter.OnGradientSelectedListener() {
            @Override
            public void onGradientSelected(GradientStyle gradient) {
                selectedGradient = gradient;
                updatePreview();
            }
        });
    }

    private void setupFontRecyclerView() {
        List<FontStyle> fonts = Arrays.asList(FontStyle.values());
        fontAdapter = new FontAdapter(this, fonts);
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        fontRecyclerView.setLayoutManager(layoutManager);
        fontRecyclerView.setAdapter(fontAdapter);
        
        fontAdapter.setOnFontSelectedListener(new FontAdapter.OnFontSelectedListener() {
            @Override
            public void onFontSelected(FontStyle font) {
                selectedFont = font;
                updatePreview();
            }
        });
    }

    private void setupButtons() {
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveConfiguration();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void updatePreview() {
        // Update text
        String previewTextValue = getPreviewText(selectedFormat);
        previewText.setText(previewTextValue);
        
        // Update background
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(8f);
        
        int startColor = android.graphics.Color.parseColor(selectedGradient.getStartColor());
        int endColor = android.graphics.Color.parseColor(selectedGradient.getEndColor());
        
        GradientDrawable.Orientation orientation = getOrientationFromAngle(selectedGradient.getAngle());
        drawable.setOrientation(orientation);
        drawable.setColors(new int[]{startColor, endColor});
        
        widgetPreview.setBackground(drawable);
    }

    private GradientDrawable.Orientation getOrientationFromAngle(int angle) {
        switch (angle) {
            case 0: return GradientDrawable.Orientation.LEFT_RIGHT;
            case 45: return GradientDrawable.Orientation.BL_TR;
            case 90: return GradientDrawable.Orientation.BOTTOM_TOP;
            case 135: return GradientDrawable.Orientation.BR_TL;
            case 180: return GradientDrawable.Orientation.RIGHT_LEFT;
            case 225: return GradientDrawable.Orientation.TR_BL;
            case 270: return GradientDrawable.Orientation.TOP_BOTTOM;
            case 315: return GradientDrawable.Orientation.TL_BR;
            default: return GradientDrawable.Orientation.TOP_BOTTOM;
        }
    }

    private String getPreviewText(DateTimeFormat format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format.getPattern(), Locale.getDefault());
            return sdf.format(new Date());
        } catch (Exception e) {
            return "Preview Error";
        }
    }

    private String getFormatDescription(DateTimeFormat format) {
        switch (format) {
            case DAY_ONLY: return getString(R.string.format_day_only);
            case DAY_DATE: return getString(R.string.format_day_date);
            case DAY_MONTH: return getString(R.string.format_day_month);
            case FULL_DATE: return getString(R.string.format_full_date);
            case SHORT_DATE: return getString(R.string.format_short_date);
            case NUMERIC_DATE: return getString(R.string.format_numeric_date);
            case TIME_12H: return getString(R.string.format_time_12h);
            case TIME_24H: return getString(R.string.format_time_24h);
            case DAY_TIME_12H: return getString(R.string.format_day_time_12h);
            case DAY_TIME_24H: return getString(R.string.format_day_time_24h);
            case FULL_DATETIME: return getString(R.string.format_full_datetime);
            default: return "Unknown Format";
        }
    }

    private void saveConfiguration() {
        // Save all selections
        WeekDayWidget.saveWidgetFormat(this, appWidgetId, selectedFormat);
        WeekDayWidget.saveWidgetGradient(this, appWidgetId, selectedGradient);
        WeekDayWidget.saveWidgetFont(this, appWidgetId, selectedFont);

        // Update the widget
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        WeekDayWidget.updateAppWidget(this, appWidgetManager, appWidgetId);

        // Return success result
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }
}
