package com.mountreach.chattingapp;

import static com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.firestore.*;
import com.google.firebase.storage.FirebaseStorage;
import com.mountreach.chattingapp.Adapters.MessageAdapter;
import com.mountreach.chattingapp.Model.Message;

import java.util.*;

public class OneToOneChatActivity extends AppCompatActivity {

    String receiverUid, receiverName, senderUid;
    FirebaseDatabase database;
    FirebaseAuth auth;
    FirebaseStorage storage;
    FirebaseFirestore firestore;

    EditText etMessage;
    ImageButton btnSend;
    ImageView btnAttachment;
    RecyclerView chatRecyclerView;
    ArrayList<Message> messageList;
    MessageAdapter adapter;

    String senderRoom, receiverRoom;
    FusedLocationProviderClient fusedLocationClient;

    private static final int REQUEST_CAMERA = 101;
    private static final int REQUEST_GALLERY = 102;
    private static final int REQUEST_DOCUMENT = 103;
    private static final int REQUEST_AUDIO = 104;
    private static final int REQUEST_CONTACT = 105;
    private static final int REQUEST_LOCATION = 106;
    private final int REQUEST_PERMISSIONS = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_to_one_chat);

        receiverUid = getIntent().getStringExtra("userUid");
        receiverName = getIntent().getStringExtra("fullName");

        if (receiverName == null || receiverName.trim().isEmpty()) {
            receiverName = receiverUid;
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(receiverName);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        getWindow().setStatusBarColor(getResources().getColor(R.color.black));

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        senderUid = auth.getUid();

        if (senderUid == null) {
            Toast.makeText(this, "User not authenticated. Please log in.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;

        etMessage = findViewById(R.id.messageInput);
        btnSend = findViewById(R.id.btnSend);
        btnAttachment = findViewById(R.id.btnAttachment);
        chatRecyclerView = findViewById(R.id.chatRecyclerView);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        messageList = new ArrayList<>();
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MessageAdapter(this, messageList, senderUid);
        chatRecyclerView.setAdapter(adapter);

        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                REQUEST_CODE
        );

        checkPermissions(new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_CONTACTS
        });

        loadMessages();
        checkUserExistence(receiverUid);

        btnSend.setOnClickListener(v -> {
            String msg = etMessage.getText().toString().trim();
            if (!msg.isEmpty()) {
                etMessage.setText("");
                sendMessage(msg);
            }
        });

        btnAttachment.setOnClickListener(v -> showAttachmentOptions());
    }

    private void loadMessages() {
        DatabaseReference messagesRef = database.getReference()
                .child("chats")
                .child(senderRoom)
                .child("messages");

        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Message msg = snap.getValue(Message.class);
                    if (msg != null) {
                        Object rawTs = snap.child("timestamp").getValue();
                        msg.setTimestamp(rawTs);
                        messageList.add(msg);
                    }
                }
                adapter.notifyDataSetChanged();
                if (!messageList.isEmpty()) {
                    chatRecyclerView.scrollToPosition(messageList.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OneToOneChatActivity.this, "Failed to load messages: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserExistence(String userId) {
        firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        showNonAppUserDialog();
                    } else {
                        if (receiverName == null || receiverName.isEmpty()) {
                            receiverName = documentSnapshot.getString("fullName");
                            if (receiverName != null && getSupportActionBar() != null) {
                                getSupportActionBar().setTitle(receiverName);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to check user existence: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showNonAppUserDialog() {
        new AlertDialog.Builder(this)
                .setTitle("User not available")
                .setMessage("This user isn’t using this app. You can chat with them on WhatsApp by clicking the WhatsApp icon next to their name.")
                .setPositiveButton("  ", (dialog, which) -> openWhatsAppChat())
                .setNegativeButton("Cancel", (dialog, which) -> {
                    etMessage.setEnabled(false);
                    etMessage.setHint("You cannot send messages to this user.");
                    btnSend.setEnabled(false);
                })
                .setCancelable(false)
                .show();
    }

    private void openWhatsAppChat() {
        String phoneNumber = receiverUid;
        String uri = "https://wa.me/" + phoneNumber;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(uri));
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "WhatsApp not installed or invalid number.", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendMessage(String content) {
        String messageId = database.getReference("chats").child(senderRoom).child("messages").push().getKey();

        Message message = new Message(messageId, senderUid, receiverUid, content, null, "text");

        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("messageId", message.getMessageId());
        messageMap.put("senderId", message.getSenderId());
        messageMap.put("receiverId", message.getReceiverId());
        messageMap.put("message", message.getMessage());
        messageMap.put("type", message.getType());
        messageMap.put("timestamp", ServerValue.TIMESTAMP);

        if (messageId != null) {
            database.getReference("chats").child(senderRoom).child("messages").child(messageId).setValue(messageMap);
            database.getReference("chats").child(receiverRoom).child("messages").child(messageId).setValue(messageMap);
        }

        Map<String, Object> lastMessageMap = new HashMap<>();
        lastMessageMap.put("lastMessage", content);
        lastMessageMap.put("timestamp", FieldValue.serverTimestamp());

        firestore.collection("users").document(senderUid).collection("contacts").document(receiverUid)
                .set(lastMessageMap, SetOptions.merge());
        firestore.collection("users").document(receiverUid).collection("contacts").document(senderUid)
                .set(lastMessageMap, SetOptions.merge());

        // ✅ FIX: Also update fullPhone to user profile to solve '1234567890' bug
        firestore.collection("users").document(senderUid).get()
                .addOnSuccessListener(doc -> {
                    String fullPhone = doc.getString("fullPhone");
                    if (fullPhone != null && !fullPhone.equals("1234567890")) {
                        Map<String, Object> updateMap = new HashMap<>();
                        updateMap.put("fullPhone", fullPhone);
                        firestore.collection("users").document(senderUid).set(updateMap, SetOptions.merge());
                    }
                });
    }

    private void checkIfUserExists(String phone) {
        FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("phone", phone)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // user doesn't exist
                        showWhatsAppDialog(phone);
                    } else {
                        // user exists, allow chatting
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error checking user", Toast.LENGTH_SHORT).show();
                });
    }
    private void showWhatsAppDialog(String phone) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("User not on this app")
                .setMessage("This user doesn't use this app. You can chat via WhatsApp.")
                .setPositiveButton("Yes, open WhatsApp", (dialog, which) -> openWhatsApp(phone))
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Do nothing
                })
                .setCancelable(false)
                .show();
    }
    private void openWhatsApp(String phone) {
        String url = "https://wa.me/" + phone;
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }


    private void showWhatsAppFallbackDialog(String phoneNumber) {
        new AlertDialog.Builder(this)
                .setTitle("User Not Registered")
                .setMessage("This user doesn’t use this app. You can chat with them via WhatsApp.")
                .setPositiveButton("Yes, open WhatsApp", (dialog, which) -> openWhatsApp(phoneNumber))
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Toast.makeText(this, "You can view messages, but can't send.", Toast.LENGTH_SHORT).show();
                    disableMessageInput();
                }).show();
    }
    private void checkPermissions(String[] permissions) {
        List<String> needed = new ArrayList<>();
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                needed.add(perm);
            }
        }
        if (!needed.isEmpty()) {
            ActivityCompat.requestPermissions(this, needed.toArray(new String[0]), REQUEST_PERMISSIONS);
        }
    }

    private void showAttachmentOptions() {
        String[] options = {"Camera", "Gallery", "Document", "Audio", "Contact", "Location"};
        new AlertDialog.Builder(this)
                .setTitle("Choose Attachment")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: captureImage(); break;
                        case 1: pickImageFromGallery(); break;
                        case 2: pickDocument(); break;
                        case 3: pickAudio(); break;
                        case 4: pickContact(); break;
                        case 5: sendLocation(); break;
                    }
                }).show();
    }

    private void captureImage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
            return;
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    private void pickDocument() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_DOCUMENT);
    }

    private void pickAudio() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        startActivityForResult(intent, REQUEST_AUDIO);
    }

    private void pickContact() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, REQUEST_CONTACT);
    }

    private void sendLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                String locationUrl = "https://www.google.com/maps/search/?api=1&query=" +
                        location.getLatitude() + "," + location.getLongitude();
                sendMessage(locationUrl);
            } else {
                Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void disableMessageInput() {
        etMessage.setEnabled(false);
        btnSend.setEnabled(false);
        btnAttachment.setVisibility(View.GONE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return item.getItemId() == android.R.id.home && super.onOptionsItemSelected(item);
    }
}


