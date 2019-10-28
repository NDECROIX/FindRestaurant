package com.decroix.nicolas.go4lunch.api;

import com.decroix.nicolas.go4lunch.models.Restaurant;
import com.decroix.nicolas.go4lunch.models.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * Management of calls on the user class in database.
 * Firebase firestore service.
 */
public class UserHelper {

    /**
     * Name of the user class
     */
    private static final String COLLECTION_NAME = "users";

    /**
     * Get the collection reference
     *
     * @return The collection reference
     */
    private static CollectionReference getUserCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    /**
     * Add in the database the user passed in parameter
     *
     * @param userToCreate User to add
     * @return Void task
     */
    public static Task<Void> createUser(User userToCreate) {
        return UserHelper.getUserCollection().document(userToCreate.getUid()).set(userToCreate);
    }

    /**
     * Retrieves a user whose ID is passed in the function parameter
     * @param uid ID of the user
     * @return Task with response
     */
    public static Task<DocumentSnapshot> getUser(String uid){
        return UserHelper.getUserCollection().document(uid).get();
    }

    /**
     * Get all users from the database
     * @return Task with response
     */
    public static Task<QuerySnapshot> getUsers(){
        return UserHelper.getUserCollection().get();
    }

    /**
     * Update the restaurant where the user will eat for lunch
     * @param uid ID of the user
     * @param restaurant Restaurant where to have lunch
     * @return Void task
     */
    public static Task<Void> updateLunchRestaurant(String uid, Restaurant restaurant){
        return UserHelper
                .getUserCollection()
                .document(uid)
                .update("lunchRestaurantName", restaurant.getName(),
                        "lunchRestaurantID", restaurant.getPlaceID());
    }

    /**
     * Add a restaurant to the list of favorites
     * @param uid ID of the user
     * @param restaurantId Restaurant to add
     * @return Void task
     */
    public static Task<Void> addRestaurantToFavorites(String uid, String restaurantId){
        return UserHelper
                .getUserCollection()
                .document(uid)
                .update("favouritePlaceID", FieldValue.arrayUnion(restaurantId));
    }

    /**
     * Delete the user from the database
     * @param uid User uid
     * @return Void task
     */
    public static Task<Void> deleteUser(String uid){
        return UserHelper.getUserCollection().document(uid).delete();
    }
}
