package com.nti.nice_gallery.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.ArrayRes;
import androidx.annotation.StringRes;

import com.nti.nice_gallery.R;
import com.nti.nice_gallery.models.ModelFilesActionRequest;
import com.nti.nice_gallery.models.ModelFilesActionResponse;
import com.nti.nice_gallery.models.ModelMediaFile;

import java.util.function.Consumer;

import kotlin.jvm.functions.Function2;

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

    public void showChoicePath(String defaultPath, Consumer<String> onChoice, Runnable onCancel) {
        showPrompt(
                R.string.dialog_title_choice_path,
                defaultPath,
                onChoice,
                onCancel
        );
    }

    public void showActionConfirm(ModelFilesActionRequest request, Runnable onConfirm, Runnable onCancel) {
        if (request == null || request.action == null) {
            return;
        }

        Convert convert = new Convert(context);
        StringBuilder message = new StringBuilder();

        String actionStr = convert.enumValueToStringArrayValue(request.action, R.array.enum_files_actions);
        message.append(context.getString(R.string.format_message_action, actionStr));

        if (request.targetPath != null) {
            message.append("\n");
            message.append(context.getString(R.string.format_message_path, request.targetPath));
        }

        if (request.duplicateNamePolicy != null) {
            message.append("\n");
            message.append(context.getString(
                    R.string.format_message_duplicate_name_policy,
                    convert.enumValueToStringArrayValue(request.duplicateNamePolicy, R.array.enum_duplicate_name_policy)
            ));
        }

        if (request.files != null && !request.files.isEmpty()) {
            message.append(message.length() > 0 ? "\n\n" : "");
            message.append(context.getString(R.string.format_message_elements, request.files.size()));

            for (ModelMediaFile file : request.files) {
                message.append("\n");
                int fileFormat = file.isFolder ? R.string.format_message_file_type_folder
                        : file.isFile ? R.string.format_message_file_type_file
                        : R.string.format_message_file_type_other;
                message.append(context.getString(fileFormat, file.name));
            }
        }

        showYesNo(
                R.string.dialog_title_perform_an_action,
                message.toString(),
                onConfirm,
                onCancel
        );
    }

    public void showActionReport(ModelFilesActionResponse response) {

        StringBuilder message = new StringBuilder();

        if (response.globalError != null) {
            message.append((context.getString(R.string.format_message_global_error, response.globalError.getMessage())));
        }

        Function2<ReadOnlyList<ModelFilesActionResponse.FileInfo>, Integer, Object> printList = (list, header) -> {
            if (list != null && !list.isEmpty()) {
                message.append(message.length() > 0 ? "\n\n" : "");
                message.append(context.getString(header, list.size()));
                for (ModelFilesActionResponse.FileInfo file : list) {
                    message.append("\n");
                    int fileFormat = file.isFolder ? R.string.format_message_file_type_folder : R.string.format_message_file_type_file;
                    message.append(context.getString(fileFormat, file.name));
                }
            }
            return null;
        };

        printList.invoke(response.replacedFiles, R.string.format_message_replaced_files);
        printList.invoke(response.skippedFiles, R.string.format_message_skipped_files);
        printList.invoke(response.renamedFiles, R.string.format_message_renamed_files);
        printList.invoke(response.successHandledFiles, R.string.format_message_success_handled_files);

        if (response.fails != null && !response.fails.isEmpty()) {
            message.append(message.length() > 0 ? "\n\n" : "");
            message.append(context.getString(R.string.format_message_failed_files, response.fails.size()));
            for (ModelFilesActionResponse.Fail fileFail : response.fails) {
                message.append("\n");
                message.append(context.getString(R.string.format_message_file_fail_item, fileFail.file.name, fileFail.error.getMessage()));
            }
        }

        showInfo(R.string.dialog_title_action_report, message.toString());
    }
}
