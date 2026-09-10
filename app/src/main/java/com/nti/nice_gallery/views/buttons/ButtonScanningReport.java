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
        managerOfDialogs.showScanningReport(source);
    }
}
