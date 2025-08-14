package com.mountreach.chattingapp.HomeFragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import com.mountreach.chattingapp.Adapters.UserAdapter;
import com.mountreach.chattingapp.AddFavouriteActivity;
import com.mountreach.chattingapp.Model.User;
import com.mountreach.chattingapp.QR_Scanner_Activity;
import com.mountreach.chattingapp.R;
import com.mountreach.chattingapp.SelectContactActivity;

import java.util.ArrayList;
import java.util.List;

public class CallFragment extends Fragment {

    private SearchView searchView;
    private RecyclerView recyclerViewFavourites;
    private RecyclerView recyclerViewAllContacts;
    private UserAdapter userAdapter;
    private List<User> userList;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ListenerRegistration listenerRegistration;
    private List<User> favouriteList = new ArrayList<>();
    private UserAdapter favouriteAdapter;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_call, container, false);

        recyclerViewFavourites = view.findViewById(R.id.recyclerViewFavourites);
        recyclerViewFavourites.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        recyclerViewAllContacts = view.findViewById(R.id.recyclerViewAllContacts);
        recyclerViewAllContacts.setLayoutManager(new LinearLayoutManager(getContext()));
        favouriteAdapter = new UserAdapter(getContext(), favouriteList, user -> {
            callUser(user);
        }, UserAdapter.TYPE_SIMPLE);
        recyclerViewFavourites.setAdapter(favouriteAdapter);

        userList = new ArrayList<>();

        userAdapter = new UserAdapter(getContext(), userList, user -> {
            String phoneNumber = user.getFullPhone(); // assuming this is the full phone number with country code

            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CALL_PHONE}, 1);
            } else {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + phoneNumber));
                startActivity(callIntent);
            }
        }, UserAdapter.TYPE_SIMPLE);

        recyclerViewAllContacts.setAdapter(userAdapter);

        loadContacts();

        FloatingActionButton fab = view.findViewById(R.id.fabCall);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddFavouriteActivity.class);
            startActivityForResult(intent,1001);
        });

        return view;
    }

    private void loadContacts() {
        String currentUid = mAuth.getCurrentUser().getUid();

        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }

        listenerRegistration = db.collection("users")
                .document(currentUid)
                .collection("contacts")
                .orderBy("fullName", Query.Direction.ASCENDING)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Toast.makeText(getContext(), "Failed to load contacts: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    userList.clear();
                    if (querySnapshot != null) {
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            User user = doc.toObject(User.class);
                            if (user != null) {
                                user.setUserId(doc.getId()); // ✅ This is the fix
                                userList.add(user);
                            }
                        }
                        userAdapter.updateList(userList);
                    }
                });
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_call_fragment, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        if (searchItem != null) {
            View actionView = searchItem.getActionView();
            if (actionView instanceof SearchView) {
                searchView = (SearchView) actionView;
                searchView.setQueryHint("Search calls...");

                int searchSrcTextId = searchView.getContext()
                        .getResources()
                        .getIdentifier("android:id/search_src_text", null, null);
                EditText searchEditText = searchView.findViewById(searchSrcTextId);
                if (searchEditText != null) {
                    searchEditText.setTextColor(Color.WHITE);
                    searchEditText.setHintTextColor(Color.LTGRAY);
                }

                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        filterUsers(newText);
                        return true;
                    }
                });
            }
        }

        MenuItem qrItem = menu.findItem(R.id.action_qr);
        if (qrItem != null) {
            qrItem.setOnMenuItemClickListener(item -> {
                String userQrData = "upi://pay?pa=yourupi@bank";
                QRDialogFragment dialog = QRDialogFragment.newInstance(userQrData);
                dialog.show(requireActivity().getSupportFragmentManager(), "ShowQR");
                return true;
            });
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void filterUsers(String query) {
        List<User> filtered = new ArrayList<>();
        for (User user : userList) {
            if (user.getUserName() != null && user.getUserName().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(user);
            }
        }
        userAdapter.updateList(filtered);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_qr) {
            startActivity(new Intent(getActivity(), QR_Scanner_Activity.class));
            return true;
        } else if (id == R.id.menu_clear_calls) {
            showClearCallLogDialog();
            return true;
        } else if (id == R.id.menu_call_settings) {
            Toast.makeText(getContext(), "Settings clicked", Toast.LENGTH_SHORT).show();
            return true;

        }
        return super.onOptionsItemSelected(item);

    }
    private void showClearCallLogDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Clear Call Log")
                .setMessage("Do you want to clear your entire call log?")
                .setPositiveButton("OK", (dialog, which) -> {
                    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                    String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    firestore.collection("call_logs")
                            .document(currentUid)
                            .collection("logs")
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                    firestore.collection("call_logs")
                                            .document(currentUid)
                                            .collection("logs")
                                            .document(doc.getId())
                                            .delete();
                                }
                                Toast.makeText(getContext(), "Call log cleared", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Failed to clear logs: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Calls");
        }

        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (searchView != null && !searchView.isIconified()) {
                    searchView.setIconified(true);
                } else {
                    setEnabled(false);
                    requireActivity().onBackPressed();
                }
            }
        });
    }

    private void callUser(User user) {
        String phoneNumber = user.getFullPhone();
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CALL_PHONE}, 1);
        } else {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(callIntent);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == AppCompatActivity.RESULT_OK && data != null) {
            ArrayList<User> selectedFavourites = (ArrayList<User>) data.getSerializableExtra("selectedFavourites");
            if (selectedFavourites != null) {
                favouriteList.clear();
                favouriteList.addAll(selectedFavourites);
                favouriteAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}
