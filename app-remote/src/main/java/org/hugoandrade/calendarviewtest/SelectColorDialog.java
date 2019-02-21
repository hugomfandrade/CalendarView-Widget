package org.hugoandrade.calendarviewtest;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.hugoandrade.calendarviewtest.utils.ColorUtils;

public class SelectColorDialog {

    @SuppressWarnings("unused")
    private static final String TAG = SelectColorDialog.class.getSimpleName();

    private Context mContext;
    private OnColorSelectedListener mListener;

    private android.app.AlertDialog alert;
    private OptionsAdapter mOptionsAdapter;

    SelectColorDialog(Context context) {
        mContext = context;

        buildPlan();
    }

    private void buildPlan() {

        View dialogView = View.inflate(mContext, R.layout.dialog_select_color, null);

        RecyclerView rvColors = dialogView.findViewById(R.id.rv_colors);
        rvColors.setHasFixedSize(true);
        rvColors.setLayoutManager(new GridLayoutManager(mContext, 6,  LinearLayoutManager.VERTICAL, false));

        mOptionsAdapter = new OptionsAdapter(ColorUtils.mColors);
        rvColors.setAdapter(mOptionsAdapter);

        View tvCancel = dialogView.findViewById(R.id.tv_cancel);
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
            }
        });

        // Initialize and build the AlertBuilderDialog
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mContext)
                .setView(dialogView);
        alert = builder.create();
        if (alert.getWindow() != null)
            alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    public void show() {
        alert.show();
    }

    void setOnColorSelectedListener(OnColorSelectedListener listener) {
        mListener = listener;
    }

    public void setSelectedColor(int selectedColor) {
        mOptionsAdapter.setSelectedColor(selectedColor);
    }

    interface OnColorSelectedListener {
        void onColorSelected(int color);
    }

    private class OptionsAdapter extends RecyclerView.Adapter<OptionsAdapter.ViewHolder> {

        final int[] mColors;
        int mSelectedItem;

        OptionsAdapter(int... colors) {
            mColors = colors;
            mSelectedItem = 0;
        }

        @Override
        public OptionsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater vi = LayoutInflater.from(parent.getContext());
            return new ViewHolder(vi.inflate(R.layout.list_item_color, parent, false));
        }

        @Override
        public void onBindViewHolder(OptionsAdapter.ViewHolder holder, int position) {
            int color = mColors[holder.getAdapterPosition()];

            holder.cardViewInner.setCardBackgroundColor(color);

            if (holder.getAdapterPosition() == mSelectedItem) {
                holder.cardViewOuter.setCardBackgroundColor(Color.RED);
            }
            else {
                holder.cardViewOuter.setCardBackgroundColor(Color.TRANSPARENT);
            }
        }

        @Override
        public int getItemCount() {
            return mColors.length;
        }

        void setSelectedItem(int position) {
            int oldPosition = mSelectedItem;
            mSelectedItem = position;

            notifyItemChanged(oldPosition);
            notifyItemChanged(mSelectedItem);
        }

        int getSelectedColor() {
            return mColors[mSelectedItem];
        }

        void setSelectedColor(int selectedColor) {
            for (int i = 0 ; i < mColors.length ; i++)  {
                int color = mColors[i];
                if (color == selectedColor) {
                    setSelectedItem(i);
                    return;
                }
            }
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            CardView cardViewInner;
            CardView cardViewOuter;

            ViewHolder(View itemView) {
                super(itemView);

                itemView.setOnClickListener(this);

                cardViewInner = itemView.findViewById(R.id.cardView_inner);
                cardViewOuter = itemView.findViewById(R.id.cardView_outer);
            }

            @Override
            public void onClick(View v) {
                int oldPosition = mSelectedItem;
                mSelectedItem = getAdapterPosition();

                notifyItemChanged(oldPosition);
                notifyItemChanged(mSelectedItem);

                if (mListener != null)
                    mListener.onColorSelected(mColors[mSelectedItem]);
                alert.dismiss();
            }
        }
    }

    static class Builder {

        private final Context mContext;
        private OnColorSelectedListener mListener;
        private int mColor;

        static Builder instance(Context context) {
            return new Builder(context);
        }

        Builder(Context context) {
            mContext = context;
        }

        Builder setOnColorSelectedListener(OnColorSelectedListener listener) {
            mListener = listener;
            return this;
        }

        SelectColorDialog create() {
            SelectColorDialog dialog = new SelectColorDialog(mContext);
            dialog.setSelectedColor(mColor);
            dialog.setOnColorSelectedListener(mListener);
            return dialog;
        }

        public Builder setSelectedColor(int color) {
            mColor = color;
            return this;
        }
    }
}
