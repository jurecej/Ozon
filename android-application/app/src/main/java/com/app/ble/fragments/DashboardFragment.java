package com.app.ble.fragments;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.app.ble.R;
import com.app.ble.activities.MainActivity;
import com.app.ble.database.DBHelper;
import com.app.ble.model.Data;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardFragment extends Fragment {
    View view;
    ImageView ivLeft, ivRight;
    TextView tvDays;
    BarChart chartX, chartY;
    DBHelper dbHelper;
    Calendar currentDate = Calendar.getInstance();
    int currentWeek = currentDate.get(Calendar.WEEK_OF_YEAR);
    int mondayX = 0, tuesdayX = 0, wednesdayX = 0, thursdayX = 0, fridayX = 0, saturdayX = 0, sundayX = 0;
    int mondayY = 0, tuesdayY = 0, wednesdayY = 0, thursdayY = 0, fridayY = 0, saturdayY = 0, sundayY = 0;
    String TAG = "theS";

    double timeInterval = 1.0; //Time in seconds representing interval to calculate DUVD

    DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(Locale.US);
    DecimalFormat decimalFormat = new DecimalFormat("0.0", decimalFormatSymbols);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        dbHelper = new DBHelper(requireContext());

        ivLeft = view.findViewById(R.id.iv_left);
        ivRight = view.findViewById(R.id.iv_right);
        tvDays = view.findViewById(R.id.tv_days);
        chartX = view.findViewById(R.id.chart_x);
        chartY = view.findViewById(R.id.chart_y);

        setBarChart(currentWeek);

        ivLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentWeek > 0) {
                    currentWeek = currentWeek - 1;
                    setBarChart(currentWeek);
                }
            }
        });

        ivRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentWeek = currentWeek + 1;
                setBarChart(currentWeek);
            }
        });
        return view;
    }


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
        if (tvDays != null) {
            setBarChart(currentWeek);
        }
    }


    @SuppressLint("MissingPermission")
    public void setBarChart(int currentWeek) {

        resetGraph();
        Log.d(TAG, "setBarChart: " + currentWeek);

        tvDays.setText(getStartEndDateOfWeekFromCalendar(currentWeek));

        List<Data> dataList = dbHelper.readData();


        Log.d(TAG, "setBarChart: " + dataList.size());
        for (int i = 0; i < dataList.size(); i++) {

            Data data = dataList.get(i);
            System.out.println("data view: " + data);
            Log.d(TAG, "setBarChart: " + data);

            /*if(data.getVuv()*0.05 > 400){
                ((MainActivity)getActivity()).sendPushNotification();
            }*/

            String date = dataList.get(i).getDate();
            String[] dateArray = date.split("-");
            Calendar targetCal = Calendar.getInstance();
            targetCal.clear();
            //targetCal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateArray[0]));
            //targetCal.set(Calendar.MONTH, Integer.parseInt(dateArray[1]) - 1);
            //targetCal.set(Calendar.YEAR, Integer.parseInt(dateArray[2]));
            targetCal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateArray[2]));
            targetCal.set(Calendar.MONTH, Integer.parseInt(dateArray[1]) - 1);
            targetCal.set(Calendar.YEAR, Integer.parseInt(dateArray[0]));
            int targetWeek = targetCal.get(Calendar.WEEK_OF_YEAR);

            if (currentWeek == targetWeek) {

                Log.d(TAG, "currentweek: " + currentWeek);
                int day = targetCal.get(Calendar.DAY_OF_WEEK);
                switch (day) {
                    case Calendar.MONDAY:
                        mondayX += Double.parseDouble(decimalFormat.format(100*data.getVuv()*timeInterval/((MainActivity) getActivity()).getAdjusteddailyUVdose()));
                        mondayY = (int) data.getTemperature();
                        //mondayY += data.getTemperature();
                        break;
                    case Calendar.TUESDAY:
                        tuesdayX += Double.parseDouble(decimalFormat.format(100*data.getVuv()*timeInterval/((MainActivity) getActivity()).getAdjusteddailyUVdose()));
                        tuesdayY = (int) data.getTemperature();
                        //tuesdayY += data.getTemperature();
                        break;
                    case Calendar.WEDNESDAY:
                        wednesdayX += Double.parseDouble(decimalFormat.format(100*data.getVuv()*timeInterval/((MainActivity) getActivity()).getAdjusteddailyUVdose()));
                        wednesdayY = (int) data.getTemperature();
                        //wednesdayY += data.getTemperature();
                        break;
                    case Calendar.THURSDAY:
                        thursdayX += Double.parseDouble(decimalFormat.format(100*data.getVuv()*timeInterval/((MainActivity) getActivity()).getAdjusteddailyUVdose()));
                        thursdayY = (int) data.getTemperature();
                        //thursdayY += data.getTemperature();
                        break;
                    case Calendar.FRIDAY:
                        fridayX += Double.parseDouble(decimalFormat.format(100*data.getVuv()*timeInterval/((MainActivity) getActivity()).getAdjusteddailyUVdose()));
                        fridayY = (int) data.getTemperature();
                        //fridayY += data.getTemperature();
                        break;
                    case Calendar.SATURDAY:
                        saturdayX += Double.parseDouble(decimalFormat.format(100*data.getVuv()*timeInterval/((MainActivity) getActivity()).getAdjusteddailyUVdose()));
                        saturdayY = (int) data.getTemperature();
                        //saturdayY += data.getTemperature();
                        break;
                    case Calendar.SUNDAY:
                        sundayX += Double.parseDouble(decimalFormat.format(100*data.getVuv()*timeInterval/((MainActivity) getActivity()).getAdjusteddailyUVdose()));
                        sundayY = (int) data.getTemperature();
                        //sundayY += data.getTemperature();
                        break;
                }
            }

            /*if (currentWeek == targetWeek) {
                Log.d(TAG, "currentweek: " + currentWeek);
                int day = targetCal.get(Calendar.DAY_OF_WEEK);
                switch (day) {
                    case Calendar.MONDAY:
                        mondayX = (int) data.getVuv();
                        mondayY = (int) data.getTemperature();
                        break;
                    case Calendar.TUESDAY:
                        tuesdayX = (int) data.getVuv();
                        tuesdayY = (int) data.getTemperature();
                        break;
                    case Calendar.WEDNESDAY:
                        wednesdayX = (int) data.getVuv();
                        wednesdayY = (int) data.getTemperature();
                        break;
                    case Calendar.THURSDAY:
                        thursdayX = (int) data.getVuv();
                        thursdayY = (int) data.getTemperature();
                        break;
                    case Calendar.FRIDAY:
                        fridayX = (int) data.getVuv();
                        fridayY = (int) data.getTemperature();
                        break;
                    case Calendar.SATURDAY:
                        saturdayX = (int) data.getVuv();
                        saturdayY = (int) data.getTemperature();
                        break;
                    case Calendar.SUNDAY:
                        sundayX = (int) data.getVuv();
                        sundayY = (int) data.getTemperature();
                        break;
                }
            }*/
        }

        drawChartX();
        drawChartY();
    }

    public void drawChartX() {

        ArrayList<String> xAxisLabel = new ArrayList<>();
        Resources resources = getResources();
        xAxisLabel.add(resources.getString(R.string.sun));
        xAxisLabel.add(resources.getString(R.string.mon));
        xAxisLabel.add(resources.getString(R.string.tue));
        xAxisLabel.add(resources.getString(R.string.wed));
        xAxisLabel.add(resources.getString(R.string.thu));
        xAxisLabel.add(resources.getString(R.string.fri));
        xAxisLabel.add(resources.getString(R.string.sat));
        XAxis xAxis = chartX.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);
        ValueFormatter formatter = new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int i = (int) Math.abs(value);
                if (value == -1.0) {
                    return "";
                } else if (i == 7) {
                    return "";
                }
                return xAxisLabel.get(i);
            }
        };

        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setValueFormatter(formatter);
        xAxis.setCenterAxisLabels(false);
        xAxis.setGranularityEnabled(true);

        List<BarEntry> yVals1 = new ArrayList<BarEntry>();
        yVals1.add(new BarEntry(0, sundayX));
        yVals1.add(new BarEntry(1, mondayX));
        yVals1.add(new BarEntry(2, tuesdayX));
        yVals1.add(new BarEntry(3, wednesdayX));
        yVals1.add(new BarEntry(4, thursdayX));
        yVals1.add(new BarEntry(5, fridayX));
        yVals1.add(new BarEntry(6, saturdayX));
        YAxis leftAxis = chartX.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setSpaceTop(30f);

        chartX.setDrawBarShadow(false);
        chartX.setDrawValueAboveBar(true);
        chartX.getDescription().setEnabled(false);
        chartX.setPinchZoom(false);
        chartX.setDrawGridBackground(false);
        chartX.getXAxis().setDrawGridLines(false);
        chartX.getAxisLeft().setDrawGridLines(false);
        chartX.getAxisRight().setDrawGridLines(false);
        chartX.getAxisLeft().setDrawAxisLine(false);
        chartX.getXAxis().setDrawAxisLine(false);
        chartX.getAxisRight().setEnabled(false);

        BarDataSet set1;

        if (chartX.getData() != null) {
            set1 = (BarDataSet) chartX.getData().getDataSetByIndex(0);
            set1.setValues(yVals1);
            chartX.getData().notifyDataChanged();
            chartX.notifyDataSetChanged();
        } else {
            set1 = new BarDataSet(yVals1, resources.getString(R.string.UV_daily_max));
            set1.setColor(ContextCompat.getColor(requireActivity(), R.color.bar1));
            ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
            dataSets.add(set1);
            BarData data = new BarData(dataSets);
            chartX.setData(data);
        }

        chartX.getBarData().setBarWidth(0.46f);
        chartX.invalidate();
    }

    public void drawChartY() {

        ArrayList<String> xAxisLabel = new ArrayList<>();
        Resources resources = getResources();
        xAxisLabel.add(resources.getString(R.string.sun));
        xAxisLabel.add(resources.getString(R.string.mon));
        xAxisLabel.add(resources.getString(R.string.tue));
        xAxisLabel.add(resources.getString(R.string.wed));
        xAxisLabel.add(resources.getString(R.string.thu));
        xAxisLabel.add(resources.getString(R.string.fri));
        xAxisLabel.add(resources.getString(R.string.sat));
        XAxis xAxis = chartY.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);
        ValueFormatter formatter = new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int i = (int) Math.abs(value);
                if (value == -1.0) {
                    return "";
                } else if (i == 7) {
                    return "";
                }
                return xAxisLabel.get(i);
            }
        };

        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setValueFormatter(formatter);
        xAxis.setCenterAxisLabels(false);
        xAxis.setGranularityEnabled(true);

        List<BarEntry> yVals1 = new ArrayList<BarEntry>();
        yVals1.add(new BarEntry(0, sundayY));
        yVals1.add(new BarEntry(1, mondayY));
        yVals1.add(new BarEntry(2, tuesdayY));
        yVals1.add(new BarEntry(3, wednesdayY));
        yVals1.add(new BarEntry(4, thursdayY));
        yVals1.add(new BarEntry(5, fridayY));
        yVals1.add(new BarEntry(6, saturdayY));
        YAxis leftAxis = chartY.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setSpaceTop(30f);

        chartY.setDrawBarShadow(false);
        chartY.setDrawValueAboveBar(true);
        chartY.getDescription().setEnabled(false);
        chartY.setPinchZoom(false);
        chartY.setDrawGridBackground(false);
        chartY.getXAxis().setDrawGridLines(false);
        chartY.getAxisLeft().setDrawGridLines(false);
        chartY.getAxisRight().setDrawGridLines(false);
        chartY.getAxisLeft().setDrawAxisLine(false);
        chartY.getXAxis().setDrawAxisLine(false);
        chartY.getAxisRight().setEnabled(false);

        BarDataSet set1;

        if (chartY.getData() != null) {
            set1 = (BarDataSet) chartY.getData().getDataSetByIndex(0);
            set1.setValues(yVals1);
            chartY.getData().notifyDataChanged();
            chartY.notifyDataSetChanged();
        } else {
            set1 = new BarDataSet(yVals1, resources.getString(R.string.daily_temperature));
            //set1 = new BarDataSet(yVals1, "Daily max temperature °C");
            set1.setColor(ContextCompat.getColor(requireActivity(), R.color.bar2));
            ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
            dataSets.add(set1);
            BarData data = new BarData(dataSets);
            chartY.setData(data);
        }

        chartY.getBarData().setBarWidth(0.46f);
        chartY.invalidate();
    }


    public String getStartEndDateOfWeekFromCalendar(int weekNumber) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.WEEK_OF_YEAR, weekNumber);
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM", Locale.ENGLISH);

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        Date startDate = calendar.getTime();
        String startDateInStr = formatter.format(startDate);

        calendar.add(Calendar.DAY_OF_WEEK, 6);
        Date endDate = calendar.getTime();
        String endDaString = formatter.format(endDate);

        return startDateInStr + " - " + endDaString;
    }

    public void resetGraph() {
        mondayX = 0;
        tuesdayX = 0;
        wednesdayX = 0;
        thursdayX = 0;
        fridayX = 0;
        saturdayX = 0;
        sundayX = 0;
        mondayY = 0;
        tuesdayY = 0;
        wednesdayY = 0;
        thursdayY = 0;
        fridayY = 0;
        saturdayY = 0;
        sundayY = 0;
    }
}
