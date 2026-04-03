package com.nti.nice_gallery.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ArrayRes;
import androidx.annotation.StringRes;

import com.nti.nice_gallery.R;
import com.nti.nice_gallery.data.IManagerOfFiles;
import com.nti.nice_gallery.data.IManagerOfSettings;
import com.nti.nice_gallery.data.ManagerOfFiles;
import com.nti.nice_gallery.data.ManagerOfSettings;
import com.nti.nice_gallery.data.ManagerOfSettings_Test1;
import com.nti.nice_gallery.models.ModelFilesActionRequest;
import com.nti.nice_gallery.models.ModelFilesActionResponse;
import com.nti.nice_gallery.models.ModelFilters;
import com.nti.nice_gallery.models.ModelGetFilesRequest;
import com.nti.nice_gallery.models.ModelGetFilesResponse;
import com.nti.nice_gallery.models.ModelMediaFile;
import com.nti.nice_gallery.views.ViewInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import kotlin.jvm.functions.Function2;
import kotlin.jvm.functions.Function3;

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
        defaultPath = defaultPath != null ? defaultPath : ManagerOfFiles.PATH_ROOT;
        defaultPath = (defaultPath + "/").replace("//", "/");

        View layout = LayoutInflater.from(context).inflate(R.layout.dialog_choice_path, null);

        EditText inputPath = layout.findViewById(R.id.inputPath);
        LinearLayout containerChildPaths = layout.findViewById(R.id.containerChildPaths);
        LinearLayout containerHistoryPaths = layout.findViewById(R.id.containerHistoryPaths);
        ViewInfo info1 = new ViewInfo(context);
        ViewInfo info2 = new ViewInfo(context);

        final IManagerOfFiles managerOfFiles = new ManagerOfFiles(context);
        final ManagerOfThreads managerOfThreads = new ManagerOfThreads(context);
        final IManagerOfSettings managerOfSettings = new ManagerOfSettings_Test1(context);

        Consumer<LinearLayout> clearContainer = container -> {
            int childCount = container.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = container.getChildAt(i);
                child.clearAnimation();
            }
            container.removeAllViews();
        };

        Function3<LinearLayout, List<String>, ViewInfo, Object> renderPaths = (container, paths, noPathsInfo) -> {
            clearContainer.accept(container);

            if (paths == null || paths.isEmpty()) {
                container.addView(noPathsInfo);
                return null;
            }

            for (String path : paths) {
                TextView item = (TextView) LayoutInflater.from(context).inflate(R.layout.dialog_choice_path_item_path, container, false);

                item.setText(path);
                item.setOnClickListener(v -> {
                    final Animation animation = AnimationUtils.loadAnimation(v.getContext(), R.anim.scale_down_and_back);
                    v.startAnimation(animation);
                    String pathWithSlash = !path.endsWith("/") ? path + "/" : path;
                    inputPath.setText(pathWithSlash);
                    inputPath.setSelection(inputPath.getText().length());
                });

                container.addView(item);
            }

            return null;
        };

        Consumer<String> onPathChange = new Consumer<String>() {
            private String currentPath = null;
            private List<String> childPaths = null;
            private List<String> filteredPaths = null;

            @Override
            public void accept(String newPath) {
                final int DEFAULT_ANDROID_PATH_PREFIX_LENGTH = 9;

                int currentPathLastSlash = currentPath != null ? currentPath.lastIndexOf('/') : -1;
                int newPathLastSlash = newPath != null ? newPath.lastIndexOf('/') : -1;
                String currentPathBase = currentPathLastSlash >= 0 ? currentPath.substring(0, currentPathLastSlash) : null;
                String newPathBase = newPathLastSlash >= 0 ? newPath.substring(0, newPathLastSlash) : null;

                if (newPath == null || newPath.length() <= DEFAULT_ANDROID_PATH_PREFIX_LENGTH) {
                    newPath = ManagerOfFiles.PATH_ROOT;
                }

                if (!Objects.equals(currentPathBase, newPathBase)) {
                    currentPath = newPath;
                    loadChildPaths(() -> {
                        filterPaths();
                        renderPaths.invoke(containerChildPaths, filteredPaths, info1);
                    });
                } else if (!Objects.equals(currentPath, newPath)) {
                    currentPath = newPath;
                    filterPaths();
                    renderPaths.invoke(containerChildPaths, filteredPaths, info1);
                }
            }

            private void loadChildPaths(Runnable callback) {
                String currentLoadPath = null;
                int currentPathLastSlash = currentPath != null ? currentPath.lastIndexOf('/') : -1;
                String currentPathBase = currentPathLastSlash >= 0 ? currentPath.substring(0, currentPathLastSlash) : null;

                if (currentPathBase == null || currentPathBase.isEmpty()) {
                    currentLoadPath = ManagerOfFiles.PATH_ROOT;
                } else {
                    currentLoadPath = currentPathBase;
                }

                ModelFilters filters = new ModelFilters(
                        true,
                        new ReadOnlyList<>(new ModelMediaFile.Type[] { ModelMediaFile.Type.Folder }),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );

                ModelGetFilesRequest request = new ModelGetFilesRequest(
                        currentLoadPath,
                        null,
                        filters,
                        ModelGetFilesRequest.SortVariant.ByName,
                        true
                );

                Consumer<ModelGetFilesResponse> onLoaded = response -> {
                    managerOfThreads.runOnUiThread(() -> {
                        if (response.error == null && response.files != null && !response.files.isEmpty()) {
                            childPaths = response.files.stream().map(f -> f.path).collect(Collectors.toList());
                        }
                        if (callback != null) {
                            callback.run();
                        }
                    });
                };

                managerOfFiles.getFilesAsync(request, onLoaded);
            }

            private void filterPaths() {
                if (childPaths != null) {
                    String currentPathLower = currentPath.toLowerCase();
                    filteredPaths = childPaths.stream().filter(p -> {
                        String pathLower = p.toLowerCase();
                        return pathLower.startsWith(currentPathLower);
                    }).collect(Collectors.toList());
                } else {
                    filteredPaths = null;
                }
            }
        };

        TextWatcher pathEditWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                onPathChange.accept(s.toString());
            }
        };

        Runnable loadPathsHistory = () -> {
            ReadOnlyList<String> history = managerOfSettings.getPathsHistory();
            List<String> historyFinal = new ArrayList<>();

            if (history != null) {
                for (int i = history.size() - 1; i >= 0; i--) {
                    historyFinal.add(history.get(i));
                }
            }

            renderPaths.invoke(containerHistoryPaths, historyFinal, info2);
        };

        inputPath.setText(defaultPath);
        inputPath.addTextChangedListener(pathEditWatcher);

        info1.setIcon(R.drawable.baseline_error_24);
        info1.setMessage(R.string.message_no_child_folders);
        info1.setIconVisibility(true);
        info1.setProgressBarVisibility(false);
        info2.setIcon(R.drawable.baseline_error_24);
        info2.setMessage(R.string.message_no_last_folders);
        info2.setIconVisibility(true);
        info2.setProgressBarVisibility(false);

        onPathChange.accept(defaultPath);
        loadPathsHistory.run();

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(R.string.dialog_title_choice_path)
                .setView(layout)
                .setPositiveButton(R.string.dialog_button_ok, (dialog1, which) -> {
                    String result = inputPath.getText().toString();
                    if (!result.isEmpty()) {
                        managerOfSettings.savePathToHistory(result);
                    }
                    if (onChoice != null) {
                        onChoice.accept(result);
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
