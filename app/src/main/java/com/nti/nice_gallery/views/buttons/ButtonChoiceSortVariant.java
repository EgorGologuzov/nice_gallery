package com.nti.nice_gallery.views.buttons;

import android.content.Context;
import android.util.AttributeSet;

import com.nti.nice_gallery.R;
import com.nti.nice_gallery.data.Domain;
import com.nti.nice_gallery.data.IManagerOfSettings;
import com.nti.nice_gallery.models.ModelGetFilesRequest;
import com.nti.nice_gallery.utils.Convert;
import com.nti.nice_gallery.utils.ManagerOfDialogs;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ButtonChoiceSortVariant extends ButtonBase {

    private static final ModelGetFilesRequest.SortVariant[] variants = ModelGetFilesRequest.SortVariant.values();

    private int selectedVariantIndex;
    private Consumer<ButtonChoiceSortVariant> variantChangeListener;

    private IManagerOfSettings managerOfSettings;
    private ManagerOfDialogs managerOfDialogs;
    private Convert convert;

    public ButtonChoiceSortVariant(Context context) {
        super(context);
        init();
    }

    public ButtonChoiceSortVariant(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ButtonChoiceSortVariant(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        managerOfSettings = Domain.getManagerOfSettings(getContext());
        managerOfDialogs = new ManagerOfDialogs(getContext());
        convert = new Convert(getContext());

        selectedVariantIndex = Arrays.stream(variants).collect(Collectors.toList()).indexOf(managerOfSettings.getSortVariant());

        setImageResource(R.drawable.baseline_sort_24);
        setOnClickListener(v -> onClick());
    }

    public ModelGetFilesRequest.SortVariant getSelectedVariant() {
        return variants[selectedVariantIndex];
    }

    public void setVariantChangeListener(Consumer<ButtonChoiceSortVariant> l) {
        variantChangeListener = l;
    }

    private void onClick() {
        managerOfDialogs.showChooseOne(
                R.string.dialog_title_sort_variant,
                R.array.enum_sort_variants,
                selectedVariantIndex,
                variantIndex -> {
                    selectedVariantIndex = variantIndex;
                    managerOfSettings.saveSortVariant(convert.indexToEnumValue(ModelGetFilesRequest.SortVariant.class, variantIndex));
                    if (variantChangeListener != null) {
                        variantChangeListener.accept(this);
                    }
                }
        );
    }
}
