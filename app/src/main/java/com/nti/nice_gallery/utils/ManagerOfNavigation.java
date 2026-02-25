package com.nti.nice_gallery.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import java.util.HashMap;
import java.util.Objects;

public class ManagerOfNavigation {

    private static final String INTENT_KEY_DATA_ID = "dataId";

    private static int idCounter;
    private static HashMap<Integer, Object> repository;

    private final Context context;

    public ManagerOfNavigation(Context context) {
        this.context = context;

        if (repository == null) {
            idCounter = 0;
            repository = new HashMap<>();
        }
    }

    public void navigate(Class to, Object payload) {
        Intent intent = new Intent(context, to);

        if (payload != null) {
            repository.put(idCounter, payload);
            intent.putExtra(INTENT_KEY_DATA_ID, idCounter);
            idCounter++;
        }

        context.startActivity(intent);
    }

    public void navigateBack() {
        if (!(context instanceof Activity)) {
            throw new IllegalStateException();
        }

        Activity activity = (Activity) context;
        activity.onBackPressed();
    }

    public Object getPayload() {
        if (!(context instanceof Activity)) {
            throw new IllegalStateException();
        }

        Activity activity = (Activity) context;
        int dataId = activity.getIntent().getIntExtra(INTENT_KEY_DATA_ID, -1);

        if (dataId == -1) {
            return null;
        }

        Object payload = repository.get(dataId);
        repository.remove(dataId);

        return payload;
    }
}
