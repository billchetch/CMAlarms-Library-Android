package net.chetch.cmalarms.models;

import android.util.Log;

import net.chetch.cmalarms.AlarmsMessageSchema;
import net.chetch.cmalarms.data.Alarm;
import net.chetch.messaging.Message;
import net.chetch.messaging.MessagingViewModel;
import net.chetch.messaging.filters.AlertFilter;
import net.chetch.messaging.filters.CommandResponseFilter;

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
        }
    };

    public AlarmsMessagingModel(){
        super();
        try {
            addMessageFilter(onAlarmAlert);
            addMessageFilter(onListAlarms);
            addMessageFilter(onAlarmStatus);
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
}
