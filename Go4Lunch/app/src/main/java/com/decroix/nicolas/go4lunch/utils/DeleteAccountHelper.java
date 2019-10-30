package com.decroix.nicolas.go4lunch.utils;

import android.content.Context;

import com.decroix.nicolas.go4lunch.R;
import com.decroix.nicolas.go4lunch.api.RestaurantHelper;
import com.decroix.nicolas.go4lunch.api.UserHelper;
import com.decroix.nicolas.go4lunch.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseUser;

/**
 * Delete an account from firebase
 */
public class DeleteAccountHelper {

    private FirebaseUser mUser;

    /**
     * Interface implemented by the class that deletes a user
     */
    public interface UserDeleteListener{
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
     * @param context App context
     * @param firebaseUser Firebase user
     * @param email User email
     * @param password User Password
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
                        getUserRestaurant(mUser, user);
                    } else {
                        callback.failureToDeleteUser(context.getString(R.string.failure_email_password));
                    }
                });
    }

    /**
     * Check if there is a restaurant where he/she has registered
     * @param mUser User to delete
     */
    private void getUserRestaurant(FirebaseUser mUser, User user) {
        if (user != null && user.getLunchRestaurantID() != null && !user.getLunchRestaurantID().isEmpty()) {
            deleteUserFromRestaurant(mUser, user);
        } else {
            deleteUser(mUser);
        }
    }

    /**
     * Delete the user from the restaurant where he/she has registered
     * @param mUser Firebase user to delete
     * @param user Firebase database user to delete
     */
    private void deleteUserFromRestaurant(FirebaseUser mUser, User user){
        RestaurantHelper.removeUserFromList(user.getLunchRestaurantID(), user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        deleteUser(mUser);
                    }
                })
                .addOnFailureListener(callback.failureToDeleteUser(context.getString(R.string.afl_remove_user_from_list)));
    }

    /**
     * Delete the user from Firebase
     * @param mUser User to delete
     */
    private void deleteUser(FirebaseUser mUser){
        UserHelper.deleteUser(mUser.getUid()).addOnSuccessListener(voidTask ->
                mUser.delete().addOnSuccessListener(task ->
                        callback.userDeleted()))
                .addOnFailureListener(callback.failureToDeleteUser(context.getString(R.string.afl_delete_user)));
    }
}
