package com.app.ble.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.app.ble.R;
import com.app.ble.activities.MainActivity;
import com.app.ble.model.Data;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class HomeFragment extends Fragment {
    View view;
    ImageView ivIcon;
    TextView tvBattery, tvX, tvY, tvNumber;
    CircularProgressBar progressX, progressY;
    String TAG = "theS";

    Button btnPlus, btnMinus;

    //int factor = 0;
    int factor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);

        Log.d(TAG, "onCreateView: register " + EventBus.getDefault().isRegistered(this));
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }


        ivIcon = view.findViewById(R.id.iv_icon);
        tvBattery = view.findViewById(R.id.tv_battery);
        progressX = view.findViewById(R.id.progress_x);
        progressY = view.findViewById(R.id.progress_y);
        tvX = view.findViewById(R.id.tv_x);
        tvY = view.findViewById(R.id.tv_y);
        tvNumber = view.findViewById(R.id.tvNumber);
        btnPlus = view.findViewById(R.id.btnPlus);
        btnMinus = view.findViewById(R.id.btnMinus);

        updateNumberTextView();

        btnPlus.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View v) {
                        ((MainActivity) getActivity()).incrementNumber();
                        updateNumberTextView();
                    }
                });

        btnMinus.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View v) {
                        ((MainActivity) getActivity()).decrementNumber();
                        updateNumberTextView();
                    }
                });

        return view;
    }

    /*public void incrementNumber() {
        factor += 10; // You can adjust the increment as needed
        ((MainActivity) getActivity()).sunScreen_Factor(factor);
        updateNumberTextView();
    }

    public void decrementNumber() {
        if (factor > 0) { // Ensure the factor doesn't go below 0.1
            factor -= 10; // You can adjust the decrement as needed
            ((MainActivity) getActivity()).sunScreen_Factor(factor);
            updateNumberTextView();
        }
    }*/

    private void updateNumberTextView() {
        tvNumber.setText(String.valueOf(((MainActivity) getActivity()).getFactor()));

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: register " + EventBus.getDefault().isRegistered(this));
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister event bus when the Fragment goes into the background
        EventBus.getDefault().unregister(this);
    }

    /*@Override
    public void onDestroy() {
        //unregister event bus
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }*/

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEventMainThread(Data data) {
        if (tvBattery != null && tvX != null && tvY != null) {
            Log.d(TAG, "onEventMainThread: ");

            System.out.println("battery"+data.getBattery());

            if (data.getBattery() < 1023 && data.getBattery() >= 700) {
                ivIcon.setImageResource(R.drawable.baseline_battery_full);
            } else if (data.getBattery() < 700 && data.getBattery() >= 681) {
                ivIcon.setImageResource(R.drawable.baseline_battery_6_bar);
            } else if (data.getBattery() < 681 && data.getBattery() >= 647) {
                ivIcon.setImageResource(R.drawable.baseline_battery_5_bar);
            } else if (data.getBattery() < 647 && data.getBattery() >= 613) {
                ivIcon.setImageResource(R.drawable.baseline_battery_4_bar);
            } else if (data.getBattery() < 613 && data.getBattery() >= 579) {
                ivIcon.setImageResource(R.drawable.baseline_battery_3_bar);
            } else if (data.getBattery() < 579 && data.getBattery() >= 545) {
                ivIcon.setImageResource(R.drawable.baseline_battery_2_bar);
            } else if (data.getBattery() < 545 && data.getBattery() >= 511) {
                ivIcon.setImageResource(R.drawable.baseline_battery_1_bar);
            } else if (data.getBattery() < 511){
                ivIcon.setImageResource(R.drawable.baseline_battery_0_bar);
            }
             else if (data.getBattery() > 1900) {
            ivIcon.setImageResource(R.drawable.baseline_battery_charging_full_24);
            }

            if (data.getBattery() < 1023 && data.getBattery() >= 700) {
                tvBattery.setText(100  + " %");
            } else if (data.getBattery() < 511) {
                tvBattery.setText(0 + " %");
            } else if (data.getBattery() > 1900) {
                //tvBattery.setText("Charging");
                tvBattery.setText(R.string.charging);
            } else {
                tvBattery.setText((int) (0.529*data.getBattery()-270.3) + " %");
            }

            if (data.gettransferState() == 0)
            {
                tvX.setText("UVI: " + String.valueOf(data.getVuv()));
                tvY.setText("T: " + String.valueOf((int) data.getTemperature()) + "°C");
                progressX.setProgress((float) data.getVuv());
                progressY.setProgress((int) data.getTemperature());
                System.out.println("transfer STATE is 0");
            }
            else if (data.gettransferState() == 1)
            {
                //tvX.setText("Downloading...");
                //tvY.setText("Downloading...");
                tvX.setText(R.string.downloading);
                tvY.setText(R.string.downloading);
                progressX.setProgress((float) 0);
                progressY.setProgress((int) 0);
                System.out.println("transfer STATE is 1");
            }
            //tvX.setText("UVI: " + String.valueOf(data.getVuv()));
            //tvY.setText("T: " + String.valueOf(data.getTemperature()) + "°C");

            System.out.println(data);

        }
    }
}
