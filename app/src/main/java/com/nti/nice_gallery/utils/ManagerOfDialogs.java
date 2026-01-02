package com.nti.nice_gallery.utils;

import android.app.AlertDialog;
import android.content.Context;

import androidx.annotation.ArrayRes;
import androidx.annotation.StringRes;

import com.nti.nice_gallery.R;

import java.util.function.Consumer;

public class ManagerOfDialogs {

    private final Context context;

    public ManagerOfDialogs(Context context) {
        this.context = context;
    }

    public void showInfo(@StringRes int title, @StringRes int message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.dialog_button_ok, (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .create()
                .show();
    }

    public void showInfo(@StringRes int title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.dialog_button_ok, (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .create()
                .show();
    }

    public void showInfo(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.dialog_button_ok, (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .create()
                .show();
    }

    public void showYesNo(@StringRes int title, @StringRes int message, Runnable onYes, Runnable onNo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.dialog_button_yes, (dialog, which) -> {
                    dialog.dismiss();
                    if (onYes != null) {
                        onYes.run();
                    }
                })
                .setNegativeButton(R.string.dialog_button_no, (dialog, which) -> {
                    dialog.dismiss();
                    if (onNo != null) {
                        onNo.run();
                    }
                })
                .setCancelable(false)
                .create()
                .show();
    }

    public void showYesNo(@StringRes int title, String message, Runnable onYes, Runnable onNo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.dialog_button_yes, (dialog, which) -> {
                    dialog.dismiss();
                    if (onYes != null) {
                        onYes.run();
                    }
                })
                .setNegativeButton(R.string.dialog_button_no, (dialog, which) -> {
                    dialog.dismiss();
                    if (onNo != null) {
                        onNo.run();
                    }
                })
                .setCancelable(false)
                .create()
                .show();
    }

    public void showYesNo(String title, String message, Runnable onYes, Runnable onNo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.dialog_button_yes, (dialog, which) -> {
                    dialog.dismiss();
                    if (onYes != null) {
                        onYes.run();
                    }
                })
                .setNegativeButton(R.string.dialog_button_no, (dialog, which) -> {
                    dialog.dismiss();
                    if (onNo != null) {
                        onNo.run();
                    }
                })
                .setCancelable(false)
                .create()
                .show();
    }

    public void showChooseOne(@StringRes int title, @ArrayRes int variants, int selectedIndex, Consumer<Integer> onChosen) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setSingleChoiceItems(variants, selectedIndex, (dialog, which) -> {
                    dialog.dismiss();
                    if (onChosen != null) {
                        onChosen.accept(which);
                    }
                })
                .create()
                .show();
    }

    public void showChooseOne(@StringRes int title, String[] variants, int selectedIndex, Consumer<Integer> onChosen) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setSingleChoiceItems(variants, selectedIndex, (dialog, which) -> {
                    dialog.dismiss();
                    if (onChosen != null) {
                        onChosen.accept(which);
                    }
                })
                .create()
                .show();
    }

    public void showChooseOne(String title, String[] variants, int selectedIndex, Consumer<Integer> onChosen) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setSingleChoiceItems(variants, selectedIndex, (dialog, which) -> {
                    dialog.dismiss();
                    if (onChosen != null) {
                        onChosen.accept(which);
                    }
                })
                .create()
                .show();
    }
}
