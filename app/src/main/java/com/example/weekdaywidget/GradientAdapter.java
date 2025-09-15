package com.example.weekdaywidget;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GradientAdapter extends RecyclerView.Adapter<GradientAdapter.GradientViewHolder> {
    private final Context context;
    private final List<GradientStyle> gradients;
    private int selectedPosition = 0;
    private OnGradientSelectedListener listener;

    public interface OnGradientSelectedListener {
        void onGradientSelected(GradientStyle gradient);
    }

    public GradientAdapter(Context context, List<GradientStyle> gradients) {
        this.context = context;
        this.gradients = gradients;
    }

    public void setOnGradientSelectedListener(OnGradientSelectedListener listener) {
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
    public GradientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.gradient_item, parent, false);
        return new GradientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GradientViewHolder holder, int position) {
        GradientStyle gradient = gradients.get(position);
        holder.bind(gradient, position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return gradients.size();
    }

    class GradientViewHolder extends RecyclerView.ViewHolder {
        private final View gradientPreview;
        private final TextView gradientName;

        public GradientViewHolder(@NonNull View itemView) {
            super(itemView);
            gradientPreview = itemView.findViewById(R.id.gradientPreview);
            gradientName = itemView.findViewById(R.id.gradientName);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    setSelectedPosition(position);
                    listener.onGradientSelected(gradients.get(position));
                }
            });
        }

        public void bind(GradientStyle gradient, boolean isSelected) {
            // Create gradient programmatically
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setCornerRadius(8f);
            
            int startColor = android.graphics.Color.parseColor(gradient.getStartColor());
            int endColor = android.graphics.Color.parseColor(gradient.getEndColor());
            
            GradientDrawable.Orientation orientation = getOrientationFromAngle(gradient.getAngle());
            drawable.setOrientation(orientation);
            drawable.setColors(new int[]{startColor, endColor});
            
            gradientPreview.setBackground(drawable);
            gradientName.setText(gradient.getName());
            
            // Highlight selected item
            if (isSelected) {
                itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.selected_background));
            } else {
                itemView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
            }
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
    }
}
