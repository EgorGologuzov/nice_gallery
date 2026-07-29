package com.nti.nice_gallery.models;

import androidx.annotation.Nullable;

import com.nti.nice_gallery.utils.JsonUtil;
import com.nti.nice_gallery.utils.ReadOnlyList;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ModelFilters {

    public static final ReadOnlyList<ModelMediaFile.Type> typeVariants = new ReadOnlyList<>(Arrays.stream(ModelMediaFile.Type.values()).collect(Collectors.toList()));

    public final boolean ignoreHidden;
    @Nullable public final ReadOnlyList<ModelMediaFile.Type> types;
    @Nullable public final Long minWeight;
    @Nullable public final Long maxWeight;
    @Nullable public final LocalDateTime minCreateAt;
    @Nullable public final LocalDateTime maxCreateAt;
    @Nullable public final LocalDateTime minUpdateAt;
    @Nullable public final LocalDateTime maxUpdateAt;
    @Nullable public final ReadOnlyList<String> extensions;
    @Nullable public final Integer minDuration;
    @Nullable public final Integer maxDuration;

    public ModelFilters(
            @Nullable Boolean ignoreHidden,
            @Nullable ReadOnlyList<ModelMediaFile.Type> types,
            @Nullable Long minWeight,
            @Nullable Long maxWeight,
            @Nullable LocalDateTime minCreateAt,
            @Nullable LocalDateTime maxCreateAt,
            @Nullable LocalDateTime minUpdateAt,
            @Nullable LocalDateTime maxUpdateAt,
            @Nullable ReadOnlyList<String> extensions,
            @Nullable Integer minDuration,
            @Nullable Integer maxDuration
    ) {
        this.ignoreHidden = ignoreHidden == null || ignoreHidden;
        this.types = types;
        this.minWeight = minWeight;
        this.maxWeight = maxWeight;
        this.minCreateAt = minCreateAt;
        this.maxCreateAt = maxCreateAt;
        this.minUpdateAt = minUpdateAt;
        this.maxUpdateAt = maxUpdateAt;
        this.extensions = extensions;
        this.minDuration = minDuration;
        this.maxDuration = maxDuration;
    }

    public ModelFilters(String jsonStr) {
        JSONObject modelJson = JsonUtil.newJsonObject(jsonStr);
        this.ignoreHidden = JsonUtil.getBoolean(modelJson, "ignoreHidden", true);
        this.types = new ReadOnlyList<>(JsonUtil.getArrayOfEnums(modelJson, "types", ModelMediaFile.Type.class, null));
        this.minWeight = JsonUtil.getLong(modelJson, "minWeight", null);
        this.maxWeight = JsonUtil.getLong(modelJson, "maxWeight", null);
        this.minCreateAt = JsonUtil.getLocalDateTime(modelJson, "minCreateAt", null);
        this.maxCreateAt = JsonUtil.getLocalDateTime(modelJson, "maxCreateAt", null);
        this.minUpdateAt = JsonUtil.getLocalDateTime(modelJson, "minUpdateAt", null);
        this.maxUpdateAt = JsonUtil.getLocalDateTime(modelJson, "maxUpdateAt", null);
        this.extensions = new ReadOnlyList<>(JsonUtil.getArrayOfPrimitives(modelJson, "extensions", null));
        this.minDuration = JsonUtil.getInt(modelJson, "minDuration", null);
        this.maxDuration = JsonUtil.getInt(modelJson, "maxDuration", null);
    }

    public String toJson() {
        JSONObject modelJson = JsonUtil.newJsonObject();

        JsonUtil.addBoolean(modelJson, "ignoreHidden", this.ignoreHidden);
        JsonUtil.addArrayOfEnums(modelJson, "types", this.types);
        JsonUtil.addLong(modelJson, "minWeight", this.minWeight);
        JsonUtil.addLong(modelJson, "maxWeight", this.maxWeight);
        JsonUtil.addLocalDateTime(modelJson, "minCreateAt", this.minCreateAt);
        JsonUtil.addLocalDateTime(modelJson, "maxCreateAt", this.maxCreateAt);
        JsonUtil.addLocalDateTime(modelJson, "minUpdateAt", this.minUpdateAt);
        JsonUtil.addLocalDateTime(modelJson, "maxUpdateAt", this.maxUpdateAt);
        JsonUtil.addArrayOfPrimitives(modelJson, "extensions", this.extensions);
        JsonUtil.addInt(modelJson, "minDuration", this.minDuration);
        JsonUtil.addInt(modelJson, "maxDuration", this.maxDuration);

        return modelJson.toString();
    }
}
