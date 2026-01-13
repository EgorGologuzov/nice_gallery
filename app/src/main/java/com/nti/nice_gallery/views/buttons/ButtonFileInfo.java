package com.nti.nice_gallery.views.buttons;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import com.nti.nice_gallery.R;
import com.nti.nice_gallery.models.ModelMediaFile;
import com.nti.nice_gallery.utils.Convert;
import com.nti.nice_gallery.utils.ManagerOfDialogs;

import java.util.ArrayList;

public class ButtonFileInfo extends ButtonBase {

    private static final String LOG_TAG = "ButtonFileInfo";

    private ModelMediaFile file;

    public ButtonFileInfo(Context context) {
        super(context);
        init();
    }

    public ButtonFileInfo(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ButtonFileInfo(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setImageResource(R.drawable.baseline_info_24);
        setOnClickListener(v -> onClick());
    }

    public void setFile(ModelMediaFile file) {
        this.file = file;
    }

    private void onClick() {
        if (file == null) {
            return;
        }

        ArrayList<String> infoItems = new ArrayList<>();
        ManagerOfDialogs managerOfDialogs = new ManagerOfDialogs(getContext());
        Convert convert = new Convert(getContext());

        try {
            String[] typesStrings = getContext().getResources().getStringArray(R.array.enum_file_types);
            infoItems.add("Тип: " + (file.isImage ? typesStrings[0] : file.isVideo ? typesStrings[1] : "null"));
            infoItems.add("Путь: " + file.path);
            infoItems.add("Создан: " + file.createdAt);
            infoItems.add("Обновлен: " + file.updatedAt);
            infoItems.add("Вес: " + convert.weightToString(file.weight));
            infoItems.add("Разрешение: " + convert.sizeToString(file.width, file.height));
            if (file.isVideo) {
                infoItems.add("Длительность: " + convert.durationToTimeString(file.duration));
            }
            if (file.error != null) {
                infoItems.add("Ошибка: " + file.error.getMessage());
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
            infoItems.add("!!!");
            infoItems.add(getContext().getString(R.string.message_error_file_properties_read_failed));
        }

        String info = String.join("\n\n", infoItems);
        managerOfDialogs.showInfo(R.string.dialog_title_file_properties, info);
    }
}
