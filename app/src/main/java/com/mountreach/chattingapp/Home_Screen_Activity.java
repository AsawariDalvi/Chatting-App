package com.mountreach.chattingapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.mountreach.chattingapp.HomeFragments.CallFragment;
import com.mountreach.chattingapp.HomeFragments.ChatFragment;
import com.mountreach.chattingapp.HomeFragments.SettingFragment;

public class Home_Screen_Activity extends AppCompatActivity {

    BottomNavigationView bottomNav;
    


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);



        // Load chats fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_container, new ChatFragment())
                .commit();

        bottomNav = findViewById(R.id.bottom_nav);

        // Load default fragment
        loadFragment(new ChatFragment());

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Fragment selectedFragment = null;

            if (itemId == R.id.item_nav_chat) {
                selectedFragment = new ChatFragment();
            } else if (itemId == R.id.item_nav_call) {
                selectedFragment = new CallFragment();
            } else if (itemId == R.id.item_nav_settings) {
                selectedFragment = new SettingFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }

            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_container, fragment)
                .commit();
    }
}
