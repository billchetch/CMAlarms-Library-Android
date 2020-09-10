package net.chetch.cmalarms;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import net.chetch.cmalarms.data.Alarm;
import net.chetch.cmalarms.models.AlarmsMessagingModel;

import java.util.HashMap;
import java.util.Map;

public class AlarmFragment extends Fragment {
    static final int MENU_ITEM_DISABLE = 1;
    static final int MENU_ITEM_ENABLE = 2;
    static final int MENU_ITEM_TEST = 3;

    Map<AlarmsMessageSchema.AlarmState, Integer> btnColourMap = new HashMap<>();
    Map<AlarmsMessageSchema.AlarmState, Integer> lblColourMap = new HashMap<>();

    public Alarm alarm;
    public boolean horizontal = true;
    View contentView;
    AlarmsMessageSchema.AlarmState currentAlarmState;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        btnColourMap.put(AlarmsMessageSchema.AlarmState.DISABLED, ContextCompat.getColor(getContext(), R.color.mediumnDarkGrey));
        btnColourMap.put(AlarmsMessageSchema.AlarmState.ON, ContextCompat.getColor(getContext(), R.color.errorRed));
        btnColourMap.put(AlarmsMessageSchema.AlarmState.OFF, ContextCompat.getColor(getContext(), R.color.bluegreen2));

        lblColourMap.put(AlarmsMessageSchema.AlarmState.DISABLED, ContextCompat.getColor(getContext(), R.color.mediumnDarkGrey));
        lblColourMap.put(AlarmsMessageSchema.AlarmState.ON, ContextCompat.getColor(getContext(), R.color.white));
        lblColourMap.put(AlarmsMessageSchema.AlarmState.OFF, ContextCompat.getColor(getContext(), R.color.mediumGrey));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        if(horizontal) {
            contentView = inflater.inflate(R.layout.alarm_horizontal, container, false);
        } else {
            contentView = inflater.inflate(R.layout.alarm_vertical, container, false);
        }

        if(alarm != null){
            TextView tv = contentView.findViewById(R.id.alarmName);
            tv.setText(alarm.getName());

            if(alarm.alarmState != null){
                updateAlarmState(alarm.alarmState);
            }
        }

        Button btn = contentView.findViewById(R.id.alarmButton);
        registerForContextMenu(btn);

        Log.i("AlarmFragment", "Created view " + getTag());
        return contentView;
    }

    public void updateAlarmState(AlarmsMessageSchema.AlarmState alarmState){
        try {
            Button btn = contentView.findViewById(R.id.alarmButton);
            GradientDrawable bg = (GradientDrawable)btn.getBackground();
            int btnColour = btnColourMap.get(alarmState);
            bg.setColor(btnColour);

            TextView tv = contentView.findViewById(R.id.alarmName);
            int lblColour = lblColourMap.get(alarmState);
            tv.setTextColor(lblColour);

            currentAlarmState = alarmState;
            Log.i("AlarmFragment", "Updated state " + getTag());
        } catch (Exception e){
            Log.e("AlarmFragment", e.getMessage());
        }
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        try {
            AlarmsMessagingModel model = ((IAlarmPanelActivity) getActivity()).getAlarmsMessagingModel();
            MenuItem.OnMenuItemClickListener selectItem = (item) -> {
                switch(item.getItemId()){
                    case MENU_ITEM_DISABLE:
                        model.disableAlarm(alarm.getDeviceID());
                        return true;
                    case MENU_ITEM_ENABLE:
                        model.enableAlarm(alarm.getDeviceID());
                        return true;
                    case MENU_ITEM_TEST:
                        model.testAlarm(alarm.getDeviceID());
                        return true;
                }
                return true;
            };


            switch (currentAlarmState) {
                case DISABLED:
                    menu.add(0, MENU_ITEM_ENABLE, 0, "Enable alarm").setOnMenuItemClickListener(selectItem);
                    break;
                case ON:
                    menu.add(0, MENU_ITEM_DISABLE, 0, "Disable alarm").setOnMenuItemClickListener(selectItem);
                    break;
                case OFF:
                    menu.add(0, MENU_ITEM_DISABLE, 0, "Disable alarm").setOnMenuItemClickListener(selectItem);
                    menu.add(0, MENU_ITEM_TEST, 0, "Test alarm").setOnMenuItemClickListener(selectItem);
                    break;
            }
        } catch (Exception e){
            Log.e("AlarmPanel", "onContextItemSelected: " + e.getMessage());
        }
    }
}
