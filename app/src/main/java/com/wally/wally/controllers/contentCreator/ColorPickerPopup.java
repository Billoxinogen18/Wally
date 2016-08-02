package com.wally.wally.controllers.contentCreator;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wally.wally.R;
import com.wally.wally.components.RevealPopup;

public class ColorPickerPopup extends RevealPopup implements View.OnClickListener {

    @SuppressWarnings("unused")
    private static final String TAG = ColorPickerPopup.class.getSimpleName();
    private static final ColorInfo[] colors = new ColorInfo[]{
            new ColorInfo(R.string.note_color_white, R.color.note_color_white, R.color.note_text_color_dark),
            new ColorInfo(R.string.note_color_red, R.color.note_color_red, R.color.note_text_color_light),
            new ColorInfo(R.string.note_color_orange, R.color.note_color_orange, R.color.note_text_color_light),
            new ColorInfo(R.string.note_color_yellow, R.color.note_color_yellow, R.color.note_text_color_dark),
            new ColorInfo(R.string.note_color_light_green, R.color.note_color_light_green, R.color.note_text_color_dark),
            new ColorInfo(R.string.note_color_green, R.color.note_color_green, R.color.note_text_color_light),
            new ColorInfo(R.string.note_color_blue, R.color.note_color_blue, R.color.note_text_color_light),
            new ColorInfo(R.string.note_color_purple, R.color.note_color_purple, R.color.note_text_color_light),
            new ColorInfo(R.string.note_color_pink, R.color.note_color_pink, R.color.note_text_color_light),
            new ColorInfo(R.string.note_color_blue_grey, R.color.note_color_blue_grey, R.color.note_text_color_light)
    };
    private ColorPickerListener mListener;

    public void show(final View anchor, ColorPickerListener listener) {
        mListener = listener;
        setUp(anchor, R.layout.color_picker_layout);
        setUpColors((ViewGroup) mContentLayout.findViewById(R.id.colors_container));
    }

    @Override
    protected void onDismiss() {
        mListener = null;
    }

    private void setUpColors(ViewGroup colorsContainer) {
        Log.wtf(TAG, "setUpColors: " + colorsContainer);
        Context context = colorsContainer.getContext();
        int padding = context.getResources().getDimensionPixelSize(R.dimen.picker_color_item_margin);

        int[] attrs = new int[]{R.attr.selectableItemBackgroundBorderless};
        TypedArray typedArray = context.obtainStyledAttributes(attrs);
        int backgroundResource = typedArray.getResourceId(0, 0);
        typedArray.recycle();

        for (ColorInfo colorInfo : colors) {
            Drawable drawable = colorInfo.create(context);
            TextView tv = new TextView(context);
            tv.setText(colorInfo.name);
            tv.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
            tv.setOnClickListener(this);
            tv.setPadding(padding, padding / 2, padding, padding / 2);
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(padding * 2 / 3);
            tv.setTag(colorInfo);
            tv.setTextColor(Color.WHITE);
            tv.setCompoundDrawablePadding(padding / 4);
            tv.setBackgroundResource(backgroundResource);

            colorsContainer.addView(tv);
        }
    }

    /**
     * ImageViews that are clicked by user
     */
    @Override
    public void onClick(View v) {
        if (mListener == null) {
            return;
        }
        ColorInfo ci = (ColorInfo) v.getTag();
        int color = ContextCompat.getColor(v.getContext(), ci.color);
        int textColor = ContextCompat.getColor(v.getContext(), ci.textColor);

        mListener.colorPicked(color, textColor);
        dismissPopup();
    }

    public interface ColorPickerListener {
        void colorPicked(int color, int textColor);
    }

    private static class ColorInfo {
        @StringRes
        int name;
        @ColorRes
        int color;
        @ColorRes
        int textColor;

        public ColorInfo(@StringRes int nameResId, @ColorRes int colorResId, @ColorRes int textColorResId) {
            this.name = nameResId;
            this.color = colorResId;
            this.textColor = textColorResId;
        }

        /**
         * This method creates Circular Drawable with A on it (Like it was on design)
         *
         * @param context to create drawable
         * @return Drawable representation to show color info
         */
        public Drawable create(Context context) {
            int size = context.getResources().getDimensionPixelSize(R.dimen.picker_color_item_size);
            int textSize = size * 2 / 3;

            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);

            Paint circlePaint = new Paint();
            // Set shadow at first
            circlePaint.setShadowLayer(5, 0, 0, ContextCompat.getColor(context, R.color.color_shadow));

            // Draw circle on canvas
            circlePaint.setColor(ContextCompat.getColor(context, color));
            circlePaint.setFlags(Paint.ANTI_ALIAS_FLAG);
            circlePaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(size / 2, size / 2 - 5, size / 2 - 10, circlePaint);

            // Now add text over circle
            Paint textPaint = new Paint();
            textPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
            textPaint.setColor(ContextCompat.getColor(context, textColor));
            textPaint.setTextSize(textSize);
            float textWidth = textPaint.measureText("A");
            canvas.drawText("A",
                    canvas.getWidth() / 2 - textWidth / 2,
                    ((canvas.getHeight() / 2) - ((textPaint.descent() + textPaint.ascent()) / 2)) - 5,
                    textPaint);

            return new BitmapDrawable(context.getResources(), bitmap);
        }
    }
}
