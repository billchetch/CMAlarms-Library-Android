package net.chetch.cmalarms;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.chetch.cmalarms.data.Alarm;
import net.chetch.cmalarms.models.AlarmsMessageSchema;
import net.chetch.cmalarms.models.AlarmsMessagingModel;
import net.chetch.utilities.Utils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AlarmFragment extends Fragment {
    static final int MENU_ITEM_DISABLE = 1;
    static final int MENU_ITEM_ENABLE = 2;
    static final int MENU_ITEM_TEST = 3;
    static final int MENU_ITEM_VIEW_LOG = 4;
    static final String ALARM_STATE_CHANGE_DATE_FORMAT = "dd/MM/yy HH:mm:ss";

    Map<AlarmsMessageSchema.AlarmState, Integer> indidcatorColourMap = new HashMap<>();
    Map<AlarmsMessageSchema.AlarmState, Integer> lblColourMap = new HashMap<>();

    AlarmsMessagingModel model;

    public IAlarmPanelListener listener;
    public Alarm alarm;
    public boolean horizontal = true;
    View contentView;
    AlarmsMessageSchema.AlarmState currentAlarmState;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        indidcatorColourMap.put(AlarmsMessageSchema.AlarmState.DISABLED, ContextCompat.getColor(getContext(), R.color.ALARM_DISABLED));
        indidcatorColourMap.put(AlarmsMessageSchema.AlarmState.CRITICAL, ContextCompat.getColor(getContext(), R.color.ALARM_CRITICAL));
        indidcatorColourMap.put(AlarmsMessageSchema.AlarmState.SEVERE, ContextCompat.getColor(getContext(), R.color.ALARM_SEVERE));
        indidcatorColourMap.put(AlarmsMessageSchema.AlarmState.MODERATE, ContextCompat.getColor(getContext(), R.color.ALARM_MODERATE));
        indidcatorColourMap.put(AlarmsMessageSchema.AlarmState.MINOR, ContextCompat.getColor(getContext(), R.color.ALARM_MINOR));
        indidcatorColourMap.put(AlarmsMessageSchema.AlarmState.OFF, ContextCompat.getColor(getContext(), R.color.ALARM_OFF));

        lblColourMap.put(AlarmsMessageSchema.AlarmState.DISABLED, ContextCompat.getColor(getContext(), net.chetch.appresources.R.color.mediumnDarkGrey));
        lblColourMap.put(AlarmsMessageSchema.AlarmState.CRITICAL, ContextCompat.getColor(getContext(), net.chetch.appresources.R.color.white));
        lblColourMap.put(AlarmsMessageSchema.AlarmState.SEVERE, ContextCompat.getColor(getContext(), net.chetch.appresources.R.color.white));
        lblColourMap.put(AlarmsMessageSchema.AlarmState.MODERATE, ContextCompat.getColor(getContext(), net.chetch.appresources.R.color.white));
        lblColourMap.put(AlarmsMessageSchema.AlarmState.MINOR, ContextCompat.getColor(getContext(), net.chetch.appresources.R.color.white));
        lblColourMap.put(AlarmsMessageSchema.AlarmState.OFF, ContextCompat.getColor(getContext(), net.chetch.appresources.R.color.mediumGrey));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        if(horizontal) {
            contentView = inflater.inflate(R.layout.alarm_horizontal, container, false);
        } else {
            contentView = inflater.inflate(R.layout.alarm_horizontal_large, container, false);
        }

        if(alarm != null){
            TextView tv = contentView.findViewById(R.id.alarmState);
            tv.setText(alarm.getName());

            if(alarm.hasAlarmState()){
                updateAlarmState();
            }
        }

        registerForContextMenu(contentView);

        Log.i("AlarmFragment", "Created view " + getTag());
        return contentView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        model = ViewModelProviders.of(getActivity()).get(AlarmsMessagingModel.class);
    }

    public void updateAlarmState(){
        try {
            AlarmsMessageSchema.AlarmState alarmState = alarm.getAlarmState();

            ImageView iv = contentView.findViewById(R.id.alarmIndicator);
            GradientDrawable gd = (GradientDrawable)iv.getDrawable();
            int indicatorColour = indidcatorColourMap.get(alarmState);
            gd.setColor(indicatorColour);

            TextView tv = contentView.findViewById(R.id.alarmState);
            int lblColour = lblColourMap.get(alarmState);
            tv.setTextColor(lblColour);

            tv = contentView.findViewById(R.id.alarmLastRaised);
            if(tv != null){
                String msg = "";
                try {
                    if (alarm.isDisabled()) {
                        msg = "Disabled on " + Utils.formatDate(alarm.getLastDisabled(), ALARM_STATE_CHANGE_DATE_FORMAT);
                    } else {
                        Calendar lastRaised= alarm.getLastRaised();
                        if(lastRaised == null){
                            msg = "This alarm has never been raised";
                        } else {
                            msg = "Last raised on " + Utils.formatDate(alarm.getLastRaised(),  ALARM_STATE_CHANGE_DATE_FORMAT);
                            if(alarm.getLastRaisedFor() > 0) msg += " for " + Utils.formatDuration(alarm.getLastRaisedFor() * 1000);
                        }
                    }
                } catch (Exception e){
                    Log.e("AF", e.getMessage());
                }
                tv.setText(msg);
            }

            currentAlarmState = alarmState;
            Log.i("AlarmFragment", "Updated state of " + getTag() + " to " + alarmState);
        } catch (Exception e){
            Log.e("AlarmFragment", e.getMessage());
        }
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        try {
            MenuItem.OnMenuItemClickListener selectItem = (item) -> {
                switch(item.getItemId()){
                    case MENU_ITEM_DISABLE:
                        if(listener != null) {
                            listener.onDisableAlarm(alarm);
                        }
                        return true;
                    case MENU_ITEM_ENABLE:
                        model.enableAlarm(alarm.getAlarmID());
                        return true;
                    case MENU_ITEM_TEST:
                        model.testAlarm(alarm.getAlarmID());
                        return true;
                    case MENU_ITEM_VIEW_LOG:
                        if(listener != null)listener.onViewAlarmsLog(alarm);
                        return true;
                }
                return true;
            };


            switch (currentAlarmState) {
                case DISABLED:
                    menu.add(0, MENU_ITEM_ENABLE, 0, "Enable alarm").setOnMenuItemClickListener(selectItem);
                    break;
                case OFF:
                    menu.add(0, MENU_ITEM_DISABLE, 0, "Disable alarm").setOnMenuItemClickListener(selectItem);
                    menu.add(0, MENU_ITEM_TEST, 0, "Test alarm").setOnMenuItemClickListener(selectItem);
                    break;
                default:
                    menu.add(0, MENU_ITEM_DISABLE, 0, "Disable alarm").setOnMenuItemClickListener(selectItem);
                    break;
            }
            menu.add(0, MENU_ITEM_VIEW_LOG, 0, "View Log").setOnMenuItemClickListener(selectItem);

        } catch (Exception e){
            Log.e("AlarmPanel", "onCreateContextMenu: " + e.getMessage());
        }
    }
}
