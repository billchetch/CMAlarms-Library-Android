package net.chetch.cmalarms;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.chetch.cmalarms.data.AlarmsLogEntry;
import net.chetch.utilities.Utils;

import androidx.fragment.app.Fragment;

public class AlarmsLogEntryFragment extends Fragment {

    private View contentView;
    public static final String ENTRY_DATE_FORMAT = "dd/MM/yyyy HH:mm:ss Z";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        contentView = inflater.inflate(R.layout.alarms_log_entry, container, false);

        return contentView;
    }

    public void populateContent(AlarmsLogEntry entry){
        TextView tv = contentView.findViewById(R.id.entryDate);
        tv.setText(Utils.formatDate(entry.getCreated(), ENTRY_DATE_FORMAT));

        tv = contentView.findViewById(R.id.alarmName);
        tv.setText(entry.getAlarmName());

        tv = contentView.findViewById(R.id.alarmState);
        tv.setText(entry.getAlarmState().toString());

        tv = contentView.findViewById(R.id.entryInfo);
        tv.setText(entry.getAlarmMessage());
    }
}
