package com.nti.nice_gallery.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.nti.nice_gallery.data.ManagerOfSettings_Test1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nti.nice_gallery.R;
import com.nti.nice_gallery.data.IManagerOfFiles;
import com.nti.nice_gallery.data.IManagerOfSettings;
import com.nti.nice_gallery.data.ManagerOfFiles_Test1;
import com.nti.nice_gallery.models.ModelScanParams;
import com.nti.nice_gallery.models.ModelStorage;
import com.nti.nice_gallery.utils.ReadOnlyList;
import com.nti.nice_gallery.views.ViewStorageListing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FragmentSettings extends Fragment {

    private static final String LOG_TAG = "FragmentSettings";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        return view;
    }
}
