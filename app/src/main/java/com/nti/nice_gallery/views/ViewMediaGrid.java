package com.nti.nice_gallery.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Size;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.Nullable;

import com.nti.nice_gallery.R;
import com.nti.nice_gallery.data.Domain;
import com.nti.nice_gallery.data.IManagerOfSettings;
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
import java.util.function.Consumer;
import java.util.function.Supplier;

import kotlin.jvm.functions.Function1;

public class ViewMediaGrid extends ScrollView {

    private static final String LOG_TAG = "ViewMediaGrid";

    public enum GridVariant { List, ThreeColumns, SixColumns, Quilt }
    public enum CurrentWork { ScanningInProgress, FilesLoading, Standby }

    private boolean isSelectedMode = false;
    private HashMap<String, ModelMediaFile> selectedFiles;

    private LinearLayout container;
    private ViewInfo viewInfoScanningInProgress;
    private ViewInfo viewInfoFilesLoading;
    private ViewInfo viewInfoNoItems;

    private ReadOnlyList<ModelMediaFile> mediaFiles;
    private GridVariant gridVariant;
    private int renderedItemsCount = 0;
    private CurrentWork currentWork = CurrentWork.Standby;
    private Consumer<ViewMediaGrid> stateChangeListener;
    private Consumer<ViewMediaGrid> selectedModeChangeListener;
    private Consumer<GridItemBase> itemClickListener;

    private IManagerOfSettings managerOfSettings;
    private ManagerOfThreads managerOfThreads;

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
        managerOfSettings = Domain.getManagerOfSettings(getContext());
        managerOfThreads = new ManagerOfThreads(getContext());
        gridVariant = managerOfSettings.getGridVariant();

        LayoutParams params = new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        setLayoutParams(params);
        setOnScrollChangeListener(this::onScrollChange);

        container = new LinearLayout(getContext());
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        container.setLayoutParams(containerParams);
        container.setOrientation(LinearLayout.VERTICAL);
        int containerPaddingPx = new Convert(getContext()).dpToPx(4);
        container.setPadding(containerPaddingPx, 0, containerPaddingPx, 0);
        addView(container);

        viewInfoScanningInProgress = new ViewInfo(getContext());
        viewInfoScanningInProgress.setIconVisibility(false);
        viewInfoScanningInProgress.setMessage(R.string.message_scanning_in_progress);
        viewInfoScanningInProgress.setProgressBarVisibility(true);

        viewInfoFilesLoading = new ViewInfo(getContext());
        viewInfoFilesLoading.setIconVisibility(false);
        viewInfoFilesLoading.setMessage(R.string.message_loading_in_progress);
        viewInfoFilesLoading.setProgressBarVisibility(true);

        viewInfoNoItems = new ViewInfo(getContext());
        viewInfoNoItems.setIcon(R.drawable.baseline_image_search_24);
        viewInfoNoItems.setIconVisibility(true);
        viewInfoNoItems.setMessage(R.string.message_no_items);
        viewInfoNoItems.setProgressBarVisibility(false);

