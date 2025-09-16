package com.example.weekdaywidget;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WidgetConfigActivity extends AppCompatActivity {
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private ListView formatListView;
    private ListView boxStyleListView;
    private ListView gradientListView;
    private ListView fontListView;
    private Button saveButton;
    private Button cancelButton;
    private DateTimeFormat selectedFormat = DateTimeFormat.DAY_ONLY;
    private BoxDesignStyle selectedBoxStyle = BoxDesignStyle.ROUNDED_CORNERS;
    private GradientStyle selectedGradient = GradientStyle.PASTEL_PINK;
    private FontStyle selectedFont = FontStyle.DANCING_SCRIPT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_config);

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
        setupBoxStyleList();
        setupGradientList();
        setupFontList();
        setupButtons();
    }

    private void initViews() {
        formatListView = findViewById(R.id.formatListView);
        boxStyleListView = findViewById(R.id.boxStyleListView);
        gradientListView = findViewById(R.id.gradientListView);
        fontListView = findViewById(R.id.fontListView);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);
    }

    private void setupFormatList() {
        List<FormatItem> formatItems = new ArrayList<>();
        
        for (DateTimeFormat format : DateTimeFormat.values()) {
            String preview = getPreviewText(format);
            String description = getFormatDescription(format);
            formatItems.add(new FormatItem(format, description, preview));
        }

        FormatAdapter adapter = new FormatAdapter(this, formatItems);
        formatListView.setAdapter(adapter);
        
        formatListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedFormat = formatItems.get(position).format;
                // Update selection visual feedback
                for (int i = 0; i < parent.getChildCount(); i++) {
                    parent.getChildAt(i).setSelected(i == position);
                }
            }
        });
        
        // Select first item by default
        formatListView.setItemChecked(0, true);
    }

    private void setupBoxStyleList() {
        List<String> boxStyleNames = new ArrayList<>();
        
        for (BoxDesignStyle style : BoxDesignStyle.values()) {
            boxStyleNames.add(style.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_list_item_single_choice, boxStyleNames);
        boxStyleListView.setAdapter(adapter);
        boxStyleListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        
        boxStyleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedBoxStyle = BoxDesignStyle.values()[position];
            }
        });
        
        // Select first item by default
        boxStyleListView.setItemChecked(0, true);
    }

    private void setupGradientList() {
        List<String> gradientNames = new ArrayList<>();
        
        for (GradientStyle gradient : GradientStyle.values()) {
            gradientNames.add(gradient.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_list_item_single_choice, gradientNames);
        gradientListView.setAdapter(adapter);
        gradientListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        
        gradientListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedGradient = GradientStyle.values()[position];
            }
        });
        
        // Select first item by default
        gradientListView.setItemChecked(0, true);
    }

    private void setupFontList() {
        List<String> fontNames = new ArrayList<>();
        
        for (FontStyle font : FontStyle.values()) {
            fontNames.add(font.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_list_item_single_choice, fontNames);
        fontListView.setAdapter(adapter);
        fontListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        
        fontListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedFont = FontStyle.values()[position];
            }
        });
        
        // Select first item by default
        fontListView.setItemChecked(0, true);
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
        // Save all selected options
        WeekDayWidgetSimple.saveWidgetFormat(this, appWidgetId, selectedFormat);
        WeekDayWidgetSimple.saveWidgetBoxStyle(this, appWidgetId, selectedBoxStyle);
        WeekDayWidgetSimple.saveWidgetGradient(this, appWidgetId, selectedGradient);
        WeekDayWidgetSimple.saveWidgetFont(this, appWidgetId, selectedFont);

        // Update the widget
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        WeekDayWidgetSimple.updateAppWidget(this, appWidgetManager, appWidgetId);

        // Return success result
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }

    static class FormatItem {
        final DateTimeFormat format;
        final String description;
        final String preview;

        FormatItem(DateTimeFormat format, String description, String preview) {
            this.format = format;
            this.description = description;
            this.preview = preview;
        }
    }
}
