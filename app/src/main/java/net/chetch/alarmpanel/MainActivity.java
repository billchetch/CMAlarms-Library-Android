package net.chetch.alarmpanel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import net.chetch.cmalarms.AlarmPanelFragment;
import net.chetch.cmalarms.AlarmsMessageSchema;
import net.chetch.cmalarms.IAlarmPanelActivity;
import net.chetch.cmalarms.models.AlarmsMessagingModel;
import net.chetch.webservices.WebserviceViewModel;
import net.chetch.webservices.network.NetworkRepository;

public class MainActivity extends AppCompatActivity implements IAlarmPanelActivity {

    static boolean loaded = false;

    AlarmsMessagingModel model;
    AlarmPanelFragment alarmPanelFragment;
    MutableLiveData<String> ld = new MutableLiveData<>();

    Observer dataLoadProgress  = obj -> {
        WebserviceViewModel.LoadProgress progress = (WebserviceViewModel.LoadProgress) obj;
        try {
            String state = progress.startedLoading ? "Loading" : "Loaded";
            String progressInfo = state + (progress.info == null ? "" : " " + progress.info.toLowerCase());
            Log.i("Main", "in load data progress");
        } catch (Exception e){
            Log.e("Main", "load prigress: " + e.getMessage());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            //String apiBaseURL = "http://192.168.43.123:8001/api/";
            String apiBaseURL = "http://192.168.1.100:8001/api/";
            NetworkRepository.getInstance().setAPIBaseURL(apiBaseURL);
        } catch (Exception e) {
            Log.e("MVM", e.getMessage());
            return;
        }

        model = ViewModelProviders.of(this).get(AlarmsMessagingModel.class);
        alarmPanelFragment = (AlarmPanelFragment)getSupportFragmentManager().findFragmentById(R.id.alarmPanel);

        model.getAlarms().observe(this, alarms->{
            Log.i("Main", "Alarms list " + alarms.size() + " alarms arrived!");
            alarmPanelFragment.populateAlarms(alarms);
        });

        model.getAlarmStates().observe( this, alarmStates->{
            Log.i("Main", "Alarm states " + alarmStates.size() + " states arrived!");
            alarmPanelFragment.updateAlarmStates(alarmStates);
        });

        model.getAlertedAlarm().observe(this, alarm->{
            Log.i("Main", "Alarm alert " + alarm.getDeviceID() + " state " + alarm.alarmState);
            alarmPanelFragment.updateAlarmState(alarm.getDeviceID(), alarm.alarmState);
        });

        model.getPilotOn().observe(this, on->{
            Log.i("Main", "Pilot light on " + on);
            alarmPanelFragment.updatePilotOn(on);
        });

        model.getBuzzerSilenced().observe(this, silenced->{
            Log.i("Main", "Buzzer silenced " + silenced);
            alarmPanelFragment.updateBuzzerSilenced(silenced);
        });

        //now load up
        Log.i("Main", "Calling load data");
        model.loadData(dataLoadProgress);

    }

    @Override
    public AlarmsMessagingModel getAlarmsMessagingModel() {
        return model;
    }
}
