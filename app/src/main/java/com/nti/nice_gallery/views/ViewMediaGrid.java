package com.nti.nice_gallery.views;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.Size;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nti.nice_gallery.R;
import com.nti.nice_gallery.data.ManagerOfSettings;
import com.nti.nice_gallery.models.ModelMediaFile;
import com.nti.nice_gallery.utils.GestureListener;
import com.nti.nice_gallery.utils.ManagerOfThreads;
import com.nti.nice_gallery.utils.ReadOnlyList;
import com.nti.nice_gallery.utils.Convert;
import com.nti.nice_gallery.views.grid_items.GridItemBase;
import com.nti.nice_gallery.views.grid_items.GridItemLine;
import com.nti.nice_gallery.views.grid_items.GridItemQuilt;
import com.nti.nice_gallery.views.grid_items.GridItemSquare;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ViewMediaGrid extends ScrollView {

    private static final String LOG_TAG = "ViewMediaGrid";

    public enum GridVariant { List, LargeGrid, SmallGrid, Quilt }

    private boolean isSelectedMode = false;
    private HashMap<String, ModelMediaFile> selectedFiles;

    private ReadOnlyList<ModelMediaFile> mediaFiles;
    private GridVariant gridVariant;
    private boolean isScanInProgress = false;
    private ModelMediaFile previousChangedFile;
    private Consumer<ViewMediaGrid> stateChangeListener;
    private Consumer<ViewMediaGrid> selectedModeChangeListener;
    private Consumer<GridItemBase> itemClickListener;

    private ViewInfo viewInfo;
    private RecyclerView recyclerView;
    private ManagerOfThreads managerOfThreads;
    private ManagerOfSettings managerOfSettings;


    public ViewMediaGrid(@NonNull Context context) {
        super(context);
        init();
    }

    public ViewMediaGrid(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewMediaGrid(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        managerOfThreads = new ManagerOfThreads(getContext());
        managerOfSettings = new ManagerOfSettings(getContext());

        gridVariant = managerOfSettings.getGridVariant();

        LayoutParams params = new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        setLayoutParams(params);

        int containerPaddingPx = new Convert(getContext()).dpToPx(4);
        setPadding(containerPaddingPx, 0, containerPaddingPx, 0);
    }

    public ReadOnlyList<ModelMediaFile> getMediaFiles() {
        return mediaFiles;
    }

    public void setMediaFiles(ReadOnlyList<ModelMediaFile> mediaFiles) {
        this.mediaFiles = mediaFiles;
        update();
    }

    public GridVariant getGridVariant() {
        return gridVariant;
    }

    public void setGridVariant(GridVariant gridVariant) {
        this.gridVariant = gridVariant;
        update();
    }

    public boolean getScanInProgress() {
        return this.isScanInProgress;
    }

    public void setScanInProgress(boolean isScanInProgress) {
        if (isScanInProgress == this.isScanInProgress) return;

        this.isScanInProgress = isScanInProgress;
        if (stateChangeListener != null) {
            stateChangeListener.accept(this);
        }

        update();
    }

    public void setStateChangeListener(Consumer<ViewMediaGrid> listener) {
        stateChangeListener = listener;
    }

    public void setSelectedModeChangeListener(Consumer<ViewMediaGrid> listener) {
        this.selectedModeChangeListener = listener;
    }

    public void setItemClickListener(Consumer<GridItemBase> listener) {
        itemClickListener = listener;
    }

    public boolean getSelectedMode() {
        return isSelectedMode;
    }

    public void setSelectedMode(boolean isSelectedMode) {
        if (this.isSelectedMode == isSelectedMode) {
            return;
        }

        if (!isSelectedMode && selectedFiles != null) {
            selectedFiles.clear();
        }

        this.isSelectedMode = isSelectedMode;
        onSelectionChanged();

        if (selectedModeChangeListener != null) {
            selectedModeChangeListener.accept(this);
        }
    }

    public HashMap<String, ModelMediaFile> getSelectedFiles() {
        return selectedFiles;
    }

    public void setSelectedFiles(HashMap<String, ModelMediaFile> selectedFiles) {
        this.selectedFiles = selectedFiles;
        onSelectionChanged();
    }

    private void update() {
        Runnable setNoItemsInfo = () -> {
            if (viewInfo != null && Objects.equals(viewInfo.getMessage(), getContext().getString(R.string.message_no_items))) {
                return;
            }

            viewInfo = null;
            recyclerView = null;

            removeAllViews();
            ViewInfo info = new ViewInfo(getContext());
            info.setIcon(R.drawable.baseline_image_search_24);
            info.setIconVisibility(true);
            info.setMessage(R.string.message_no_items);
            info.setProgressBarVisibility(false);

            viewInfo = info;
            addView(info);
        };

        Runnable setScanInProgressInfo = () -> {
            if (viewInfo != null && Objects.equals(viewInfo.getMessage(), getContext().getString(R.string.message_scanning_in_progress))) {
                return;
            }

            viewInfo = null;
            recyclerView = null;

            removeAllViews();
            ViewInfo info = new ViewInfo(getContext());
            info.setIconVisibility(false);
            info.setMessage(R.string.message_scanning_in_progress);
            info.setProgressBarVisibility(true);

            viewInfo = info;
            addView(info);
        };

        Runnable setRecyclerView = () -> {
            viewInfo = null;
            recyclerView = new RecyclerView(getContext());

            removeAllViews();
            LayoutParams rwParams = new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            recyclerView.setLayoutParams(rwParams);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

            int orientation = getResources().getConfiguration().orientation;

            if (gridVariant == GridVariant.List) {
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    recyclerView.setAdapter(new ViewMediaGrid.GridListAdapter(this));
                    recyclerView.setItemViewCacheSize(10);
                } else {
                    recyclerView.setAdapter(new ViewMediaGrid.GridListAdapter(this));
                    recyclerView.setItemViewCacheSize(5);
                }
            } else if (gridVariant == GridVariant.LargeGrid) {
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    recyclerView.setAdapter(new ViewMediaGrid.GridSquareAdapter(this, 3, false));
                    recyclerView.setItemViewCacheSize(5);
                } else {
                    recyclerView.setAdapter(new ViewMediaGrid.GridSquareAdapter(this, 6, false));
                    recyclerView.setItemViewCacheSize(3);
                }
            } else if (gridVariant == GridVariant.SmallGrid) {
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    recyclerView.setAdapter(new ViewMediaGrid.GridSquareAdapter(this, 6, true));
                    recyclerView.setItemViewCacheSize(10);
                } else {
                    recyclerView.setAdapter(new ViewMediaGrid.GridSquareAdapter(this, 12, true));
                    recyclerView.setItemViewCacheSize(5);
                }
            } else if (gridVariant == GridVariant.Quilt) {
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    recyclerView.setAdapter(new ViewMediaGrid.GridQuiltAdapter(this, 1920f / 1080f, 3));
                    recyclerView.setItemViewCacheSize(8);
                } else {
                    recyclerView.setAdapter(new ViewMediaGrid.GridQuiltAdapter(this, 2860f / 1080f, 6));
                    recyclerView.setItemViewCacheSize(4);
                }
            }

            addView(recyclerView);
        };

        if (isScanInProgress) {
            setScanInProgressInfo.run();
        } else if (mediaFiles == null || mediaFiles.isEmpty()) {
            setNoItemsInfo.run();
        } else {
            setRecyclerView.run();
        }
    }

    private void bindItem(GridItemBase item, ModelMediaFile fileInfo) {
        item.setModel(fileInfo);
        item.setOnTouchListener(this::onItemGestureDetected);
        item.setCheckBoxVisibility(isSelectedMode);
        if (isSelectedMode) item.setIsSelected(selectedFiles != null && selectedFiles.containsKey(fileInfo.path));
    }

    private void onItemGestureDetected(GridItemBase.TouchArgs touchArgs) {
        GridItemBase gridItem = touchArgs.item;

        if (!isSelectedMode) {
            if (touchArgs.gestureArgs.gesture == GestureListener.Gesture.Tap) {
                managerOfThreads.safeAccept(itemClickListener, gridItem);
            }
            if (touchArgs.gestureArgs.gesture == GestureListener.Gesture.LongPress) {
                setSelectedMode(true);
                changeItemSelectState(gridItem);
            }
        } else {
            if (touchArgs.gestureArgs.gesture == GestureListener.Gesture.Tap) {
                changeItemSelectState(gridItem);
            }
            if (touchArgs.gestureArgs.gesture == GestureListener.Gesture.LongPress) {
                setSelectedMode(false);
            }
            if (touchArgs.gestureArgs.gesture == GestureListener.Gesture.DoubleTap) {
                changeItemSelectStateWithShift(gridItem);
            }
        }
    }

    private void onSelectionChanged() {
        if (recyclerView == null) return;

        ViewMediaGrid.GridAdapterBase adapter = (ViewMediaGrid.GridAdapterBase) recyclerView.getAdapter();
        adapter.forEachGridItem(item -> {
            item.setCheckBoxVisibility(isSelectedMode);
            if (isSelectedMode) item.setIsSelected(selectedFiles != null && selectedFiles.containsKey(item.getModel().path));
        });
    }

    private void changeItemSelectState(GridItemBase gridItem) {
        ModelMediaFile file = gridItem.getModel();

        if (selectedFiles.containsKey(file.path)) {
            selectedFiles.remove(file.path);
            gridItem.setIsSelected(false);
        } else {
            selectedFiles.put(file.path, file);
            gridItem.setIsSelected(true);
        }

        previousChangedFile = file;
    }

    private void changeItemSelectStateWithShift(GridItemBase gridItem) {
        ModelMediaFile file = gridItem.getModel();

        if (previousChangedFile != null) {
            int previousTouchedIndex = mediaFiles.indexOf(previousChangedFile);
            int nowTouchedIndex = mediaFiles.indexOf(file);
            boolean isPreviousTouchedSelected = selectedFiles.containsKey(previousChangedFile.path);
            int step = previousTouchedIndex <= nowTouchedIndex ? 1 : -1;

            for (int i = previousTouchedIndex; i - nowTouchedIndex != step; i += step) {
                ModelMediaFile iFile = mediaFiles.get(i);
                if (isPreviousTouchedSelected) {
                    selectedFiles.put(iFile.path, iFile);
                } else {
                    selectedFiles.remove(iFile.path);
                }
            }

            onSelectionChanged();
        } else {
            changeItemSelectState(gridItem);
        }

        previousChangedFile = file;
    }

    private static class Holder extends RecyclerView.ViewHolder {
        public Holder(@NonNull View itemView) {
            super(itemView);
        }
    }

    private static abstract class GridAdapterBase extends RecyclerView.Adapter<ViewMediaGrid.Holder> {
        private final HashSet<ViewMediaGrid.Holder> holders = new HashSet<>();

        @Override
        public void onViewRecycled(@NonNull ViewMediaGrid.Holder holder) {
            super.onViewRecycled(holder);
            holders.remove(holder);
        }

        protected void regHolder(ViewMediaGrid.Holder holder) {
            holders.add(holder);
        }

        public void forEachGridItem(Consumer<GridItemBase> gridItem) {
            for (ViewMediaGrid.Holder holder : holders) {
                View itemView = holder.itemView;
                if (itemView instanceof GridItemBase) {
                    gridItem.accept((GridItemBase) itemView);
                } else if (itemView instanceof LinearLayout) {
                    LinearLayout row = (LinearLayout) itemView;
                    int childCount = row.getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        gridItem.accept((GridItemBase) row.getChildAt(i));
                    }
                }
            }
        }
    }

    private static class GridListAdapter extends ViewMediaGrid.GridAdapterBase {

        private final ViewMediaGrid parentGrid;

        public GridListAdapter(ViewMediaGrid parentGrid) {
            this.parentGrid = parentGrid;
        }

        @NonNull
        @Override
        public ViewMediaGrid.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            GridItemLine item = new GridItemLine(parent.getContext());
            return new ViewMediaGrid.Holder(item);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewMediaGrid.Holder holder, int position) {
            regHolder(holder);
            GridItemLine item = (GridItemLine) holder.itemView;
            parentGrid.bindItem(item, parentGrid.getMediaFiles().get(position));
        }

        @Override
        public int getItemCount() {
            return parentGrid.getMediaFiles().size();
        }
    }

    private static class GridSquareAdapter extends ViewMediaGrid.GridAdapterBase {

        private final int columnsCount;
        private final boolean hideItemData;

        private final ViewMediaGrid parentGrid;
        private List<List<ModelMediaFile>> rows;

        public GridSquareAdapter(ViewMediaGrid parentGrid, int columnsCount, boolean hideItemData) {
            this.parentGrid = parentGrid;
            this.columnsCount = columnsCount;
            this.hideItemData = hideItemData;
            splitFilesByRows();
        }

        @NonNull
        @Override
        public ViewMediaGrid.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LinearLayout row = new LinearLayout(parentGrid.getContext());
            row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            row.setOrientation(LinearLayout.HORIZONTAL);
            return new ViewMediaGrid.Holder(row);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewMediaGrid.Holder holder, int position) {
            regHolder(holder);
            List<ModelMediaFile> row = rows.get(position);
            LinearLayout rowView = (LinearLayout) holder.itemView;
            rowView.removeAllViews();

            for (ModelMediaFile file : row) {
                GridItemSquare item = new GridItemSquare(parentGrid.getContext());
                item.setIsInfoHidden(hideItemData);
                parentGrid.bindItem(item, file);
                rowView.addView(item);
            }
        }

        @Override
        public int getItemCount() {
            return rows.size();
        }

        private void splitFilesByRows() {
            rows = new ArrayList<>();
            List<ModelMediaFile> row = null;

            for (ModelMediaFile file : parentGrid.getMediaFiles()) {
                if (row == null) row = new ArrayList<>();
                if (row.size() < columnsCount) row.add(file);
                if (row.size() == columnsCount) {
                    rows.add(row);
                    row = null;
                }
            }

            if (row != null) {
                rows.add(row);
            }
        }
    }

    private static class GridQuiltAdapter extends ViewMediaGrid.GridAdapterBase {

        final Size NO_SIZE_ITEM_RESOLUTION = new Size(960, 960);
        final int CONTAINER_HORIZONTAL_PADDING_DP = 4;
        final int ITEM_MARGIN_DP = 4;
        final float minRowWidthToHeightRatio;
        final int maxItemsInRow;

        private final ViewMediaGrid parentGrid;
        private final Convert convert;
        private List<List<ModelMediaFile>> rows;
        private List<List<Float>> rowsWidths;
        private List<List<Float>> rowsHeights;

        public GridQuiltAdapter(ViewMediaGrid parentGrid, float minRowWidthToHeightRatio, int maxItemsInRow) {
            this.parentGrid = parentGrid;
            this.minRowWidthToHeightRatio = minRowWidthToHeightRatio;
            this.maxItemsInRow = maxItemsInRow;
            this.convert = new Convert(parentGrid.getContext());
            splitFilesByRows();
        }

        @NonNull
        @Override
        public ViewMediaGrid.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LinearLayout row = new LinearLayout(parent.getContext());
            row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            row.setOrientation(LinearLayout.HORIZONTAL);
            return new ViewMediaGrid.Holder(row);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewMediaGrid.Holder holder, int position) {
            regHolder(holder);
            List<ModelMediaFile> row = rows.get(position);
            List<Float> widths = rowsWidths.get(position);
            List<Float> heights = rowsHeights.get(position);
            LinearLayout rowView = (LinearLayout) holder.itemView;
            rowView.removeAllViews();

            for (int i = 0; i < row.size(); i++) {
                ModelMediaFile file = row.get(i);
                Float width = widths.get(i);
                Float height = heights.get(i);
                GridItemQuilt item = new GridItemQuilt(parentGrid.getContext(), width, height);
                parentGrid.bindItem(item, file);
                rowView.addView(item);
            }
        }

        @Override
        public int getItemCount() {
            return rows.size();
        }

        private void splitFilesByRows() {
            rows = new ArrayList<>();
            rowsWidths = new ArrayList<>();
            rowsHeights = new ArrayList<>();

            List<ModelMediaFile> row = null;
            ArrayList<Float> rowWidths = null;
            ArrayList<Float> rowHeights = null;
            ReadOnlyList<ModelMediaFile> mediaFiles = parentGrid.getMediaFiles();
            int displayWidth = parentGrid.getResources().getDisplayMetrics().widthPixels;

            for (int j = 0; j < mediaFiles.size(); j++) {
                if (row == null) {
                    row = new ArrayList<>();
                    rowWidths = new ArrayList<>();
                    rowHeights = new ArrayList<>();
                }

                ModelMediaFile item = mediaFiles.get(j);
                row.add(item);

                int itemWidth, itemHeight;

                if (item.width != null && item.width > 0 && item.height != null && item.height > 0) {
                    itemWidth = item.width;
                    itemHeight = item.height;
                } else {
                    itemWidth = NO_SIZE_ITEM_RESOLUTION.getWidth();
                    itemHeight = NO_SIZE_ITEM_RESOLUTION.getHeight();
                }

                rowWidths.add((float) itemWidth);
                rowHeights.add((float) itemHeight);

                float sumWidth = rowWidths.stream().reduce(0f, Float::sum);
                float maxHeight = rowHeights.stream().max(Float::compareTo).get();
                float avgHeight = rowHeights.stream().reduce(0f, Float::sum) / rowHeights.size();
                boolean isItemLast = j == mediaFiles.size() - 1;

                if (sumWidth / avgHeight < minRowWidthToHeightRatio
                        && rowWidths.size() < maxItemsInRow
                        && !isItemLast
                ) {
                    continue;
                }

                for (int i = 0; i < rowWidths.size(); i++) {
                    float w1 = rowWidths.get(i);
                    float h1 = rowHeights.get(i);
                    float w2 = w1 * maxHeight / h1;
                    float h2 = h1 * maxHeight / h1;
                    rowWidths.set(i, w2);
                    rowHeights.set(i, h2);
                }

                int displayWidthWithoutPaddings = displayWidth - convert.dpToPx(2 * CONTAINER_HORIZONTAL_PADDING_DP + 2 * row.size() * ITEM_MARGIN_DP);
                sumWidth = rowWidths.stream().reduce(0f, Float::sum);
                int itemMarginsPx = convert.dpToPx(2 * ITEM_MARGIN_DP);

                for (int i = 0; i < rowWidths.size(); i++) {
                    float w1 = rowWidths.get(i);
                    float h1 = rowHeights.get(i);
                    float w2 = w1 * displayWidthWithoutPaddings / sumWidth + itemMarginsPx;
                    float h2 = h1 * displayWidthWithoutPaddings / sumWidth + itemMarginsPx;
                    rowWidths.set(i, w2);
                    rowHeights.set(i, h2);
                }

                rows.add(row);
                rowsWidths.add(rowWidths);
                rowsHeights.add(rowHeights);

                row = null;
                rowWidths = null;
                rowHeights = null;
            }
        }
    }
}
