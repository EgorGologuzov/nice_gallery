package com.nti.nice_gallery.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Size;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.Nullable;

import com.nti.nice_gallery.R;
import com.nti.nice_gallery.models.ModelMediaFile;
import com.nti.nice_gallery.utils.Convert;
import com.nti.nice_gallery.views.grid_items.GridItemLine;
import com.nti.nice_gallery.views.grid_items.GridItemQuilt;
import com.nti.nice_gallery.views.grid_items.GridItemSquare;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ViewMediaGrid extends ScrollView {

    public enum GridVariant { List, ThreeColumns, SixColumns, Quilt }

    private LinearLayout container;
    private List<ModelMediaFile> items;
    private GridVariant gridVariant = GridVariant.ThreeColumns;

    public ViewMediaGrid(Context context) {
        super(context);
        init();
    }

    public ViewMediaGrid(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewMediaGrid(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutParams params = new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        setLayoutParams(params);

        container = new LinearLayout(getContext());
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        container.setLayoutParams(containerParams);
        container.setOrientation(LinearLayout.VERTICAL);
        int containerPaddingPx = new Convert(getContext()).dpToPx(4);
        container.setPadding(containerPaddingPx, 0, containerPaddingPx, 0);
        addView(container);

        updateGrid();
    }

    public List<ModelMediaFile> getItems() {
        return items;
    }

    public void setItems(List<ModelMediaFile> items) {
        this.items = items;
        updateGrid();
    }

    public GridVariant getGridVariant() {
        return gridVariant;
    }

    public void setGridVariant(GridVariant gridVariant) {
        this.gridVariant = gridVariant;
        updateGrid();
    }

    private void updateGrid() {
        if (items == null || items.isEmpty()) {
            noItemsUpdate();
            return;
        }

        progressBarUpdate();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            post(() -> {
                switch (gridVariant) {
                    case List: listVariantUpdate(); break;
                    case ThreeColumns: columnsVariantUpdate(3); break;
                    case SixColumns: columnsVariantUpdate(6); break;
                    case Quilt: quiltVariantUpdate(); break;
                }
                executor.shutdown();
            });
        });
    }

    private void noItemsUpdate() {
        container.removeAllViews();
        ViewInfo info = new ViewInfo(getContext());
        info.setIcon(R.drawable.baseline_image_search_24);
        info.setIconVisibility(true);
        info.setMessage(R.string.message_no_items);
        info.setProgressBarVisibility(false);
        container.addView(info);
    }

    private void progressBarUpdate() {
        container.removeAllViews();
        ViewInfo info = new ViewInfo(getContext());
        info.setIconVisibility(false);
        info.setMessage(R.string.message_update_in_progress);
        info.setProgressBarVisibility(true);
        container.addView(info);
    }

    private void listVariantUpdate() {
        container.removeAllViews();
        for (ModelMediaFile item : items) {
            GridItemLine itemView = new GridItemLine(getContext());
            itemView.setModel(item);
            container.addView(itemView);
        }
    }

    private void columnsVariantUpdate(int columnsCount) {

        final int HIDE_ITEM_DATA_IF_COLUMNS_COUNT_MORE_THAN = 3;

        container.removeAllViews();

        LinearLayout row = null;
        for (ModelMediaFile item : items) {
            if (row == null) {
                row = new LinearLayout(getContext());
                row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                row.setOrientation(LinearLayout.HORIZONTAL);
            }

            GridItemSquare itemView = new GridItemSquare(getContext());
            itemView.setModel(item);
            itemView.setIsInfoHidden(columnsCount > HIDE_ITEM_DATA_IF_COLUMNS_COUNT_MORE_THAN);
            row.addView(itemView);

            if (row.getChildCount() == columnsCount) {
                container.addView(row);
                row = null;
            }
        }

        if (row != null) {
            container.addView(row);
        }
    }

    private void quiltVariantUpdate() {

        final Size NO_SIZE_ITEM_RESOLUTION = new Size(960, 960);
        final int MIN_IMAGES_ROW_WIDTH_PX = 1920;
        final int CONTAINER_HORIZONTAL_PADDING_DP = 4;
        final int ITEM_MARGIN_DP = 4;
        final float MIN_ROW_WIDTH_TO_HEIGHT_RATIO = 1.5f;

        container.removeAllViews();

        int displayWidth = getResources().getDisplayMetrics().widthPixels;
        Convert convert = new Convert(getContext());
        LinearLayout rowLayout = null;
        ArrayList<ModelMediaFile> rowItems = null;
        ArrayList<Float> rowWidths = null;
        ArrayList<Float> rowHeights = null;

        for (ModelMediaFile item : items) {
            if (rowLayout == null) {
                rowLayout = new LinearLayout(getContext());
                rowLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                rowLayout.setOrientation(LinearLayout.HORIZONTAL);
                rowItems = new ArrayList<>();
                rowWidths = new ArrayList<>();
                rowHeights = new ArrayList<>();
            }

            rowItems.add(item);

            int itemWidth, itemHeight;

            if (item.resolution != null) {
                itemWidth = item.resolution.getWidth();
                itemHeight = item.resolution.getHeight();
            } else {
                itemWidth = NO_SIZE_ITEM_RESOLUTION.getWidth();
                itemHeight = NO_SIZE_ITEM_RESOLUTION.getHeight();
            }

            rowWidths.add((float)itemWidth);
            rowHeights.add((float)itemHeight);

            float sumWidth = rowWidths.stream().reduce(0f, Float::sum);
            float maxHeight = rowHeights.stream().max(Float::compareTo).get();
            float avgHeight = rowHeights.stream().reduce(0f, Float::sum) / rowHeights.size();
            boolean isItemLast = items.indexOf(item) == items.size() - 1;

//            Log.d("QUILT_ALGORITHM", String.format("%s: %dx%d, %.2f, %.2f, %.2f", item.path, itemWidth, itemHeight, sumWidth, maxHeight, sumWidth / maxHeight));

            if ((sumWidth < MIN_IMAGES_ROW_WIDTH_PX || sumWidth / avgHeight < MIN_ROW_WIDTH_TO_HEIGHT_RATIO) && !isItemLast) {
                continue;
            }

            for (int i = 0; i < rowWidths.size(); i++) {
                rowWidths.set(i, rowWidths.get(i) * maxHeight / rowHeights.get(i));
            }

            int displayWidthWithoutPaddings = displayWidth - convert.dpToPx(2 * CONTAINER_HORIZONTAL_PADDING_DP + 2 * rowItems.size() * ITEM_MARGIN_DP);
            sumWidth = rowWidths.stream().reduce(0f, Float::sum);
            int itemMarginsPx = convert.dpToPx(2 * ITEM_MARGIN_DP);

            for (int i = 0; i < rowWidths.size(); i++) {
                rowWidths.set(i, rowWidths.get(i) * displayWidthWithoutPaddings / sumWidth + itemMarginsPx);
            }

            for (int i = 0; i < rowWidths.size(); i++) {
                GridItemQuilt itemView = new GridItemQuilt(getContext(), rowWidths.get(i));
                itemView.setModel(rowItems.get(i));
                rowLayout.addView(itemView);
            }

            container.addView(rowLayout);

            rowLayout = null;
            rowItems = null;
            rowWidths = null;
            rowHeights = null;
        }
    }
}
