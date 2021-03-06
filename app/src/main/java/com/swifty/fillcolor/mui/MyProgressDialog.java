package com.swifty.fillcolor.mui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.swifty.fillcolor.R;


/**
 * Created by Swifty.Wang on 2015/5/29.
 */
public class MyProgressDialog extends Dialog {
    public static MyProgressDialog dialog;

    public MyProgressDialog(Context context) {
        super(context);
    }


    public static MyProgressDialog show(Context context, CharSequence title,
                                        CharSequence message,
                                        boolean cancelable, OnCancelListener cancelListener) {
        dialog = new MyProgressDialog(context);
        dialog.setCancelable(cancelable);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View detail_layout = View.inflate(
                context,
                R.layout.progress_dialog, null);
        dialog.setContentView(detail_layout);
        dialog.getWindow()
                .setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        TextView textView = (TextView) detail_layout.findViewById(R.id.progress_text);
        textView.setText(message);
        dialog.show();
        dialog.setOnCancelListener(cancelListener);
        return dialog;
    }

    public static MyProgressDialog show(Context context, CharSequence title,
                                        CharSequence message) {
        return show(context, title, message, false);
    }

    public static MyProgressDialog show(Context context, CharSequence title,
                                        CharSequence message, boolean cancelable) {
        return show(context, title, message, cancelable, null);
    }

    public static void DismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
