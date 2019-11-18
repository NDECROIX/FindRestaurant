package com.decroix.nicolas.go4lunch.controller.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.decroix.nicolas.go4lunch.R;
import com.decroix.nicolas.go4lunch.api.UserHelper;
import com.decroix.nicolas.go4lunch.base.BaseActivity;
import com.decroix.nicolas.go4lunch.models.User;
import com.decroix.nicolas.go4lunch.utils.DeleteAccountHelper;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Authentication management
 */
public class AuthActivity extends BaseActivity implements DeleteAccountHelper.UserDeleteListener {

    @BindView(R.id.auth_activity_progress_bar)
    ProgressBar progressBar;

    /**
     * Google Connection Request Code
     */
    private static final int RC_SIGN_IN = 123;

    private FirebaseAuth mAuth;

    /**
     * Callback manager for the facebook api
     */
    private CallbackManager mCallbackManager;

    private boolean callBySettings;

    /**
     * Create an intent for this activity
     *
     * @param context Context of the application
     * @return Intent
     */
    public static Intent newIntent(Context context, String caller) {
        return new Intent(context, AuthActivity.class).putExtra(EXTRA_CALLER, caller);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        ButterKnife.bind(this);
        progressBar.bringToFront();
        progressBar.setVisibility(View.GONE);
        progressBar.getIndeterminateDrawable().setColorFilter(
                Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);
        // Get an instance of firebase
        mAuth = FirebaseAuth.getInstance();
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
        if (Objects.equals(getIntent().getStringExtra(EXTRA_CALLER), SettingsActivity.class.getName())
                && !callBySettings) {
            callBySettings = true;
            deleteMyAccount();
        } else {
            progressBar.setVisibility(View.GONE);
            startActivity(MainActivity.newIntent(this));
        }
    }

    /**
     * Delete the user account
     * Pass null as the parent view because its going in the dialog layout
     */
    @SuppressLint("InflateParams")
    private void deleteMyAccount() {
        LayoutInflater inflater = this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.fragment_settings_delete_account_dialog, null);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton(getString(R.string.alert_dialog_delete_account_btn_delete), (dialogInterface, i) ->
                        UserHelper.getUser(getCurrentUserID()).addOnSuccessListener(result -> {
                    DeleteAccountHelper deleteAccountHelper = new DeleteAccountHelper(this);
                    User user = result.toObject(User.class);
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    if (user != null && firebaseUser != null && user.getUid() != null) {
                        deleteAccountHelper.deleteAccount(this, user, firebaseUser);
                    }
                }))
                .setNegativeButton(getString(R.string.alert_dialog_delete_account_btn_return),
                        (dialogInterface, i) -> startMainActivity());
        alertDialog.create().show();
    }

    /**
     * Handle the click on the facebook button
     */
    @OnClick(R.id.auth_activity_sign_in_facebook)
    public void signInWithFacebook() {
        if (progressBar.getVisibility() == View.GONE) {
            progressBar.setVisibility(View.VISIBLE);
            configFacebookSignIn();
        }
    }

    /**
     * Handle the click on the google button
     */
    @OnClick(R.id.auth_activity_sign_in_google)
    public void signInWithGoogle() {
        if (progressBar.getVisibility() == View.GONE) {
            progressBar.setVisibility(View.VISIBLE);
            signInGoogle();
        }
    }

    /**
     * Configuring the facebook connection
     */
    private void configFacebookSignIn() {
        FacebookSdk.sdkInitialize(getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
        LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                    }
                });
    }

    /**
     * Handle the facebook access token from the last step
     *
     * @param token Facebook token
     */
    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Objects.requireNonNull(user, getString(R.string.rnn_user_must_not_be_null));
                        if (task.getResult() != null
                                && task.getResult().getAdditionalUserInfo() != null
                                && task.getResult().getAdditionalUserInfo().isNewUser()) {
                            createUserInFirestore(user);
                        }
                        startMainActivity();
                    } else {
                        progressBar.setVisibility(View.GONE);
                        // If sign in fails, display a message to the user.
                        Snackbar.make(findViewById(R.id.auth_activity_constraint_layout),
                                getString(R.string.error_auth_failed), Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Configures the google sign in options and start a google intent
     */
    private void signInGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
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
            progressBar.setVisibility(View.GONE);
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
                        progressBar.setVisibility(View.GONE);
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
        } else if (mCallbackManager != null && resultCode == RESULT_OK) {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        } else {
            progressBar.setVisibility(View.GONE);
            showMessage(getString(R.string.uac_connection_cancelled));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void userDeleted() {
        progressBar.setVisibility(View.GONE);
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(this, R.string.msg_account_deleted, Toast.LENGTH_LONG).show();
    }

    @Override
    public OnFailureListener failureToDeleteUser(String text) {
        progressBar.setVisibility(View.GONE);
        return e -> Toast.makeText(this, text + e, Toast.LENGTH_SHORT).show();
    }
}