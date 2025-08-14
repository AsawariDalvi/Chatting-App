package com.mountreach.chattingapp.HomeFragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.mountreach.chattingapp.QR_Scanner_Activity;
import com.mountreach.chattingapp.R;

public class QRDialogFragment extends DialogFragment {

    private String qrData;

    public static QRDialogFragment newInstance(String data) {
        QRDialogFragment fragment = new QRDialogFragment();
        Bundle args = new Bundle();
        args.putString("qrData", data);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_q_r_dialog, container, false);

        qrData = getArguments().getString("qrData");

        ImageView qrImage = view.findViewById(R.id.qr_image);
        TextView qrText = view.findViewById(R.id.qr_text);
        Button btnScan = view.findViewById(R.id.btn_scan_qr);

        qrText.setText(qrData);

        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(
                    qrData,
                    BarcodeFormat.QR_CODE,
                    500, 500
            );
            qrImage.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }

        btnScan.setOnClickListener(v -> {
            dismiss(); // close dialog
            startActivity(new Intent(getActivity(), QR_Scanner_Activity.class)); // open scanner
        });

        return view;
    }
}
