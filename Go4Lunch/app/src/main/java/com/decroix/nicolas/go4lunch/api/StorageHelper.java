package com.decroix.nicolas.go4lunch.api;

import android.net.Uri;

import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


public class StorageHelper {

    /**
     * Retrieves the storage reference
     * @param uid image uid
     * @return Storage reference of the image
     */
    private static StorageReference getStorageReference(String uid){
        return FirebaseStorage.getInstance().getReference(uid);
    }

    /**
     * Add a file on firebase storage
     * @param uid Image uid
     * @param uri Image uri
     * @return Upload task
     */
    public static UploadTask putFileOnFirebaseStorage(String uid, Uri uri){
        return getStorageReference(uid).putFile(uri);
    }

    /**
     * Delete a file from firebase storage
     * @param uid Image uid
     * @return Void task
     */
    static Task<Void> deleteFileFromFirebaseStorage(String uid){
        return getStorageReference(uid).delete();
    }

    /**
     * Retrieves the url from firebase storage
     * @param uid Image uid
     * @return Uri task
     */
    public static Task<Uri> getUrlPicture(String uid){
        return getStorageReference(uid).getDownloadUrl();
    }

}
