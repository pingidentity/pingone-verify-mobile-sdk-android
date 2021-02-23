package com.pingidentity.sample.P1VerifyApp.utils;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.pingidentity.sample.P1VerifyApp.R;

public class AlertUtil {

    public static void showAlert(@NonNull final Context context, int title, int message) {
        showAlert(context, context.getString(title), context.getString(message), null);
    }

    public static void showAlert(@NonNull final Context context, String title, String message) {
        showAlert(context, title == null ? context.getString(R.string.error_title) : title, message, null);
    }

    public static void showAlert(@NonNull final Context context, int title, int message, final Runnable onPositiveAction) {
        showAlert(context, context.getString(title), context.getString(message), onPositiveAction);
    }

    public static void showAlert(@NonNull final Context context, String title, String message, final Runnable onPositiveAction) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(R.string.home_cancel, (dialog, which) -> dialog.dismiss());
        if (onPositiveAction != null) {
            builder.setPositiveButton(R.string.home_continue, (dialog, which) -> {
                onPositiveAction.run();
                dialog.dismiss();
            });
        }
        builder.show();
    }

}
