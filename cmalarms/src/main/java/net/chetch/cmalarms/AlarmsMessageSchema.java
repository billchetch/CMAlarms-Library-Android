package net.chetch.cmalarms;

import net.chetch.cmalarms.data.Alarm;
import net.chetch.messaging.Message;
import net.chetch.messaging.MessageSchema;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AlarmsMessageSchema extends MessageSchema{
    public enum AlarmState{
        OFF,
        ON,
        DISABLED,
        ENABLED
    }

    static public final String SERVICE_NAME = "BBAlarms";

    static public final String COMMAND_ALARM_STATUS = "alarm-status";
    static public final String COMMAND_LIST_ALARMS = "list-alarms";
    static public final String COMMAND_SILENCE = "silence";
    static public final String COMMAND_UNSILENCE = "unsilence";
    static public final String COMMAND_DISABLE_ALARM = "disable-alarm";
    static public final String COMMAND_ENABLE_ALARM = "enable-alarm";
    static public final String COMMAND_TEST_ALARM = "test-alarm";


    public AlarmsMessageSchema(Message message){
        super(message);
    }

    public String getDeviceID(){
        return message.getString("DeviceID");
    }

    public AlarmState getAlarmState(){
        return message.getEnum("AlarmState", AlarmState.class);
    }

    public Map<String, AlarmState> getAlarmStates(){
        return message.getMap("AlarmStates", AlarmState.class);
    }

    public List<Alarm> getAlarms(){
        List<String> alarms = message.getList("Alarms", String.class);
        if(alarms == null)return null;

        List<Alarm> alarms2return = new ArrayList<>();
        for(String s : alarms){

            //Split each string in to key/value strings, then split each key/value string in to key string and value string
            Alarm a = new Alarm();
            String[] ps = s.split(",");
            for(String kv : ps){
                String[] kva = kv.split("=");
                if(kva.length > 0){
                    String key = kva[0].trim();
                    String val = kva.length == 2 ? kva[1].trim() : null;
                    a.setValue(key, val);
                }
            }
            alarms2return.add(a);
        }
        return alarms2return;
    }
}
