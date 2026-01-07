package com.nti.nice_gallery.activities;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.nti.nice_gallery.R;
import com.nti.nice_gallery.fragments.FragmentMediaAll;
import com.nti.nice_gallery.fragments.FragmentMediaTree;
import com.nti.nice_gallery.fragments.FragmentSettings;
import com.nti.nice_gallery.utils.ManagerOfPermissions;
import com.nti.nice_gallery.utils.ManagerOfThreads;

import java.util.HashMap;
import java.util.function.Consumer;

import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

public class ActivityMain extends AppCompatActivity {

    private static final String TAG_MEDIA_ALL = "fragment_media_all";
    private static final String TAG_MEDIA_TREE = "fragment_media_tree";
    private static final String TAG_SETTINGS = "fragment_settings";

    private BottomNavigationView bottomNavigationView;

    private FragmentMediaAll fragmentMediaAll;
    private FragmentMediaTree fragmentMediaTree;
    private FragmentSettings fragmentSettings;

    private Fragment currentFragment;
    private HashMap<Fragment, Consumer<ActivityMain>> backButtonPressedListeners;

    private ManagerOfPermissions managerOfPermissions;
    private ManagerOfThreads managerOfThreads;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        fragmentMediaAll = (FragmentMediaAll) getSupportFragmentManager()
                .findFragmentByTag(TAG_MEDIA_ALL);
        fragmentMediaTree = (FragmentMediaTree) getSupportFragmentManager()
                .findFragmentByTag(TAG_MEDIA_TREE);
        fragmentSettings = (FragmentSettings) getSupportFragmentManager()
                .findFragmentByTag(TAG_SETTINGS);

        backButtonPressedListeners = new HashMap<>();
        managerOfPermissions = new ManagerOfPermissions(this);
        managerOfThreads = new ManagerOfThreads(this);

        Function2<Fragment, String, Integer> showFragment = (fragment, tag) -> {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            if (currentFragment != null) {
                transaction.hide(currentFragment);
            }

            if (fragment.isAdded()) {
                transaction.show(fragment);
            } else {
                transaction.add(R.id.contentFrame, fragment, tag);
            }

            currentFragment = fragment;
            transaction.commit();

            return 0;
        };

        Function1<MenuItem, Boolean> onSelectedFragmentChange = menuItem -> {
            int itemId = menuItem.getItemId();

            if (itemId == R.id.bottom_menu_button_all) {
                if (fragmentMediaAll == null) {
                    fragmentMediaAll = new FragmentMediaAll();
                }
                showFragment.invoke(fragmentMediaAll, TAG_MEDIA_ALL);
                return true;
            } else if (itemId == R.id.bottom_menu_button_folders) {
                if (fragmentMediaTree == null) {
                    fragmentMediaTree = new FragmentMediaTree();
                }
                showFragment.invoke(fragmentMediaTree, TAG_MEDIA_TREE);
                return true;
            } else if (itemId == R.id.bottom_menu_button_settings) {
                if (fragmentSettings == null) {
                    fragmentSettings = new FragmentSettings();
                }
                showFragment.invoke(fragmentSettings, TAG_SETTINGS);
                return true;
            }

            return false;
        };

        Runnable onManageExternalStoragePermissionGranted = () -> {
            bottomNavigationView.setSelectedItemId(R.id.bottom_menu_button_all);
        };

        Runnable onManageExternalStoragePermissionDenied = () -> {
            finishAndRemoveTask();
        };

        Runnable requestManageExternalStoragePermission = () -> {
            managerOfPermissions.requestExternalStorageManagerPermission(onManageExternalStoragePermissionGranted, onManageExternalStoragePermissionDenied);
        };

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Consumer<ActivityMain> fragmentHandler = backButtonPressedListeners.get(currentFragment);
                managerOfThreads.safeAccept(fragmentHandler, ActivityMain.this);
            }
        };

        bottomNavigationView.setOnItemSelectedListener(onSelectedFragmentChange::invoke);
        getOnBackPressedDispatcher().addCallback(this, callback);

        requestManageExternalStoragePermission.run();
    }

    public void setBackButtonPressedListener(Fragment sender, Consumer<ActivityMain> listener) {
        backButtonPressedListeners.put(sender, listener);
    }
}