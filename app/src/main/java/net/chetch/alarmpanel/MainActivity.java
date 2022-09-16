package net.chetch.alarmpanel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import net.chetch.appframework.GenericActivity;
import net.chetch.cmalarms.AlarmPanelFragment;
import net.chetch.appframework.GenericActivity;
import net.chetch.cmalarms.IAlarmPanelListener;
import net.chetch.cmalarms.data.Alarm;
import net.chetch.cmalarms.models.AlarmsMessageSchema;
import net.chetch.cmalarms.models.AlarmsMessagingModel;
import net.chetch.cmalarms.models.AlarmsWebserviceModel;
import net.chetch.messaging.ClientConnection;
import net.chetch.webservices.ConnectManager;
import net.chetch.webservices.WebserviceViewModel;
import net.chetch.webservices.network.NetworkRepository;

public class MainActivity extends GenericActivity implements IAlarmPanelListener {

    static boolean loaded = false;

    AlarmsMessagingModel model;
    AlarmsWebserviceModel wsModel;

    AlarmPanelFragment alarmPanelFragment;

    Observer connectProgress  = obj -> {
        if(obj instanceof WebserviceViewModel.LoadProgress) {
            WebserviceViewModel.LoadProgress progress = (WebserviceViewModel.LoadProgress) obj;
            try {
                String state = progress.startedLoading ? "Loading" : "Loaded";
                String progressInfo = state + (progress.info == null ? "" : " " + progress.info.toLowerCase());
                if(progress.dataLoaded != null){
                    progressInfo += " - " + progress.dataLoaded.getClass().toString();
                }
                Log.i("Main", "in load data progress ..." + progressInfo);

            } catch (Exception e) {
                Log.e("Main", "load progress: " + e.getMessage());
            }
        } else if(obj instanceof ClientConnection){

        } else if(obj instanceof ConnectManager) {
            ConnectManager cm = (ConnectManager) obj;
            switch(cm.getState()){
                case CONNECT_REQUEST:
                    if(cm.fromError()){
                        setProgressInfo("There was an error ... retrying...");
                    } else {
                        setProgressInfo("Connecting...");
                    }
                    break;

                case RECONNECT_REQUEST:
                    setProgressInfo("Disconnected!... Attempting to reconnect...");
                    break;

                case CONNECTED:
                    hideProgress();
                    Log.i("Main", "All connections made");
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //includeActionBar(SettingsActivity.class);

        //Get models
        Log.i("Main", "Calling load data");
        model = ViewModelProviders.of(this).get(AlarmsMessagingModel.class);
        model.getError().observe(this, throwable -> {
            handleError(throwable, model);
        });

        wsModel = new ViewModelProvider(this).get(AlarmsWebserviceModel.class);
        wsModel.getError().observe(this, throwable ->{
            handleError(throwable, wsModel);
        });


        //Components
        alarmPanelFragment = (AlarmPanelFragment)getSupportFragmentManager().findFragmentById(R.id.alarmPanelFragment);
        alarmPanelFragment.listener = this;

        ConnectManager connectManager = new ConnectManager();
        try {
            model.setClientName("ACMCAPAlarms");

            connectManager.addModel(model);
            connectManager.addModel(wsModel);

            connectManager.requestConnect(connectProgress);
        } catch (Exception e){
            showError(e);
        }

    }

    private void handleError(Throwable t, Object source){
        showError(t);
        Log.e("MAIN", t.getClass() + ": " + t.getMessage());
    }

    @Override
    public void onAlarmStateChange(Alarm alarm, AlarmsMessageSchema.AlarmState newState, AlarmsMessageSchema.AlarmState oldState) {

    }

    @Override
    public void onViewAlarmsLog(Alarm alarm) {
        //wsModel.getLog();
    }

    @Override
    public void onSilenceAlarmBuzzer(int duration) {

    }


}
