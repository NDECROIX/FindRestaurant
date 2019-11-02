package com.decroix.nicolas.go4lunch.utils;

import android.content.Context;

import com.decroix.nicolas.go4lunch.R;
import com.decroix.nicolas.go4lunch.api.MessageHelper;
import com.decroix.nicolas.go4lunch.api.RestaurantHelper;
import com.decroix.nicolas.go4lunch.api.StorageHelper;
import com.decroix.nicolas.go4lunch.api.UserHelper;
import com.decroix.nicolas.go4lunch.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

/**
 * Delete an account from firebase
 */
public class DeleteAccountHelper {

    private FirebaseUser mUser;

    /**
     * Interface implemented by the class that deletes a user
     */
    public interface UserDeleteListener {
        void userDeleted();

        OnFailureListener failureToDeleteUser(String text);
    }

    private final UserDeleteListener callback;

    /**
     * Use only to access the resource
     */
    private Context context;

    public DeleteAccountHelper(UserDeleteListener callback) {
        this.callback = callback;
    }

    /**
     * Start a re-authentication to delete the account
     *
     * @param context      App context
     * @param firebaseUser Firebase user
     * @param email        User email
     * @param password     User Password
     */
    public void deleteAccount(Context context, User user, FirebaseUser firebaseUser, String email, String password) {
        this.context = context;
        mUser = firebaseUser;
        // Get auth credentials from the user for re-authentication. The example below shows
        // email and password credentials but there are multiple possible providers,
        // such as GoogleAuthProvider or FacebookAuthProvider.
        AuthCredential credential = EmailAuthProvider
                .getCredential(email, password);
        // Prompt the user to re-provide their sign-in credentials
        mUser.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        getUserRestaurant(user);
                    } else {
                        callback.failureToDeleteUser(context.getString(R.string.failure_email_password));
                    }
                });
    }

    /**
     * Check if there is a restaurant where he/she has registered
     */
    private void getUserRestaurant(User user) {
        if (user != null && user.getLunchRestaurantID() != null && !user.getLunchRestaurantID().isEmpty()) {
            deleteUserFromRestaurant(user);
        } else {
            deleteMessages();
        }
    }

    /**
     * Delete the user from the restaurant where he/she has registered
     *
     * @param user Firebase database user to delete
     */
    private void deleteUserFromRestaurant(User user) {
        RestaurantHelper.removeUserFromList(user.getLunchRestaurantID(), user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        deleteMessages();
                    }
                })
                .addOnFailureListener(callback.failureToDeleteUser(context.getString(R.string.afl_remove_user_from_list)));
    }

    private void deleteMessages() {
        MessageHelper.getMessageFromUserSender(mUser.getUid()).get().addOnSuccessListener(result -> {
            List<DocumentSnapshot> messages = result.getDocuments();
            for (DocumentSnapshot message : messages) {
                MessageHelper.deleteMessage(message.getId())
                        .addOnFailureListener(callback.failureToDeleteUser(context.getString(R.string.afl_delete_messages)));
                StorageHelper.deleteFilesFromFirebaseStorage(mUser.getUid())
                        .addOnFailureListener(callback.failureToDeleteUser(context.getString(R.string.afl_delete_files)));
            }
            deleteUser();
        }).addOnFailureListener(callback.failureToDeleteUser(context.getString(R.string.afl_delete_user)));
    }

    /**
     * Delete the user from Firebase
     */
    private void deleteUser() {
        UserHelper.deleteUser(mUser.getUid()).addOnSuccessListener(voidTask ->
                mUser.delete().addOnSuccessListener(task ->
                        callback.userDeleted()))
                .addOnFailureListener(callback.failureToDeleteUser(context.getString(R.string.afl_delete_user)));
    }
}
