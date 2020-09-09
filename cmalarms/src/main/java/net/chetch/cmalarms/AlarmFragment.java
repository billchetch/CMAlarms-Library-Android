package net.chetch.cmalarms;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.chetch.cmalarms.data.Alarm;

public class AlarmFragment extends Fragment {
    public Alarm alarm;
    public boolean horizontal = true;
    public int contentViewID = 0;
    View contentView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        if(horizontal) {
            contentView = inflater.inflate(R.layout.alarm_horizontal, container, false);
        } else {
            contentView = inflater.inflate(R.layout.alarm_vertical, container, false);
        }
        TextView tv = contentView.findViewById(R.id.alarmName);

        if(alarm != null){
            tv.setText(alarm.getName());

            tv = contentView.findViewById(R.id.alarmState);
            tv.setText("Not set");

            if(alarm.alarmState != null){
                updateAlarmState(alarm.alarmState);
            }
        }

        Log.i("AlarmFragment", "Created view " + getTag());
        return contentView;
    }

    public void updateAlarmState(AlarmsMessageSchema.AlarmState alarmState){
        try {
            TextView tv = contentView.findViewById(R.id.alarmState);
            tv.setText(alarmState.name());
            Log.i("AlarmFragment", "Updated state " + getTag());
        } catch (Exception e){
            Log.e("AlarmFragment", e.getMessage());
        }
    }
}
