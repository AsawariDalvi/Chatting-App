package com.mountreach.chattingapp;

import android.Manifest;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.firebase.firestore.FieldValue; // Add this import at the top


import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.mountreach.chattingapp.HomeFragments.QRDialogFragment;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class AddContactActivity extends AppCompatActivity {

    EditText etFirstName, etLastName, etPhone;
    Spinner spinnerCountryCode, spinnerSyncTo;
    Switch switchSync;
    Button btnSave;
    Toolbar toolbar;

    FirebaseAuth auth;
    FirebaseFirestore db;

    private static final int PERMISSION_REQUEST_WRITE_CONTACTS = 101;

    String firstName, lastName, countryCode, phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        // UI Bindings
        toolbar = findViewById(R.id.toolbar);
        etFirstName = findViewById(R.id.et_first_name);
        etLastName = findViewById(R.id.et_last_name);
        etPhone = findViewById(R.id.et_phone);
        spinnerCountryCode = findViewById(R.id.spinner_country_code);
        spinnerSyncTo = findViewById(R.id.spinner_sync_to);
        switchSync = findViewById(R.id.switch_sync);
        btnSave = findViewById(R.id.btn_save);

        // Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Toolbar setup
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("New contact");
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#075E54")));
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.whatsapp_green));

        // Spinner for sync account
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.sync_accounts, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSyncTo.setAdapter(adapter);

        btnSave.setOnClickListener(v -> saveContact());
    }

    private void saveContact() {
        firstName = etFirstName.getText().toString().trim();
        lastName = etLastName.getText().toString().trim();
        phone = etPhone.getText().toString().trim();
        countryCode = spinnerCountryCode.getSelectedItem().toString().split(" ")[1]; // e.g. +91

        if (firstName.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "First name and phone number are required", Toast.LENGTH_SHORT).show();
            return;
        }

        String fullPhone = countryCode + phone;
        String contactId = UUID.randomUUID().toString();
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "unknown_user";

        Map<String, Object> contact = new HashMap<>();
        contact.put("uid", contactId);
        contact.put("userId", userId);
        contact.put("firstName", firstName);
        contact.put("lastName", lastName);
        contact.put("fullName", firstName + " " + lastName);
        contact.put("phoneNumber", phone);
        contact.put("countryCode", countryCode);
        contact.put("fullPhone", fullPhone);
        contact.put("profileImageUrl", ""); // Placeholder, can be updated later
        contact.put("isOnline", false); // Default
        contact.put("timestamp", FieldValue.serverTimestamp());

        // ✅ Save to subcollection: users/{userId}/contacts/{contactId}
        db.collection("users")
                .document(userId)
                .collection("contacts")
                .document(contactId)
                .set(contact)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Contact saved to Firestore", Toast.LENGTH_SHORT).show();

                    // ✅ Also update fullPhone in root user document: users/{userId}
                    Map<String, Object> userUpdate = new HashMap<>();
                    userUpdate.put("fullPhone", fullPhone);
                    userUpdate.put("firstName", firstName);
                    userUpdate.put("lastName", lastName);
                    userUpdate.put("fullName", firstName + " " + lastName);

                    db.collection("users")
                            .document(userId)
                            .set(userUpdate, SetOptions.merge());

                    if (switchSync.isChecked()) {
                        checkPermissionAndSync(fullPhone);
                    } else {
                        finish();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    private void checkPermissionAndSync(String fullPhone) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_CONTACTS},
                    PERMISSION_REQUEST_WRITE_CONTACTS);
        } else {
            saveToPhoneContacts(fullPhone);
        }
    }

    private void saveToPhoneContacts(String fullPhone) {
        try {
            String selectedAccount = spinnerSyncTo.getSelectedItem().toString();

            ContentValues values = new ContentValues();
            if (selectedAccount.equals("Email Account")) {
                values.put(ContactsContract.RawContacts.ACCOUNT_TYPE, "com.google");
                values.put(ContactsContract.RawContacts.ACCOUNT_NAME,
                        auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : "email@example.com");
            } else {
                values.putNull(ContactsContract.RawContacts.ACCOUNT_TYPE);
                values.putNull(ContactsContract.RawContacts.ACCOUNT_NAME);
            }

            Uri rawContactUri = getContentResolver().insert(ContactsContract.RawContacts.CONTENT_URI, values);
            long rawContactId = ContentUris.parseId(rawContactUri);

            // Name
            values.clear();
            values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
            values.put(ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
            values.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, firstName);
            values.put(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, lastName);
            getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);

            // Phone
            values.clear();
            values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
            values.put(ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
            values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, fullPhone);
            values.put(ContactsContract.CommonDataKinds.Phone.TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
            getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);

            Toast.makeText(this, "Contact saved to phone", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Phone save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            saveToPhoneContacts(countryCode + phone);
        } else {
            Toast.makeText(this, "Permission denied to write contacts", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_qr) {
            startActivity(new Intent(this, QR_Scanner_Activity.class));
            String userQrData = "upi://pay?pa=yourupi@bank"; // or custom data
            QRDialogFragment dialog = QRDialogFragment.newInstance(userQrData);
            dialog.show(getSupportFragmentManager(), "ShowQR");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
