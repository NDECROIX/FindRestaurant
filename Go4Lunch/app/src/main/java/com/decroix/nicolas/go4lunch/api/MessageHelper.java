package com.decroix.nicolas.go4lunch.api;

import com.decroix.nicolas.go4lunch.models.Message;
import com.decroix.nicolas.go4lunch.models.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class MessageHelper {

    private static final String COLLECTION_NAME_MESSAGE = "messages";
    private static final int NUMBER_OF_MESSAGE = 10;

    // --- GET ---

    private static CollectionReference getMessageCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME_MESSAGE);
    }

    public static Task<DocumentReference> createMessage(String text, User currentUser){
        Message message = new Message(text, currentUser);
        return MessageHelper.getMessageCollection().add(message);
    }

    public static Query getAllMessageForChat(){
        return MessageHelper.getMessageCollection()
                .orderBy("dateCreated")
                .limit(NUMBER_OF_MESSAGE);
    }

    public static Task<DocumentReference> createMessageWithImageForChat(String uriImage, String text, User userSender){
        Message message = new Message(text, uriImage, userSender);
        return MessageHelper.getMessageCollection().add(message);
    }

    public static Query getMessageFromUserSender(String userSenderID) {
        return MessageHelper.getMessageCollection().whereEqualTo("userSenderID", userSenderID);
    }

    public static Task<Void> deleteMessage(String messageID) {
        return MessageHelper.getMessageCollection().document(messageID).delete();
    }

}
