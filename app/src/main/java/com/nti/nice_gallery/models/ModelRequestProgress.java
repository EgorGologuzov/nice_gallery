package com.nti.nice_gallery.models;

public class ModelRequestProgress {

    public final int numberCompletedSteps;
    public final int numberTotalSteps;
    public final String currentStep;

    public ModelRequestProgress(int numberCompletedSteps, int numberTotalSteps, String currentStep) {
        this.numberCompletedSteps = numberCompletedSteps;
        this.numberTotalSteps = numberTotalSteps;
        this.currentStep = currentStep;
    }
}
