package com.mountreach.chattingapp.Model;

public class SettingsItem {
    private String title;
    private String description;
    private int iconResId;

    public SettingsItem(String title, String description, int iconResId) {
        this.title = title;
        this.description = description;
        this.iconResId = iconResId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getIconResId() {
        return iconResId;
    }
}

