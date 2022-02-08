package com.idan.teamusup.services;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.idan.teamusup.R;

public class MyPopupWindow {

    @SuppressLint("ClickableViewAccessibility")
    public static void createPopupWindow(Context context, View view, String text) {
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.layout_popup_window, null);
        TextView popupText = popupView.findViewById(R.id.text);
        popupText.setText(text);

        // create the popup window
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        final boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
        popupView.setOnTouchListener((v, event) -> {
            popupWindow.dismiss();
            return true;
        });
    }

}
