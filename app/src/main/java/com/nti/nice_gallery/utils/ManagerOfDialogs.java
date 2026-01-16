package com.nti.nice_gallery.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

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

    public void showPrompt(@StringRes int title, String defaultText, Consumer<String> onOk, Runnable onCancel) {
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(defaultText);

        FrameLayout container = new FrameLayout(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        Convert convert = new Convert(context);
        int marginSide = convert.dpToPx(16);
        params.leftMargin = marginSide;
        params.rightMargin = marginSide;

        input.setLayoutParams(params);
        container.addView(input);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setView(container)
                .setPositiveButton(R.string.dialog_button_ok, (dialog1, which) -> {
                    String result = input.getText().toString();
                    if (onOk != null) {
                        onOk.accept(result);
                    }
                })
                .setNegativeButton(R.string.dialog_button_cancel, (dialogInterface, i) -> {
                    if (onCancel != null) {
                        onCancel.run();
                    }
                })
                .create();

        dialog.show();
    }

    public void showPrompt(String title, String defaultText, Consumer<String> onOk, Runnable onCancel) {
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(defaultText);

        FrameLayout container = new FrameLayout(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        Convert convert = new Convert(context);
        int marginSide = convert.dpToPx(16);
        params.leftMargin = marginSide;
        params.rightMargin = marginSide;

        input.setLayoutParams(params);
        container.addView(input);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setView(container)
                .setPositiveButton(R.string.dialog_button_ok, (dialog1, which) -> {
                    String result = input.getText().toString();
                    if (onOk != null) {
                        onOk.accept(result);
                    }
                })
                .setNegativeButton(R.string.dialog_button_cancel, (dialogInterface, i) -> {
                    if (onCancel != null) {
                        onCancel.run();
                    }
                })
                .create();

        dialog.show();
    }
}
