package com.nti.nice_gallery.views.buttons;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;

import androidx.core.content.ContextCompat;

import com.nti.nice_gallery.R;
import com.nti.nice_gallery.activities.ActivityFilters;
import com.nti.nice_gallery.utils.ManagerOfNavigation;
import com.nti.nice_gallery.utils.ManagerOfNotifications;

public class ButtonChoiceFilters extends ButtonBase {

//    private boolean areFiltersSet = false;

    ManagerOfNavigation managerOfNavigation;

    public ButtonChoiceFilters(Context context) {
        super(context);
        init();
    }

    public ButtonChoiceFilters(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ButtonChoiceFilters(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        managerOfNavigation = new ManagerOfNavigation(getContext());
        setImageResource(R.drawable.baseline_filter_alt_24);
        setOnClickListener(v -> onClick());
    }

    private void onClick() {
        managerOfNavigation.navigate((Activity)getContext(), ActivityFilters.class);

//        areFiltersSet = !areFiltersSet;
//        if (areFiltersSet) {
//            int color = ContextCompat.getColor(getContext(), R.color.orange_700);
//            setColorFilter(color);
//        } else {
//            setColorFilter(null);
//        }
    }
}
