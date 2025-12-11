package com.nti.nice_gallery.data;

import android.graphics.Bitmap;
import android.net.Uri;

import com.nti.nice_gallery.models.ModelMediaTreeItem;
import com.nti.nice_gallery.models.ModelStorage;

import java.util.List;

public interface IManagerOfFiles {
    List<ModelMediaTreeItem> getAllFiles();
    Bitmap getItemThumbnail(ModelMediaTreeItem item);
    Uri getItemContentUri(ModelMediaTreeItem item);
    List<ModelStorage> getAllStorages();
}
