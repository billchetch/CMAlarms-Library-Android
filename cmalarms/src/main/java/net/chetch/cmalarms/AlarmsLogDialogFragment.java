package net.chetch.cmalarms;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import net.chetch.appframework.GenericDialogFragment;
import net.chetch.cmalarms.data.Alarm;
import net.chetch.cmalarms.data.AlarmsLog;
import net.chetch.cmalarms.data.AlarmsRepository;
import net.chetch.cmalarms.models.AlarmsWebserviceModel;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class AlarmsLogDialogFragment extends GenericDialogFragment{

    public AlarmsWebserviceModel alarmsWebserviceModel;
    public Alarm alarm;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        inflateContentView(R.layout.alarms_log_dialog);

        Dialog dialog = createDialog();

        RecyclerView alarmsLogRecyclerView = contentView.findViewById(R.id.alarmsLogRecyclerView);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        alarmsLogRecyclerView.setHasFixedSize(true);
        alarmsLogRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        AlarmsLogAdapter adapter = new AlarmsLogAdapter();
        alarmsLogRecyclerView.setAdapter(adapter);

        alarmsWebserviceModel.getLog(alarm).observe(getActivity(), (entries) ->{
            Log.i("ALDF", "Returned " + entries.size() + "entries");

            adapter.setDataset(entries);
        });

        TextView tv = contentView.findViewById(R.id.alarmsLogTitle);
        tv.setText(alarm == null ? "Alarms Log" : alarm.getName() + " Log");

        ImageView iv = contentView.findViewById(R.id.alarmsLogDialogClose);
        iv.setOnClickListener((v)->{
            dismiss();
        });

        setFullScreen(0.85);

        return dialog;
    }
}
