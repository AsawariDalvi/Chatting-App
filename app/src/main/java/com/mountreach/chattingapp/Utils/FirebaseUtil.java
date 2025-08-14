package com.mountreach.chattingapp.Utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseUtil {

    public static String currentUserId() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public static CollectionReference getContactsCollectionReference() {
        return FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId())
                .collection("contacts");
    }
}

