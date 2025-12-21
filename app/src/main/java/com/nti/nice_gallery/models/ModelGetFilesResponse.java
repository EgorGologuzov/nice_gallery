package com.nti.nice_gallery.models;

import kotlin.jvm.functions.Function1;

public class ModelGetFilesResponse {

    public final ReadOnlyList<ModelMediaFile> files;
    public final ReadOnlyList<ModelStorage> scannedStorages;
    public final ReadOnlyList<ModelMediaFile> filesWithErrors;
    public final ReadOnlyList<ModelStorage> storagesWithErrors;

    public ModelGetFilesResponse(
            ReadOnlyList<ModelMediaFile> files,
            ReadOnlyList<ModelStorage> scannedStorages,
            ReadOnlyList<ModelMediaFile> filesWithErrors,
            ReadOnlyList<ModelStorage> storagesWithErrors
    ) {
        this.files = files;
        this.scannedStorages = scannedStorages;
        this.filesWithErrors = filesWithErrors;
        this.storagesWithErrors = storagesWithErrors;
    }

    public String toStringReport() {
        StringBuilder builder = new StringBuilder();
        Function1<String, Integer> addLine = line -> { builder.append(line); builder.append("\n"); return 0; };

        addLine.invoke("Найдено хранилищ (" + scannedStorages.size() + "):");
        addLine.invoke("");

        for (ModelStorage storage : scannedStorages) {
            addLine.invoke((storage.error == null ? "[ OK ]" : "[ ERR ]") + " " + storage.name);
        }

        int countFolders = 0, countFiles = 0;

        for (ModelMediaFile file : files) {
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

        addLine.invoke("Файлов с ошибками (" + filesWithErrors.size() + "):");
        addLine.invoke("");

        for (ModelMediaFile file : filesWithErrors) {
            addLine.invoke("- " + file.name + ", " + file.error.getMessage().substring(0, 50));
        }

        return builder.toString();
    }
}
