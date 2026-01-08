package com.nti.nice_gallery.views.buttons;

import android.content.Context;
import android.util.AttributeSet;

import com.nti.nice_gallery.R;
import com.nti.nice_gallery.utils.ManagerOfThreads;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;

public class ButtonPathsStack extends ButtonBase {

    private ArrayList<String> pathsStack;

    private Consumer<ButtonPathsStack> topItemChangeListener;

    private ManagerOfThreads managerOfThreads;

    public ButtonPathsStack(Context context) {
        super(context);
        init();
    }

    public ButtonPathsStack(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ButtonPathsStack(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        pathsStack = new ArrayList<>();
        managerOfThreads = new ManagerOfThreads(getContext());

        setImageResource(R.drawable.baseline_arrow_back_24);
        setOnClickListener(v -> onClick());
        setVisibility(GONE);
    }

    public void setPathsStack(ArrayList<String> source) {
        String pastTopPath = getTopPath();

        if (source != null && !source.isEmpty()) {
            pathsStack = source;
        }

        String currentTopPath = getTopPath();

        if (!Objects.equals(pastTopPath, currentTopPath)) {
            onTopItemChange();
        }
    }

    public void addTopItem(String path) {
        pathsStack.add(path);
        onTopItemChange();
    }

    public void removeTopItem() {
        if (getItemsCount() > 1) {
            pathsStack.remove(pathsStack.size() - 1);
            onTopItemChange();
        }
    }

    public String getTopPath() {
        return !pathsStack.isEmpty() ? pathsStack.get(pathsStack.size() - 1) : null;
    }

    public int getItemsCount() {
        return pathsStack.size();
    }

    public void setTopPathChangeListener(Consumer<ButtonPathsStack> listener) {
        this.topItemChangeListener = listener;
    }

    private void onClick() {
        removeTopItem();
    }

    private void onTopItemChange() {
        managerOfThreads.safeAccept(topItemChangeListener, this);
        if (getItemsCount() > 1) {
            setVisibility(VISIBLE);
        } else {
            jumpDrawablesToCurrentState();
            clearAnimation();
            setVisibility(GONE);
        }
    }
}
