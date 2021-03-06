package net.chetch.cmalarms.models;

import android.util.Log;

import net.chetch.cmalarms.data.Alarm;
import net.chetch.messaging.ClientConnection;
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
    static final int ALARM_TEST_DURATION = 10;
    static final AlarmsMessageSchema.AlarmState ALARM_TEST_STATE = AlarmsMessageSchema.AlarmState.CRITICAL;

    Map<String, Alarm> alarmsMap = new HashMap<>();
    String buzzerID;
    MutableLiveData<Alarm> liveDataAlertedAlarm = new MutableLiveData<>();
    MutableLiveData<List<Alarm>> liveDataAlarms = new MutableLiveData<>();
    MutableLiveData<Map<String, AlarmsMessageSchema.AlarmState>> liveDataAlarmStates = new MutableLiveData<>();
    MutableLiveData<Boolean> liveDataPilotOn = new MutableLiveData<>();
    MutableLiveData<Boolean> liveDataBuzzerOn = new MutableLiveData<>();
    MutableLiveData<Boolean> liveDataBuzzerSilenced = new MutableLiveData<>();

    public AlertFilter onAlarmAlert = new AlertFilter(AlarmsMessageSchema.SERVICE_NAME){
        @Override
        protected void onMatched(Message message) {
            Log.i("AMM", "On Alarm Alert");
            AlarmsMessageSchema schema = new AlarmsMessageSchema(message);
            String alarmID = schema.getAlarmID();
            AlarmsMessageSchema.AlarmState astate = schema.getAlarmState();
            String amessage = schema.getAlarmMessage();
            if(alarmsMap.containsKey(alarmID)) {
                Alarm alarm = alarmsMap.get(alarmID);
                alarm.alarmState = astate;
                alarm.alarmMessage = amessage;
                liveDataAlertedAlarm.postValue(alarm);
            }
            liveDataPilotOn.postValue(Boolean.valueOf(schema.isPilotOn()));
            liveDataBuzzerOn.postValue(Boolean.valueOf(schema.isBuzzerOn()));
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
                alarmsMap.put(a.getAlarmID(), a);
            }
            //update liv data
            liveDataAlarms.postValue(l);

            //get alarm states
            requestAlarmStates();
        }
    };

    public CommandResponseFilter onAlarmStatus = new CommandResponseFilter(AlarmsMessageSchema.SERVICE_NAME, AlarmsMessageSchema.COMMAND_ALARM_STATUS){
        @Override
        protected void onMatched(Message message){
            Log.i("AMM", "On Alarm Status");
            AlarmsMessageSchema schema = new AlarmsMessageSchema(message);

            if(!message.hasValue("Pilot")){
                String msg = "Alarm panel offline (no pilot light detected)";
                setError(msg);
                return;
            }

            Map<String, AlarmsMessageSchema.AlarmState> l = schema.getAlarmStates();

            //we take this opportunity to update the alarm state properties
            for(Map.Entry<String, AlarmsMessageSchema.AlarmState> entry : l.entrySet()){
                if(alarmsMap.containsKey(entry.getKey())){
                    Alarm a = alarmsMap.get(entry.getKey());
                    a.alarmState = entry.getValue();
                }
            }

            liveDataAlarmStates.postValue(l);
            liveDataPilotOn.postValue(Boolean.valueOf(schema.isPilotOn()));
            liveDataBuzzerOn.postValue(Boolean.valueOf(schema.isBuzzerOn()));
            liveDataBuzzerSilenced.postValue(Boolean.valueOf(schema.isBuzzerSilenced()));

            buzzerID = schema.getBuzzerID();
        }
    };

    public CommandResponseFilter onBuzzerSilenced = new CommandResponseFilter(AlarmsMessageSchema.SERVICE_NAME, AlarmsMessageSchema.COMMAND_SILENCE_BUZZER) {
        @Override
        protected void onMatched(Message message) {
            AlarmsMessageSchema schema = new AlarmsMessageSchema(message);
            liveDataBuzzerSilenced.postValue(Boolean.valueOf(schema.isBuzzerSilenced()));
        }
    };

    public CommandResponseFilter onBuzzerUnsilenced = new CommandResponseFilter(AlarmsMessageSchema.SERVICE_NAME, AlarmsMessageSchema.COMMAND_UNSILENCE_BUZZER) {
        @Override
        protected void onMatched(Message message) {
            AlarmsMessageSchema schema = new AlarmsMessageSchema(message);
            liveDataBuzzerSilenced.postValue(Boolean.valueOf(schema.isBuzzerSilenced()));
        }
    };

    public NotificationFilter onBuzzerNotification = new NotificationFilter(AlarmsMessageSchema.SERVICE_NAME) {
        @Override
        protected boolean matches(Message message) {
            boolean matches = super.matches(message);
            if(matches){
                AlarmsMessageSchema schema = new AlarmsMessageSchema(message);
                matches = schema.getAlarmID() != null && schema.getAlarmID().equals(buzzerID);
            }
            return matches;
        }

        @Override
        protected void onMatched(Message message) {
            AlarmsMessageSchema schema = new AlarmsMessageSchema(message);
            liveDataBuzzerSilenced.postValue(Boolean.valueOf(schema.isBuzzerSilenced()));
            Log.i("AMM", "Notification from buzzer");
        }
    };

    public AlarmsMessagingModel(){
        super();
        try {
            addMessageFilter(onAlarmAlert);
            addMessageFilter(onListAlarms);
            addMessageFilter(onAlarmStatus);
            addMessageFilter(onBuzzerSilenced);
            addMessageFilter(onBuzzerUnsilenced);
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

    @Override
    protected int onTimer(){
        return super.onTimer();
    }

    @Override
    public void handleReceivedMessage(Message message, ClientConnection cnn) {
        super.handleReceivedMessage(message, cnn);
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

    public LiveData<Boolean> getBuzzerOn(){ return liveDataBuzzerOn; }

    public LiveData<Boolean> getBuzzerSilenced(){
        return liveDataBuzzerSilenced;
    }

    //make calls to the service
    public void requestAlarmStates(){
        getClient().sendCommand(AlarmsMessageSchema.SERVICE_NAME, AlarmsMessageSchema.COMMAND_ALARM_STATUS);
    }

    public void disableAlarm(String alarmID){
        getClient().sendCommand(AlarmsMessageSchema.SERVICE_NAME, AlarmsMessageSchema.COMMAND_DISABLE_ALARM, alarmID);
    }

    public void enableAlarm(String alarmID){
        getClient().sendCommand(AlarmsMessageSchema.SERVICE_NAME, AlarmsMessageSchema.COMMAND_ENABLE_ALARM, alarmID);
    }

    public void testAlarm(String alarmID){
        getClient().sendCommand(AlarmsMessageSchema.SERVICE_NAME, AlarmsMessageSchema.COMMAND_TEST_ALARM, alarmID, ALARM_TEST_STATE.ordinal(), ALARM_TEST_DURATION);
    }

    public boolean isPilotOn(){
        return liveDataPilotOn.getValue();
    }
    public boolean isBuzzerOn() { return liveDataBuzzerOn.getValue(); }
    public boolean isBuzzerSilenced(){
        return liveDataBuzzerSilenced.getValue();
    }
    public void silenceBuzzer(int silenceDuration){
        getClient().sendCommand(AlarmsMessageSchema.SERVICE_NAME, AlarmsMessageSchema.COMMAND_SILENCE_BUZZER, silenceDuration);
    }

    public void unsilenceBuzzer(){
        getClient().sendCommand(AlarmsMessageSchema.SERVICE_NAME, AlarmsMessageSchema.COMMAND_UNSILENCE_BUZZER);
    }
}
