package net.chetch.cmalarms.models;

import net.chetch.cmalarms.data.Alarm;
import net.chetch.messaging.Message;
import net.chetch.messaging.MessageSchema;
import net.chetch.utilities.Utils;
import net.chetch.webservices.Webservice;
import net.chetch.webservices.WebserviceRepository;
import net.chetch.webservices.WebserviceViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Calendar;

public class AlarmsMessageSchema extends MessageSchema{
    public enum AlarmState{
        DISABLED,
        OFF,
        MINOR,
        MODERATE,
        SEVERE,
        CRITICAL
    }

    static public boolean isAlarmStateOn(AlarmState state){
        return state != AlarmState.OFF && state != AlarmState.DISABLED;
    }

    static public final String SERVICE_NAME = "BBAlarms";

    static public final String COMMAND_ALARM_STATUS = "alarm-status";
    static public final String COMMAND_LIST_ALARMS = "list-alarms";
    static public final String COMMAND_SILENCE_BUZZER = "silence";
    static public final String COMMAND_UNSILENCE_BUZZER = "unsilence";
    static public final String COMMAND_DISABLE_ALARM = "disable-alarm";
    static public final String COMMAND_ENABLE_ALARM = "enable-alarm";
    static public final String COMMAND_TEST_ALARM = "test-alarm";
    static public final String COMMAND_TEST_BUZZER = "test-buzzer";
    static public final String COMMAND_TEST_PILOT = "test-pilot";


    public AlarmsMessageSchema(Message message){
        super(message);
    }

    public String getAlarmID(){
        return message.getString("AlarmID");
    }

    public AlarmState getAlarmState(){
        return message.getEnum("AlarmState", AlarmState.class);
    }
    public String getAlarmMessage(){
        return message.hasValue("AlarmMessage") ? message.getString("AlarmMessage") : null;
    }

    public Calendar getAlarmLastRaised(){
        return message.hasValue("AlarmLastRaised") ? message.getCalendar("AlarmLastRaised") : null;
    }

    public Map<String, AlarmState> getAlarmStates(){
        return message.getMap("AlarmStates", AlarmState.class);
    }

    public List<Alarm> getAlarms(){
        List<String> alarms = message.getList("Alarms", String.class);
        if(alarms == null)return null;

        List<Alarm> alarms2return = new ArrayList<>();
        List<String> dateFields = Arrays.asList("last_raised", "last_lowered", "last_disabled");

        for(String s : alarms){

            //Split each string in to key/value strings, then split each key/value string in to key string and value string
            Alarm a = new Alarm();
            String[] ps = s.split(",");
            for(String kv : ps){
                String[] kva = kv.split("=");
                if(kva.length > 0){
                    String key = kva[0].trim();
                    Object val = kva.length == 2 ? kva[1].trim() : null;
                    if(val != null && !val.toString().isEmpty() && dateFields.contains(key)){
                        try {
                            val = Utils.parseDate(val.toString(), Webservice.DEFAULT_DATE_FORMAT);
                        } catch (Exception e){
                            val = "";
                        }
                    }
                    a.setValue(key, val);
                }
            }
            alarms2return.add(a);
        }
        return alarms2return;
    }

    public String getPilotID(){
        return message.getString("PilotID");
    }

    public boolean isPilotOn(){
        return message.getBoolean("PilotOn");
    }

    public String getBuzzerID(){
        return message.getString("BuzzerID");
    }

    public boolean isBuzzerSilenced(){
        return message.getBoolean("BuzzerSilenced");
    }

    public boolean isBuzzerOn(){
        return message.getBoolean("BuzzerOn");
    }

    public boolean isTesting(){ return message.getBoolean("Testing"); }
}
