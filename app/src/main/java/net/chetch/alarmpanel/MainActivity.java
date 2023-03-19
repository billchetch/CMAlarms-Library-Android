package net.chetch.alarmpanel;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;
import android.util.Log;

import net.chetch.appframework.GenericActivity;
import net.chetch.cmalarms.AlarmsLogDialogFragment;
import net.chetch.cmalarms.AlarmPanelFragment;
import net.chetch.cmalarms.IAlarmPanelListener;
import net.chetch.cmalarms.data.Alarm;
import net.chetch.cmalarms.models.AlarmsMessageSchema;
import net.chetch.cmalarms.models.AlarmsMessagingModel;
import net.chetch.cmalarms.models.AlarmsWebserviceModel;
import net.chetch.messaging.ClientConnection;
import net.chetch.webservices.ConnectManager;
import net.chetch.webservices.WebserviceViewModel;

import java.net.SocketException;
import java.util.Stack;

public class MainActivity extends GenericActivity implements IAlarmPanelListener {

    static boolean connected = false;
    static boolean suppressConnectionErrors = false;

    AlarmsMessagingModel model;
    AlarmsWebserviceModel wsModel;

    AlarmPanelFragment alarmPanelFragment;

    ConnectManager connectManager = new ConnectManager();

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
                    connected = true;
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        includeActionBar(SettingsActivity.class);

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

        try {
            model.setClientName("ACMCAPAlarms");

            connectManager.addModel(model);
            connectManager.addModel(wsModel);

            connectManager.setPermissableServerTimeDifference(5 * 60);
            connectManager.requestConnect(connectProgress);
        } catch (Exception e){
            showError(e);
        }

    }

    private String getStackTrace(Throwable t){
        String stackTrace = "";
        StackTraceElement[] st = t.getStackTrace();
        for(StackTraceElement ste : st){
            String s = ste.getFileName() + " @ " + ste.getLineNumber() + " in " + ste.getMethodName();
            stackTrace += s + "\n";
        }
        return stackTrace;
    }

    private void handleError(Throwable t, Object source){

        if(suppressConnectionErrors && connected &&  connectManager.isConnectionError(t)){
            String errMsg = t.getMessage() + "\n" + t.getCause() + "\n" +  getStackTrace(t);
            Log.e("MAIN", "Suppressed connection error: " + errMsg);
            return;
        }

        if(t instanceof SocketException){
            showError(t.getMessage() + "\n" + t.getCause() + "\n" + getStackTrace(t));
        } else {
            showError(t);
        }
        Log.e("MAIN", t.getClass() + ": " + t.getMessage());
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        connectManager.resume();
    }

    @Override
    protected void onStop() {
        super.onStop();

        stopTimer();
        connectManager.pause();
    }

    @Override
    public void onAlarmStateChange(Alarm alarm, AlarmsMessageSchema.AlarmState newState, AlarmsMessageSchema.AlarmState oldState) {

    }

    @Override
    public void onViewAlarmsLog(Alarm alarm) {
        //wsModel.getLog(alarm);
        AlarmsLogDialogFragment alarmsLogDialog = new AlarmsLogDialogFragment();
        alarmsLogDialog.alarmsWebserviceModel = wsModel;
        alarmsLogDialog.alarm = alarm;
        alarmsLogDialog.show(getSupportFragmentManager(), "AlarmsLogDialog");
    }

    @Override
    public void onSilenceAlarmBuzzer(int duration) {

    }

    @Override
    public void onDisableAlarm(Alarm alarm) {
        showConfirmationDialog("Are you sure you want to disable " + alarm.getName() + "?", (dialog, which)->{
            model.disableAlarm(alarm.getAlarmID());
             });
    }


}
