package com.mountreach.chattingapp;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mountreach.chattingapp.Adapters.UserAdapter;
import com.mountreach.chattingapp.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SelectContactActivity extends AppCompatActivity {

    private SearchView searchView;
    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<User> allUsers;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_contact);

        Toolbar toolbar = findViewById(R.id.selectContactToolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Select Contact");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }




        getSupportActionBar().setTitle("Select contact");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        searchView = findViewById(R.id.searchViewSelect);
        recyclerView = findViewById(R.id.recyclerViewSelect);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        allUsers = new ArrayList<>();
        adapter = new UserAdapter(this, allUsers, user -> {
            // TODO: Handle contact click
            Toast.makeText(this, "Clicked: " + user.getFullName(), Toast.LENGTH_SHORT).show();
        }, UserAdapter.TYPE_SIMPLE);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadContacts();
        setupSearchLogic();
    }

    private void loadContacts() {
        String uid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";

        db.collection("users").get().addOnSuccessListener(queryDocumentSnapshots -> {
            allUsers.clear();

            for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                User user = snapshot.toObject(User.class);

                if (user != null) {
                    user.setUserId(snapshot.getId()); // ✅ Set the UID here

                    // Skip current user
                    if (!user.getUserId().equals(uid)) {
                        allUsers.add(user);
                    }
                }
            }

            adapter.notifyDataSetChanged();
        });
    }



    private void setupSearchLogic() {
        EditText editText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        editText.setHintTextColor(Color.GRAY);
        editText.setTextColor(Color.BLACK);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                List<User> filtered = new ArrayList<>();
                for (User user : allUsers) {
                    if (user.getUserName().toLowerCase().contains(newText.toLowerCase())) {
                        filtered.add(user);
                    }
                }
                adapter.updateList(filtered);
                return true;
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
