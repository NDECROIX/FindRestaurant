package com.decroix.nicolas.go4lunch.controller.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.decroix.nicolas.go4lunch.R;
import com.decroix.nicolas.go4lunch.api.UserHelper;
import com.decroix.nicolas.go4lunch.base.BaseActivity;
import com.decroix.nicolas.go4lunch.models.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class AuthActivity extends BaseActivity {

    /**
     * Google Connection Request Code
     */
    private static final int RC_SIGN_IN = 123;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    /**
     * Create an intent for this activity
     *
     * @param context Context of the application
     * @return Intent
     */
    public static Intent newIntent(Context context) {
        return new Intent(context, AuthActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        ButterKnife.bind(this);
        // Get an instance of firebase
        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    public void onBackPressed() {

    }

    /**
     * Check if the user is currently logged in.
     * if connected, start the main activity.
     */
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) startMainActivity();

    }

    /**
     * Start main activity
     */
    private void startMainActivity() {
        //startActivity(MainActivity.newIntent(this));
    }

    /**
     * Handle the click on the facebook button
     */
    @OnClick(R.id.auth_activity_sign_in_facebook)
    public void signInWithFacebook() {

    }

    /**
     * Handle the click on the google button
     */
    @OnClick(R.id.auth_activity_sign_in_google)
    public void signInWithGoogle() {
        signInGoogle();
    }


    private void signInGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * Handle the sign-in google task
     *
     * @param completedTask Task of the last step
     */
    private void handleSignInGoogle(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                firebaseAuthWithGoogle(account);
            }
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create the user in firebase if this is the first connection
     *
     * @param acct Google sign-in account
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Objects.requireNonNull(user, getString(R.string.rnn_user_must_not_be_null));
                        if (task.getResult() != null && task.getResult().getAdditionalUserInfo() != null &&
                                task.getResult().getAdditionalUserInfo().isNewUser()) {
                            createUserInFirestore(user);
                        }
                        startMainActivity();
                    } else {
                        // If sign in fails, display a message to the user.
                        Snackbar.make(findViewById(R.id.auth_activity_constraint_layout),
                                getString(R.string.error_auth_failed), Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Add the user in firestore
     *
     * @param user Current user
     */
    private void createUserInFirestore(FirebaseUser user) {
        String uid = user.getUid();
        String name = user.getDisplayName();
        String urlPicture = (user.getPhotoUrl() != null) ? user.getPhotoUrl().toString() : null;
        User myUser = new User(uid, name, urlPicture, "", "", new ArrayList<>());
        UserHelper.createUser(myUser)
                .addOnFailureListener(this.onFailureListener(getString(R.string.afl_get_user)));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInGoogle(task);

        } else {
            showMessage(getString(R.string.uac_connection_cancelled));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}