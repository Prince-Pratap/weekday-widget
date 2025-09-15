package com.example.weekdaywidget;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

public class FontAdapter extends RecyclerView.Adapter<FontAdapter.FontViewHolder> {
    private final Context context;
    private final List<FontStyle> fonts;
    private int selectedPosition = 0;
    private OnFontSelectedListener listener;

    public interface OnFontSelectedListener {
        void onFontSelected(FontStyle font);
    }

    public FontAdapter(Context context, List<FontStyle> fonts) {
        this.context = context;
        this.fonts = fonts;
    }

    public void setOnFontSelectedListener(OnFontSelectedListener listener) {
        this.listener = listener;
    }

    public void setSelectedPosition(int position) {
        int oldPosition = selectedPosition;
        selectedPosition = position;
        notifyItemChanged(oldPosition);
        notifyItemChanged(selectedPosition);
    }

    @NonNull
    @Override
    public FontViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.font_item, parent, false);
        return new FontViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FontViewHolder holder, int position) {
        FontStyle font = fonts.get(position);
        holder.bind(font, position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return fonts.size();
    }

    class FontViewHolder extends RecyclerView.ViewHolder {
        private final TextView fontPreview;
        private final TextView fontName;
        private final View downloadIndicator;

        public FontViewHolder(@NonNull View itemView) {
            super(itemView);
            fontPreview = itemView.findViewById(R.id.fontPreview);
            fontName = itemView.findViewById(R.id.fontName);
            downloadIndicator = itemView.findViewById(R.id.downloadIndicator);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    FontStyle font = fonts.get(position);
                    
                    if (font.isPreInstalled() || FontDownloader.isFontDownloaded(context, font)) {
                        setSelectedPosition(position);
                        if (listener != null) {
                            listener.onFontSelected(font);
                        }
                    } else {
                        // Download font
                        downloadFont(font);
                    }
                }
            });
        }

        public void bind(FontStyle font, boolean isSelected) {
            fontName.setText(font.getName());
            
            // Set font if available
            if (font.isPreInstalled()) {
                    try {
                        int fontResource = context.getResources().getIdentifier(
                            font.getFileName(), "font", context.getPackageName());
                        if (fontResource != 0) {
                            Typeface typeface = androidx.core.content.res.ResourcesCompat.getFont(context, fontResource);
                            fontPreview.setTypeface(typeface);
                        }
                    } catch (Exception e) {
                        // Fallback to default font
                    }
                downloadIndicator.setVisibility(View.GONE);
            } else if (FontDownloader.isFontDownloaded(context, font)) {
                // Load downloaded font
                try {
                    File fontFile = new File(context.getFilesDir(), "fonts/" + font.getFileName() + ".ttf");
                    Typeface typeface = Typeface.createFromFile(fontFile);
                    fontPreview.setTypeface(typeface);
                } catch (Exception e) {
                    // Fallback to default font
                }
                downloadIndicator.setVisibility(View.VISIBLE);
            } else {
                fontPreview.setTypeface(Typeface.DEFAULT);
                downloadIndicator.setVisibility(View.GONE);
            }
            
            // Highlight selected item
            if (isSelected) {
                itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.selected_background));
            } else {
                itemView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
            }
        }

        private void downloadFont(FontStyle font) {
            Toast.makeText(context, context.getString(R.string.downloading_font), Toast.LENGTH_SHORT).show();
            
            FontDownloader.downloadFont(context, font, new FontDownloader.FontDownloadListener() {
                @Override
                public void onFontDownloaded(String fontName, boolean success) {
                    if (success) {
                        Toast.makeText(context, context.getString(R.string.font_downloaded), Toast.LENGTH_SHORT).show();
                        notifyItemChanged(getAdapterPosition());
                    } else {
                        Toast.makeText(context, context.getString(R.string.font_download_failed), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onProgress(String fontName, int progress) {
                    // Could show progress bar here
                }
            });
        }
    }
}
