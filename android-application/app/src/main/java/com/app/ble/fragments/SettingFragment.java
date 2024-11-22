package com.app.ble.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.app.ble.R;
import com.app.ble.activities.MainActivity;
import com.app.ble.utils.BLEController;
import com.app.ble.utils.RemoteControl;

public class SettingFragment extends Fragment {
    View view;
    Button btnConnect,btnDisconnect;

    RemoteControl remoteControl;

    BLEController bleController;

    TextView deviceNametext;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_setting, container, false);

        btnConnect = view.findViewById(R.id.btn_connect);
        btnDisconnect = view.findViewById(R.id.btn_disconnect);
        deviceNametext = view.findViewById(R.id.device_name);

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).connect();
            }
        });

        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).disconnect();
            }
        });

        // Set the name in the TextView
        String name = ((MainActivity) requireActivity()).getName();
        if (name != null) {
            requireActivity().runOnUiThread(() -> deviceNametext.setText(name));
        } else {
            //requireActivity().runOnUiThread(() -> deviceNametext.setText("Searching device..."));
            requireActivity().runOnUiThread(() -> deviceNametext.setText(R.string.searching_device));
        }

        return view;
    }
}
