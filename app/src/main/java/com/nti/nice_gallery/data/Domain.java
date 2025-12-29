package com.nti.nice_gallery.data;

import android.content.Context;

public class Domain {

    public static IManagerOfFiles getManagerOfFiles(Context context) {
        return new ManagerOfFiles(context);
//        return new ManagerOfFiles_Test1(context);
    }

    public static IManagerOfSettings getManagerOfSettings(Context context) {
//        return new ManagerOfSettings();
        return new ManagerOfSettings_Test1();
    }

}
