package com.mountreach.chattingapp.HomeFragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mountreach.chattingapp.Login_Activity;
import com.mountreach.chattingapp.Model.SettingsItem;
import com.mountreach.chattingapp.R;
import com.mountreach.chattingapp.Adapter.SettingsAdapter;
import com.mountreach.chattingapp.Registeration_Activity;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingFragment extends Fragment {

    private TextView tvName, tvPhone;
    private CircleImageView profileImage;
    private ImageView qrIcon, editIcon;
//    Button btnDeleteAccount;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        setHasOptionsMenu(true);

        Toolbar toolbar = view.findViewById(R.id.toolbar_calls);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.LogOut) {
                showLogoutDialog();
                return true;
            } else if (item.getItemId() == R.id.DeleteAccount) {
                showDeleteAccountDialog();
                return true;
            }
            return false;
        });


        // Initialize views
        tvName = view.findViewById(R.id.tvName);
        tvPhone = view.findViewById(R.id.tvPhone);
        profileImage = view.findViewById(R.id.profileImage);
        qrIcon = view.findViewById(R.id.qrIcon);
        editIcon = view.findViewById(R.id.editIcon);

        loadUserData();

        // Set user data from SharedPreferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String name = prefs.getString("username", "User");
        String phone = prefs.getString("userPhone", "+91-0000000000");
        String imagePath = prefs.getString("profileImage", "");

        tvName.setText(name);
        tvPhone.setText(phone);

        if (!imagePath.isEmpty()) {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                profileImage.setImageURI(Uri.fromFile(imageFile));
            } else {
                profileImage.setImageResource(R.drawable.icon_account);
            }
        } else {
            profileImage.setImageResource(R.drawable.icon_account);
        }

        // Handle QR icon click (layout)
        qrIcon.setOnClickListener(v -> {
            String userQrData = "upi://pay?pa=" + phone;  // OR customize this string
            QRDialogFragment dialog = QRDialogFragment.newInstance(userQrData);
            dialog.show(requireActivity().getSupportFragmentManager(), "ShowQR");
        });

        // Handle Edit icon click
        editIcon.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frame_container, new EditProfileFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Settings list setup
        RecyclerView recyclerView = view.findViewById(R.id.settingsRecyclerView);
        List<SettingsItem> settingsItems = new ArrayList<>();
        settingsItems.add(new SettingsItem("Account", "Security notifications, change number", R.drawable.icon_account));
        settingsItems.add(new SettingsItem("Privacy", "Blocked contacts, disappearing messages", R.drawable.icon_privacy));
        settingsItems.add(new SettingsItem("Lists", "Manage People and groups", R.drawable.icon_list));
        settingsItems.add(new SettingsItem("Chats", "Theme, Wallpaper, chat history", R.drawable.icon_chat_24));
        settingsItems.add(new SettingsItem("Notifications", "Message, groups & call tones", R.drawable.icon_notifications));
        settingsItems.add(new SettingsItem("Storage and data", "Network usage, auto-download", R.drawable.icon_storage));
        settingsItems.add(new SettingsItem("Accessibility", "Animation", R.drawable.icon_accessibility));
        settingsItems.add(new SettingsItem("App language", "English(device's language)", R.drawable.icon_language));
        settingsItems.add(new SettingsItem("Help centre, contact us, privacy policy", "Help", R.drawable.icon_help));
        settingsItems.add(new SettingsItem("Invite a friend", "", R.drawable.icon_friend));


        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new SettingsAdapter(settingsItems));

        return view;
    }

    private void loadUserData() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String name = document.getString("name");
                        String phone = document.getString("mobile"); // ✅ actual mobile number

                        if (name != null) {
                            tvName.setText(name);
                        }

                        if (phone != null) {
                            tvPhone.setText("+91-" + phone); // Format as needed
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load user data", Toast.LENGTH_SHORT).show();
                });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Log Out", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(getActivity(), Login_Activity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account?")
                .setPositiveButton("Delete Account", (dialog, which) -> {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        user.delete().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(getContext(), "Account Deleted", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getActivity(), Login_Activity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                Toast.makeText(getContext(), "Error deleting account", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.toolbar_menu, menu);

        MenuItem qrItem = menu.findItem(R.id.action_qr);
        if (qrItem != null) {
            qrItem.setOnMenuItemClickListener(item -> {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                String phone = prefs.getString("userPhone", "+91-0000000000");
                String userQrData = "upi://pay?pa=" + phone;
                QRDialogFragment dialog = QRDialogFragment.newInstance(userQrData);
                dialog.show(requireActivity().getSupportFragmentManager(), "ShowQR");
                return true;
            });
        }


    }
}


