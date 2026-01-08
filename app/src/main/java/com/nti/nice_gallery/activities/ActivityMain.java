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

public class ActivityMain extends AppCompatActivity {

    private static final String LOG_TAG = "ActivityMain";

    private static final String TAG_MEDIA_ALL = "fragment_media_all";
    private static final String TAG_MEDIA_TREE = "fragment_media_tree";
    private static final String TAG_SETTINGS = "fragment_settings";

    private static final int STARTUP_MENU_BUTTON_ID = R.id.bottom_menu_button_folders;

    private static Integer menuSelectedItemId;

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

        if (menuSelectedItemId != null) {
            if (menuSelectedItemId == R.id.bottom_menu_button_all) {
                currentFragment = fragmentMediaAll;
            }
            if (menuSelectedItemId == R.id.bottom_menu_button_folders) {
                currentFragment = fragmentMediaTree;
            }
            if (menuSelectedItemId == R.id.bottom_menu_button_settings) {
                currentFragment = fragmentSettings;
            }
        }

        backButtonPressedListeners = new HashMap<>();
        managerOfPermissions = new ManagerOfPermissions(this);
        managerOfThreads = new ManagerOfThreads(this);

        Runnable showCurrentFragment = () -> {
            if (currentFragment == null) {
                return;
            }

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            if (fragmentMediaAll != null && fragmentMediaAll != currentFragment) {
                transaction.hide(fragmentMediaAll);
            }
            if (fragmentMediaTree != null && fragmentMediaTree != currentFragment) {
                transaction.hide(fragmentMediaTree);
            }
            if (fragmentSettings != null && fragmentSettings != currentFragment) {
                transaction.hide(fragmentSettings);
            }

            String currentFragmentTag = null;

            if (currentFragment == fragmentMediaAll) {
                currentFragmentTag = TAG_MEDIA_ALL;
            }
            if (currentFragment == fragmentMediaTree) {
                currentFragmentTag = TAG_MEDIA_TREE;
            }
            if (currentFragment == fragmentSettings) {
                currentFragmentTag = TAG_SETTINGS;
            }

            if (currentFragment.isAdded()) {
                transaction.show(currentFragment);
            } else {
                transaction.add(R.id.contentFrame, currentFragment, currentFragmentTag);
            }

            transaction.commit();
        };

        Function1<MenuItem, Boolean> onSelectedFragmentChange = menuItem -> {
            int itemId = menuItem.getItemId();

            if (itemId == R.id.bottom_menu_button_all) {
                if (fragmentMediaAll == null) {
                    fragmentMediaAll = new FragmentMediaAll();
                }
                currentFragment = fragmentMediaAll;
                menuSelectedItemId = itemId;
                showCurrentFragment.run();
                return true;
            }
            if (itemId == R.id.bottom_menu_button_folders) {
                if (fragmentMediaTree == null) {
                    fragmentMediaTree = new FragmentMediaTree();
                }
                currentFragment = fragmentMediaTree;
                menuSelectedItemId = itemId;
                showCurrentFragment.run();
                return true;
            }
            if (itemId == R.id.bottom_menu_button_settings) {
                if (fragmentSettings == null) {
                    fragmentSettings = new FragmentSettings();
                }
                currentFragment = fragmentSettings;
                menuSelectedItemId = itemId;
                showCurrentFragment.run();
                return true;
            }

            return false;
        };

        Runnable onManageExternalStoragePermissionGranted = () -> {
            if (currentFragment != null) {
                showCurrentFragment.run();
            } else {
                bottomNavigationView.setSelectedItemId(STARTUP_MENU_BUTTON_ID);
            }
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