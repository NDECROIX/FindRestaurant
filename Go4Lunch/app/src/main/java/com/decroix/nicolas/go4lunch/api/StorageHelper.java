package com.decroix.nicolas.go4lunch.api;

import android.net.Uri;

import androidx.annotation.VisibleForTesting;

import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


public class StorageHelper {

    /**
     * Retrieves the storage reference
     * @param uid User uid
     * @return Storage reference of the image
     */
    private static StorageReference getStorageReference(String uid){
        return FirebaseStorage.getInstance().getReference(uid);
    }

    /**
     * Add a file on firebase storage
     * @param userUid User uid
     * @param imageUid Image uid
     * @param uri Image uri
     * @return Upload task
     */
    public static UploadTask putFileOnFirebaseStorage(String userUid, String imageUid, Uri uri){
        return getStorageReference(userUid).child(imageUid).putFile(uri);
    }

    /**
     * Delete a file from firebase storage
     * @param uid Image uid
     * @return Void task
     */
    @VisibleForTesting
    static Task<Void> deleteFileFromFirebaseStorage(String userId, String uid){
        return getStorageReference(userId).child(uid).delete();
    }

    /**
     * Delete a file from firebase storage
     * @return Void task
     */
    public static Task<Void> deleteFilesFromFirebaseStorage(String userUid){
        return getStorageReference(userUid).delete();
    }

    /**
     * Retrieves the url from firebase storage
     * @param uid Image uid
     * @return Uri task
     */
    public static Task<Uri> getUrlPicture(String userUid, String uid){
        return getStorageReference(userUid).child(uid).getDownloadUrl();
    }

}
