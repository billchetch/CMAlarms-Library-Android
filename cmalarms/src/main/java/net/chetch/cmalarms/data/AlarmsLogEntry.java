package net.chetch.cmalarms.data;

import net.chetch.cmalarms.models.AlarmsMessageSchema;
import net.chetch.webservices.DataObject;

import java.util.Calendar;

public class AlarmsLogEntry extends DataObject {
    
    public Calendar getCreated(){
        return getCasted("created");
    }

    public String getAlarmID(){
        return getCasted("alarm_id");
    }

    public String getAlarmName(){
        return getCasted("alarm_name");
    }

    public AlarmsMessageSchema.AlarmState getAlarmState(){
        String s =  getCasted("alarm_state");
        return AlarmsMessageSchema.AlarmState.valueOf(s);
    }

    public String getAlarmMessage(){
        return getCasted("alarm_message");
    }

    public String getComments(){
        return getCasted("comments");
    }
}
