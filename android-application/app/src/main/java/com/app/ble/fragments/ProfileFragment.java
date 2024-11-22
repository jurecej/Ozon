package com.app.ble.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.app.ble.R;
import com.app.ble.activities.MainActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ProfileFragment extends Fragment {
    View view;

    EditText name, eMail;

    TextView nameID;
    Button btn_skintype_I, btn_skintype_II,btn_skintype_III, btn_skintype_IV,btn_skintype_V,btn_skintype_VI, btn_save;

    // SharedPreferences
    private static final String PREF_NAME = "profile_pref";
    private static final String KEY_NAME = "name_key";
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile, container, false);

        btn_skintype_I = view.findViewById(R.id.btn_skintype_I);
        btn_skintype_II = view.findViewById(R.id.btn_skintype_II);
        btn_skintype_III = view.findViewById(R.id.btn_skintype_III);
        btn_skintype_IV = view.findViewById(R.id.btn_skintype_IV);
        btn_skintype_V = view.findViewById(R.id.btn_skintype_V);
        btn_skintype_VI = view.findViewById(R.id.btn_skintype_VI);
        nameID = view.findViewById(R.id.name_id);
        btn_save = view.findViewById(R.id.btn_save);
        name = view.findViewById(R.id.et_name);

        // Load the saved name from SharedPreferences
        SharedPreferences preferences = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String savedName = preferences.getString(KEY_NAME, "");
        nameID.setText(savedName);

        btn_skintype_I.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View v) {
                        ((MainActivity) getActivity()).skinType_I();
                    }
                });

        btn_skintype_II.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View v) {
                        ((MainActivity) getActivity()).skinType_II();
                    }
                });

        btn_skintype_III.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View v) {
                        ((MainActivity) getActivity()).skinType_III();
                    }
                });

        btn_skintype_IV.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View v) {
                        ((MainActivity) getActivity()).skinType_IV();
                    }
                });

        btn_skintype_V.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View v) {
                        ((MainActivity) getActivity()).skinType_V();
                    }
                });

        btn_skintype_VI.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View v) {
                        ((MainActivity) getActivity()).skinType_VI();
                    }
                });

        btn_save.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View v) {
                        if (name != null) {
                            String enteredName = name.getText().toString();

                            // Save the name to SharedPreferences
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString(KEY_NAME, enteredName);
                            editor.apply();

                            // Update the TextView with the entered name
                            nameID.setText(enteredName);

                            Toast.makeText(requireContext(), R.string.name_saved, Toast.LENGTH_SHORT).show();
                            System.out.println(name.getText().toString());
                        } else {
                            Log.e("ProfileFragment", "Name EditText is null");
                        }

                    }
                });

        return view;
    }
}
