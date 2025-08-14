package com.mountreach.chattingapp.HomeFragments;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.mountreach.chattingapp.Model.Message;
import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import com.mountreach.chattingapp.Adapters.UserAdapter;
import com.mountreach.chattingapp.AddContactActivity;
import com.mountreach.chattingapp.Model.Message;
import com.mountreach.chattingapp.Model.User;
import com.mountreach.chattingapp.OneToOneChatActivity;
import com.mountreach.chattingapp.QR_Scanner_Activity;
import com.mountreach.chattingapp.R;

import java.util.*;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<User> userList = new ArrayList<>();
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SearchView searchView;
    private FloatingActionButton fabAddContact;
    private String currentUserId;

    public ChatFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("WhatsApp");
            ((AppCompatActivity) getActivity()).getSupportActionBar()
                    .setBackgroundDrawable(new ColorDrawable(Color.parseColor("#075E54")));
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        recyclerView = view.findViewById(R.id.recyclerViewAllContacts);
        searchView = view.findViewById(R.id.searchView);
        fabAddContact = view.findViewById(R.id.fabAddContact);

        requireActivity().getWindow().setStatusBarColor(ContextCompat.getColor(requireContext(), R.color.whatsapp_green));


        SearchView searchView = view.findViewById(R.id.searchView);
        searchView.setQueryHint("Search ");

// Change hint text color
        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchEditText.setHintTextColor(Color.parseColor("#FFFFFF"));
        searchEditText.setTextColor(Color.WHITE);


        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new UserAdapter(getContext(), userList, user -> {
            Intent intent = new Intent(getActivity(), OneToOneChatActivity.class);
            intent.putExtra("userUid", user.getUid());
            intent.putExtra("fullName", user.getFullName());
            intent.putExtra("profileImageUrl", user.getProfileImageUrl());
            intent.putExtra("fullPhone", user.getFullPhone());
            startActivity(intent);
        }, UserAdapter.TYPE_CHAT);
        recyclerView.setAdapter(adapter);

        customizeSearchView();

        fabAddContact.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), AddContactActivity.class)));

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        loadContactsRealtime();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterUsers(newText);
                return true;
            }
        });

        searchView.setIconified(false);
        searchView.clearFocus();
    }

    private void loadContactsRealtime() {
        db.collection("users")
                .document(currentUserId)
                .collection("contacts")
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) {
                        Toast.makeText(getContext(), "Failed to load contacts", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    userList.clear();

                    for (DocumentSnapshot doc : value.getDocuments()) {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            user.setUserId(doc.getId());  // userId = contact document ID
                            fetchLastMessage(user);       // fetch chat preview separately
                        }
                    }
                });
    }


    private void fetchLastMessage(User user) {
        String senderRoom = currentUserId + user.getUid();

        CollectionReference messagesRef = db
                .collection("chats")
                .document(senderRoom)
                .collection("messages");

        messagesRef
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot lastMsgDoc = queryDocumentSnapshots.getDocuments().get(0);
                        Message lastMessage = lastMsgDoc.toObject(Message.class);

                        if (lastMessage != null) {
                            user.setLastMessage(lastMessage.getMessage());

                            long millis = 0;
                            if (lastMessage.getTimestamp() != null) {
                                millis = lastMessage.getTimestamp().toDate().getTime();
                            }

                            user.setLastMessageTime(millis);
                        }
                    } else {
                        user.setLastMessage("");
                        user.setLastMessageTime(0);
                    }

                    userList.add(user);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    user.setLastMessage("");
                    user.setLastMessageTime(0);
                    userList.add(user);
                    adapter.notifyDataSetChanged();
                });
    }



    private void filterUsers(String query) {
        List<User> filtered = new ArrayList<>();
        for (User user : userList) {
            if (!TextUtils.isEmpty(user.getUserName()) &&
                    user.getUserName().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(user);
            }
        }
        adapter.updateList(filtered);
    }

    private void customizeSearchView() {
        searchView.setBackgroundResource(R.drawable.bg_dark_searchview);
        EditText searchEditText = searchView.findViewById(
                searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null));
        if (searchEditText != null) {
            searchEditText.setTextColor(Color.WHITE);
            searchEditText.setHintTextColor(Color.parseColor("#B0BEC5"));
            searchEditText.setTextSize(16f);

            searchEditText.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
                }
            });
        }

        View plate = searchView.findViewById(
                searchView.getContext().getResources().getIdentifier("android:id/search_plate", null, null));
        if (plate != null) {
            plate.setBackground(null);
        }

        ImageView icon = searchView.findViewById(
                searchView.getContext().getResources().getIdentifier("android:id/search_mag_icon", null, null));
        if (icon != null) {
            icon.setColorFilter(Color.LTGRAY);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.top_menu, menu);
        inflater.inflate(R.menu.toolbar_menu, menu);

        MenuItem qrItem = menu.findItem(R.id.action_qr);
        if (qrItem != null) {
            qrItem.setOnMenuItemClickListener(item -> {
                String userQrData = "upi://pay?pa=yourupi@bank";
                QRDialogFragment dialog = QRDialogFragment.newInstance(userQrData);
                dialog.show(requireActivity().getSupportFragmentManager(), "ShowQR");
                return true;
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_qr_code) {
            startActivity(new Intent(getContext(), QR_Scanner_Activity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
