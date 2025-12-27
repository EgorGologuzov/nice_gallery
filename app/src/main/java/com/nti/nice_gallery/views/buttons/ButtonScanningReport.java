package com.nti.nice_gallery.views.buttons;

import android.content.Context;
import android.util.AttributeSet;

import com.nti.nice_gallery.R;
import com.nti.nice_gallery.models.ModelGetFilesResponse;
import com.nti.nice_gallery.models.ModelMediaFile;
import com.nti.nice_gallery.models.ModelStorage;
import com.nti.nice_gallery.utils.ManagerOfDialogs;

import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;

import kotlin.jvm.functions.Function1;

public class ButtonScanningReport extends ButtonBase {

    private ModelGetFilesResponse source;

    private ManagerOfDialogs managerOfDialogs;

    public ButtonScanningReport(Context context) {
        super(context);
        init();
    }

    public ButtonScanningReport(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ButtonScanningReport(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        managerOfDialogs = new ManagerOfDialogs(getContext());
        setImageResource(R.drawable.baseline_info_24);
        setOnClickListener(v -> onClick());
    }

    public void setSource(ModelGetFilesResponse source) {
        this.source = source;
    }

    private void onClick() {
        Supplier<String> generateReport = () -> {
            if (source == null) {
                return getContext().getString(R.string.message_scanning_report_source_not_set);
            }

            StringBuilder builder = new StringBuilder();
            Function1<String, Integer> addLine = line -> { builder.append(line); builder.append("\n"); return 0; };

            addLine.invoke("Длилось: " + ChronoUnit.MILLIS.between(source.scanningStartedAt, source.scanningFinishedAt) / 1000f + " сек. (начато: " + source.scanningStartedAt + ")");
            addLine.invoke("");

            addLine.invoke("Найдено хранилищ (" + source.scannedStorages.size() + "):");
            addLine.invoke("");

            for (ModelStorage storage : source.scannedStorages) {
                addLine.invoke((storage.error == null ? "[ OK ]" : "[ ERR ]") + " " + storage.name);
            }

            int countFolders = 0, countFiles = 0;

            for (ModelMediaFile file : source.files) {
                if (file.type == ModelMediaFile.Type.Folder) {
                    countFolders++;
                } else {
                    countFiles++;
                }
            }

            addLine.invoke("");
            addLine.invoke("Найдено папок (" + countFolders + ")");
            addLine.invoke("");

            addLine.invoke("Найдено файлов (" + countFiles + ")");
            addLine.invoke("");

            addLine.invoke("Файлов с ошибками (" + source.filesWithErrors.size() + "):");
            addLine.invoke("");

            for (ModelMediaFile file : source.filesWithErrors) {
                addLine.invoke("- " + file.name + ", " + file.error.getMessage().substring(0, 50));
            }

            return builder.toString();
        };

        managerOfDialogs.showInfo(
                R.string.dialog_title_scanning_report,
                generateReport.get()
        );
    }
}
