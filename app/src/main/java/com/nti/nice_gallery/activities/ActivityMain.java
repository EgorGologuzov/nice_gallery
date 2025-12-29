package com.nti.nice_gallery.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.nti.nice_gallery.R;
import com.nti.nice_gallery.fragments.FragmentMediaAll;
import com.nti.nice_gallery.fragments.FragmentMediaTree;
import com.nti.nice_gallery.fragments.FragmentSettings;
import com.nti.nice_gallery.utils.ManagerOfPermissions;

public class ActivityMain extends AppCompatActivity {

    private static final String TAG_MEDIA_ALL = "fragment_media_all";
    private static final String TAG_MEDIA_TREE = "fragment_media_tree";
    private static final String TAG_SETTINGS = "fragment_settings";

    private BottomNavigationView bottomNavigationView;

    private FragmentMediaAll fragmentMediaAll;
    private FragmentMediaTree fragmentMediaTree;
    private FragmentSettings fragmentSettings;

    private Fragment currentFragment;

    private ManagerOfPermissions managerOfPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initManagers();
        requestManageExternalStoragePermission();
    }

    private void initViews() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        fragmentMediaAll = (FragmentMediaAll) getSupportFragmentManager()
                .findFragmentByTag(TAG_MEDIA_ALL);
        fragmentMediaTree = (FragmentMediaTree) getSupportFragmentManager()
                .findFragmentByTag(TAG_MEDIA_TREE);
        fragmentSettings = (FragmentSettings) getSupportFragmentManager()
                .findFragmentByTag(TAG_SETTINGS);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.bottom_menu_button_all) {
                if (fragmentMediaAll == null) {
                    fragmentMediaAll = new FragmentMediaAll();
                }
                showFragment(fragmentMediaAll, TAG_MEDIA_ALL);
                return true;
            } else if (itemId == R.id.bottom_menu_button_folders) {
                if (fragmentMediaTree == null) {
                    fragmentMediaTree = new FragmentMediaTree();
                }
                showFragment(fragmentMediaTree, TAG_MEDIA_TREE);
                return true;
            } else if (itemId == R.id.bottom_menu_button_settings) {
                if (fragmentSettings == null) {
                    fragmentSettings = new FragmentSettings();
                }
                showFragment(fragmentSettings, TAG_SETTINGS);
                return true;
            }

            return false;
        });
    }

    private void initManagers() {
        managerOfPermissions = new ManagerOfPermissions(this);
    }

    private void showFragment(Fragment fragment, String tag) {
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
    }

    private void onManageExternalStoragePermissionGranted() {
        bottomNavigationView.setSelectedItemId(R.id.bottom_menu_button_all);
    }

    private void onManageExternalStoragePermissionDenied() {
        finishAndRemoveTask();
    }

    private void requestManageExternalStoragePermission() {
        managerOfPermissions.requestExternalStorageManagerPermission(this::onManageExternalStoragePermissionGranted, this::onManageExternalStoragePermissionDenied);
    }
}