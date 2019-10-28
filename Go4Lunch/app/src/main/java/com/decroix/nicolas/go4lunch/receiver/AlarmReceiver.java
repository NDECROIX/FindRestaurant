package com.decroix.nicolas.go4lunch.receiver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.decroix.nicolas.go4lunch.R;
import com.decroix.nicolas.go4lunch.api.RestaurantHelper;
import com.decroix.nicolas.go4lunch.api.UserHelper;
import com.decroix.nicolas.go4lunch.base.BaseActivity;
import com.decroix.nicolas.go4lunch.controller.activities.MainActivity;
import com.decroix.nicolas.go4lunch.models.Restaurant;
import com.decroix.nicolas.go4lunch.models.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "channel_01";
    private static final int NOTIFICATION_ID = 0;
    private Context mContext;
    private FirebaseUser mUser;

    @Override
    public void onReceive(Context context, Intent intent) {
        startNotification(context);
    }

    /**
     * Start to create the notification
     * @param context the context in which it is created
     */
    private void startNotification(Context context) {
        mContext = context;
        createNotificationChannel();
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mUser != null) {
            manageNotification();
        }
    }

    /**
     * Retrieves the information necessary to create the notification
     */
    private void manageNotification() {
        // get user from firestore
        getUserFromFirestore()
                .addOnCompleteListener(userTask -> {

                    if (userTask.isSuccessful() && userTask.getResult() != null) {
                        User user = userTask.getResult().toObject(User.class);

                        if (user != null && user.getLunchRestaurantID() != null) {
                            // Take the lunch restaurant
                            getRestaurantFromFirestore(user.getLunchRestaurantID())
                                    .addOnSuccessListener(restaurantResult -> {
                                        if (restaurantResult != null) {
                                            // Create the notification with the restaurant
                                            Restaurant restaurant = restaurantResult.toObject(Restaurant.class);
                                            if (restaurant != null) {
                                                NotificationCompat.Builder builder = createNotification(restaurant);
                                                showNotification(builder);
                                            }
                                        }
                                    }).addOnFailureListener(e ->
                                    Toast.makeText(mContext, mContext.getString(R.string.afl_get_restaurant), Toast.LENGTH_SHORT)
                                            .show());
                        }
                    }
                });
    }

    /**
     * Retrieves the restaurant where the user is registered
     * @param restaurantID Restaurant ID
     * @return Task with result
     */
    private Task<DocumentSnapshot> getRestaurantFromFirestore(String restaurantID) {
        return RestaurantHelper.getRestaurant(restaurantID);
    }

    /**
     * Retrieves the user from Firebase database
     * @return Task with result
     */
    private Task<DocumentSnapshot> getUserFromFirestore() {
        return UserHelper.getUser(mUser.getUid());
    }

    /**
     * Create the notification to display
     * @param restaurant Restaurant where user is register
     * @return The notification to display
     */
    private NotificationCompat.Builder createNotification(Restaurant restaurant) {
        StringBuilder content = new StringBuilder();
        content.append(restaurant.getAddress());
        if (restaurant.getUsers().size() > 1) {
            content.append(R.string.notification_joins_you);
            for (User user : restaurant.getUsers()) {
                if (!user.getUid().equals(mUser.getUid())) {
                    content.append(user.getUsername()).append(", ");
                }
            }
            content.replace(content.length() - 2, content.length(), ".");
        }

        Intent intent = new Intent(mContext, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(BaseActivity.EXTRA_CALLER, getClass().getName());
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);

        return new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(restaurant.getName())
                .setStyle(new NotificationCompat
                        .BigTextStyle().bigText(content.toString()))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent);
    }

    /**
     * Display the notification on the user's screen
     * @param builder The notification to display
     */
    private void showNotification(NotificationCompat.Builder builder) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    /**
     * Create the NotificationChannel, but only on API 26+ because
     * the NotificationChannel class is new and not in the support library
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = mContext.getString(R.string.notification_channel_name);
            String description = mContext.getString(R.string.notification_channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = mContext.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
