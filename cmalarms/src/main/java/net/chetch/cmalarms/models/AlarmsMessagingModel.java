package net.chetch.cmalarms.models;

import android.util.Log;

import net.chetch.cmalarms.AlarmsMessageSchema;
import net.chetch.cmalarms.data.Alarm;
import net.chetch.messaging.Message;
import net.chetch.messaging.MessagingViewModel;
import net.chetch.messaging.filters.AlertFilter;
import net.chetch.messaging.filters.CommandResponseFilter;
import net.chetch.messaging.filters.NotificationFilter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class AlarmsMessagingModel extends MessagingViewModel {
    Map<String, Alarm> alarmsMap = new HashMap<>();
    MutableLiveData<Alarm> liveDataAlertedAlarm = new MutableLiveData<>();
    MutableLiveData<List<Alarm>> liveDataAlarms = new MutableLiveData<>();
    MutableLiveData<Map<String, AlarmsMessageSchema.AlarmState>> liveDataAlarmStates = new MutableLiveData<>();
    MutableLiveData<Boolean> liveDataPilotOn = new MutableLiveData<>();
    MutableLiveData<Boolean> liveDataBuzzerSilenced = new MutableLiveData<>();

    public AlertFilter onAlarmAlert = new AlertFilter(AlarmsMessageSchema.SERVICE_NAME){
        @Override
        protected void onMatched(Message message) {
            Log.i("AMM", "On Alarm Alert");
            AlarmsMessageSchema schema = new AlarmsMessageSchema(message);
            String deviceID = schema.getDeviceID();
            AlarmsMessageSchema.AlarmState astate = schema.getAlarmState();
            if(alarmsMap.containsKey(deviceID)) {
                Alarm alarm = alarmsMap.get(deviceID);
                alarm.alarmState = astate;
                liveDataAlertedAlarm.postValue(alarm);
            }
            liveDataPilotOn.postValue(Boolean.valueOf(schema.isPilotOn()));
            liveDataBuzzerSilenced.postValue(Boolean.valueOf(schema.isBuzzerSilenced()));
        }
    };

    public CommandResponseFilter onListAlarms = new CommandResponseFilter(AlarmsMessageSchema.SERVICE_NAME, AlarmsMessageSchema.COMMAND_LIST_ALARMS){
        @Override
        protected void onMatched(Message message) {
            Log.i("AMM", "On List Alarms");

            AlarmsMessageSchema schema = new AlarmsMessageSchema(message);
            List<Alarm> l = schema.getAlarms();
            //keep a map record
            alarmsMap.clear();
            for(Alarm a : l){
                alarmsMap.put(a.getDeviceID(), a);
            }
            //update liv data
            liveDataAlarms.postValue(l);

            //get alarm states
            requestAlarmStates();
        }
    };

    public CommandResponseFilter onAlarmStatus = new CommandResponseFilter(AlarmsMessageSchema.SERVICE_NAME, AlarmsMessageSchema.COMMAND_ALARM_STATUS){
        @Override
        protected void onMatched(Message message) {
            Log.i("AMM", "On Alarm Status");
            AlarmsMessageSchema schema = new AlarmsMessageSchema(message);
            Map<String, AlarmsMessageSchema.AlarmState> l = schema.getAlarmStates();

            //we take this opportunity to update the alarm state properties
            for(Map.Entry<String, AlarmsMessageSchema.AlarmState> entry : l.entrySet()){
                if(alarmsMap.containsKey(entry.getKey())){
                    alarmsMap.get(entry.getKey()).alarmState = entry.getValue();
                }
            }

            liveDataAlarmStates.postValue(l);
            liveDataPilotOn.postValue(Boolean.valueOf(schema.isPilotOn()));
            liveDataBuzzerSilenced.postValue(Boolean.valueOf(schema.isBuzzerSilenced()));

            //set the notification
            onBuzzerNotification.Sender = schema.getBuzzerID();
        }
    };

    public CommandResponseFilter onSilenced = new CommandResponseFilter(AlarmsMessageSchema.SERVICE_NAME, AlarmsMessageSchema.COMMAND_SILENCE) {
        @Override
        protected void onMatched(Message message) {
            AlarmsMessageSchema schema = new AlarmsMessageSchema(message);
            liveDataBuzzerSilenced.postValue(Boolean.valueOf(schema.isBuzzerSilenced()));
        }
    };

    public CommandResponseFilter onUnsilenced = new CommandResponseFilter(AlarmsMessageSchema.SERVICE_NAME, AlarmsMessageSchema.COMMAND_UNSILENCE) {
        @Override
        protected void onMatched(Message message) {
            AlarmsMessageSchema schema = new AlarmsMessageSchema(message);
            liveDataBuzzerSilenced.postValue(Boolean.valueOf(schema.isBuzzerSilenced()));
        }
    };

    public NotificationFilter onBuzzerNotification = new NotificationFilter("buzzer4") {
        @Override
        protected void onMatched(Message message) {
            AlarmsMessageSchema schema = new AlarmsMessageSchema(message);
            Log.i("AMM", "Notification from buzzer");
        }
    };

    public AlarmsMessagingModel(){
        super();
        try {
            addMessageFilter(onAlarmAlert);
            addMessageFilter(onListAlarms);
            addMessageFilter(onAlarmStatus);
            addMessageFilter(onSilenced);
            addMessageFilter(onUnsilenced);
            addMessageFilter(onBuzzerNotification);
        } catch (Exception e){
            Log.e("AMM", e.getMessage());
        }
    }

    @Override
    public void onClientConnected() {
        super.onClientConnected();
        Log.i("AMM", "Client connected so requesting list of alarms");
        getClient().sendCommand(AlarmsMessageSchema.SERVICE_NAME, AlarmsMessageSchema.COMMAND_LIST_ALARMS);
    }

    public LiveData<List<Alarm>> getAlarms(){
        return liveDataAlarms;
    }

    public LiveData<Map<String, AlarmsMessageSchema.AlarmState>> getAlarmStates(){
        return liveDataAlarmStates;
    }

    public LiveData<Alarm> getAlertedAlarm(){
        return liveDataAlertedAlarm;
    }

    public LiveData<Boolean> getPilotOn(){
        return liveDataPilotOn;
    }

    public LiveData<Boolean> getBuzzerSilenced(){
        return liveDataBuzzerSilenced;
    }

    //make calls to the service
    public void requestAlarmStates(){
        getClient().sendCommand(AlarmsMessageSchema.SERVICE_NAME, AlarmsMessageSchema.COMMAND_ALARM_STATUS);
    }

    public void disableAlarm(String deviceID){
        getClient().sendCommand(AlarmsMessageSchema.SERVICE_NAME, AlarmsMessageSchema.COMMAND_DISABLE_ALARM, deviceID);
    }

    public void enableAlarm(String deviceID){
        getClient().sendCommand(AlarmsMessageSchema.SERVICE_NAME, AlarmsMessageSchema.COMMAND_ENABLE_ALARM, deviceID);
    }

    public void testAlarm(String deviceID){
        getClient().sendCommand(AlarmsMessageSchema.SERVICE_NAME, AlarmsMessageSchema.COMMAND_TEST_ALARM, deviceID);
    }

    public void silenceBuzzer(int silenceDuration){
        getClient().sendCommand(AlarmsMessageSchema.SERVICE_NAME, AlarmsMessageSchema.COMMAND_SILENCE, silenceDuration);
    }

    public void unilenceBuzzer(){
        getClient().sendCommand(AlarmsMessageSchema.SERVICE_NAME, AlarmsMessageSchema.COMMAND_UNSILENCE);
    }
}
