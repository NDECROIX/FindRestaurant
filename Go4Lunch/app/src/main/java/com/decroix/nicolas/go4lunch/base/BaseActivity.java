package com.decroix.nicolas.go4lunch.base;

import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public abstract class BaseActivity extends AppCompatActivity {

    public static final String EXTRA_CALLER = "caller";

    /**
     * Get the current user logged
     * @return Firebase user
     */
    @Nullable
    protected FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    /**
     * Get the current user ID
     * @return Firebase user ID
     */
    @Nullable
    protected String getCurrentUserID() {
        if (getCurrentUser() != null) {
            return getCurrentUser().getUid();
        }
        return null;
    }

    /**
     * Returns true if a user is currently logged in
     * @return True if the user is logged in
     */
    protected Boolean isCurrentUserLogged() {
        return (this.getCurrentUser() != null);
    }


    /**
     * Handled the failure from a Task
     * @return On failure listener
     */
    protected OnFailureListener onFailureListener(String message) {
        return e -> Toast.makeText(getApplicationContext(),
                message, Toast.LENGTH_LONG).show();
    }

    /**
     * Display the message passed as a parameter
     * @param message Message that we show to the user
     */
    protected void showMessage(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
