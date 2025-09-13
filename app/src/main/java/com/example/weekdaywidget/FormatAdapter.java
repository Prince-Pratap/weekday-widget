package com.example.weekdaywidget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class FormatAdapter extends BaseAdapter {
    private final Context context;
    private final List<WidgetConfigActivity.FormatItem> formatItems;
    private final LayoutInflater inflater;

    public FormatAdapter(Context context, List<WidgetConfigActivity.FormatItem> formatItems) {
        this.context = context;
        this.formatItems = formatItems;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return formatItems.size();
    }

    @Override
    public Object getItem(int position) {
        return formatItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.format_list_item, parent, false);
            holder = new ViewHolder();
            holder.descriptionText = convertView.findViewById(R.id.descriptionText);
            holder.previewText = convertView.findViewById(R.id.previewText);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        WidgetConfigActivity.FormatItem item = formatItems.get(position);
        holder.descriptionText.setText(item.description);
        holder.previewText.setText(item.preview);

        return convertView;
    }

    static class ViewHolder {
        TextView descriptionText;
        TextView previewText;
    }
}
