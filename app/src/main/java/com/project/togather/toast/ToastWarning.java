package com.project.togather.toast;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.project.togather.R;

public class ToastWarning {
    public ToastWarning(String message, Activity activity) {
        Toast toast = new Toast(activity);
        View view = LayoutInflater.from(activity)
                .inflate(R.layout.toast_warning, null);

        TextView tvMessage = view.findViewById(R.id.toastWarning_textView);
        tvMessage.setText(message);

        toast.setView(view);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.show();
    }
}
