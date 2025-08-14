package com.mountreach.chattingapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.mountreach.chattingapp.Adapters.FavouriteSelectAdapter;
import com.mountreach.chattingapp.Model.User;

import java.util.ArrayList;
import java.util.List;

public class AddFavouriteActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FavouriteSelectAdapter adapter;
    private List<User> userList = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentUserId;
    private FirebaseAuth mAuth;
    private FloatingActionButton fabAdd;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_favourite);

        recyclerView = findViewById(R.id.recyclerViewAddFav);
        fabAdd = findViewById(R.id.fabAddToFav);

        Toolbar toolbar = findViewById(R.id.toolbar_add_fav);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Add Favourites");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        adapter = new FavouriteSelectAdapter(this, userList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadUsers();

        fabAdd.setOnClickListener(v -> {
            List<User> selected = adapter.getSelectedUsers();
            if (selected.isEmpty()) {
                Toast.makeText(this, "Select at least one contact", Toast.LENGTH_SHORT).show();
                return;
            }

            for (User u : selected) {
                DocumentReference contactRef = db.collection("users").document(u.getUserId());
                contactRef.get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String phoneNumber = documentSnapshot.getString("phone");
                        u.setPhoneNumber(phoneNumber);

                        db.collection("users")
                                .document(currentUserId)
                                .collection("favourites")
                                .document(u.getUserId())
                                .set(u);
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch contact phone", Toast.LENGTH_SHORT).show();
                });
            }

            Toast.makeText(this, "Added to Favourites", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadUsers() {
        String currentUid = mAuth.getUid();

        db.collection("users").get().addOnCompleteListener(task -> {
            userList.clear();
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    User user = doc.toObject(User.class);
                    user.setUserId(doc.getId());

                    if (user.getUserId() != null && !user.getUserId().equals(currentUid)) {
                        userList.add(user);
                    }
                }
                adapter.notifyDataSetChanged();

            } else {
                Toast.makeText(this, "Failed to load users", Toast.LENGTH_SHORT).show();
            }

        });
    }

}
