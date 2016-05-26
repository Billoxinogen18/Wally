package com.wally.wally.components;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.wally.wally.R;

public class ColorPickerPopup implements View.OnClickListener {

    private static final String TAG = ColorPickerPopup.class.getSimpleName();
    private ColorPickerListener mListener;
    private PopupWindow mPopup;

    public void show(ColorPickerListener listener, View anchor) {
        mListener = listener;
        Context context = anchor.getContext();
        View pickerLayout = LayoutInflater.from(context).inflate(R.layout.color_picker_layout, null);
        setUpListenerOnItems(pickerLayout);
        // Creating the PopupWindow
        mPopup = new PopupWindow(
                pickerLayout,
                context.getResources().getDimensionPixelSize(R.dimen.color_picker_popup_width),
                context.getResources().getDimensionPixelSize(R.dimen.color_picker_popup_height));
        // Closes the popup window when touch outside.
        mPopup.setOutsideTouchable(true);
        mPopup.setFocusable(true);

        // Removes default background.
        mPopup.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mPopup.setElevation(context.getResources().getDimension(R.dimen.color_picker_popup_elevation));
        }
        mPopup.showAsDropDown(anchor);
        // popupWindow.showAsDropDown(anchor, 0, -anchor.getHeight());//, Gravity.LEFT | Gravity.TOP);
    }

    private void setUpListenerOnItems(View view) {
        if (view instanceof ImageView) {
            view.setOnClickListener(this);
        } else if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                setUpListenerOnItems(viewGroup.getChildAt(i));
            }
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
        int color = ((ColorDrawable) ((ImageView) v).getDrawable()).getColor();
        mListener.colorPicked(color);
        mPopup.dismiss();
        mListener = null;
    }


    public interface ColorPickerListener {
        void colorPicked(int color);
    }
}
