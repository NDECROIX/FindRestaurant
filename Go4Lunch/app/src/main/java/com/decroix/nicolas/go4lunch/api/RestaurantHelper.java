package com.decroix.nicolas.go4lunch.api;

import com.decroix.nicolas.go4lunch.models.Restaurant;
import com.decroix.nicolas.go4lunch.models.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Management of calls on the restaurant class of the database.
 * Firebase firestore service.
 */
public class RestaurantHelper {
    /**
     * restaurant class name;
     */
    private static final String COLLECTION_NAME_RESTAURANT = "restaurants";

    /**
     * Get the collection reference
     *
     * @return The collection reference
     */
    private static CollectionReference getRestaurantCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME_RESTAURANT);
    }

    /**
     * Add a restaurant to the database
     * @param newRestaurant restaurant to add to the database
     * @return Void task
     */
    public static Task<Void> createRestaurant(Restaurant newRestaurant){
        return RestaurantHelper.getRestaurantCollection().document(newRestaurant.getPlaceID()).set(newRestaurant);
    }

    /**
     * Delete a restaurant to the database.
     * Only use for test.
     * @param restaurantId restaurant to delete
     * @return Void task
     */
    public static Task<Void> deleteRestaurant(String restaurantId){
        return RestaurantHelper.getRestaurantCollection().document(restaurantId).delete();
    }

    /**
     * Retrieves the restaurant whose the id is passed as a parameter
     * @param placeID id of the place
     * @return Task with response
     */
    public static Task<DocumentSnapshot> getRestaurant(String placeID){
        return RestaurantHelper.getRestaurantCollection().document(placeID).get();
    }

    /**
     * Adding a user to the restaurant's user list in the database
     * @param placeID id of the restaurant where we add the user
     * @param myUser User to add
     * @return Void task
     */
    public static Task<Void> addUserInList(String placeID, User myUser) {
        return RestaurantHelper
                .getRestaurantCollection()
                .document(placeID)
                .update("users", FieldValue.arrayUnion(getMapUser(myUser)));
    }

    /**
     * Delete a user from the restaurant user list in the database
     * @param placeID id of the restaurant where we delete the user
     * @param myUser User to delete
     * @return Void task
     */
    public static Task<Void> removeUserFromList(String placeID, User myUser) {
        return RestaurantHelper
                .getRestaurantCollection()
                .document(placeID)
                .update("users", FieldValue.arrayRemove(getMapUser(myUser)));
    }

    /**
     * Create a HasMap of a user's data
     * @param myUser User
     * @return User HashMap passed in parameter
     */
    private static Map<String, Object> getMapUser(User myUser) {
        Map<String, Object> user = new HashMap<>();
        user.put("uid", myUser.getUid());
        user.put("urlPicture", myUser.getUrlPicture());
        user.put("username", myUser.getUsername());
        return user;
    }
}
