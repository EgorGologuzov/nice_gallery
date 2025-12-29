package com.nti.nice_gallery.views.buttons;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.DrawableRes;

import com.nti.nice_gallery.R;
import com.nti.nice_gallery.data.Domain;
import com.nti.nice_gallery.data.IManagerOfSettings;
import com.nti.nice_gallery.models.ReadOnlyList;
import com.nti.nice_gallery.views.ViewMediaGrid;

import java.util.function.Consumer;

public class ButtonChoiceGridVariant extends ButtonBase {

    private static final ReadOnlyList<VariantInfo> variants = new ReadOnlyList<>(new VariantInfo[] {
            new VariantInfo(ViewMediaGrid.GridVariant.List, R.drawable.baseline_view_list_24),
            new VariantInfo(ViewMediaGrid.GridVariant.ThreeColumns, R.drawable.baseline_view_module_24),
            new VariantInfo(ViewMediaGrid.GridVariant.SixColumns, R.drawable.baseline_view_compact_24),
            new VariantInfo(ViewMediaGrid.GridVariant.Quilt, R.drawable.baseline_view_quilt_24),
    });

    private int selectedVariantIndex;
    private Consumer<ButtonChoiceGridVariant> variantChangeListener;
    private IManagerOfSettings managerOfSettings;

    public ButtonChoiceGridVariant(Context context) {
        super(context);
        init();
    }

    public ButtonChoiceGridVariant(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ButtonChoiceGridVariant(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        managerOfSettings = Domain.getManagerOfSettings(getContext());
        ViewMediaGrid.GridVariant currentGridVariant = managerOfSettings.getGridVariant();
        VariantInfo selectedVariantInfo = variants.stream().filter(vi -> vi.variant == currentGridVariant).findFirst().get();
        selectedVariantIndex = variants.indexOf(selectedVariantInfo);
        setImageResource(selectedVariantInfo.iconResourceId);
        setOnClickListener(v -> onClick());
    }

    public ViewMediaGrid.GridVariant getSelectedVariant() {
        return variants.get(selectedVariantIndex).variant;
    }

    public void setVariantChangeListener(Consumer<ButtonChoiceGridVariant> l) {
        variantChangeListener = l;
    }

    private void onClick() {
        selectedVariantIndex++;

        if (selectedVariantIndex >= variants.size()) {
            selectedVariantIndex = 0;
        }

        setImageResource(variants.get(selectedVariantIndex).iconResourceId);

        if (variantChangeListener != null) {
            variantChangeListener.accept(this);
        }
    }

    private static class VariantInfo {
        public final ViewMediaGrid.GridVariant variant;
        @DrawableRes public final int iconResourceId;

        public VariantInfo(
                ViewMediaGrid.GridVariant variant,
                @DrawableRes int iconResourceId
        ) {
            this.variant = variant;
            this.iconResourceId = iconResourceId;
        }
    }
}
