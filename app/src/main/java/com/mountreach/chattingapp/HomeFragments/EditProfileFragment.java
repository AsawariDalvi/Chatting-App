package com.mountreach.chattingapp.HomeFragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.mountreach.chattingapp.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class EditProfileFragment extends Fragment {

    private EditText etName, etPhone;
    private ImageView imgProfile;
    private Button btnSave;
    private Uri selectedImageUri;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        // UI references
        etName = view.findViewById(R.id.etEditProfileName);
        etPhone = view.findViewById(R.id.etEditProfilePhone);
        imgProfile = view.findViewById(R.id.imgEditProfileProfile);
        btnSave = view.findViewById(R.id.EditProfileBtnSave);

        // Load saved data
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        etName.setText(prefs.getString("username", ""));
        etPhone.setText(prefs.getString("userPhone", ""));

        String savedImagePath = prefs.getString("profileImage", "");
        if (!savedImagePath.isEmpty()) {
            File imageFile = new File(savedImagePath);
            if (imageFile.exists()) {
                imgProfile.setImageURI(Uri.fromFile(imageFile));
            }
        }

        // Set up launcher for image picker
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        imgProfile.setImageURI(selectedImageUri);
                    }
                });

        // Pick image from gallery
        imgProfile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        // Save profile
        btnSave.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("username", etName.getText().toString());
            editor.putString("userPhone", etPhone.getText().toString());

            if (selectedImageUri != null) {
                String copiedPath = copyUriToInternalStorage(selectedImageUri);
                if (!copiedPath.isEmpty()) {
                    editor.putString("profileImage", copiedPath);
                }
            }

            editor.apply();
            Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack();
        });

        return view;
    }

    private String copyUriToInternalStorage(Uri uri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            File file = new File(requireContext().getFilesDir(), "profile.jpg");
            OutputStream outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }

            inputStream.close();
            outputStream.close();

            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