        updateGrid();
    }

    public ReadOnlyList<ModelMediaFile> getMediaFiles() {
        return mediaFiles;
    }

    public void setMediaFiles(ReadOnlyList<ModelMediaFile> mediaFiles) {
        this.mediaFiles = mediaFiles;
        updateGrid();
    }

    public GridVariant getGridVariant() {
        return gridVariant;
    }

    public void setGridVariant(GridVariant gridVariant) {
        this.gridVariant = gridVariant;
        updateGrid();
    }

    public CurrentWork getState() {
        return currentWork;
    }

    private void setCurrentWork(CurrentWork currentWork) {
        this.currentWork = currentWork;
        if (stateChangeListener != null) {
            stateChangeListener.accept(this);
        }
    }

    public boolean trySetStateScanningInProgress(boolean isScanningInProgress) {
        if (currentWork == CurrentWork.FilesLoading) {
            return false;
        }

        if (isScanningInProgress) {
            if (currentWork != CurrentWork.ScanningInProgress) {
                setCurrentWork(CurrentWork.ScanningInProgress);
                updateGrid();
            }
        } else {
            if (currentWork != CurrentWork.Standby) {
                setCurrentWork(CurrentWork.Standby);
                updateGrid();
            }
        }

        return true;
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
        recursivelyHandleAllGridItems(container, item -> {
            item.setCheckBoxVisibility(isSelectedMode);
            item.setIsSelected(selectedFiles != null && selectedFiles.containsKey(item.getModel().path));
        });

        if (selectedModeChangeListener != null) {
            selectedModeChangeListener.accept(this);
        }
    }

    public HashMap<String, ModelMediaFile> getSelectedFiles() {
        return selectedFiles;
    }

    public void setSelectedFiles(HashMap<String, ModelMediaFile> selectedFiles) {
        this.selectedFiles = selectedFiles;
        recursivelyHandleAllGridItems(container, item -> {
            item.setIsSelected(selectedFiles.containsKey(item.getModel().path));
        });
    }

    private void updateGrid() {
        if (currentWork == CurrentWork.ScanningInProgress) {
            container.removeAllViews();
            container.addView(viewInfoScanningInProgress);
            return;
        }

        if (mediaFiles == null || mediaFiles.isEmpty()) {
            container.removeAllViews();
            container.addView(viewInfoNoItems);
            return;
        }

        renderedItemsCount = 0;

        container.removeAllViews();
        renderNextItems();
    }

    private void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        final int SCROLL_END_THRESHOLD_PX = 200;

        View lastChild = getChildAt(getChildCount() - 1);
        int diff = (lastChild.getBottom() - (getHeight() + getScrollY()));
        if (diff <= SCROLL_END_THRESHOLD_PX) {
            renderNextItems();
        }
    }

    private void renderNextItems() {
        final int RENDERING_STEP_ITEMS_COUNT = 25;

        if (this.currentWork == CurrentWork.FilesLoading
                || this.currentWork == CurrentWork.ScanningInProgress
                || this.mediaFiles == null
                || this.mediaFiles.isEmpty()
                || this.renderedItemsCount == this.mediaFiles.size()
        ) {
            return;
        }

        setCurrentWork(CurrentWork.FilesLoading);
        container.addView(viewInfoFilesLoading);

        Supplier<LinearLayout> renderNextForListVariant = () -> {
            int from = renderedItemsCount;
            int to = Math.min(renderedItemsCount + RENDERING_STEP_ITEMS_COUNT, mediaFiles.size());

            LinearLayout pageContainer = new LinearLayout(getContext());
            pageContainer.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            pageContainer.setOrientation(LinearLayout.VERTICAL);

            for (int i = from; i < to; i++) {
                ModelMediaFile item = mediaFiles.get(i);
                GridItemLine itemView = new GridItemLine(getContext());
                itemView.setModel(item);
                pageContainer.addView(itemView);
            }

            renderedItemsCount = to;

            return pageContainer;
        };

        Function1<Integer, LinearLayout> renderNextForColumnsVariant = columnsCount -> {
            final int HIDE_ITEM_DATA_IF_COLUMNS_COUNT_MORE_THAN = 3;

            int from = renderedItemsCount;
            int to = Math.min(renderedItemsCount + RENDERING_STEP_ITEMS_COUNT, mediaFiles.size());

            LinearLayout pageContainer = new LinearLayout(getContext());
            pageContainer.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            pageContainer.setOrientation(LinearLayout.VERTICAL);

            int itemsCount = 0;
            LinearLayout row = null;

            for (int i = from; i < to || (row != null && i < mediaFiles.size()); i++) {
                if (row == null) {
                    row = new LinearLayout(getContext());
                    row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    row.setOrientation(LinearLayout.HORIZONTAL);
                }

                ModelMediaFile item = mediaFiles.get(i);
                GridItemSquare itemView = new GridItemSquare(getContext());
                itemView.setIsInfoHidden(columnsCount > HIDE_ITEM_DATA_IF_COLUMNS_COUNT_MORE_THAN);
                itemView.setModel(item);
                row.addView(itemView);
                itemsCount++;

                if (row.getChildCount() == columnsCount) {
                    pageContainer.addView(row);
                    row = null;
                }
            }

            if (row != null) {
                pageContainer.addView(row);
            }

            this.renderedItemsCount += itemsCount;

            return pageContainer;
        };

        Supplier<LinearLayout> renderNextForQuiltVariant = () -> {
            final Size NO_SIZE_ITEM_RESOLUTION = new Size(960, 960);
            final int MIN_IMAGES_ROW_WIDTH_PX = 1920;
            final int CONTAINER_HORIZONTAL_PADDING_DP = 4;
            final int ITEM_MARGIN_DP = 4;
            final float MIN_ROW_WIDTH_TO_HEIGHT_RATIO = 1.5f;
            final int MAX_ITEMS_IN_ROW = 3;

            int from = renderedItemsCount;
            int to = Math.min(renderedItemsCount + RENDERING_STEP_ITEMS_COUNT, mediaFiles.size());

            LinearLayout pageContainer = new LinearLayout(getContext());
            pageContainer.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            pageContainer.setOrientation(LinearLayout.VERTICAL);

            int displayWidth = getResources().getDisplayMetrics().widthPixels;
            Convert convert = new Convert(getContext());
            LinearLayout rowLayout = null;
            ArrayList<ModelMediaFile> rowItems = null;
            ArrayList<Float> rowWidths = null;
            ArrayList<Float> rowHeights = null;
            int itemsCount = 0;

            for (int j = from; j < to || (rowLayout != null && j < mediaFiles.size()); j++) {
                if (rowLayout == null) {
                    rowLayout = new LinearLayout(getContext());
                    rowLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    rowLayout.setOrientation(LinearLayout.HORIZONTAL);
                    rowItems = new ArrayList<>();
                    rowWidths = new ArrayList<>();
                    rowHeights = new ArrayList<>();
                }

                ModelMediaFile item = mediaFiles.get(j);

                rowItems.add(item);
                itemsCount++;

                int itemWidth, itemHeight;

                if (item.width != null && item.width > 0 && item.height != null && item.height > 0) {
                    itemWidth = item.width;
                    itemHeight = item.height;
                } else {
                    itemWidth = NO_SIZE_ITEM_RESOLUTION.getWidth();
                    itemHeight = NO_SIZE_ITEM_RESOLUTION.getHeight();
                }

                rowWidths.add((float)itemWidth);
                rowHeights.add((float)itemHeight);

                float sumWidth = rowWidths.stream().reduce(0f, Float::sum);
                float maxHeight = rowHeights.stream().max(Float::compareTo).get();
                float avgHeight = rowHeights.stream().reduce(0f, Float::sum) / rowHeights.size();
                boolean isItemLast = mediaFiles.indexOf(item) == mediaFiles.size() - 1;

                if ((sumWidth < MIN_IMAGES_ROW_WIDTH_PX || sumWidth / avgHeight < MIN_ROW_WIDTH_TO_HEIGHT_RATIO)
                        && rowWidths.size() < MAX_ITEMS_IN_ROW
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

                int displayWidthWithoutPaddings = displayWidth - convert.dpToPx(2 * CONTAINER_HORIZONTAL_PADDING_DP + 2 * rowItems.size() * ITEM_MARGIN_DP);
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

                for (int i = 0; i < rowWidths.size(); i++) {
                    GridItemQuilt itemView = new GridItemQuilt(getContext(), rowWidths.get(i), rowHeights.get(i));
                    itemView.setModel(rowItems.get(i));
                    rowLayout.addView(itemView);
                }

                pageContainer.addView(rowLayout);

                rowLayout = null;
                rowItems = null;
                rowWidths = null;
                rowHeights = null;
            }

            this.renderedItemsCount += itemsCount;

            return pageContainer;
        };

        Runnable checkIsContainerFullAndLoadNextIfNot = () -> {
            getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    if (!canScrollVertically(1)) {
                        renderNextItems();
                    }
                }
            });
        };

        Consumer<GridItemBase.TouchArgs> onItemGestureDetected = new Consumer<GridItemBase.TouchArgs>() {
            ModelMediaFile previousChangedFile;
            @Override
            public void accept(GridItemBase.TouchArgs touchArgs) {
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

                    recursivelyHandleAllGridItems(container, item -> {
                        item.setIsSelected(selectedFiles.containsKey(item.getModel().path));
                    });
                } else {
                    changeItemSelectState(gridItem);
                }

                previousChangedFile = file;
            }
        };

        Consumer<GridItemBase> itemPostRenderHandler = item -> {
            item.setOnTouchListener(onItemGestureDetected);
            item.setCheckBoxVisibility(isSelectedMode);
            item.setIsSelected(selectedFiles != null && selectedFiles.containsKey(item.getModel().path));
        };

        Runnable renderOnePage = () -> {
            managerOfThreads.executeAsync(() -> {
                LinearLayout pageContainer = null;

                switch (gridVariant) {
                    case List: pageContainer = renderNextForListVariant.get(); break;
                    case ThreeColumns: pageContainer = renderNextForColumnsVariant.invoke(3); break;
                    case SixColumns: pageContainer = renderNextForColumnsVariant.invoke(6); break;
                    case Quilt: pageContainer = renderNextForQuiltVariant.get(); break;
                }

                final LinearLayout pageContainerFinal = pageContainer;

                managerOfThreads.runOnUiThread(() -> {
                    recursivelyHandleAllGridItems(pageContainerFinal, itemPostRenderHandler);
                    container.removeView(viewInfoFilesLoading);
                    container.addView(pageContainerFinal);
                    setCurrentWork(CurrentWork.Standby);
                    checkIsContainerFullAndLoadNextIfNot.run();
                });
            });
        };

        renderOnePage.run();
    }

    private void recursivelyHandleAllGridItems(ViewGroup root, Consumer<GridItemBase> handler) {
        if (root == null || handler == null) return;

        int childCount = root.getChildCount();

        for (int i = 0; i < childCount; i++) {
            View child = root.getChildAt(i);

            if (child instanceof GridItemBase) {
                GridItemBase item = (GridItemBase) child;
                handler.accept(item);
            }

            if (child instanceof ViewGroup) {
                recursivelyHandleAllGridItems((ViewGroup) child, handler);
            }
        }
    }
}
