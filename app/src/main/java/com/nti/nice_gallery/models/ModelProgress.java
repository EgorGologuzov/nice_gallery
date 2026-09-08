package com.nti.nice_gallery.models;

import com.nti.nice_gallery.utils.JsonUtil;

import org.json.JSONObject;

public class ModelProgress {

    public final int numberCompletedSteps;
    public final int numberTotalSteps;
    public final String currentStep;

    public ModelProgress(int numberCompletedSteps, int numberTotalSteps, String currentStep) {
        this.numberCompletedSteps = numberCompletedSteps;
        this.numberTotalSteps = numberTotalSteps;
        this.currentStep = currentStep;
    }

    public ModelProgress(String jsonStr) {
        JSONObject json = JsonUtil.newJsonObject(jsonStr);
        this.numberCompletedSteps = JsonUtil.getInt(json, "numberCompletedSteps", null);
        this.numberTotalSteps = JsonUtil.getInt(json, "numberTotalSteps", null);
        this.currentStep = JsonUtil.getString(json, "currentStep", null);
    }

    public String getMessage() {
        return currentStep + " (" + numberCompletedSteps + " / " + numberTotalSteps + ")";
    }

    public String toJson() {
        JSONObject json = JsonUtil.newJsonObject();
        JsonUtil.addInt(json, "numberCompletedSteps", numberCompletedSteps);
        JsonUtil.addInt(json, "numberTotalSteps", numberTotalSteps);
        JsonUtil.addString(json, "currentStep", currentStep);
        return json.toString();
    }
}
