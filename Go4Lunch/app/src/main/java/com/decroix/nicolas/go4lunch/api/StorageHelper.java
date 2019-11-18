package com.decroix.nicolas.go4lunch.api;

import android.net.Uri;

import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class StorageHelper {

    /**
     * Retrieves the storage reference
     *
     * @param uid User uid
     * @return Storage reference of the image
     */
    public static StorageReference getStorageReference(String uid) {
        return FirebaseStorage.getInstance().getReference(uid);
    }

    /**
     * Add a file on firebase storage
     *
     * @param userUid  User uid
     * @param imageUid Image uid
     * @param uri      Image uri
     * @return Upload task
     */
    public static UploadTask putFileOnFirebaseStorage(String userUid, String imageUid, Uri uri) {
        return getStorageReference(userUid).child(imageUid).putFile(uri);
    }

    /**
     * Delete files from firebase storage
     */
    public static void deleteFileFromFirebaseStorage(String path) {
        getStorageReference(path).delete();
    }

    /**
     * Retrieves the url from firebase storage
     *
     * @param uid Image uid
     * @return Uri task
     */
    public static Task<Uri> getUrlPicture(String userUid, String uid) {
        return getStorageReference(userUid).child(uid).getDownloadUrl();
    }

}
