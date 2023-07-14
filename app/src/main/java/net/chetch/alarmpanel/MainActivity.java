package net.chetch.alarmpanel;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import net.chetch.appframework.GenericActivity;
import net.chetch.appframework.NotificationBar;
import net.chetch.cmalarms.AlarmsLogDialogFragment;
import net.chetch.cmalarms.AlarmPanelFragment;
import net.chetch.cmalarms.IAlarmPanelListener;
import net.chetch.cmalarms.data.Alarm;
import net.chetch.cmalarms.models.AlarmsMessageSchema;
import net.chetch.cmalarms.models.AlarmsMessagingModel;
import net.chetch.cmalarms.models.AlarmsWebserviceModel;
import net.chetch.messaging.ClientConnection;
import net.chetch.messaging.MessagingViewModel;
import net.chetch.messaging.TCPClient;
import net.chetch.messaging.exceptions.MessagingException;
import net.chetch.messaging.exceptions.MessagingServiceException;
import net.chetch.utilities.Logger;
import net.chetch.utilities.SLog;
import net.chetch.webservices.ConnectManager;
import net.chetch.webservices.WebserviceViewModel;

import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class MainActivity extends GenericActivity implements IAlarmPanelListener, NotificationBar.INotifiable{

    static boolean connected = false;
    static boolean suppressConnectionErrors = false;

    AlarmsMessagingModel model;
    AlarmsWebserviceModel wsModel;

    AlarmPanelFragment alarmPanelFragment;

    ConnectManager connectManager = new ConnectManager();

    Observer connectProgress  = obj -> {
        showProgress();
        if(obj instanceof WebserviceViewModel.LoadProgress) {
            WebserviceViewModel.LoadProgress progress = (WebserviceViewModel.LoadProgress) obj;
            try {
                String state = progress.startedLoading ? "Loading" : "Loaded";
                String progressInfo = state + (progress.info == null ? "" : " " + progress.info.toLowerCase());
                /*if(progress.dataLoaded != null){
                    progressInfo += " - " + progress.dataLoaded.getClass().toString();
                }*/
                setProgressInfo(progressInfo);
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

    boolean testButtonClicked = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        includeActionBar(SettingsActivity.class);

        //Get models
        Logger.info("Main activity setting up model callbacks ...");
        model = ViewModelProviders.of(this).get(AlarmsMessagingModel.class);
        model.getError().observe(this, throwable -> {
            try {
                handleError(throwable, model);
            } catch (Exception e){
                SLog.e("Main", e.getMessage());
            }
        });


        wsModel = new ViewModelProvider(this).get(AlarmsWebserviceModel.class);
        wsModel.getError().observe(this, throwable ->{
            handleError(throwable, wsModel);
        });


        //Components
        Logger.info("Main activity setting up creting components ...");
        alarmPanelFragment = (AlarmPanelFragment)getSupportFragmentManager().findFragmentById(R.id.alarmPanelFragment);
        alarmPanelFragment.listener = this;

        try {
            Logger.info("Main activity sstting cm client name, adding modules and requesting connect ...");
            model.setClientName("ACMCAPAlarms", getApplicationContext());

            connectManager.addModel(model);
            connectManager.addModel(wsModel);

            connectManager.setPermissableServerTimeDifference(5 * 60);

            connectManager.requestConnect(connectProgress);

            NotificationBar.setView(findViewById(R.id.notificationbar), 100);
            NotificationBar.monitor(this, connectManager, "connection");
        } catch (Exception e){
            showError(e);
        }

        //REMOVE THIS
        /*Button btn = findViewById(R.id.testButton);
        btn.setOnClickListener((v)->{
            if(testButtonClicked){
                connectManager.resume();
            } else {
                model.getClient().close();
                connectManager.pause();
            }
            testButtonClicked = !testButtonClicked;
        });*/
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
        if (suppressConnectionErrors && connected && (ConnectManager.isConnectionError(t) || t instanceof MessagingServiceException)) {
            final String errMsg = t.getClass().getName() + "\n" + t.getMessage() + "\n" + t.getCause() + "\n" + getStackTrace(t);
            SLog.e("MAIN", "Suppressed connection error: " + errMsg);
            Logger.error("Suppressed connection error: " + errMsg);
            NotificationBar.show(NotificationBar.NotificationType.ERROR,
                    "An exception has occurred ...click for more details",
                    t).setListener(new NotificationBar.INotificationListener() {
                @Override
                public void onClick(NotificationBar nb, NotificationBar.NotificationType ntype) {
                    showError(errMsg);
                }
            });
            return;
        }

        String errMsg = "SCE: " + suppressConnectionErrors + ", CNCT: " + connected + ", ICE: " + ConnectManager.isConnectionError(t);
        errMsg += "\n" + t.getClass().getName() + "\n" + t.getMessage() + "\n" + t.getCause() + "\n" + getStackTrace(t);

        showError(errMsg);

        SLog.e("MAIN", t.getClass() + ": " + t.getMessage());

    }


    @Override
    protected void onRestart() {
        super.onRestart();

        connectManager.resume();
    }

    @Override
    protected void onStop() {
        super.onStop();

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

    @Override
    public void openAbout() {
        super.openAbout();
        try {
            ClientConnection client = model.getClient();
            String s = client.getName() + " is of state " + client.getState() + "\n";
            MessagingViewModel.MessagingService bbalarms = model.getMessaingService(AlarmsMessageSchema.SERVICE_NAME);
            s+= bbalarms.name + " service is of state " + bbalarms.state;
            aboutDialog.aboutBlurb = s;

        } catch (Exception e){

        }
    }


    @Override
    public void handleNotification(Object notifier, String tag) {
        if(notifier instanceof ConnectManager){
            ConnectManager cm = (ConnectManager)notifier;
            switch(cm.getState()){
                case CONNECTED:
                    NotificationBar.show(NotificationBar.NotificationType.INFO, "Connected and ready to use.", null,5);
                    break;

                case ERROR:
                    NotificationBar.show(NotificationBar.NotificationType.ERROR, "Service unavailable.");
                    break;

                case RECONNECT_REQUEST:
                    NotificationBar.show(NotificationBar.NotificationType.WARNING, "Attempting to reconnect...");
                    break;
            }
        }


    }
}
